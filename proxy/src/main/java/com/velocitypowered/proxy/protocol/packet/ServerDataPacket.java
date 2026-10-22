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
import com.velocitypowered.api.util.Favicon;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Represents the server data packet sent from the server to the client, which contains information
 * such as the server description, favicon, and secure chat enforcement status.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerDataPacket implements MinecraftPacket {

  private @Nullable ComponentHolder description;
  private @Nullable Favicon favicon;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction,
      ProtocolVersion protocolVersion) {
    this.description = ComponentHolder.read(buf);
    if (buf.readBoolean()) {
      byte[] iconBytes = ProtocolUtils.readByteArray(buf);
      this.favicon = new Favicon("data:image/png;base64,"
          + new String(Base64.getEncoder().encode(iconBytes), StandardCharsets.UTF_8));
    }
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction,
      ProtocolVersion protocolVersion) {
    this.description.write(buf);

    boolean hasFavicon = this.favicon != null;
    buf.writeBoolean(hasFavicon);
    if (hasFavicon) {
      String cutIconBase64 = favicon.getBase64Url().substring("data:image/png;base64,".length());
      byte[] iconBytes = Base64.getDecoder().decode(cutIconBase64.getBytes(StandardCharsets.UTF_8));
      ProtocolUtils.writeByteArray(buf, iconBytes);
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }

  @Override
  public int encodeSizeHint(Direction direction, ProtocolVersion version) {
    return 8 * 1024;
  }
}