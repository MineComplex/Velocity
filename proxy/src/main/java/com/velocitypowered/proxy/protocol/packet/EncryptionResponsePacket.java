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

package com.velocitypowered.proxy.protocol.packet;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.velocitypowered.proxy.connection.VelocityConstants.EMPTY_BYTE_ARRAY;

/**
 * Represents the encryption response packet in Minecraft, which is sent by the client
 * during the encryption handshake process. This packet contains the shared secret
 * and verifies the token used to establish secure communication between the client
 * and the server.
 */
@Getter
@Setter
@ToString
public class EncryptionResponsePacket implements MinecraftPacket {

  private byte[] sharedSecret = EMPTY_BYTE_ARRAY;
  private byte[] verifyToken = EMPTY_BYTE_ARRAY;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    this.sharedSecret = ProtocolUtils.readByteArray(buf, 128);
    this.verifyToken = ProtocolUtils.readByteArray(buf, 256);
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    ProtocolUtils.writeByteArray(buf, sharedSecret);
    ProtocolUtils.writeByteArray(buf, verifyToken);
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }

  @Override
  public int decodeExpectedMaxLength(ByteBuf buf, Direction direction, ProtocolVersion version) {
    // Both arrays have a 2 byte length prefix.
    return 256 + 2 + 2 + 128;
  }

  @Override
  public int decodeExpectedMinLength(ByteBuf buf, Direction direction, ProtocolVersion version) {
    return decodeExpectedMaxLength(buf, direction, version) - (128 + 8);
  }
}
