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

package com.velocitypowered.proxy.connection.util;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.ModInfo;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.config.VelocityConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Common utilities for handling server list ping results.
 */
public class ServerListPingHandler {

  private final VelocityServer server;

  public ServerListPingHandler(VelocityServer server) {
    this.server = server;
  }

  private ServerPing constructLocalPing(ProtocolVersion version) {
    if (version == ProtocolVersion.UNKNOWN) {
      version = ProtocolVersion.MAXIMUM_VERSION;
    }
    VelocityConfiguration configuration = server.getConfiguration();
    List<ServerPing.SamplePlayer> samplePlayers;
    if (configuration.getSamplePlayersInPing()) {
      List<ServerPing.SamplePlayer> unshuffledPlayers = server.getAllPlayers().stream()
          .map(p -> {
            if (p.getPlayerSettings().isClientListingAllowed()) {
              return new ServerPing.SamplePlayer(p.getUsername(), p.getUniqueId());
            } else {
              return ServerPing.SamplePlayer.ANONYMOUS;
            }
          })
          .collect(Collectors.toList());
      Collections.shuffle(unshuffledPlayers);
      samplePlayers = unshuffledPlayers.subList(0, Math.min(12, unshuffledPlayers.size()));
    } else {
      samplePlayers = ImmutableList.of();
    }
    return new ServerPing(
        new ServerPing.Version(version.getProtocol(),
            "Velocity " + ProtocolVersion.SUPPORTED_VERSION_STRING),
        new ServerPing.Players(server.getPlayerCount(), configuration.getShowMaxPlayers(),
            samplePlayers),
        configuration.getMotd(),
        configuration.getFavicon().orElse(null),
        configuration.isAnnounceForge() ? ModInfo.DEFAULT : null
    );
  }

  /**
   * Fetches the "default" server ping for a player.
   *
   * @param connection the connection
   * @return a future with the initial ping result
   */
  public CompletableFuture<ServerPing> getInitialPing(VelocityInboundConnection connection) {
    ProtocolVersion shownVersion = connection.getProtocolVersion().isSupported()
        ? connection.getProtocolVersion() : ProtocolVersion.MAXIMUM_VERSION;
    return CompletableFuture.completedFuture(constructLocalPing(shownVersion));
  }
}
