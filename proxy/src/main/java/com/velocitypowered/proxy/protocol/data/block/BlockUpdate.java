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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
public final class BlockUpdate {
  private final BlockPosition position;
  private final BlockType blockType;
  private final int legacyBlockState, blockState;

  public BlockUpdate(@NotNull BlockPosition position, BlockType blockType) {
    this.position = position;
    this.blockType = blockType;
    int x = position.getX() - (position.getChunkX() << 4);
    int y = position.getY();
    int z = position.getZ() - (position.getChunkZ() << 4);
    this.legacyBlockState = x << 12 | z << 8 | y;
    this.blockState = x << 8 | z << 4 | y - ((y >> 4) << 4);
  }

  @Getter
  @RequiredArgsConstructor
  public static final class BlockPosition {
    private final int x, y, z;
    private final int chunkX, chunkZ;
  }
}
