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

package com.velocitypowered.proxy.protocol.packet.title;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code LegacyTitlePacket} class represents a packet that handles title-related functionality
 * for older versions of Minecraft where title handling differs.
 *
 * <p>This packet is used to send title and subtitle information using legacy methods for clients
 * that do not support the newer title packet format.</p>
 *
 * <p>It extends the {@link GenericTitlePacket}, inheriting basic title properties but is specifically
 * focused on legacy title implementations.</p>
 */
@Getter
@Setter
@ToString
public class LegacyTitlePacket extends GenericTitlePacket {

  private final ActionType action;

  private @Nullable ComponentHolder component;
  private int fadeIn;
  private int stay;
  private int fadeOut;

  public LegacyTitlePacket() {
    throw new UnsupportedOperationException("Decode is not implemented");
  }

  public LegacyTitlePacket(ActionType action) {
    this.action = Preconditions.checkNotNull(action, "action");
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    if (version.lessThan(ProtocolVersion.MINECRAFT_1_11)
        && this.action == ActionType.SET_ACTION_BAR) {
      throw new IllegalStateException("Action bars are only supported on 1.11 and newer");
    }
    ProtocolUtils.writeVarInt(buf, this.action.getAction(version));

    switch (this.action) {
      case SET_TITLE, SET_SUBTITLE, SET_ACTION_BAR -> {
        if (component == null) {
          throw new IllegalStateException("No component found for " + this.action);
        }
        component.write(buf);
      }
      case SET_TIMES -> {
        buf.writeInt(fadeIn);
        buf.writeInt(stay);
        buf.writeInt(fadeOut);
      }
      case HIDE, RESET -> {}
      default -> throw new UnsupportedOperationException("Unknown action " + this.action);
    }
  }

  @Override
  public @NotNull ActionType getAction() {
    return action;
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }
}
