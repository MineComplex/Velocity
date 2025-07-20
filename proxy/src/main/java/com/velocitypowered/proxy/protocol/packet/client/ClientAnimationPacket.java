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
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"entityId", "hand"})
public class ClientAnimationPacket implements MinecraftPacket {

  public static int MAIN_HAND = 0;
  private int entityId = -1;
  private int hand = MAIN_HAND;
  private LegacyAnimationType type = LegacyAnimationType.SWING_ARM;

  @Override
  public void encode(ByteBuf bytebuf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void decode(@NotNull ByteBuf bytebuf, ProtocolUtils.Direction direction, @NotNull ProtocolVersion version) {
    hand = ProtocolUtils.readVarInt(bytebuf);
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    handler.handleGeneric(this);
    return true;
  }

  @Getter
  public enum LegacyAnimationType {
    NO_ANIMATION,
    SWING_ARM,
    DAMAGE_ANIMATION,
    LEAVE_BED,
    EAT_FOOD,
    CRITICAL_EFFECT,
    MAGIC_CRITICAL_EFFECT,
    UNKNOWN(102),
    CROUCH(104),
    @SuppressWarnings("SpellCheckingInspection")
    UNCROUCH(105);

    private final int id;

    LegacyAnimationType(int id) {
      this.id = id;
    }

    LegacyAnimationType() {
      this.id = ordinal();
    }

    public static LegacyAnimationType getById(int id) {
      if (id >= 0 && id <= 7) {
        return LegacyAnimationType.values()[id];
      }
      return switch (id) {
        case 102 -> UNKNOWN;
        case 104 -> CROUCH;
        case 105 -> UNCROUCH;
        default -> throw new IllegalArgumentException("Unknown type with id: " + id);
      };
    }
  }
}
