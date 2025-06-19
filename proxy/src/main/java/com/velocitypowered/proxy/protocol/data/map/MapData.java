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

package com.velocitypowered.proxy.protocol.data.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * For MapData packet.
 */
@ToString
@Getter
@AllArgsConstructor
public class MapData {

  public static final int MAP_DIM_SIZE = 128;
  public static final int MAP_SIZE = MAP_DIM_SIZE * MAP_DIM_SIZE; // 128² == 16384

  private final int columns;
  private final int rows;
  private final int x;
  private final int y;
  private final byte[] data;

  public MapData(byte[] data) {
    this(0, data);
  }

  public MapData(int posX, byte[] data) {
    this(MAP_DIM_SIZE, MAP_DIM_SIZE, posX, 0, data);
  }

}
