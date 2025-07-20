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

package com.velocitypowered.proxy.util;

import java.util.HashMap;
import java.util.Map;

public class EnumUniverse {

  public static <T extends Enum<T>> Map<String, T> createProtocolLookup(T[] values) {
    Map<String, T> lookup = new HashMap<>();
    for (T value : values) {
      if (value.name().startsWith("MINECRAFT_")) {
        lookup.put(value.name().substring("MINECRAFT_".length()).replace("_", "."), value);
      }
    }
    return lookup;
  }
}
