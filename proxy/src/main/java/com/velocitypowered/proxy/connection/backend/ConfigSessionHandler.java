/*
 * Copyright (C) 2019-2023 Velocity Contributors
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

package com.velocitypowered.proxy.connection.backend;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PreTransferEvent;
import com.velocitypowered.api.event.player.CookieRequestEvent;
import com.velocitypowered.api.event.player.CookieStoreEvent;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.event.player.ServerResourcePackSendEvent;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.ClientConfigSessionHandler;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.connection.player.resourcepack.VelocityResourcePackInfo;
import com.velocitypowered.proxy.connection.player.resourcepack.handler.ResourcePackHandler;
import com.velocitypowered.proxy.connection.util.ConnectionMessages;
import com.velocitypowered.proxy.connection.util.ConnectionRequestResults;
import com.velocitypowered.proxy.connection.util.ConnectionRequestResults.Impl;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.netty.MinecraftDecoder;
import com.velocitypowered.proxy.protocol.netty.MinecraftVarintFrameDecoder;
import com.velocitypowered.proxy.protocol.packet.ClientboundCookieRequestPacket;
import com.velocitypowered.proxy.protocol.packet.ClientboundStoreCookiePacket;
import com.velocitypowered.proxy.protocol.packet.DisconnectPacket;
import com.velocitypowered.proxy.protocol.packet.KeepAlivePacket;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import com.velocitypowered.proxy.protocol.packet.RemoveResourcePackPacket;
import com.velocitypowered.proxy.protocol.packet.ResourcePackRequestPacket;
import com.velocitypowered.proxy.protocol.packet.ResourcePackResponsePacket;
import com.velocitypowered.proxy.protocol.packet.TransferPacket;
import com.velocitypowered.proxy.protocol.packet.config.ClientboundCustomReportDetailsPacket;
import com.velocitypowered.proxy.protocol.packet.config.ClientboundServerLinksPacket;
import com.velocitypowered.proxy.protocol.packet.config.CodeOfConductPacket;
import com.velocitypowered.proxy.protocol.packet.config.FinishedUpdatePacket;
import com.velocitypowered.proxy.protocol.packet.config.RegistrySyncPacket;
import com.velocitypowered.proxy.protocol.packet.config.StartUpdatePacket;
import com.velocitypowered.proxy.protocol.packet.config.TagsUpdatePacket;
import com.velocitypowered.proxy.protocol.util.PluginMessageUtil;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A special session handler that catches "last minute" disconnects. This version is to accommodate
 * 1.20.2+ switching. Yes, some of this is exceptionally stupid.
 */
public class ConfigSessionHandler implements MinecraftSessionHandler {
  private static final boolean BACKPRESSURE_LOG =
      Boolean.getBoolean("velocity.log-server-backpressure");

  private static final Logger logger = LogManager.getLogger(ConfigSessionHandler.class);
  private final VelocityServer server;
  private final VelocityServerConnection serverConn;
  private final CompletableFuture<Impl> resultFuture;

  /**
   * Creates the new transition handler.
   *
   * @param server       the Velocity server instance
   * @param serverConn   the server connection
   * @param resultFuture the result future
   */
  ConfigSessionHandler(VelocityServer server, VelocityServerConnection serverConn,
                       CompletableFuture<Impl> resultFuture) {
    this.server = server;
    this.serverConn = serverConn;
    this.resultFuture = resultFuture;
  }

  @Override
  public boolean beforeHandle() {
    if (!serverConn.isActive()) {
      // Obsolete connection
      serverConn.disconnect();
      return true;
    }
    return false;
  }

  @Override
  public boolean handle(StartUpdatePacket packet) {
    serverConn.ensureConnected().write(packet);
    return true;
  }

  @Override
  public boolean handle(TagsUpdatePacket packet) {
    serverConn.getPlayer().getConnection().write(packet);
    return true;
  }

  @Override
  public boolean handle(ClientboundCustomReportDetailsPacket packet) {
    serverConn.getPlayer().getConnection().write(packet);
    return true;
  }

  @Override
  public boolean handle(ClientboundServerLinksPacket packet) {
    serverConn.getPlayer().getConnection().write(packet);
    return true;
  }

  @Override
  public boolean handle(KeepAlivePacket packet) {
    serverConn.getPendingPings().put(packet.getRandomId(), System.nanoTime());
    serverConn.getPlayer().getConnection().write(packet);
    return true;
  }

  @Override
  public boolean handle(final ResourcePackRequestPacket packet) {
    final MinecraftConnection playerConnection = serverConn.getPlayer().getConnection();

    final ResourcePackInfo resourcePackInfo = packet.toServerPromptedPack();
    final ServerResourcePackSendEvent event =
        new ServerResourcePackSendEvent(resourcePackInfo, this.serverConn);

    server.getEventManager().fire(event).thenAcceptAsync(serverResourcePackSendEvent -> {
      if (playerConnection.isClosed()) {
        return;
      }
      if (serverResourcePackSendEvent.getResult().isAllowed()) {
        final ResourcePackInfo toSend = serverResourcePackSendEvent.getProvidedResourcePack();
        boolean modifiedPack = false;
        if (toSend != serverResourcePackSendEvent.getReceivedResourcePack()) {
          ((VelocityResourcePackInfo) toSend).setOriginalOrigin(
              ResourcePackInfo.Origin.DOWNSTREAM_SERVER);
          modifiedPack = true;
        }
        final ResourcePackHandler packs = serverConn.getPlayer().resourcePackHandler();
        // A pack already applied or declined by the player is not sent to it again
        if (packs.answerKnownPack(packet.getId(), toSend.getHash(), serverConn.getConnection())) {
          if (modifiedPack) {
            logger.warn("A plugin has tried to modify a ResourcePack provided by the backend server "
                    + "with a ResourcePack already applied or declined, the applying of the resource pack will be skipped.");
          }
        } else {
          packs.queueServerPack(toSend);
        }
      } else if (serverConn.getConnection() != null) {
        serverConn.getConnection().write(new ResourcePackResponsePacket(
                packet.getId(), PlayerResourcePackStatusEvent.Status.DECLINED));
      }
    }, playerConnection.eventLoop()).exceptionally((ex) -> {
      if (serverConn.getConnection() != null) {
        serverConn.getConnection().write(new ResourcePackResponsePacket(
                packet.getId(), PlayerResourcePackStatusEvent.Status.DECLINED));
      }
      logger.error("Exception while handling resource pack send for {}", playerConnection, ex);
      return null;
    });

    return true;
  }

  @Override
  public boolean handle(RemoveResourcePackPacket packet) {
    // Packs of servers stay on the client while the player switches servers, the pack of the next
    // server replaces them (see ResourcePackHandler#queueServerPack)
    return true;
  }

  @Override
  public boolean handle(FinishedUpdatePacket packet) {
    final MinecraftConnection smc = serverConn.ensureConnected();
    final ConnectedPlayer player = serverConn.getPlayer();
    final ClientConfigSessionHandler configHandler = (ClientConfigSessionHandler) player.getConnection().getActiveSessionHandler();

    smc.getChannel().pipeline().get(MinecraftVarintFrameDecoder.class).setState(StateRegistry.PLAY);
    smc.getChannel().pipeline().get(MinecraftDecoder.class).setState(StateRegistry.PLAY);
    //noinspection DataFlowIssue
    configHandler.handleBackendFinishUpdate(serverConn).thenRunAsync(() -> {
      smc.write(FinishedUpdatePacket.INSTANCE);
      if (serverConn == player.getConnectedServer()) {
        smc.setActiveSessionHandler(StateRegistry.PLAY);
      } else {
        smc.setActiveSessionHandler(StateRegistry.PLAY, new TransitionSessionHandler(server, serverConn, resultFuture));
      }
    }, smc.eventLoop());
    return true;
  }

  @Override
  public boolean handle(DisconnectPacket packet) {
    serverConn.disconnect();
    // If the player receives a DisconnectPacket without a connection to a server in progress,
    // it means that the backend server has kicked the player during reconfiguration
    if (serverConn.getPlayer().getConnectionInFlight() != null) {
      resultFuture.complete(ConnectionRequestResults.forDisconnect(packet, serverConn.getServer()));
    } else {
      serverConn.getPlayer().handleConnectionException(serverConn.getServer(), packet, true);
    }
    return true;
  }

  @Override
  public boolean handle(PluginMessagePacket packet) {
    if (PluginMessageUtil.isMcBrand(packet)) {
      serverConn.getPlayer().getConnection().write(
          PluginMessageUtil.rewriteMinecraftBrand(packet, server.getVersion()));
    } else {
      ChannelIdentifier id = this.server.getChannelRegistrar().getFromId(packet.getChannel());

      if (id == null) {
        serverConn.getPlayer().getConnection().write(packet.retain());
        return true;
      }

      // Handling this stuff async means that we should probably pause
      // the connection while we toss this off into another pool
      byte[] bytes = ByteBufUtil.getBytes(packet.content());
      this.serverConn.getConnection().setAutoReading(false);
      this.server.getEventManager()
          .fire(new PluginMessageEvent(serverConn, serverConn.getPlayer(), id, bytes))
          .thenAcceptAsync(pme -> {
            if (pme.getResult().isAllowed() && !serverConn.getPlayer().getConnection().isClosed()) {
              serverConn.getPlayer().getConnection().write(new PluginMessagePacket(
                  pme.getIdentifier().getId(), Unpooled.wrappedBuffer(bytes)));
            }
            this.serverConn.getConnection().setAutoReading(true);
          },  serverConn.ensureConnected().eventLoop()).exceptionally((ex) -> {
            logger.error("Exception while handling plugin message {}", packet, ex);
            return null;
          });
    }
    return true;
  }

  @Override
  public boolean handle(RegistrySyncPacket packet) {
    serverConn.getPlayer().getConnection().write(packet.retain());
    return true;
  }

  @Override
  public boolean handle(TransferPacket packet) {
    final InetSocketAddress originalAddress = packet.address();
    if (originalAddress == null) {
      logger.error("""
          Unexpected nullable address received in TransferPacket \
          from Backend Server in Configuration State""");
      return true;
    }
    this.server.getEventManager()
            .fire(new PreTransferEvent(this.serverConn.getPlayer(), originalAddress))
            .thenAcceptAsync(event -> {
              if (event.getResult().isAllowed()) {
                InetSocketAddress resultedAddress = event.getResult().address();
                if (resultedAddress == null) {
                  resultedAddress = originalAddress;
                }
                serverConn.getPlayer().getConnection().write(new TransferPacket(
                        resultedAddress.getHostName(), resultedAddress.getPort()));
              }
            }, serverConn.ensureConnected().eventLoop());
    return true;
  }

  @Override
  public boolean handle(ClientboundStoreCookiePacket packet) {
    server.getEventManager()
        .fire(new CookieStoreEvent(serverConn.getPlayer(), packet.getKey(), packet.getPayload()))
        .thenAcceptAsync(event -> {
          if (event.getResult().isAllowed()) {
            final Key resultedKey = event.getResult().getKey() == null
                ? event.getOriginalKey() : event.getResult().getKey();
            final byte[] resultedData = event.getResult().getData() == null
                ? event.getOriginalData() : event.getResult().getData();

            serverConn.getPlayer().getConnection()
                .write(new ClientboundStoreCookiePacket(resultedKey, resultedData));
          }
        }, serverConn.ensureConnected().eventLoop());

    return true;
  }

  @Override
  public boolean handle(ClientboundCookieRequestPacket packet) {
    server.getEventManager().fire(new CookieRequestEvent(serverConn.getPlayer(), packet.getKey()))
        .thenAcceptAsync(event -> {
          if (event.getResult().isAllowed()) {
            final Key resultedKey = event.getResult().getKey() == null
                ? event.getOriginalKey() : event.getResult().getKey();

            serverConn.getPlayer().getConnection().write(new ClientboundCookieRequestPacket(resultedKey));
          }
        }, serverConn.ensureConnected().eventLoop());

    return true;
  }

  @Override
  public boolean handle(CodeOfConductPacket packet) {
    this.serverConn.getPlayer().getConnection().write(packet.retain());
    return true;
  }

  @Override
  public void disconnected() {
    resultFuture.complete(ConnectionRequestResults.forDisconnect(
        ConnectionMessages.INTERNAL_SERVER_CONNECTION_ERROR, serverConn.getServer()));
  }

  @Override
  public void handleGeneric(MinecraftPacket packet) {
    serverConn.getPlayer().getConnection().write(packet);
  }

  @Override
  public void writabilityChanged() {
    Channel serverChan = serverConn.ensureConnected().getChannel();
    boolean writable = serverChan.isWritable();

    if (BACKPRESSURE_LOG) {
      if (writable) {
        logger.info("{} is writable, will auto-read player connection data", this.serverConn);
      } else {
        logger.info("{} is not writable, not auto-reading player connection data", this.serverConn);
      }
    }

    serverConn.getPlayer().getConnection().setAutoReading(writable);
  }

  private void switchFailure(Throwable cause) {
    logger.error("Unable to switch to new server {} for {}", serverConn.getServerInfo().getName(),
        serverConn.getPlayer().getUsername(), cause);
    serverConn.getPlayer().disconnect(ConnectionMessages.INTERNAL_SERVER_CONNECTION_ERROR);
    resultFuture.completeExceptionally(cause);
  }

}