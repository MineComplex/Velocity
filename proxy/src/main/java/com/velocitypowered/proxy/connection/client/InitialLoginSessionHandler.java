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
import com.velocitypowered.api.proxy.auth.AuthHandler;
import com.velocitypowered.api.proxy.auth.AuthResult;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.velocitypowered.proxy.connection.VelocityConstants.EMPTY_BYTE_ARRAY;
import static com.velocitypowered.proxy.crypto.EncryptionUtils.decryptRsa;
import static com.velocitypowered.proxy.crypto.EncryptionUtils.generateServerId;

/**
 * Authenticates the player with the token of its client, there is no Mojang session server.
 *
 * <p>After the encryption is enabled, the proxy asks the client for its token on the
 * {@value AuthHandler#TOKEN_CHANNEL} login channel, so the token never travels in plain text.
 * The {@link AuthHandler} set by a plugin checks it; without a handler nobody can join.
 */
public class InitialLoginSessionHandler implements MinecraftSessionHandler {

  private static final Logger logger = LogManager.getLogger(InitialLoginSessionHandler.class);
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final MinecraftChannelIdentifier TOKEN_CHANNEL =
      MinecraftChannelIdentifier.from(AuthHandler.TOKEN_CHANNEL);
  private static final int MAX_TOKEN_BYTES = 4096;
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
          // Every player is authenticated by the token, a forced offline mode is ignored
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
      requestToken(login, serverId);
    } catch (GeneralSecurityException e) {
      logger.error("Unable to enable encryption", e);
      mcConnection.close(true);
    }
    return true;
  }

  /**
   * Asks the client for its token, the connection is encrypted already.
   */
  private void requestToken(ServerLoginPacket login, String serverId) {
    AuthHandler handler = server.getAuthHandler().orElse(null);
    if (handler == null) {
      logger.error("No auth handler is set, {} cannot be authenticated", login.getUsername());
      inbound.disconnect(AUTH_UNAVAILABLE);
      return;
    }

    this.currentState = LoginState.TOKEN_REQUESTED;
    inbound.sendLoginPluginMessage(TOKEN_CHANNEL, EMPTY_BYTE_ARRAY, response -> {
      // A client which does not know the channel answers without a body
      String token = response == null || response.length == 0 || response.length > MAX_TOKEN_BYTES
          ? null : new String(response, StandardCharsets.UTF_8);
      authenticate(handler, login, token, serverId);
    });
  }

  private void authenticate(AuthHandler handler, ServerLoginPacket login, @Nullable String token,
                            String serverId) {
    CompletableFuture<AuthResult> future;
    try {
      future = handler.authenticate(inbound, login.getUsername(), token);
    } catch (RuntimeException e) {
      future = CompletableFuture.failedFuture(e);
    }

    future.whenCompleteAsync((result, throwable) -> {
      if (mcConnection.isClosed()) {
        // The player disconnected while being authenticated
        return;
      }

      if (throwable != null || result == null) {
        logger.error("Unable to authenticate {}", login.getUsername(), throwable);
        inbound.disconnect(AUTH_UNAVAILABLE);
        return;
      }

      Optional<GameProfile> profile = result.getProfile();
      if (profile.isEmpty()) {
        inbound.disconnect(result.getReason().orElse(AUTH_UNAVAILABLE));
        return;
      }

      if (!profile.get().getName().equalsIgnoreCase(login.getUsername())) {
        logger.error("The auth handler returned the profile {} for {}", profile.get().getName(),
            login.getUsername());
        inbound.disconnect(AUTH_UNAVAILABLE);
        return;
      }

      // All went well, initialize the session.
      this.currentState = LoginState.AUTHENTICATED;
      mcConnection.setActiveSessionHandler(StateRegistry.LOGIN,
          new AuthSessionHandler(server, inbound, profile.get(), true, serverId));
    }, mcConnection.eventLoop());
  }

  private EncryptionRequestPacket generateEncryptionRequest() {
    byte[] verify = new byte[4];
    SECURE_RANDOM.nextBytes(verify);

    EncryptionRequestPacket request = new EncryptionRequestPacket();
    request.setPublicKey(server.getServerKeyPair().getPublic().getEncoded());
    request.setVerifyToken(verify);
    // The client sends its token to the proxy, not to a session server
    request.setShouldAuthenticate(false);
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
    ENCRYPTION_RESPONSE_RECEIVED,
    TOKEN_REQUESTED,
    AUTHENTICATED
  }
}
