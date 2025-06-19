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

package com.velocitypowered.proxy.protocol.data.item;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.material.Material;
import com.velocitypowered.proxy.protocol.data.world.WorldVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class Item {

  private static final Gson GSON = new Gson();

  private static final Map<String, Item> MODERN_ID_MAP = new HashMap<>();
  private static final Map<Integer, Item> LEGACY_ID_MAP = new HashMap<>();

  @Getter
  private final String modernId;
  private final Map<WorldVersion, Short> versionIds = new EnumMap<>(WorldVersion.class);

  @SuppressWarnings("unchecked")
  public static void init() {
    LinkedTreeMap<String, LinkedTreeMap<String, String>> itemsMapping = GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(Item.class.getClassLoader().getResourceAsStream("mapping/items_mapping.json")), StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    LinkedTreeMap<String, String> modernItems = GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(Item.class.getClassLoader().getResourceAsStream("mapping/items.json")), StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    LinkedTreeMap<String, String> legacyItems = GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(Item.class.getClassLoader().getResourceAsStream("mapping/legacyitems.json")), StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    LinkedTreeMap<String, String> modernIdRemap = GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(Item.class.getClassLoader().getResourceAsStream("mapping/modern_item_id_remap.json")), StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    modernItems.forEach((modernId, modernProtocolId) -> {
      Item simpleItem = new Item(modernId);
      itemsMapping.get(modernProtocolId).forEach((key, value) -> simpleItem.versionIds.put(WorldVersion.parse(key), Short.parseShort(value)));
      MODERN_ID_MAP.put(modernId, simpleItem);

      String remapped = modernIdRemap.get(modernId);
      if (remapped != null) {
        if (MODERN_ID_MAP.containsKey(remapped)) {
          throw new IllegalStateException("Remapped id " + remapped + " (from " + modernId + ") already exists");
        }

        MODERN_ID_MAP.put(remapped, simpleItem);
      }
    });

    legacyItems.forEach((legacyProtocolId, modernId) -> LEGACY_ID_MAP.put(Integer.parseInt(legacyProtocolId), MODERN_ID_MAP.get(modernId)));
  }

  public static Item fromItem(Material material) {
    return LEGACY_ID_MAP.get(material.getLegacyId());
  }

  public static Item fromLegacyId(int id) {
    return LEGACY_ID_MAP.get(id);
  }

  public static Item fromModernId(String id) {
    return MODERN_ID_MAP.get(id);
  }

  public short getId(ProtocolVersion version) {
    return getId(WorldVersion.from(version));
  }

  public short getId(WorldVersion version) {
    return versionIds.get(version);
  }

  public boolean isSupportedOn(ProtocolVersion version) {
    return isSupportedOn(WorldVersion.from(version));
  }

  public boolean isSupportedOn(WorldVersion version) {
    return versionIds.containsKey(version);
  }

}
