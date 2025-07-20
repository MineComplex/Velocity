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
import com.velocitypowered.proxy.protocol.data.chunk.Chunk;
import com.velocitypowered.proxy.protocol.data.storage.NibbleArray3D;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LightSection {

  private static final NibbleArray3D NO_LIGHT = new NibbleArray3D(Chunk.MAX_BLOCKS_PER_SECTION);
  private static final NibbleArray3D ALL_LIGHT = new NibbleArray3D(Chunk.MAX_BLOCKS_PER_SECTION, 15);

  private NibbleArray3D blockLight;
  private NibbleArray3D skyLight;
  private long lastUpdate;

  public LightSection() {
    this(NO_LIGHT, ALL_LIGHT, System.nanoTime());
  }

  public void setBlockLight(int posX, int posY, int posZ, byte light) {
    checkIndexes(posX, posY, posZ);
    Preconditions.checkArgument(light >= 0 && light <= 15, "light should be between 0 and 15");

    if (blockLight == NO_LIGHT && light != 0) {
      blockLight = new NibbleArray3D(Chunk.MAX_BLOCKS_PER_SECTION);
    }

    blockLight.set(posX, posY, posZ, light);
    lastUpdate = System.nanoTime();
  }

  public byte getBlockLight(int posX, int posY, int posZ) {
    checkIndexes(posX, posY, posZ);
    return (byte) blockLight.get(posX, posY, posZ);
  }

  public void setSkyLight(int posX, int posY, int posZ, byte light) {
    checkIndexes(posX, posY, posZ);
    Preconditions.checkArgument(light >= 0 && light <= 15, "light should be between 0 and 15");

    if (skyLight == ALL_LIGHT && light != 15) {
      skyLight = new NibbleArray3D(Chunk.MAX_BLOCKS_PER_SECTION);
    }

    skyLight.set(posX, posY, posZ, light);
    lastUpdate = System.nanoTime();
  }

  public byte getSkyLight(int posX, int posY, int posZ) {
    checkIndexes(posX, posY, posZ);
    return (byte) skyLight.get(posX, posY, posZ);
  }

  private void checkIndexes(int posX, int posY, int posZ) {
    Preconditions.checkArgument(checkIndex(posX), "x should be between 0 and 15");
    Preconditions.checkArgument(checkIndex(posY), "y should be between 0 and 15");
    Preconditions.checkArgument(checkIndex(posZ), "z should be between 0 and 15");
  }

  private boolean checkIndex(int pos) {
    return pos >= 0 && pos <= 15;
  }

  public LightSection copy() {
    NibbleArray3D skyLight = this.skyLight == ALL_LIGHT ? ALL_LIGHT : this.skyLight.copy();
    NibbleArray3D blockLight = this.blockLight == NO_LIGHT ? NO_LIGHT : this.blockLight.copy();
    return new LightSection(blockLight, skyLight, lastUpdate);
  }
}
