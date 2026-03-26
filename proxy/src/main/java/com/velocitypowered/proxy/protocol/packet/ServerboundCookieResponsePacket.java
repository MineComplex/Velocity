/*
 * Copyright (C) 2024 Velocity Contributors
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a server-bound packet sent by the client containing a key and an optional payload.
 * This packet is typically used for exchanging metadata or other information between the client
 * and server.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerboundCookieResponsePacket implements MinecraftPacket {

  private Key key;
  private byte @Nullable [] payload;

  @Override
  public void decode(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion) {
    this.key = ProtocolUtils.readKey(buf);
    if (buf.readBoolean()) {
      this.payload = ProtocolUtils.readByteArray(buf, 5120);
    }
  }

  @Override
  public void encode(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion) {
    ProtocolUtils.writeKey(buf, key);
    final boolean hasPayload = payload != null && payload.length > 0;
    buf.writeBoolean(hasPayload);
    if (hasPayload) {
      ProtocolUtils.writeByteArray(buf, payload);
    }
  }

  @Override
  public int decodeExpectedMaxLength(ByteBuf buf, Direction direction, ProtocolVersion version) {
    return ProtocolUtils.DEFAULT_MAX_STRING_BYTES + 1 + 2 + 5120;
  }

  @Override
  public int decodeExpectedMinLength(ByteBuf buf, Direction direction, ProtocolVersion version) {
    return 1;
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }
}
