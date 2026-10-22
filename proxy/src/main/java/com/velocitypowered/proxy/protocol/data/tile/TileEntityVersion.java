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

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@AllArgsConstructor
public enum TileEntityVersion {

  MINECRAFT_26_3(EnumSet.range(ProtocolVersion.MINIMUM_VERSION, ProtocolVersion.MAXIMUM_VERSION));

  private static final EnumMap<ProtocolVersion, TileEntityVersion> MC_VERSION_TO_ITEM_VERSIONS = new EnumMap<>(ProtocolVersion.class);
  private static final Map<String, TileEntityVersion> KEY_LOOKUP = Map.copyOf(EnumUniverse.createProtocolLookup(values()));

  static {
    for (TileEntityVersion version : TileEntityVersion.values()) {
      for (ProtocolVersion protocolVersion : version.getVersions()) {
        MC_VERSION_TO_ITEM_VERSIONS.put(protocolVersion, version);
      }
    }
  }

  TileEntityVersion(ProtocolVersion... versions) {
    this.versions = EnumSet.copyOf(Arrays.asList(versions));
  }

  private final Set<ProtocolVersion> versions;

  /**
   * Returns the version for a key from the mapping files, or {@code null} for versions that
   * are no longer supported.
   */
  public static @Nullable TileEntityVersion parse(String from) {
    return KEY_LOOKUP.get(from);
  }

  public static TileEntityVersion from(ProtocolVersion protocolVersion) {
    return MC_VERSION_TO_ITEM_VERSIONS.get(protocolVersion);
  }

  public ProtocolVersion getMinSupportedVersion() {
    return versions.iterator().next();
  }

}
