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
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.entity.PackedVector;
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
public class ClientInteractPacket implements MinecraftPacket {

  private int entityId;
  private int type;
  private float targetX;
  private float targetY;
  private float targetZ;
  private int hand;
  private boolean sneaking;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_26_1)) {
      this.entityId = ProtocolUtils.readVarInt(buf);
      this.hand = ProtocolUtils.readVarInt(buf);
      PackedVector target = PackedVector.read(buf);
      this.targetX = (float) target.x();
      this.targetY = (float) target.y();
      this.targetZ = (float) target.z();
      this.sneaking = buf.readBoolean();
      return;
    }

    entityId = ProtocolUtils.readVarInt(buf);
    type = ProtocolUtils.readVarInt(buf);
    if (type == 2) {
      targetX = buf.readFloat();
      targetY = buf.readFloat();
      targetZ = buf.readFloat();
    }
    if (type == 0 || type == 2) {
      hand = ProtocolUtils.readVarInt(buf);
    }
    sneaking = buf.readBoolean();
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    throw new IllegalStateException();
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    handler.handleGeneric(this);
    return true;
  }

}
