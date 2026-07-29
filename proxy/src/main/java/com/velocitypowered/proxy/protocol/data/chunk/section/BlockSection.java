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

package com.velocitypowered.proxy.protocol.data.chunk.section;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.block.BlockTypeStorage;
import com.velocitypowered.proxy.protocol.data.storage.BlockStorage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
@AllArgsConstructor
public class BlockSection {

  private final BlockTypeStorage blocks;

  @Getter
  private long lastUpdate = System.nanoTime();

  public BlockSection() {
    this(new BlockStorage(ProtocolVersion.MINECRAFT_1_21_4));
  }

  public void setBlockAt(int posX, int posY, int posZ, @Nullable Block block) {
    this.checkIndexes(posX, posY, posZ);
    blocks.set(posX, posY, posZ, block == null ? Block.AIR : block);
    lastUpdate = System.nanoTime();
  }

  public Block getBlockAt(int posX, int posY, int posZ) {
    checkIndexes(posX, posY, posZ);
    return blocks.get(posX, posY, posZ);
  }

  private void checkIndexes(int posX, int posY, int posZ) {
    Preconditions.checkArgument(checkIndex(posX), "x should be between 0 and 15");
    Preconditions.checkArgument(checkIndex(posY), "y should be between 0 and 15");
    Preconditions.checkArgument(checkIndex(posZ), "z should be between 0 and 15");
  }

  private boolean checkIndex(int pos) {
    return pos >= 0 && pos <= 15;
  }

  public BlockSection getSnapshot() {
    return new BlockSection(blocks.copy(), lastUpdate);
  }

}
