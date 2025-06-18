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
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.velocitypowered.api.network.ProtocolVersion.*;

@Getter
@Setter
@ToString
public class TabCompleteRequestPacket implements MinecraftPacket {

  private static final int VANILLA_MAX_TAB_COMPLETE_LEN = 2048;

  private @Nullable String command;
  private int transactionId;
  private boolean assumeCommand;
  private boolean hasPosition;
  private long position;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    if (version.noLessThan(MINECRAFT_1_13)) {
      this.transactionId = ProtocolUtils.readVarInt(buf);
      this.command = ProtocolUtils.readString(buf, VANILLA_MAX_TAB_COMPLETE_LEN);
    } else {
      this.command = ProtocolUtils.readString(buf, VANILLA_MAX_TAB_COMPLETE_LEN);
      if (version.noLessThan(MINECRAFT_1_9)) {
        this.assumeCommand = buf.readBoolean();
      }
      if (version.noLessThan(MINECRAFT_1_8)) {
        this.hasPosition = buf.readBoolean();
        if (hasPosition) {
          this.position = buf.readLong();
        }
      }
    }
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    if (command == null) {
      throw new IllegalStateException("Command is not specified");
    }

    if (version.noLessThan(MINECRAFT_1_13)) {
      ProtocolUtils.writeVarInt(buf, transactionId);
      ProtocolUtils.writeString(buf, command);
    } else {
      ProtocolUtils.writeString(buf, command);
      if (version.noLessThan(MINECRAFT_1_9)) {
        buf.writeBoolean(assumeCommand);
      }
      if (version.noLessThan(MINECRAFT_1_8)) {
        buf.writeBoolean(hasPosition);
        if (hasPosition) {
          buf.writeLong(position);
        }
      }
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }
}
