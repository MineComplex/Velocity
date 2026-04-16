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

package com.velocitypowered.proxy.protocol.data.block;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.world.WorldVersion;
import io.netty.util.collection.ShortObjectHashMap;
import io.netty.util.collection.ShortObjectMap;
import lombok.Getter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ToString
@Getter
public class Block {

  public static final Block AIR = new Block(false, true, false, "minecraft:air", (short) 0, (short) 0);
  private static final Gson GSON = new Gson();
  private static final Map<ProtocolVersion, ShortObjectMap<Short>> MODERN_BLOCK_STATE_IDS_MAP = new EnumMap<>(ProtocolVersion.class);
  private static final ShortObjectHashMap<String> MODERN_BLOCK_STATE_PROTOCOL_ID_MAP = new ShortObjectHashMap<>();
  private static final Map<String, Map<Set<String>, Short>> MODERN_BLOCK_STATE_STRING_MAP = new HashMap<>();
  private static final Map<String, Short> MODERN_BLOCK_STRING_MAP = new HashMap<>();
  private static final ShortObjectHashMap<Map<WorldVersion, Short>> LEGACY_BLOCK_IDS_MAP = new ShortObjectHashMap<>();
  private static final Map<String, Map<String, String>> DEFAULT_PROPERTIES_MAP = new HashMap<>();
  private static final Map<String, String> MODERN_ID_REMAP = new HashMap<>();

  @ToString.Include
  private final boolean solid;
  @ToString.Include
  private final boolean air;
  @ToString.Include
  private final boolean motionBlocking; // 1.14+
  private final String modernId;
  @ToString.Include
  private final short blockStateId;
  private final short blockId;

  public Block(boolean solid, boolean air, boolean motionBlocking, short blockStateId) {
    this(solid, air, motionBlocking, MODERN_BLOCK_STATE_PROTOCOL_ID_MAP.get(blockStateId), blockStateId);
  }

  public Block(boolean solid, boolean air, boolean motionBlocking, String modernId, short blockStateId) {
    this(solid, air, motionBlocking, modernId, blockStateId, findId(modernId));
  }

  private static short findId(String modernId) {
    String block = modernId.split("\\[")[0];
    Short id = MODERN_BLOCK_STRING_MAP.get(block);
    if (id == null) {
      throw new IllegalStateException("failed to find local id for specific block: " + block);
    }
    return id;
  }

  public Block(boolean solid, boolean air, boolean motionBlocking, String modernId, short blockStateId, short blockId) {
    this.solid = solid;
    this.air = air;
    this.motionBlocking = motionBlocking;
    this.modernId = modernId;
    this.blockStateId = blockStateId;
    this.blockId = blockId;
  }

  public Block(boolean solid, boolean air, boolean motionBlocking, String modernId, Map<String, String> properties) {
    this(solid, air, motionBlocking, modernId, transformId(modernId, properties));
  }

  public Block(boolean solid, boolean air, boolean motionBlocking, String modernId, Map<String, String> properties, short blockId) {
    this(solid, air, motionBlocking, modernId, transformId(modernId, properties), blockId);
  }

  public Block(Block block) {
    solid = block.solid;
    air = block.air;
    motionBlocking = block.motionBlocking;
    modernId = block.modernId;
    blockStateId = block.blockStateId;
    blockId = block.blockId;
  }

  @SuppressWarnings("unchecked")
  public static void init() {
    LinkedTreeMap<String, String> blocks = GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(Block.class.getClassLoader().getResourceAsStream("mapping/blocks.json")),
            StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    blocks.forEach((modernId, protocolId) -> MODERN_BLOCK_STRING_MAP.put(modernId, Short.valueOf(protocolId)));

    LinkedTreeMap<String, LinkedTreeMap<String, String>> blockVersionMapping = GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(Block.class.getClassLoader().getResourceAsStream("mapping/blocks_mapping.json")),
            StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    blockVersionMapping.forEach((protocolId, versionMap) -> {
      EnumMap<WorldVersion, Short> deserializedVersionMap = new EnumMap<>(WorldVersion.class);
      versionMap.forEach((version, id) -> deserializedVersionMap.put(WorldVersion.parse(version), Short.valueOf(id)));
      LEGACY_BLOCK_IDS_MAP.put(Short.valueOf(protocolId), deserializedVersionMap);
    });

    LinkedTreeMap<String, String> blockStates = GSON.fromJson(
        new InputStreamReader(Objects.requireNonNull(Block.class.getClassLoader().getResourceAsStream("mapping/blockstates.json")),
            StandardCharsets.UTF_8),
        LinkedTreeMap.class
    );
    blockStates.forEach((key, value) -> {
      MODERN_BLOCK_STATE_PROTOCOL_ID_MAP.put(Short.valueOf(value), key);

      String[] stringIdArgs = key.split("\\[");
      if (!MODERN_BLOCK_STATE_STRING_MAP.containsKey(stringIdArgs[0])) {
        MODERN_BLOCK_STATE_STRING_MAP.put(stringIdArgs[0], new HashMap<>());
      }

      if (stringIdArgs.length == 1) {
        MODERN_BLOCK_STATE_STRING_MAP.get(stringIdArgs[0]).put(null, Short.valueOf(value));
      } else {
        stringIdArgs[1] = stringIdArgs[1].substring(0, stringIdArgs[1].length() - 1);
        MODERN_BLOCK_STATE_STRING_MAP.get(stringIdArgs[0]).put(new HashSet<>(Arrays.asList(stringIdArgs[1].split(","))), Short.valueOf(value));
      }
    });

    LinkedTreeMap<String, LinkedTreeMap<String, String>> modernMap = GSON.fromJson(
        new InputStreamReader(Objects.requireNonNull(Block.class.getClassLoader().getResourceAsStream("mapping/blockstates_mapping.json")),
            StandardCharsets.UTF_8),
        LinkedTreeMap.class
    );

    modernMap.forEach((modernId, versionMap) -> {
      Short id = null;
      for (ProtocolVersion version : EnumSet.range(ProtocolVersion.MINECRAFT_1_16_4, ProtocolVersion.MAXIMUM_VERSION)) {
        id = Short.valueOf(versionMap.getOrDefault(version.toString(), String.valueOf(id)));
        Block.MODERN_BLOCK_STATE_IDS_MAP.computeIfAbsent(version, k -> new ShortObjectHashMap<>()).put(Short.parseShort(modernId), id);
      }
    });

    LinkedTreeMap<String, LinkedTreeMap<String, String>> properties = GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(Block.class.getClassLoader().getResourceAsStream("mapping/defaultblockproperties.json")),
            StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );
    properties.forEach((key, value) -> DEFAULT_PROPERTIES_MAP.put(key, new HashMap<>(value)));

    LinkedTreeMap<String, String> modernIdRemap = GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(Block.class.getClassLoader().getResourceAsStream("mapping/modern_block_id_remap.json")),
            StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );
    MODERN_ID_REMAP.putAll(modernIdRemap);
  }

  public static Block fromModernId(String modernId) {
    String[] deserializedModernId = modernId.split("[\\[\\]]");
    if (deserializedModernId.length < 2) {
      return fromModernId(modernId, Map.of());
    } else {
      Map<String, String> properties = new HashMap<>();
      for (String property : deserializedModernId[1].split(",")) {
        String[] propertyKeyValue = property.split("=");
        properties.put(propertyKeyValue[0], propertyKeyValue[1]);
      }

      return fromModernId(deserializedModernId[0], properties);
    }
  }

  public static Block fromModernId(String modernId, Map<String, String> properties) {
    modernId = remapModernId(modernId);
    return solid(modernId, transformId(modernId, properties));
  }

  private static short transformId(String modernId, Map<String, String> properties) {
    Map<String, String> defaultProperties = DEFAULT_PROPERTIES_MAP.get(modernId);
    if (defaultProperties == null || defaultProperties.isEmpty()) {
      return transformId(modernId, (Set<String>) null);
    } else {
      Set<String> propertiesSet = new HashSet<>();
      defaultProperties.forEach((key, value) -> {
        if (properties != null) {
          value = properties.getOrDefault(key, value);
        }

        propertiesSet.add(key + "=" + value.toLowerCase(Locale.ROOT));
      });
      return transformId(modernId, propertiesSet);
    }
  }

  private static short transformId(String modernId, Set<String> properties) {
    Map<Set<String>, Short> blockInfo = MODERN_BLOCK_STATE_STRING_MAP.get(modernId);

    if (blockInfo == null) {
      System.out.println("Block " + modernId + " is not supported, and was replaced with air.");
      return AIR.getModernId();
    }

    Short id;
    if (properties == null || properties.isEmpty()) {
      id = blockInfo.get(null);
    } else {
      id = blockInfo.get(properties);
    }

    if (id == null) {
      System.out.println("Block " + modernId + " is not supported with " + properties + " properties, and was replaced with air.");
      return AIR.getModernId();
    }

    return id;
  }

  private static String remapModernId(String modernId) {
    String strippedId = modernId.split("\\[")[0];
    String remappedId = MODERN_ID_REMAP.get(strippedId);
    if (remappedId != null) {
      modernId = remappedId + modernId.substring(strippedId.length());
    }

    return modernId;
  }

  @NonNull
  public static Block solid(short id) {
    return solid(true, MODERN_BLOCK_STATE_PROTOCOL_ID_MAP.get(id), id);
  }

  @NonNull
  public static Block solid(String modernId, short id) {
    return solid(true, remapModernId(modernId), id);
  }

  @NonNull
  public static Block solid(boolean motionBlocking, short id) {
    return new Block(true, false, motionBlocking, MODERN_BLOCK_STATE_PROTOCOL_ID_MAP.get(id), id);
  }

  @NonNull
  public static Block solid(boolean motionBlocking, String modernId, short id) {
    return new Block(true, false, motionBlocking, remapModernId(modernId), id);
  }

  @NonNull
  public static Block nonSolid(short id) {
    return nonSolid(true, MODERN_BLOCK_STATE_PROTOCOL_ID_MAP.get(id), id);
  }

  @NonNull
  public static Block nonSolid(String modernId, short id) {
    return nonSolid(true, remapModernId(modernId), id);
  }

  @NonNull
  public static Block nonSolid(boolean motionBlocking, short id) {
    return new Block(false, false, motionBlocking, MODERN_BLOCK_STATE_PROTOCOL_ID_MAP.get(id), id);
  }

  @NonNull
  public static Block nonSolid(boolean motionBlocking, String modernId, short id) {
    return new Block(false, false, motionBlocking, remapModernId(modernId), id);
  }

  public short getModernId() {
    return blockStateId;
  }

  public String getModernStringId() {
    return modernId;
  }

  public short getId(ProtocolVersion version) {
    return getBlockStateId(version);
  }

  public short getBlockId(WorldVersion version) {
    return LEGACY_BLOCK_IDS_MAP.get(blockId).get(version);
  }

  public short getBlockId(ProtocolVersion version) {
    return getBlockId(WorldVersion.from(version));
  }

  public boolean isSupportedOn(WorldVersion version) {
    return LEGACY_BLOCK_IDS_MAP.get(blockId).containsKey(version);
  }

  public boolean isSupportedOn(ProtocolVersion version) {
    return isSupportedOn(WorldVersion.from(version));
  }

  public short getBlockStateId(ProtocolVersion version) {
    return MODERN_BLOCK_STATE_IDS_MAP.get(version).getOrDefault(blockStateId, blockStateId);
  }

}
