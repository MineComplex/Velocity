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

package com.velocitypowered.proxy.protocol.data.world;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.util.EnumUniverse;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public enum WorldVersion {
  /*
  MINECRAFT_1_16_4(EnumSet.of(ProtocolVersion.MINECRAFT_1_16_4)),
  MINECRAFT_1_17(EnumSet.range(ProtocolVersion.MINECRAFT_1_17, ProtocolVersion.MINECRAFT_1_18_2)),
  MINECRAFT_1_19(EnumSet.range(ProtocolVersion.MINECRAFT_1_19, ProtocolVersion.MINECRAFT_1_19_1)),
  MINECRAFT_1_19_3(ProtocolVersion.MINECRAFT_1_19_3),
  MINECRAFT_1_19_4(EnumSet.range(ProtocolVersion.MINECRAFT_1_19_4, ProtocolVersion.MINECRAFT_1_20)),
  MINECRAFT_1_20(EnumSet.range(ProtocolVersion.MINECRAFT_1_20, ProtocolVersion.MINECRAFT_1_20_2)),
  MINECRAFT_1_20_3(ProtocolVersion.MINECRAFT_1_20_3),
  MINECRAFT_1_20_5(EnumSet.range(ProtocolVersion.MINECRAFT_1_20_5, ProtocolVersion.MINECRAFT_1_21)),
  MINECRAFT_1_21_2(ProtocolVersion.MINECRAFT_1_21_2),
  */
  MINECRAFT_1_21_4(ProtocolVersion.MINECRAFT_1_21_4),
  MINECRAFT_1_21_5(ProtocolVersion.MINECRAFT_1_21_5),
  MINECRAFT_1_21_6(ProtocolVersion.MINECRAFT_1_21_6),
  MINECRAFT_1_21_7(ProtocolVersion.MINECRAFT_1_21_7),
  MINECRAFT_1_21_9(ProtocolVersion.MINECRAFT_1_21_9),
  MINECRAFT_1_21_11(ProtocolVersion.MINECRAFT_1_21_11),
  MINECRAFT_26_1(EnumSet.range(ProtocolVersion.MINECRAFT_26_1, ProtocolVersion.MAXIMUM_VERSION));

  private static final EnumMap<ProtocolVersion, WorldVersion> MC_VERSION_TO_ITEM_VERSIONS = new EnumMap<>(ProtocolVersion.class);
  private static final Map<String, WorldVersion> KEY_LOOKUP = Map.copyOf(EnumUniverse.createProtocolLookup(values()));

  static {
    for (WorldVersion version : WorldVersion.values()) {
      for (ProtocolVersion protocolVersion : version.getVersions()) {
        MC_VERSION_TO_ITEM_VERSIONS.put(protocolVersion, version);
      }
    }
  }

  private final Set<ProtocolVersion> versions;

  WorldVersion(ProtocolVersion... versions) {
    this.versions = EnumSet.copyOf(Arrays.asList(versions));
  }

  public static WorldVersion parse(String from) {
    //return KEY_LOOKUP.getOrDefault(from, MINECRAFT_1_16_4);
    return KEY_LOOKUP.getOrDefault(from, MINECRAFT_1_21_4);
  }

  public static WorldVersion from(ProtocolVersion protocolVersion) {
    return MC_VERSION_TO_ITEM_VERSIONS.get(protocolVersion);
  }

  public ProtocolVersion getMinSupportedVersion() {
    return this.versions.iterator().next();
  }

  public Set<ProtocolVersion> getVersions() {
    return this.versions;
  }
}
