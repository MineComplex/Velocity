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

package com.velocitypowered.proxy.protocol.data.storage;

import com.velocitypowered.api.network.ProtocolVersion;

public interface CompactStorage {

  static int fixBitsPerEntry(ProtocolVersion version, int bitsPerEntry) {
    if (bitsPerEntry < 4) {
      return 4;
    } else if (bitsPerEntry < 9) {
      return bitsPerEntry;
    } else if (version.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) {
      return 13;
    } else if (version.compareTo(ProtocolVersion.MINECRAFT_1_16_4) < 0) {
      return 14;
    } else {
      return 15;
    }
  }

  void set(int index, int value);

  int get(int index);

  void write(Object byteBufObject, ProtocolVersion version);

  int getBitsPerEntry();

  int getDataLength();

  long[] getData();

  CompactStorage copy();
}
