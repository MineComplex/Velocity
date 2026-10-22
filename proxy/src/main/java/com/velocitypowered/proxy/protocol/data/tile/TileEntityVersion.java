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
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum TileEntityVersion {
  MINECRAFT_26_3;

  private static final Map<String, TileEntityVersion> KEY_LOOKUP = Map.copyOf(EnumUniverse.createProtocolLookup(values()));

  public static @Nullable TileEntityVersion parse(String key) {
    return KEY_LOOKUP.get(key);
  }

  public static @Nullable TileEntityVersion from(ProtocolVersion version) {
    return version == ProtocolVersion.MINECRAFT_26_3 ? MINECRAFT_26_3 : null;
  }
}
