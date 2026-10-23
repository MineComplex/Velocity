/*
 * Copyright (C) 2018-2023 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.connection.client;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.netty.MinecraftDecoder;
import com.velocitypowered.proxy.protocol.packet.EncryptionRequestPacket;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import com.velocitypowered.proxy.protocol.packet.LoginPluginResponsePacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;

import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;
import static com.velocitypowered.proxy.VelocityServer.GENERAL_GSON;
import static com.velocitypowered.proxy.connection.VelocityConstants.EMPTY_BYTE_ARRAY;
import static com.velocitypowered.proxy.crypto.EncryptionUtils.decryptRsa;
import static com.velocitypowered.proxy.crypto.EncryptionUtils.generateServerId;

/**
 * Handles authenticating the player through the Minecomplex launcher backend.
 *
 * <p>Every player must be authenticated: the launcher client redeems its launch ticket for a
 * game token and joins through the backend, the proxy confirms the join with {@code hasJoined}.
 * There is no Mojang authentication and no offline mode, so only the launcher client can join.
 */
public class InitialLoginSessionHandler implements MinecraftSessionHandler {

  private static final Logger logger = LogManager.getLogger(InitialLoginSessionHandler.class);
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final String HASJOINED_PATH =
      "/sessionserver/session/minecraft/hasJoined?username=%s&serverId=%s";
  private static final Component LAUNCHER_ONLY = Component.text(
      "Вход на сервер возможен только через лаунчер Minecomplex.\n"
          + "Если вы уже играете через лаунчер, перезапустите игру из лаунчера.",
      NamedTextColor.RED);
  private static final Component AUTH_UNAVAILABLE = Component.text(
      "Сервер авторизации временно недоступен, попробуйте позже.", NamedTextColor.RED);

  private final VelocityServer server;
  public MinecraftConnection mcConnection;
  private final LoginInboundConnection inbound;
  private @MonotonicNonNull ServerLoginPacket login;
  private byte[] verify = EMPTY_BYTE_ARRAY;
  private LoginState currentState = LoginState.LOGIN_PACKET_EXPECTED;

  InitialLoginSessionHandler(VelocityServer server, MinecraftConnection mcConnection,
                             LoginInboundConnection inbound) {
    this.server = Preconditions.checkNotNull(server, "server");
    this.mcConnection = Preconditions.checkNotNull(mcConnection, "mcConnection");
    this.inbound = Preconditions.checkNotNull(inbound, "inbound");
  }

  @Override
  public boolean handle(ServerLoginPacket packet) {
    assertState(LoginState.LOGIN_PACKET_EXPECTED);
    this.currentState = LoginState.LOGIN_PACKET_RECEIVED;
    this.login = packet;

    final PreLoginEvent event = new PreLoginEvent(inbound, login.getUsername(), login.getHolderUuid());
    server.getEventManager().fire(event).thenRunAsync(() -> {
      if (mcConnection.isClosed()) {
        // The player was disconnected
        return;
      }

      PreLoginComponentResult result = event.getResult();
      Optional<Component> disconnectReason = result.getReasonComponent();
      if (disconnectReason.isPresent()) {
        // The component is guaranteed to be provided if the connection was denied.
        inbound.disconnect(disconnectReason.get());
        return;
      }

      inbound.loginEventFired(() -> {
        if (mcConnection.isClosed()) {
          // The player was disconnected
          return;
        }

        mcConnection.eventLoop().execute(() -> {
          // Always authenticated by the launcher backend, a forced offline mode is ignored
          if (result.isForceOfflineMode()) {
            logger.debug("Ignoring forced offline mode for {}", login.getUsername());
          }
          EncryptionRequestPacket request = generateEncryptionRequest();
          this.verify = Arrays.copyOf(request.getVerifyToken(), 4);
          mcConnection.write(request);
          this.currentState = LoginState.ENCRYPTION_REQUEST_SENT;
        });
      });
    }, mcConnection.eventLoop()).exceptionally((ex) -> {
      logger.error("Exception in pre-login stage", ex);
      return null;
    });

    return true;
  }

  @Override
  public boolean handle(LoginPluginResponsePacket packet) {
    this.inbound.handleLoginPluginResponse(packet);
    return true;
  }

  @Override
  public boolean handle(EncryptionResponsePacket packet) {
    assertState(LoginState.ENCRYPTION_REQUEST_SENT);
    this.currentState = LoginState.ENCRYPTION_RESPONSE_RECEIVED;
    ServerLoginPacket login = this.login;
    if (login == null) {
      throw new IllegalStateException("No ServerLogin packet received yet.");
    }

    if (verify.length == 0) {
      throw new IllegalStateException("No EncryptionRequest packet sent yet.");
    }

    try {
      KeyPair serverKeyPair = server.getServerKeyPair();
      byte[] decryptedVerifyToken = decryptRsa(serverKeyPair, packet.getVerifyToken());
      if (!MessageDigest.isEqual(verify, decryptedVerifyToken)) {
        throw new IllegalStateException("Unable to successfully decrypt the verification token.");
      }

      byte[] decryptedSharedSecret = decryptRsa(serverKeyPair, packet.getSharedSecret());

      // Go ahead and enable encryption. Once the client sends EncryptionResponse, encryption
      // is enabled.
      mcConnection.enableEncryption(decryptedSharedSecret);

      String serverId = generateServerId(decryptedSharedSecret, serverKeyPair.getPublic());
      String playerIp = ((InetSocketAddress) mcConnection.getRemoteAddress()).getHostString();
      String url = server.getConfiguration().getAuthServer() + String.format(HASJOINED_PATH,
          urlFormParameterEscaper().escape(login.getUsername()), serverId);

      if (server.getConfiguration().shouldPreventClientProxyConnections()) {
        url += "&ip=" + urlFormParameterEscaper().escape(playerIp);
      }

      final HttpRequest httpRequest = HttpRequest.newBuilder()
          .setHeader("User-Agent",
              server.getVersion().getName() + "/" + server.getVersion().getVersion())
          .uri(URI.create(url))
          .build();
      //noinspection resource
      final HttpClient httpClient = server.createHttpClient();
      httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
          .whenCompleteAsync((response, throwable) -> {
            if (mcConnection.isClosed()) {
              // The player disconnected after we authenticated them.
              return;
            }

            if (throwable != null) {
              logger.error("Unable to authenticate player through the launcher backend", throwable);
              inbound.disconnect(AUTH_UNAVAILABLE);
              return;
            }

            if (response.statusCode() == 200) {
              final GameProfile profile = parseProfile(response.body());
              if (profile == null
                  || !profile.getName().equalsIgnoreCase(login.getUsername())) {
                logger.error("The launcher backend returned an invalid profile for {} ({})",
                    login.getUsername(), playerIp);
                inbound.disconnect(AUTH_UNAVAILABLE);
                return;
              }
              // All went well, initialize the session.
              mcConnection.setActiveSessionHandler(StateRegistry.LOGIN,
                  new AuthSessionHandler(server, inbound, profile, true, serverId));
            } else if (response.statusCode() == 204) {
              // Not joined through the launcher backend: not the launcher client, or another
              // account / an expired game session
              inbound.disconnect(LAUNCHER_ONLY);
            } else {
              if (response.statusCode() == 403) {
                logger.error("The launcher backend refused hasJoined from this proxy, add its IP "
                    + "to MINECOMPLEX_YGGDRASIL_ALLOWED_SERVER_IPS of the backend");
              } else {
                logger.error(
                    "Got an unexpected status {} from the launcher backend to log in {} ({})",
                    response.statusCode(), login.getUsername(), playerIp);
              }
              inbound.disconnect(AUTH_UNAVAILABLE);
            }
          }, mcConnection.eventLoop())
          .whenComplete((ignored, throwable) -> {
            httpClient.close();
          });
    } catch (GeneralSecurityException e) {
      logger.error("Unable to enable encryption", e);
      mcConnection.close(true);
    }
    return true;
  }

  private static @Nullable GameProfile parseProfile(String body) {
    try {
      GameProfile profile = GENERAL_GSON.fromJson(body, GameProfile.class);
      return profile != null && profile.getId() != null && profile.getName() != null
          && !profile.getName().isEmpty() ? profile : null;
    } catch (RuntimeException e) {
      return null;
    }
  }

  private EncryptionRequestPacket generateEncryptionRequest() {
    byte[] verify = new byte[4];
    SECURE_RANDOM.nextBytes(verify);

    EncryptionRequestPacket request = new EncryptionRequestPacket();
    request.setPublicKey(server.getServerKeyPair().getPublic().getEncoded());
    request.setVerifyToken(verify);
    return request;
  }

  @Override
  public void handleUnknown(ByteBuf buf) {
    mcConnection.close(true);
  }

  @Override
  public void disconnected() {
    this.inbound.cleanup();
  }

  private void assertState(LoginState expectedState) {
    if (this.currentState != expectedState) {
      if (MinecraftDecoder.DEBUG) {
        logger.error("{} Received an unexpected packet requiring state {}, but we are in {}",
            inbound,
            expectedState, this.currentState);
      }
      mcConnection.close(true);
    }
  }

  private enum LoginState {
    LOGIN_PACKET_EXPECTED,
    LOGIN_PACKET_RECEIVED,
    ENCRYPTION_REQUEST_SENT,
    ENCRYPTION_RESPONSE_RECEIVED
  }
}
