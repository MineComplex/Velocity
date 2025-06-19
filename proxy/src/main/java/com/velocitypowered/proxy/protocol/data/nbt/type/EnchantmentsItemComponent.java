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
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;

import java.util.List;

public class EnchantmentsItemComponent extends ItemComponent<Pair<List<Pair<Integer, Integer>>, Boolean>> {

  public EnchantmentsItemComponent(String name) {
    super(name);
  }

  @Override
  public void write(ProtocolVersion version, ByteBuf buffer) {
    ProtocolUtils.writeVarInt(buffer, getValue().left().size());
    for (Pair<Integer, Integer> enchantment : getValue().left()) {
      ProtocolUtils.writeVarInt(buffer, enchantment.left());
      ProtocolUtils.writeVarInt(buffer, enchantment.right());
    }
    buffer.writeBoolean(getValue().right());
  }
}
