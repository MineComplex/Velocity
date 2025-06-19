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

package com.velocitypowered.proxy.protocol.data.block;

import com.velocitypowered.api.network.ProtocolVersion;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface BlockStorage {

  static int index(int posX, int posY, int posZ) {
    return posY << 8 | posZ << 4 | posX;
  }

  void write(Object byteBufObject, ProtocolVersion version, int pass);

  void set(int posX, int posY, int posZ, @NonNull Block block);

  @NonNull
  Block get(int posX, int posY, int posZ);

  int getDataLength(ProtocolVersion version);

  BlockStorage copy();
}
