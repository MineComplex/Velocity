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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ClientSettingsPacket implements MinecraftPacket {
  private @Nullable String locale;
  private byte viewDistance;
  private int chatVisibility;
  private boolean chatColors;
  private byte difficulty; // 1.7 Protocol
  private short skinParts;
  private int mainHand;
  private boolean textFilteringEnabled; // Added in 1.17
  private boolean clientListingAllowed; // Added in 1.18, overwrites server-list "anonymous" mode
  private int particleStatus; // Added in 1.21.2

  public ClientSettingsPacket(String locale, byte viewDistance, int chatVisibility, boolean chatColors,
                              short skinParts, int mainHand, boolean textFilteringEnabled, boolean clientListingAllowed,
                              int particleStatus) {
    this.locale = locale;
    this.viewDistance = viewDistance;
    this.chatVisibility = chatVisibility;
    this.chatColors = chatColors;
    this.skinParts = skinParts;
    this.mainHand = mainHand;
    this.textFilteringEnabled = textFilteringEnabled;
    this.clientListingAllowed = clientListingAllowed;
    this.particleStatus = particleStatus;
  }

  public ClientSettingsPacket() {
  }

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

    if (version.noGreaterThan(ProtocolVersion.MINECRAFT_1_7_6)) {
      this.difficulty = buf.readByte();
    }

    this.skinParts = buf.readUnsignedByte();

    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_9)) {
      this.mainHand = ProtocolUtils.readVarInt(buf);

      if (version.noLessThan(ProtocolVersion.MINECRAFT_1_17)) {
        this.textFilteringEnabled = buf.readBoolean();

        if (version.noLessThan(ProtocolVersion.MINECRAFT_1_18)) {
          this.clientListingAllowed = buf.readBoolean();

          if (version.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
            this.particleStatus = ProtocolUtils.readVarInt(buf);
          }
        }
      }
    }
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

    if (version.noGreaterThan(ProtocolVersion.MINECRAFT_1_7_6)) {
      buf.writeByte(difficulty);
    }

    buf.writeByte(skinParts);

    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_9)) {
      ProtocolUtils.writeVarInt(buf, mainHand);

      if (version.noLessThan(ProtocolVersion.MINECRAFT_1_17)) {
        buf.writeBoolean(textFilteringEnabled);

        if (version.noLessThan(ProtocolVersion.MINECRAFT_1_18)) {
          buf.writeBoolean(clientListingAllowed);
        }

        if (version.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
          ProtocolUtils.writeVarInt(buf, particleStatus);
        }
      }
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
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
        && difficulty == that.difficulty
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
        difficulty,
        skinParts,
        mainHand,
            textFilteringEnabled,
        clientListingAllowed,
        particleStatus);
  }
}
