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

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerPositionRotationPacket implements MinecraftPacket {

  private double x, y, z;
  private float yaw, pitch;
  private int teleportId, relativeMask;
  private boolean onGround, horizontalCollision;
  private boolean dismountVehicle;

  @Override
  public void encode(ByteBuf bytebuf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    boolean v1_21_2 = version.noLessThan(ProtocolVersion.MINECRAFT_1_21_2);
    if (v1_21_2) {
      ProtocolUtils.writeVarInt(bytebuf, teleportId);
    }
    bytebuf.writeDouble(x);
    // Account for the minimum Y bounding box issue on 1.7.2-1.7.10
    bytebuf.writeDouble(version.noLessThan(ProtocolVersion.MINECRAFT_1_8) ? y : y + 1.62f);
    bytebuf.writeDouble(z);
    if (v1_21_2) {
      // Delta movement?
      bytebuf.writeDouble(0);
      bytebuf.writeDouble(0);
      bytebuf.writeDouble(0);
      bytebuf.writeFloat(yaw);
      bytebuf.writeFloat(pitch);
      bytebuf.writeInt(relativeMask);
    } else {
      bytebuf.writeFloat(yaw);
      bytebuf.writeFloat(pitch);
      bytebuf.writeByte(relativeMask);
    }

    if (version.greaterThan(ProtocolVersion.MINECRAFT_1_8)) {
      if (!v1_21_2) {
        ProtocolUtils.writeVarInt(bytebuf, teleportId);
      }

      if (version.noLessThan(ProtocolVersion.MINECRAFT_1_17)
          && version.noGreaterThan(ProtocolVersion.MINECRAFT_1_19_3)) {
        bytebuf.writeBoolean(dismountVehicle);
      }
    }
  }

  @Override
  public void decode(ByteBuf bytebuf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    x = bytebuf.readDouble();
    y = bytebuf.readDouble();
    if (version.lessThan(ProtocolVersion.MINECRAFT_1_8)) {
      // 1.7.2-1.7.10 send the minimum bounding box Y coordinate
      bytebuf.readDouble();
    }
    z = bytebuf.readDouble();
    yaw = bytebuf.readFloat();
    pitch = bytebuf.readFloat();
    if (version.greaterThan(ProtocolVersion.MINECRAFT_1_21_2)) {
      short flag = bytebuf.readUnsignedByte();
      onGround = (flag & 1) != 0;
      horizontalCollision = (flag & 2) != 0;
    } else {
      onGround = bytebuf.readBoolean();
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return true;
  }

  @Override
  public int expectedMaxLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    return version.lessThan(ProtocolVersion.MINECRAFT_1_8) ? 41 : 33;
  }

  @Override
  public int expectedMinLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    return 33;
  }

}
