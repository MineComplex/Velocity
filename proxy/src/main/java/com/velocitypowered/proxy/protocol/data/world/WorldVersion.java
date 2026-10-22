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
import org.checkerframework.checker.nullness.qual.Nullable;

@AllArgsConstructor
public enum WorldVersion {

  MINECRAFT_26_3(EnumSet.range(ProtocolVersion.MINIMUM_VERSION, ProtocolVersion.MAXIMUM_VERSION));

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

  /**
   * Returns the version for a key from the mapping files, or {@code null} for versions that
   * are no longer supported.
   */
  public static @Nullable WorldVersion parse(String from) {
    return KEY_LOOKUP.get(from);
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
