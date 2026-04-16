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

package com.velocitypowered.proxy.protocol.data.chat;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@UtilityClass
public class ChatTagManager {

  public final CompoundBinaryTag CHAT_TYPE_1191;
  public final CompoundBinaryTag DAMAGE_TYPE_120;

  static {
    try {
      try (InputStream stream = ChatTagManager.class.getClassLoader().getResourceAsStream("mapping/chat_type_1_19_1.nbt")) {
        CHAT_TYPE_1191 = BinaryTagIO.unlimitedReader().read(Objects.requireNonNull(stream), BinaryTagIO.Compression.GZIP);
      }
      try (InputStream stream = ChatTagManager.class.getClassLoader().getResourceAsStream("mapping/damage_type_1_20.nbt")) {
        DAMAGE_TYPE_120 = BinaryTagIO.unlimitedReader().read(Objects.requireNonNull(stream), BinaryTagIO.Compression.GZIP);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
