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

package com.velocitypowered.proxy.protocol.data.chunk;

import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.chunk.section.BlockSection;
import com.velocitypowered.proxy.protocol.data.chunk.section.LightSection;
import com.velocitypowered.proxy.protocol.data.tile.TileEntity;
import com.velocitypowered.proxy.protocol.data.world.Biome;

import java.util.List;

public record ChunkSnapshot(int posX, int posZ,
                            boolean fullChunk,
                            BlockSection[] sections,
                            LightSection[] light,
                            Biome[] biomes,
                            List<TileEntity.Entry> tileEntityEntries) {

  public Block getBlock(int posX, int posY, int posZ) {
    BlockSection section = sections[posY >> 4];
    return section == null ? Block.AIR : section.getBlockAt(posX, posY & 15, posZ);
  }

}
