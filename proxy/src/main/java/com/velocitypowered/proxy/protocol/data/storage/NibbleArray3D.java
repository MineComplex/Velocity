/*
 * Copyright (C) 2013-2021 Velocity Contributors
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

package com.velocitypowered.proxy.protocol.data.storage;

import com.velocitypowered.proxy.protocol.data.block.BlockStorage;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class NibbleArray3D {

  private final byte[] data;

  public NibbleArray3D(int size) {
    data = new byte[size >> 1];
  }

  public NibbleArray3D(int size, int defaultValue) {
    data = new byte[size >> 1];
    fill(defaultValue);
  }

  public NibbleArray3D(byte[] array) {
    data = array;
  }

  public int get(int posX, int posY, int posZ) {
    int key = BlockStorage.index(posX, posY, posZ);
    int index = key >> 1;
    return (key & 1) == 0 ? data[index] & 15 : data[index] >> 4 & 15;
  }

  public void set(int posX, int posY, int posZ, int value) {
    set(BlockStorage.index(posX, posY, posZ), value);
  }

  public void set(int key, int val) {
    int index = key >> 1;
    if ((key & 1) == 0) {
      data[index] = (byte) (data[index] & 240 | val & 15);
    } else {
      data[index] = (byte) (data[index] & 15 | (val & 15) << 4);
    }
  }

  public void fill(int value) {
    for (int index = 0; index < data.length << 1; ++index) {
      set(index, value);
    }
  }

  public NibbleArray3D copy() {
    return new NibbleArray3D(Arrays.copyOf(data, data.length));
  }
}
