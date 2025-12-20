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
import com.velocitypowered.proxy.protocol.data.material.Material;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponentMap;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;

import java.util.Map;

import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_16_4;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_18_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_19_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_19_3;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_20_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_20_3;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_20_5;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_21;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_21_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_21_5;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_21_7;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_21_9;

@UtilityClass
public class EntityData {

  public int getFrameId(ProtocolVersion version) {
    if (version.noGreaterThan(MINECRAFT_1_16_4)) {
      return 38;
    } else if (version.noGreaterThan(MINECRAFT_1_18_2)) {
      return 42;
    } else if (version.noGreaterThan(MINECRAFT_1_19_1)) {
      return 45;
    } else if (version.noGreaterThan(MINECRAFT_1_19_3)) {
      return 46;
    } else if (version.noGreaterThan(MINECRAFT_1_20_2)) {
      return 56;
    } else if (version.noGreaterThan(MINECRAFT_1_20_3)) {
      return 57;
    } else if (version.noGreaterThan(MINECRAFT_1_21)) {
      return 60;
    } else if (version.noGreaterThan(MINECRAFT_1_21_2)) {
      return 71;
    } else if (version.noGreaterThan(MINECRAFT_1_21_5)) {
      return 70;
    } else if (version.noGreaterThan(MINECRAFT_1_21_7)) {
      return 71;
    } else if (version.noGreaterThan(MINECRAFT_1_21_9)) {
      return 72;
    } else {
      return 73;
    }
  }

  public int getBoatId(ProtocolVersion version) {
    if (version.noGreaterThan(MINECRAFT_1_16_4)) {
      return 6;
    } else if (version.noGreaterThan(MINECRAFT_1_18_2)) {
      return 7;
    } else if (version.noGreaterThan(MINECRAFT_1_19_3)) {
      return 8;
    } else if (version.noGreaterThan(MINECRAFT_1_20_3)) {
      return 9;
    } else if (version.noGreaterThan(MINECRAFT_1_21)) {
      return 10;
    } else if (version.noGreaterThan(MINECRAFT_1_21_2)) { // 1.21.2 split the boat type in id registries.
      return 85;
    } else if (version.noGreaterThan(MINECRAFT_1_21_5)) {
      return 84;
    } else if (version.noGreaterThan(MINECRAFT_1_21_7)) {
      return 85;
    } else if (version.noGreaterThan(MINECRAFT_1_21_9)) {
      return 87;
    }
    return 89;
  }

  public int getMinecartId(ProtocolVersion version) {
    if (version.noGreaterThan(MINECRAFT_1_16_4)) {
      return 45;
    } else if (version.noGreaterThan(MINECRAFT_1_18_2)) {
      return 50;
    } else if (version.noGreaterThan(MINECRAFT_1_19_1)) {
      return 53;
    } else if (version.noGreaterThan(MINECRAFT_1_19_3)) {
      return 54;
    } else if (version.noGreaterThan(MINECRAFT_1_20_2)) {
      return 64;
    } else if (version.noGreaterThan(MINECRAFT_1_20_3)) {
      return 65;
    } else if (version.noGreaterThan(MINECRAFT_1_21)) {
      return 69;
    } else if (version.noGreaterThan(MINECRAFT_1_21_2)) {
      return 82;
    } else if (version.noGreaterThan(MINECRAFT_1_21_5)) {
      return 81;
    } else if (version.noGreaterThan(MINECRAFT_1_21_7)) {
      return 82;
    } else if (version.noGreaterThan(MINECRAFT_1_21_9)) {
      return 84;
    }
    return 85;
  }

  public byte getMetadataIndex(ProtocolVersion version) {
    if (version.noGreaterThan(MINECRAFT_1_16_4)) {
      return 7;
    } else if (version.noGreaterThan(MINECRAFT_1_21_5)) {
      return 8;
    }
    return 9;
  }

  public EntityMetadata createMapMetadata(ProtocolVersion version, int mapId) {
    return new EntityMetadata(Map.of(
        getMetadataIndex(version), new EntityMetadata.SlotEntry(Item.fromItem(Material.FILLED_MAP), 1, 0,
            CompoundBinaryTag.builder().put("map", IntBinaryTag.intBinaryTag(mapId)).build(),
            new ItemComponentMap().add(MINECRAFT_1_20_5, "minecraft:map_id", mapId))
    ));
  }

  public EntityMetadata createRotationMetadata(ProtocolVersion version, int rotation) {
    return new EntityMetadata(Map.of(
        (byte) (getMetadataIndex(version) + 1), new EntityMetadata.VarIntEntry(rotation)
    ));
  }

}
