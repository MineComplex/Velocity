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

package com.velocitypowered.proxy.protocol.data.tag;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.item.Item;
import com.velocitypowered.proxy.protocol.data.world.WorldVersion;
import com.velocitypowered.proxy.protocol.packet.server.ServerTagsPacket;
import lombok.experimental.UtilityClass;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class TagManager {

  private final Map<String, Integer> FLUIDS = new HashMap<>();
  private final Map<WorldVersion, ServerTagsPacket> VERSION_MAP = new EnumMap<>(WorldVersion.class);

  @SuppressWarnings("unchecked")
  public void init() {
    Gson gson = new Gson();
    LinkedTreeMap<String, String> fluids = gson.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(TagManager.class.getClassLoader().getResourceAsStream("mapping/fluids.json")),
            StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    fluids.forEach((id, protocolId) -> FLUIDS.put(id, Integer.valueOf(protocolId)));

    LinkedTreeMap<String, LinkedTreeMap<String, List<String>>> tags = gson.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(TagManager.class.getClassLoader().getResourceAsStream("mapping/tags.json")),
            StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    for (WorldVersion version : WorldVersion.values()) {
      VERSION_MAP.put(version, localGetTagsForVersion(tags, version));
    }
  }

  public ServerTagsPacket getUpdateTagsPacket(ProtocolVersion version) {
    ServerTagsPacket packet = VERSION_MAP.get(WorldVersion.from(version));
    if (packet == null) {
      throw new NullPointerException("Not found tags packet for " + version + " or world version " + WorldVersion.from(version));
    }
    return packet;
  }

  public ServerTagsPacket getUpdateTagsPacket(WorldVersion version) {
    return VERSION_MAP.get(version);
  }

  private ServerTagsPacket localGetTagsForVersion(LinkedTreeMap<String, LinkedTreeMap<String, List<String>>> defaultTags,
                                                  WorldVersion version) {
    Map<String, Map<String, List<Integer>>> tags = new LinkedTreeMap<>();
    defaultTags.forEach((tagType, defaultTagList) -> {
      LinkedTreeMap<String, List<Integer>> tagList = new LinkedTreeMap<>();
      switch (tagType) {
        case "minecraft:block": {
          defaultTagList.forEach((tagName, blockList) ->
              tagList.put(tagName, blockList.stream()
                  .map(e -> Block.fromModernId(e, Map.of()))
                  .filter(e -> e.isSupportedOn(version))
                  .map(e -> (int) e.getBlockId(version))
                  .collect(Collectors.toList())));
          break;
        }
        case "minecraft:fluid": {
          defaultTagList.forEach((tagName, fluidList) ->
              tagList.put(tagName, fluidList.stream().map(FLUIDS::get).collect(Collectors.toList())));
          break;
        }
        case "minecraft:item": {
          defaultTagList.forEach((tagName, itemList) ->
              tagList.put(tagName, itemList.stream()
                  .map(Item::fromModernId)
                  .filter(item -> item.isSupportedOn(version))
                  .map(item -> (int) item.getId(version))
                  .collect(Collectors.toList())));
          break;
        }
        default: {
          defaultTagList.forEach((tagName, entryList) -> {
            if (!entryList.isEmpty()) {
              throw new IllegalStateException("The " + tagType + " tag type is not supported yet.");
            }

            tagList.put(tagName, List.of());
          });
          break;
        }
      }

      tags.put(tagType, tagList);
    });

    return new ServerTagsPacket(tags);
  }
}
