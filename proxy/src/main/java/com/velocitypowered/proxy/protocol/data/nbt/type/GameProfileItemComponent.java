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

package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class GameProfileItemComponent extends ItemComponent<GameProfile> {

  private static final UUID ZERO = new UUID(0, 0);

  public GameProfileItemComponent(String name) {
    super(name);
  }

  @Override
  public void write(ProtocolVersion version, ByteBuf buffer) {
    buffer.writeBoolean(!getValue().getName().isEmpty());
    if (!getValue().getName().isEmpty()) {
      ProtocolUtils.writeString(buffer, getValue().getName());
    }

    buffer.writeBoolean(!getValue().getId().equals(ZERO));
    if (!getValue().getId().equals(ZERO)) {
      ProtocolUtils.writeUuid(buffer, getValue().getId());
    }

    ProtocolUtils.writeProperties(buffer, getValue().getProperties());
  }
}
