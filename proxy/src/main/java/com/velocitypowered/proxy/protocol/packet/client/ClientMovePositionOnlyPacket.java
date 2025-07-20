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

package com.velocitypowered.proxy.protocol.packet.client;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.ClientLimboSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientMovePositionOnlyPacket implements MinecraftPacket {

  private double x;
  private double y;
  private double z;
  private boolean onGround;
  private boolean collideHorizontally;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    x = buf.readDouble();
    y = buf.readDouble();
    z = buf.readDouble();

    if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
      onGround = buf.readBoolean();
    } else {
      int flags = buf.readUnsignedByte();
      onGround = (flags & 1) != 0;
      collideHorizontally = (flags & 2) != 0;
    }
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    throw new IllegalStateException();
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    if (handler instanceof ClientLimboSessionHandler client) {
      return client.handle(this);
    } else {
      return true;
    }
  }

  @Override
  public int expectedMaxLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    return 25;
  }

  @Override
  public int expectedMinLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    return 25;
  }

}
