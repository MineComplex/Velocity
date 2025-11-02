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

package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerDefaultSpawnPositionPacket implements MinecraftPacket {

  private String dimension;
  private int posX;
  private int posY;
  private int posZ;
  private float yaw;
  private float pitch;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    throw new IllegalStateException();
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_9)) {
      ProtocolUtils.writeString(buf, dimension);
      buf.writeLong(((posX & 0x3FFFFFFL) << 38) | ((posZ & 0x3FFFFFFL) << 12) | (posY & 0xFFFL));
      buf.writeFloat(yaw);
      buf.writeFloat(pitch);
    } else {
      buf.writeLong(((posX & 0x3FFFFFFL) << 38) | ((posZ & 0x3FFFFFFL) << 12) | (posY & 0xFFFL));
      if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_17)) {
        buf.writeFloat(yaw);
      }
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return true;
  }

}
