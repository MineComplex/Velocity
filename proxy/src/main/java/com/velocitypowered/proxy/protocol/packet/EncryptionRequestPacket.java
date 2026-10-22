/*
 * Copyright (C) 2018-2021 Velocity Contributors
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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.velocitypowered.proxy.connection.VelocityConstants.EMPTY_BYTE_ARRAY;

/**
 * Represents the encryption request packet in Minecraft, which is sent by the server
 * during the encryption handshake process. This packet is used to initiate secure
 * communication by providing the client with the server's public key and a verified token.
 * The client must respond with the encrypted shared secret and verify token.
 */
@Getter
@Setter
@ToString
public class EncryptionRequestPacket implements MinecraftPacket {

  private String serverId = "";
  private byte[] publicKey = EMPTY_BYTE_ARRAY;
  private byte[] verifyToken = EMPTY_BYTE_ARRAY;
  private boolean shouldAuthenticate = true;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    this.serverId = ProtocolUtils.readString(buf, 20);
    publicKey = ProtocolUtils.readByteArray(buf, 256);
    verifyToken = ProtocolUtils.readByteArray(buf, 16);
    shouldAuthenticate = buf.readBoolean();
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    ProtocolUtils.writeString(buf, this.serverId);
    ProtocolUtils.writeByteArray(buf, publicKey);
    ProtocolUtils.writeByteArray(buf, verifyToken);
    buf.writeBoolean(shouldAuthenticate);
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }
}
