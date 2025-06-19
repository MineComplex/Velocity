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

package com.velocitypowered.proxy.protocol.packet;

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

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPacket implements MinecraftPacket {

  private int windowId, transactionId;
  private boolean accepted;

  @Override
  public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, @NotNull ProtocolVersion protocolVersion) {
    if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_17)) {
      windowId = byteBuf.readByte();
      transactionId = byteBuf.readShort();
      accepted = byteBuf.readBoolean();
    } else {
      transactionId = byteBuf.readInt();
      // Always set accepted to true since 1.17 or higher don't use
      // transactions for inventory confirmation anymore.
      accepted = true;
    }
  }

  @Override
  public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, @NotNull ProtocolVersion protocolVersion) {
    if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_17)) {
      byteBuf.writeByte(windowId);
      byteBuf.writeShort((short) transactionId);
      byteBuf.writeBoolean(accepted);
    } else {
      byteBuf.writeInt(transactionId);
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    handler.handleGeneric(this);
    return true;
  }
}
