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

package com.velocitypowered.proxy.protocol.data.nbt;

import com.google.common.collect.Lists;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ItemComponentMap {

  private final List<ItemComponent<?>> addedComponents = Lists.newArrayList();
  private final List<ItemComponent<?>> removedComponents = Lists.newArrayList();

  public <T> ItemComponentMap add(ProtocolVersion version, String name, T value) {
    addedComponents.add((ItemComponent<?>) ItemComponentManager.createComponent(version, name).setValue(value));
    return this;
  }

  public ItemComponentMap remove(ProtocolVersion version, String name) {
    removedComponents.add(ItemComponentManager.createComponent(version, name));
    return null;
  }

  public List<ItemComponent<?>> getAdded() {
    return addedComponents;
  }

  public List<ItemComponent<?>> getRemoved() {
    return removedComponents;
  }

  public void read(ProtocolVersion version, Object buffer) {
    // TODO: implement
    throw new UnsupportedOperationException("read");
  }

  public void write(ProtocolVersion version, Object buffer) {
    ByteBuf buf = (ByteBuf) buffer;

    ProtocolUtils.writeVarInt(buf, getAdded().size());
    ProtocolUtils.writeVarInt(buf, getRemoved().size());

    for (ItemComponent<?> component : addedComponents) {
      ProtocolUtils.writeVarInt(buf, ItemComponentManager.getId(component.getName(), version));
      component.write(version, buf);
    }

    for (ItemComponent<?> component : removedComponents) {
      ProtocolUtils.writeVarInt(buf, ItemComponentManager.getId(component.getName(), version));
    }
  }
}
