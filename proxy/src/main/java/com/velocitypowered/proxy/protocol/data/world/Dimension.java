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

import com.google.common.collect.ImmutableSet;
import lombok.Getter;

@Getter
public enum Dimension {

  OVERWORLD("minecraft:overworld", 0, 0, 28, true, Biome.PLAINS), // (384 + 64) / 16
  NETHER("minecraft:the_nether", -1, 1, 16, false, Biome.NETHER_WASTES), // 256 / 16
  THE_END("minecraft:the_end", 1, 2, 16, false, Biome.THE_END); // 256 / 16

  public static final ImmutableSet<String> LEVELS = ImmutableSet.of(
      Dimension.OVERWORLD.getKey(),
      Dimension.NETHER.getKey(),
      Dimension.THE_END.getKey()
  );

  private final String key;
  private final int legacyId;
  private final int modernId;
  private final int maxSections;
  private final boolean hasLegacySkyLight;
  private final Biome defaultBiome;

  Dimension(String key, int legacyId, int modernId, int maxSections, boolean hasLegacySkyLight, Biome defaultBiome) {
    this.key = key;
    this.legacyId = legacyId;
    this.modernId = modernId;
    this.maxSections = maxSections;
    this.hasLegacySkyLight = hasLegacySkyLight;
    this.defaultBiome = defaultBiome;
  }

  public boolean hasLegacySkyLight() {
    return this.hasLegacySkyLight;
  }

}

