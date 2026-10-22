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

import com.google.common.base.Preconditions;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import com.velocitypowered.proxy.util.except.QuietDecoderException;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * Represents the packet sent from the client to the server during the login phase.
 * This packet contains the player's username and UUID.
 */
@Getter
@Setter
@ToString
public class ServerLoginPacket implements MinecraftPacket {

  private static final QuietDecoderException EMPTY_USERNAME = new QuietDecoderException(
      "Empty username!");

  private @Nullable String username;
  private @Nullable UUID holderUuid;

  public ServerLoginPacket() {
  }

  /**
   * Constructs a new {@code ServerLoginPacket} with the specified username and holder UUID.
   *
   * @param username the player's username
   * @param holderUuid the holder UUID
   */
  public ServerLoginPacket(String username, UUID holderUuid) {
    this.username = Preconditions.checkNotNull(username, "username");
    this.holderUuid = Preconditions.checkNotNull(holderUuid, "holderUuid");
  }

  /**
   * Gets the player's username from the login packet.
   *
   * @return the player's username
   * @throws IllegalStateException if the username is not specified
   */
  public String getUsername() {
    if (username == null) {
      throw new IllegalStateException("No username found!");
    }
    return username;
  }

  @Override
  public void decode(ByteBuf buf, Direction direction, ProtocolVersion version) {
    username = ProtocolUtils.readString(buf, 16);
    if (username.isEmpty()) {
      throw EMPTY_USERNAME;
    }
    this.holderUuid = ProtocolUtils.readUuid(buf);
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    if (username == null) {
      throw new IllegalStateException("No username found!");
    }
    ProtocolUtils.writeString(buf, username);
    ProtocolUtils.writeUuid(buf, this.holderUuid);
  }

  @Override
  public int decodeExpectedMaxLength(ByteBuf buf, Direction direction, ProtocolVersion version) {
    // Accommodate the rare (but likely malicious) use of UTF-8 usernames, since it is technically
    // legal on the protocol level.
    // + 2 * 8 for the UUID msb/lsb (plus one spare byte, as before)
    return 1 + (16 * 3) + 1 + 8 + 8;
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }
}
