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

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.block.BlockTypeStorage;
import com.velocitypowered.proxy.protocol.data.chunk.Chunk;
import com.velocitypowered.proxy.protocol.data.storage.BiomeStorage;
import com.velocitypowered.proxy.protocol.data.storage.BlockStorage;
import com.velocitypowered.proxy.protocol.data.world.Biome;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor
public class NetworkSection {

  private final Map<ProtocolVersion, BlockTypeStorage> storages = new EnumMap<>(ProtocolVersion.class);
  private final Map<ProtocolVersion, BiomeStorage> biomeStorages = new EnumMap<>(ProtocolVersion.class);
  private final int index;
  private final BlockSection section;
  private final Biome[] biomes;

  private int blockCount = -1;
  private int fluidCount = -1;

  public int getDataLength(ProtocolVersion version) {
    int dataLength = ensureStorageCreated(version).getDataLength(version) + 2;
    if (version.compareTo(ProtocolVersion.MINECRAFT_26_1) >= 0) {
      dataLength += 2; // Fluid count short.
    }
    if (version.greaterThan(ProtocolVersion.MINECRAFT_1_17_1)) {
      dataLength += ensure118BiomeCreated(version).getDataLength(version);
    }
    return dataLength;
  }

  public void writeData(ByteBuf buf, int pass, ProtocolVersion version) {
    if (pass == 0) {
      BlockTypeStorage storage = ensureStorageCreated(version);
      write114Data(buf, storage, version, pass);

      if (version.greaterThan(ProtocolVersion.MINECRAFT_1_17_1)) {
        write118Biomes(buf, version);
      }
    }
  }

  private BlockTypeStorage ensureStorageCreated(ProtocolVersion version) {
    BlockTypeStorage storage = storages.get(version);
    if (storage == null) {
      synchronized (storages) {
        BlockTypeStorage blockStorage = new BlockStorage(version);
        fillBlocks(blockStorage);
        storages.put(version, blockStorage);
        storage = blockStorage;
      }
    }

    return storage;
  }

  private void write114Data(ByteBuf buf, BlockTypeStorage storage, ProtocolVersion version, int pass) {
    buf.writeShort(blockCount);
    if (version.noLessThan(ProtocolVersion.MINECRAFT_26_1)) {
      buf.writeShort(fluidCount);
    }
    storage.write(buf, version, pass);
  }

  private void write118Biomes(ByteBuf buf, ProtocolVersion version) {
    ensure118BiomeCreated(version).write(buf, version);
  }

  private BiomeStorage ensure118BiomeCreated(ProtocolVersion version) {
    BiomeStorage storage = biomeStorages.get(version);
    if (storage == null) {
      synchronized (biomeStorages) {
        storage = new BiomeStorage(version);
        int offset = index * Chunk.MAX_BIOMES_PER_SECTION;
        for (int biomeIndex = 0, biomeArrayIndex = offset; biomeIndex < Chunk.MAX_BIOMES_PER_SECTION; ++biomeIndex, ++biomeArrayIndex) {
          storage.set(biomeIndex, biomes[biomeArrayIndex]);
        }

        biomeStorages.put(version, storage);
      }
    }

    return storage;
  }

  private void fillBlocks(BlockTypeStorage storage) {
    int blockCount = 0;
    for (int posX = 0; posX < 16; ++posX) {
      for (int posY = 0; posY < 16; ++posY) {
        for (int posZ = 0; posZ < 16; ++posZ) {
          Block block = section.getBlockAt(posX, posY, posZ);
          if (!block.isAir()) {
            ++blockCount;
            storage.set(posX, posY, posZ, block);
          }
        }
      }
    }

    if (blockCount == -1) {
      this.blockCount = blockCount;

      // TODO: properly set fluidCount, as of 26.1 it is used only to guess about fluid in chunks.
      this.fluidCount = blockCount;
    }
  }
}
