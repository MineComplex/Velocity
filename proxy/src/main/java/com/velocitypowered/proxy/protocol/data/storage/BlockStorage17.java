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
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.block.BlockStorage;
import com.velocitypowered.proxy.protocol.data.chunk.Chunk;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;

@ToString
@AllArgsConstructor
public class BlockStorage17 implements BlockStorage {

  private final Block[] blocks;

  public BlockStorage17() {
    this(new Block[Chunk.MAX_BLOCKS_PER_SECTION]);
  }

  @Override
  public void write(Object byteBufObject, ProtocolVersion version, int pass) {
    Preconditions.checkArgument(byteBufObject instanceof ByteBuf);
    ByteBuf buf = (ByteBuf) byteBufObject;
    if (pass == 0) {
      if (version.compareTo(ProtocolVersion.MINECRAFT_1_8) < 0) {
        byte[] raw = new byte[blocks.length];
        for (int i = 0; i < blocks.length; ++i) {
          Block block = blocks[i];
          raw[i] = (byte) (block == null ? 0 : block.getBlockStateId(ProtocolVersion.MINECRAFT_1_7_2) >> 4);
        }

        buf.writeBytes(raw);
      } else {
        short[] raw = new short[blocks.length];
        for (int i = 0; i < blocks.length; ++i) {
          Block block = blocks[i];
          raw[i] = (short) (block == null ? 0 : block.getBlockStateId(ProtocolVersion.MINECRAFT_1_8));
        }

        for (short s : raw) {
          buf.writeShortLE(s);
        }
      }
    } else if (pass == 1 && version.compareTo(ProtocolVersion.MINECRAFT_1_8) < 0) {
      NibbleArray3D metadata = new NibbleArray3D(Chunk.MAX_BLOCKS_PER_SECTION);
      for (int i = 0; i < blocks.length; ++i) {
        Block block = blocks[i];
        metadata.set(i, block == null ? 0 : block.getBlockStateId(ProtocolVersion.MINECRAFT_1_7_2) & 0xFFFF);
      }

      buf.writeBytes(metadata.getData());
    }
  }

  @Override
  public void set(int posX, int posY, int posZ, @NonNull Block block) {
    blocks[BlockStorage.index(posX, posY, posZ)] = block;
  }

  @NonNull
  @Override
  public Block get(int posX, int posY, int posZ) {
    Block block = blocks[BlockStorage.index(posX, posY, posZ)];
    return block == null ? Block.AIR : block;
  }

  @Override
  public int getDataLength(ProtocolVersion version) {
    return version.compareTo(ProtocolVersion.MINECRAFT_1_8) < 0 ? blocks.length + (Chunk.MAX_BLOCKS_PER_SECTION >> 1) : blocks.length * 2;
  }

  @Override
  public BlockStorage copy() {
    return new BlockStorage17(Arrays.copyOf(blocks, blocks.length));
  }

}
