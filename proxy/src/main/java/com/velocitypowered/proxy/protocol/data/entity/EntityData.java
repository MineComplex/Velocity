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

package com.velocitypowered.proxy.protocol.data.entity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.item.Item;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponentMap;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;

import java.util.Map;

@UtilityClass
public class EntityData {

  public int getFrameId(ProtocolVersion version) {
    return 74;
  }

  public int getBoatId(ProtocolVersion version) {
    return 90;
  }

  public int getMinecartId(ProtocolVersion version) {
    return 86;
  }

  public byte getMetadataIndex(ProtocolVersion version) {
    return 9;
  }

  public EntityMetadata createMapMetadata(ProtocolVersion version, int mapId) {
    return new EntityMetadata(Map.of(
        getMetadataIndex(version), new EntityMetadata.SlotEntry(Item.fromModernId("minecraft:filled_map"), 1,
            CompoundBinaryTag.builder().put("map", IntBinaryTag.intBinaryTag(mapId)).build(),
            new ItemComponentMap().add(version, "minecraft:map_id", mapId))
    ));
  }

  public EntityMetadata createRotationMetadata(ProtocolVersion version, int rotation) {
    return new EntityMetadata(Map.of(
        (byte) (getMetadataIndex(version) + 1), new EntityMetadata.VarIntEntry(rotation)
    ));
  }

}
