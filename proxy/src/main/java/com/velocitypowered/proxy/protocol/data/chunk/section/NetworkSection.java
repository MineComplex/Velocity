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
import com.velocitypowered.proxy.protocol.data.block.BlockStorage;
import com.velocitypowered.proxy.protocol.data.chunk.Chunk;
import com.velocitypowered.proxy.protocol.data.storage.BiomeStorage118;
import com.velocitypowered.proxy.protocol.data.storage.BlockStorage17;
import com.velocitypowered.proxy.protocol.data.storage.BlockStorage19;
import com.velocitypowered.proxy.protocol.data.storage.NibbleArray3D;
import com.velocitypowered.proxy.protocol.data.world.Biome;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor
public class NetworkSection {

  private final Map<ProtocolVersion, BlockStorage> storages = new EnumMap<>(ProtocolVersion.class);
  private final Map<ProtocolVersion, BiomeStorage118> biomeStorages = new EnumMap<>(ProtocolVersion.class);
  private final int index;
  private final BlockSection section;
  private final NibbleArray3D blockLight;
  private final NibbleArray3D skyLight;
  private final Biome[] biomes;

  private int blockCount = -1;

  public int getDataLength(ProtocolVersion version) {
    int dataLength = ensureStorageCreated(version).getDataLength(version);
    if (version.compareTo(ProtocolVersion.MINECRAFT_1_14) < 0) {
      dataLength += blockLight.getData().length;
      if (skyLight != null) {
        dataLength += skyLight.getData().length;
      }
    }
    if (version.compareTo(ProtocolVersion.MINECRAFT_1_14) >= 0) {
      dataLength += 2; // Block count short.
    }
    if (version.compareTo(ProtocolVersion.MINECRAFT_1_17_1) > 0) {
      dataLength += ensure118BiomeCreated(version).getDataLength();
    }

    return dataLength;
  }

  public void writeData(ByteBuf buf, int pass, ProtocolVersion version) {
    if (version.compareTo(ProtocolVersion.MINECRAFT_1_9) < 0) {
      BlockStorage storage = ensureStorageCreated(version);
      write17Data(buf, storage, version, pass);
    } else if (pass == 0) {
      BlockStorage storage = ensureStorageCreated(version);
      if (version.compareTo(ProtocolVersion.MINECRAFT_1_14) < 0) {
        write19Data(buf, storage, version, pass);
      } else {
        write114Data(buf, storage, version, pass);

        if (version.compareTo(ProtocolVersion.MINECRAFT_1_17_1) > 0) {
          write118Biomes(buf, version);
        }
      }
    }
  }

  private BlockStorage ensureStorageCreated(ProtocolVersion version) {
    BlockStorage storage = storages.get(version);
    if (storage == null) {
      synchronized (storages) {
        BlockStorage blockStorage = createStorage(version);
        fillBlocks(blockStorage);
        storages.put(version, blockStorage);
        storage = blockStorage;
      }
    }

    return storage;
  }

  private BlockStorage createStorage(ProtocolVersion version) {
    if (version.compareTo(ProtocolVersion.MINECRAFT_1_9) < 0) {
      return new BlockStorage17();
    } else {
      return new BlockStorage19(version);
    }
  }

  private void write17Data(ByteBuf buf, BlockStorage storage, ProtocolVersion version, int pass) {
    if (pass == 0 || pass == 1) {
      storage.write(buf, version, pass);
    } else if (pass == 2) {
      buf.writeBytes(blockLight.getData());
    } else if (pass == 3 && skyLight != null) {
      buf.writeBytes(skyLight.getData());
    }
  }

  private void write19Data(ByteBuf buf, BlockStorage storage, ProtocolVersion version, int pass) {
    storage.write(buf, version, pass);
    buf.writeBytes(blockLight.getData());
    if (skyLight != null) {
      buf.writeBytes(skyLight.getData());
    }
  }

  private void write114Data(ByteBuf buf, BlockStorage storage, ProtocolVersion version, int pass) {
    buf.writeShort(blockCount);
    storage.write(buf, version, pass);
  }

  private void write118Biomes(ByteBuf buf, ProtocolVersion version) {
    ensure118BiomeCreated(version).write(buf, version);
  }

  private BiomeStorage118 ensure118BiomeCreated(ProtocolVersion version) {
    BiomeStorage118 storage = biomeStorages.get(version);
    if (storage == null) {
      synchronized (biomeStorages) {
        storage = new BiomeStorage118(version);
        int offset = index * Chunk.MAX_BIOMES_PER_SECTION;
        for (int biomeIndex = 0, biomeArrayIndex = offset; biomeIndex < Chunk.MAX_BIOMES_PER_SECTION; ++biomeIndex, ++biomeArrayIndex) {
          storage.set(biomeIndex, biomes[biomeArrayIndex]);
        }

        biomeStorages.put(version, storage);
      }
    }

    return storage;
  }

  private void fillBlocks(BlockStorage storage) {
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
    }
  }
}
