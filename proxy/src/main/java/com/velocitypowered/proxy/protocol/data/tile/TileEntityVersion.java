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

package com.velocitypowered.proxy.protocol.data.tile;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.util.EnumUniverse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum TileEntityVersion {
  /*
  LEGACY(EnumSet.range(ProtocolVersion.MINECRAFT_1_16_4, ProtocolVersion.MINECRAFT_1_18_2)),
  MINECRAFT_1_19(EnumSet.of(ProtocolVersion.MINECRAFT_1_19)),
  MINECRAFT_1_19_1(EnumSet.of(ProtocolVersion.MINECRAFT_1_19_1)),
  MINECRAFT_1_19_3(EnumSet.of(ProtocolVersion.MINECRAFT_1_19_3)),
  MINECRAFT_1_19_4(EnumSet.of(ProtocolVersion.MINECRAFT_1_19_4)),
  MINECRAFT_1_20(EnumSet.of(ProtocolVersion.MINECRAFT_1_20)),
  MINECRAFT_1_20_2(EnumSet.of(ProtocolVersion.MINECRAFT_1_20_2)),
  MINECRAFT_1_20_3(EnumSet.of(ProtocolVersion.MINECRAFT_1_20_3)),
  MINECRAFT_1_20_5(EnumSet.of(ProtocolVersion.MINECRAFT_1_20_5)),
  MINECRAFT_1_21(EnumSet.of(ProtocolVersion.MINECRAFT_1_21)),
  MINECRAFT_1_21_2(EnumSet.of(ProtocolVersion.MINECRAFT_1_21_2)),
  */
  MINECRAFT_1_21_4(EnumSet.of(ProtocolVersion.MINECRAFT_1_21_4)),
  MINECRAFT_1_21_5(EnumSet.of(ProtocolVersion.MINECRAFT_1_21_5)),
  MINECRAFT_1_21_6(EnumSet.of(ProtocolVersion.MINECRAFT_1_21_6)),
  MINECRAFT_1_21_7(EnumSet.of(ProtocolVersion.MINECRAFT_1_21_7)),
  MINECRAFT_1_21_9(EnumSet.of(ProtocolVersion.MINECRAFT_1_21_9)),
  MINECRAFT_1_21_11(EnumSet.of(ProtocolVersion.MINECRAFT_1_21_11)),
  MINECRAFT_26_1(EnumSet.of(ProtocolVersion.MINECRAFT_26_1));

  private static final EnumMap<ProtocolVersion, TileEntityVersion> MC_VERSION_TO_ITEM_VERSIONS = new EnumMap<>(ProtocolVersion.class);
  private static final Map<String, TileEntityVersion> KEY_LOOKUP = Map.copyOf(EnumUniverse.createProtocolLookup(values()));

  static {
    for (TileEntityVersion version : TileEntityVersion.values()) {
      for (ProtocolVersion protocolVersion : version.getVersions()) {
        MC_VERSION_TO_ITEM_VERSIONS.put(protocolVersion, version);
      }
    }
  }

  private final Set<ProtocolVersion> versions;

  public static TileEntityVersion parse(String from) {
    //return KEY_LOOKUP.getOrDefault(from, LEGACY);
    return KEY_LOOKUP.getOrDefault(from, MINECRAFT_1_21_4);
  }

  public static TileEntityVersion from(ProtocolVersion protocolVersion) {
    return MC_VERSION_TO_ITEM_VERSIONS.get(protocolVersion);
  }

  public ProtocolVersion getMinSupportedVersion() {
    return versions.iterator().next();
  }

}
