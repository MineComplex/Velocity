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
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Function;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerSpawnEntityPacket implements MinecraftPacket {

  private int entityId;
  private UUID uuid;
  private Function<ProtocolVersion, Integer> type;
  private double x, y, z;
  private float pitch, yaw, headYaw;
  private int data;
  private double velocityX, velocityY, velocityZ;

  @Override
  public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    ProtocolUtils.writeVarInt(byteBuf, entityId);

    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_9)) {
      ProtocolUtils.writeUuid(byteBuf, uuid != null ? uuid : UUID.randomUUID());
    }

    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_14)) {
      ProtocolUtils.writeVarInt(byteBuf, type.apply(protocolVersion));
    } else {
      byteBuf.writeByte(type.apply(protocolVersion));
    }

    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_9)) {
      byteBuf.writeDouble(x);
      byteBuf.writeDouble(y);
      byteBuf.writeDouble(z);
    } else {
      byteBuf.writeInt((int) (x * 32D));
      byteBuf.writeInt((int) (y * 32D));
      byteBuf.writeInt((int) (z * 32D));
    }

    byteBuf.writeByte((int) (pitch * (256.0F / 360.0F)));
    byteBuf.writeByte((int) (yaw * (256.0F / 360.0F)));

    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_19)) {
      byteBuf.writeByte((int) (headYaw * (256.0F / 360.0F)));
      ProtocolUtils.writeVarInt(byteBuf, data); // data
    } else {
      byteBuf.writeInt(data); // data
    }

    if (data > 0 || protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_9)) {
      byteBuf.writeShort((int) (velocityX * 8000D));
      byteBuf.writeShort((int) (velocityY * 8000D));
      byteBuf.writeShort((int) (velocityZ * 8000D));
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return true;
  }

  @Override
  public void decode(ByteBuf bytebuf, ProtocolUtils.Direction direction, @NotNull ProtocolVersion protocolVersion) {
    throw new UnsupportedOperationException();
  }
}
