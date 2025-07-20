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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServerMapDataPacket implements MinecraftPacket {

  private int mapId;
  private byte[] buffer;
  private int x, y;
  private int scaling;
  private boolean locked;

  @Override
  public void encode(@NotNull ByteBuf byteBuf, @NotNull ProtocolUtils.Direction direction, @NotNull ProtocolVersion protocolVersion) {
    ProtocolUtils.writeVarInt(byteBuf, mapId); // item damage
    byteBuf.writeByte(scaling);

    if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_17)) {
      byteBuf.writeBoolean(false); // no icon
    }

    byteBuf.writeBoolean(locked);

    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_17)) {
      byteBuf.writeBoolean(false); // no icon
    } else {
      ProtocolUtils.writeVarInt(byteBuf, 0); // no icon
    }

    byteBuf.writeByte(128); // rows
    byteBuf.writeByte(128); // columns
    byteBuf.writeByte(x);
    byteBuf.writeByte(y);

    ProtocolUtils.writeVarInt(byteBuf, buffer.length);
    byteBuf.writeBytes(buffer);
  }

  @Override
  public void decode(ByteBuf byteBuf, @NotNull ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return true;
  }

}