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

package com.velocitypowered.proxy.protocol.packet.server;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.chunk.ChunkSnapshot;
import com.velocitypowered.proxy.protocol.data.chunk.section.BlockSection;
import com.velocitypowered.proxy.protocol.data.chunk.section.LightSection;
import com.velocitypowered.proxy.protocol.data.chunk.section.NetworkSection;
import com.velocitypowered.proxy.protocol.data.storage.BitStorage;
import com.velocitypowered.proxy.protocol.data.tile.TileEntity;
import com.velocitypowered.proxy.protocol.data.world.Biome;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerChunkDataPacket implements MinecraftPacket {

  private int sectionX, sectionZ;

  private ChunkSnapshot chunk;
  private NetworkSection[] sections;
  private int mask;
  private int[] biomes;
  private int maxSections;
  private int nonNullSections;
  private Map<Integer, long[]> heightmaps;

  public ServerChunkDataPacket(int sectionX, int sectionZ) {
    this.sectionX = sectionX;
    this.sectionZ = sectionZ;
  }

  public ServerChunkDataPacket(ChunkSnapshot chunkSnapshot, int maxSections) {
    this.maxSections = maxSections;
    this.sections = new NetworkSection[maxSections];
    this.chunk = chunkSnapshot;

    int mask = 0;
    int nonNullSections = 0;
    for (int i = 0; i < chunk.sections().length; ++i) {
      BlockSection blockSection = chunk.sections()[i];
      if (blockSection != null) {
        ++nonNullSections;
        mask |= 1 << i;
        NetworkSection section = new NetworkSection(i, blockSection, chunk.biomes());
        sections[i] = section;
      }
    }

    this.nonNullSections = nonNullSections;
    this.mask = mask;

    heightmaps = new HashMap<>();
    for (Map.Entry<String, ? extends BinaryTag> entry : createHeightMap()) {
      heightmaps.put(findHeightMapId(entry.getKey()), ((LongArrayBinaryTag) entry.getValue()).value());
    }

    biomes = new int[1024];
    for (int i = 0; i < chunk.biomes().length; ++i) {
      biomes[i] = chunk.biomes()[i].getId();
    }
  }

  public ServerChunkDataPacket() {
    throw new IllegalStateException();
  }

  private int findHeightMapId(String key) {
    return switch (key) {
      case "WORLD_SURFACE" -> 1; /* taken from minecraft decompiled source code */
      case "MOTION_BLOCKING" -> 4; /* taken from minecraft decompiled source code */
      default -> throw new IllegalArgumentException("Unsupported heightmap: " + key);
    };
  }

  @Override
  public void decode(ByteBuf buf, Direction direction, ProtocolVersion version) {
    throw new IllegalStateException();
  }

  public void encodeVoid(ByteBuf buf, Direction direction, ProtocolVersion version) {
    buf.writeInt(sectionX);
    buf.writeInt(sectionZ);

    // Heightmaps: List<EnumMap<Heightmap.Type, long[]>>
    ProtocolUtils.writeVarInt(buf, 1); // List size
    ProtocolUtils.writeVarInt(buf, 4); // Ordinal of MOTION_BLOCKING
    // Write long array
    ProtocolUtils.writeVarInt(buf, 37);
    for (int i = 0; i < 37; i++) {
      buf.writeLong(0);
    }

    // nonEmptyBlockCount. short occupies 2 bytes.
    // SingularPalette (byte) + id (varint). The long array length is no longer written.
    byte[] sectionData = new byte[] {0, 0, 0, 0, 0, 1};
    int count = 24;
    ProtocolUtils.writeVarInt(buf, sectionData.length * count);

    for (int i = 0; i < count; i++) {
      buf.writeBytes(sectionData);
    }

    ProtocolUtils.writeVarInt(buf, 0);

    for (int i = 0; i < 6; i++) {
      ProtocolUtils.writeVarInt(buf, 0);
    }
  }

  @Override
  public void encode(ByteBuf buf, Direction direction, ProtocolVersion version) {
    if (chunk == null) {
      encodeVoid(buf, direction, version);
      return;
    }
    Preconditions.checkState(chunk.fullChunk(), "Only full chunks are supported");

    buf.writeInt(chunk.posX());
    buf.writeInt(chunk.posZ());

    ProtocolUtils.writeVarInt(buf, heightmaps.size());
    for (Map.Entry<Integer, long[]> entry : heightmaps.entrySet()) {
      ProtocolUtils.writeVarInt(buf, entry.getKey());
      ProtocolUtils.writeVarInt(buf, entry.getValue().length);
      for (long l : entry.getValue()) {
        buf.writeLong(l);
      }
    }

    ByteBuf data = createChunkData(version);
    try {
      ProtocolUtils.writeVarInt(buf, data.readableBytes());
      buf.writeBytes(data);
      List<TileEntity.Entry> blockEntityEntries = chunk.tileEntityEntries();
      ProtocolUtils.writeVarInt(buf, blockEntityEntries.size());
      for (TileEntity.Entry blockEntityEntry : blockEntityEntries) {
        buf.writeByte(((blockEntityEntry.getPosX() & 15) << 4) | (blockEntityEntry.getPosZ() & 15));
        buf.writeShort(blockEntityEntry.getPosY());
        ProtocolUtils.writeVarInt(buf, blockEntityEntry.getId(version));
        ProtocolUtils.writeBinaryTag(buf, blockEntityEntry.getNbt());
      }

      long[] mask = createLightMask();
      ProtocolUtils.writeBitSet(buf, mask); // Skylight mask.
      ProtocolUtils.writeBitSet(buf, mask); // BlockLight mask.
      ProtocolUtils.writeVarInt(buf, 0); // EmptySkylight mask.
      ProtocolUtils.writeVarInt(buf, 0); // EmptyBlockLight mask.
      ProtocolUtils.writeVarInt(buf, chunk.light().length);
      for (LightSection section : chunk.light()) {
        ProtocolUtils.writeByteArray(buf, section.getSkyLight().getData());
      }
      ProtocolUtils.writeVarInt(buf, chunk.light().length);
      for (LightSection section : chunk.light()) {
        ProtocolUtils.writeByteArray(buf, section.getBlockLight().getData());
      }
    } finally {
      data.release();
    }
  }

  private ByteBuf createChunkData(ProtocolVersion version) {
    int dataLength = 0;
    for (NetworkSection networkSection : sections) {
      if (networkSection != null) {
        dataLength += networkSection.getDataLength(version);
      }
    }
    int emptySectionSize = 8;
    dataLength += (maxSections - nonNullSections) * emptySectionSize;

    ByteBuf data = Unpooled.buffer(dataLength);
    for (int pass = 0; pass < 4; ++pass) {
      for (NetworkSection section : sections) {
        if (section != null) {
          section.writeData(data, pass, version);
        } else if (pass == 0) {
          data.writeShort(0); // Block count = 0.
          data.writeShort(0); // Fluid count = 0.
          data.writeByte(0); // BlockStorage: 0 bit per entry = Single palette.
          ProtocolUtils.writeVarInt(data, 0); // Only air block in the palette.

          data.writeByte(0); // BiomeStorage: 0 bit per entry = Single palette.
          ProtocolUtils.writeVarInt(data, Biome.PLAINS.getId()); // Only Plain biome in the palette.
        }
      }
    }

    return data;
  }

  private CompoundBinaryTag createHeightMap() {
    BitStorage surface = new BitStorage(9, 256);
    BitStorage motionBlocking = new BitStorage(9, 256);

    for (int posY = 0; posY < 256; ++posY) {
      for (int posX = 0; posX < 16; ++posX) {
        for (int posZ = 0; posZ < 16; ++posZ) {
          Block block = chunk.getBlock(posX, posY, posZ);
          if (!block.isAir()) {
            surface.set(posX + (posZ << 4), posY + 1);
          }
          if (block.isMotionBlocking()) {
            motionBlocking.set(posX + (posZ << 4), posY + 1);
          }
        }
      }
    }

    return CompoundBinaryTag.builder()
        .putLongArray("MOTION_BLOCKING", motionBlocking.getData())
        .putLongArray("WORLD_SURFACE", surface.getData())
        .build();
  }

  private long[] createLightMask() {
    return BitSet.valueOf(new long[] {mask}).toLongArray();
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return true;
  }
}