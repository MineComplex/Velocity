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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.block.BlockTypeStorage;
import com.velocitypowered.proxy.protocol.data.chunk.Chunk;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockStorage implements BlockTypeStorage {

  private final ProtocolVersion version;
  private final List<Block> palette;
  private final Map<Short, Block> rawToBlock;
  private BitStorage storage;

  public BlockStorage(ProtocolVersion version) {
    this.version = version;
    palette = Lists.newArrayList();
    rawToBlock = Maps.newHashMap();
    palette.add(Block.AIR);
    rawToBlock.put(Block.AIR.getBlockStateId(version), Block.AIR);
    storage = createStorage(4);
  }

  @Override
  public void write(Object byteBufObject, ProtocolVersion version, int pass) {
    Preconditions.checkArgument(byteBufObject instanceof ByteBuf);
    ByteBuf buf = (ByteBuf) byteBufObject;
    buf.writeByte(storage.getBitsPerEntry());
    if (storage.getBitsPerEntry() <= 8) {
      ProtocolUtils.writeVarInt(buf, palette.size());
      for (Block state : palette) {
        ProtocolUtils.writeVarInt(buf, state.getBlockStateId(this.version));
      }
    }

    storage.write(buf, version);
  }

  @Override
  public void set(int posX, int posY, int posZ, @NonNull Block block) {
    int id = getIndex(block);
    storage.set(BlockTypeStorage.index(posX, posY, posZ), id);
  }

  private int getIndex(Block block) {
    if (storage.getBitsPerEntry() > 8) {
      short raw = block.getBlockStateId(version);
      rawToBlock.put(raw, block);
      return raw;
    } else {
      int id = palette.indexOf(block);
      if (id == -1) {
        if (palette.size() >= (1 << storage.getBitsPerEntry())) {
          int bitsPerEntry = BitStorage.fixBitsPerEntry(version, storage.getBitsPerEntry() + 1);
          BitStorage newStorage = createStorage(bitsPerEntry);
          for (int i = 0; i < Chunk.MAX_BLOCKS_PER_SECTION; ++i) {
            newStorage.set(i, bitsPerEntry > 8 ? palette.get(storage.get(i)).getBlockStateId(version) : storage.get(i));
          }

          storage = newStorage;

          return getIndex(block);
        }

        palette.add(block);
        id = palette.size() - 1;
      }

      return id;
    }
  }

  private BitStorage createStorage(int bitsPerEntry) {
    return new BitStorage(bitsPerEntry, Chunk.MAX_BLOCKS_PER_SECTION);
  }

  @NonNull
  @Override
  public Block get(int posX, int posY, int posZ) {
    int id = storage.get(BlockTypeStorage.index(posX, posY, posZ));
    if (storage.getBitsPerEntry() > 8) {
      return rawToBlock.get((short) id);
    } else {
      return palette.get(id);
    }
  }

  @Override
  public int getDataLength(ProtocolVersion version) {
    int length = 1;
    if (storage.getBitsPerEntry() <= 8) {
      length += ProtocolUtils.varIntBytes(palette.size());
      for (Block state : palette) {
        length += ProtocolUtils.varIntBytes(state.getBlockStateId(this.version));
      }
    }

    return length + storage.getDataLength(version);
  }

  @Override
  public BlockTypeStorage copy() {
    return new BlockStorage(version, new ArrayList<>(palette), new HashMap<>(rawToBlock), storage.copy());
  }

}
