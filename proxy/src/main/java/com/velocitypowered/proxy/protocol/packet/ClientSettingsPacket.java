/*
 * Copyright (C) 2018-2022 Velocity Contributors
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
import io.netty.buffer.ByteBufUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * Represents the client settings packet in Minecraft, which is sent by the client
 * to the server to communicate its settings such as locale, view distance, chat preferences,
 * skin customization, and other client-side configurations.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientSettingsPacket implements MinecraftPacket {
  private @Nullable String locale;
  private byte viewDistance;
  private int chatVisibility;
  private boolean chatColors;
  private short skinParts;
  private int mainHand;
  private boolean textFilteringEnabled;
  private boolean clientListingAllowed; // overwrites server-list "anonymous" mode
  private int particleStatus;

  /**
   * Gets the client's locale.
   *
   * @return the locale
   * @throws IllegalStateException if no locale is specified
   */
  public String getLocale() {
    if (locale == null) {
      throw new IllegalStateException("No locale specified");
    }
    return locale;
  }

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    this.locale = ProtocolUtils.readString(buf, 16);
    this.viewDistance = buf.readByte();
    this.chatVisibility = ProtocolUtils.readVarInt(buf);
    this.chatColors = buf.readBoolean();
    this.skinParts = buf.readUnsignedByte();
    this.mainHand = ProtocolUtils.readVarInt(buf);
    this.textFilteringEnabled = buf.readBoolean();
    this.clientListingAllowed = buf.readBoolean();
    this.particleStatus = ProtocolUtils.readVarInt(buf);
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    if (locale == null) {
      throw new IllegalStateException("No locale specified");
    }
    ProtocolUtils.writeString(buf, locale);
    buf.writeByte(viewDistance);
    ProtocolUtils.writeVarInt(buf, chatVisibility);
    buf.writeBoolean(chatColors);
    buf.writeByte(skinParts);
    ProtocolUtils.writeVarInt(buf, mainHand);
    buf.writeBoolean(textFilteringEnabled);
    buf.writeBoolean(clientListingAllowed);
    ProtocolUtils.writeVarInt(buf, particleStatus);
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }

  @Override
  public int decodeExpectedMaxLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    return 1 + ByteBufUtil.utf8MaxBytes(16) + 8;
  }

  @Override
  public int decodeExpectedMinLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    return 1 // locale
        + 1  // viewDistance
        + 1  // chatVisibility
        + 1  // chatColors
        + 1  // skinParts
        + 1  // mainHand
        + 1  // textFilteringEnabled
        + 1  // clientListingAllowed
        + 1; // particleStatus
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ClientSettingsPacket that = (ClientSettingsPacket) o;
    return viewDistance == that.viewDistance
        && chatVisibility == that.chatVisibility
        && chatColors == that.chatColors
        && skinParts == that.skinParts
        && mainHand == that.mainHand
        && textFilteringEnabled == that.textFilteringEnabled
        && clientListingAllowed == that.clientListingAllowed
        && particleStatus == that.particleStatus
        && Objects.equals(locale, that.locale);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        locale,
        viewDistance,
        chatVisibility,
        chatColors,
        skinParts,
        mainHand,
        textFilteringEnabled,
        clientListingAllowed,
        particleStatus);
  }
}
