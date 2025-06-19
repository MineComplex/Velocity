/*
 * Copyright (C) 2025 Velocity Contributors
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

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.packet.client.ClientMoveOnGroundOnlyPacket;
import com.velocitypowered.proxy.protocol.packet.client.ClientMovePacket;
import com.velocitypowered.proxy.protocol.packet.client.ClientMovePositionOnlyPacket;
import com.velocitypowered.proxy.protocol.packet.client.ClientMoveRotationOnlyPacket;
import com.velocitypowered.proxy.protocol.packet.client.ClientTeleportConfirmPacket;

public interface ClientLimboSessionHandler extends MinecraftSessionHandler {
  boolean handle(ClientMovePacket packet);

  boolean handle(ClientMovePositionOnlyPacket packet);

  boolean handle(ClientMoveRotationOnlyPacket packet);

  boolean handle(ClientMoveOnGroundOnlyPacket packet);

  boolean handle(ClientTeleportConfirmPacket packet);
}
