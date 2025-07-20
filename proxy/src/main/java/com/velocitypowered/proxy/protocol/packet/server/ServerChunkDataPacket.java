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
import com.velocitypowered.proxy.protocol.data.material.Material;
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
  private CompoundBinaryTag heightmap116;
  private Map<Integer, long[]> heightmap1215;

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

    heightmap116 = createHeightMap();
    heightmap1215 = new HashMap<>();
    for (Map.Entry<String, ? extends BinaryTag> entry : heightmap116) {
      heightmap1215.put(findHeightMapId(entry.getKey()), ((LongArrayBinaryTag) entry.getValue()).value());
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

    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_17)) {
      if (version.noGreaterThan(ProtocolVersion.MINECRAFT_1_17_1)) {
        ProtocolUtils.writeVarInt(buf, 0); // mask
      }
    } else {
      buf.writeBoolean(true); // full chunk
      ProtocolUtils.writeVarInt(buf, 0);
    }

    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_21_5)) {
      // In 1.21.5. They changed to List<EnumMap<Heightmap.Type, long[]>>
      ProtocolUtils.writeVarInt(buf, 1); // List size
      ProtocolUtils.writeVarInt(buf, 4); // Ordinal of MOTION_BLOCKING
      // Write long array
      ProtocolUtils.writeVarInt(buf, 37);
      for (int i = 0; i < 37; i++) {
        buf.writeLong(0);
      }
    } else { // Nbt for older version
      long[] motionBlockingData = new long[version.lessThan(ProtocolVersion.MINECRAFT_1_18) ? 36 : 37];
      CompoundBinaryTag motionBlockingTag = CompoundBinaryTag.builder()
          .put("MOTION_BLOCKING", LongArrayBinaryTag.longArrayBinaryTag(motionBlockingData))
          .build();
      CompoundBinaryTag rootTag = CompoundBinaryTag.builder()
          .put("root", motionBlockingTag)
          .build();

      ProtocolUtils.writeBinaryTag(buf, version, rootTag);
    }

    if (version.noGreaterThan(ProtocolVersion.MINECRAFT_1_17_1)) {
      ProtocolUtils.writeVarInt(buf, 1024);

      for (int i = 0; i < 1024; i++) {
        ProtocolUtils.writeVarInt(buf, 1);
      }
    }

    if (version.lessThan(ProtocolVersion.MINECRAFT_1_18)) {
      ProtocolUtils.writeVarInt(buf, 0);
    } else {
      // nonEmptyBlockCount. short occupies 2 bytes.
      // SingularPalette (byte) + id (varint) + long array with size (older)
      // Since 1.21.5. The length of the long array of PaletteStorage no longer depends on the VarInt in the packet
      // SingularPalette doesn't need to read any additional long array.
      // So we'll remove suffix 0 as array length here. The cost of writing a palette has been reduced from 3 to 2 bytes.
      byte[] sectionData = version.lessThan(ProtocolVersion.MINECRAFT_1_21_5) ?
          new byte[] {0, 0, 0, 0, 0, 0, 1, 0} :
          new byte[] {0, 0, 0, 0, 0, 1};
      int count = 24;
      ProtocolUtils.writeVarInt(buf, sectionData.length * count);

      for (int i = 0; i < count; i++) {
        buf.writeBytes(sectionData);
      }
    }

    ProtocolUtils.writeVarInt(buf, 0);

    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
      for (int i = 0; i < 6; i++) {
        ProtocolUtils.writeVarInt(buf, 0);
      }
    } else if (version.noLessThan(ProtocolVersion.MINECRAFT_1_18)) {
      final byte[] lightData = new byte[] {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, -1, -1, 0, 0};

      buf.ensureWritable(lightData.length);

      if (version.noLessThan(ProtocolVersion.MINECRAFT_1_20)) {
        buf.writeBytes(lightData, 1, lightData.length - 1);
      } else {
        buf.writeBytes(lightData);
      }
    }
  }

  @Override
  public void encode(ByteBuf buf, Direction direction, ProtocolVersion version) {
    if (chunk == null) {
      encodeVoid(buf, direction, version);
      return;
    }
    if (!chunk.fullChunk()) {
      // 1.17 supports only full chunks.
      Preconditions.checkState(version.lessThan(ProtocolVersion.MINECRAFT_1_17));
    }

    buf.writeInt(chunk.posX());
    buf.writeInt(chunk.posZ());
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_17)) {
      // 1.17 mask.
      if (version.noGreaterThan(ProtocolVersion.MINECRAFT_1_17_1)) {
        long[] mask = create117Mask();
        ProtocolUtils.writeVarInt(buf, mask.length);
        for (long l : mask) {
          buf.writeLong(l);
        }
      }
    } else {
      buf.writeBoolean(chunk.fullChunk());

      // Mask.
      ProtocolUtils.writeVarInt(buf, mask);
    }

    if (version.lessThan(ProtocolVersion.MINECRAFT_1_21_5)) {
      ProtocolUtils.writeBinaryTag(buf, version, heightmap116);
    } else {
      ProtocolUtils.writeVarInt(buf, heightmap1215.size());
      for (Map.Entry<Integer, long[]> entry : heightmap1215.entrySet()) {
        ProtocolUtils.writeVarInt(buf, entry.getKey());
        ProtocolUtils.writeVarInt(buf, entry.getValue().length);
        for (long l : entry.getValue()) {
          buf.writeLong(l);
        }
      }
    }

    // 1.16 - 1.17 biomes.
    if (chunk.fullChunk() && version.noGreaterThan(ProtocolVersion.MINECRAFT_1_17_1)) {
      ProtocolUtils.writeVarInt(buf, biomes.length);
      for (int biome : biomes) {
        ProtocolUtils.writeVarInt(buf, biome);
      }
    }

    ByteBuf data = createChunkData(version);
    try {
      ProtocolUtils.writeVarInt(buf, data.readableBytes());
      buf.writeBytes(data);
      List<TileEntity.Entry> blockEntityEntries = chunk.tileEntityEntries();
      ProtocolUtils.writeVarInt(buf, blockEntityEntries.size());
      for (TileEntity.Entry blockEntityEntry : blockEntityEntries) {
        CompoundBinaryTag blockEntityNbt = blockEntityEntry.getNbt();
        if (version.noLessThan(ProtocolVersion.MINECRAFT_1_18)) {
          buf.writeByte(((blockEntityEntry.getPosX() & 15) << 4) | (blockEntityEntry.getPosZ() & 15));
          buf.writeShort(blockEntityEntry.getPosY());
          ProtocolUtils.writeVarInt(buf, blockEntityEntry.getId(version));
        } else {
          blockEntityNbt.putString("id", blockEntityEntry.getTileEntity().getModernId());
          blockEntityNbt.putInt("x", blockEntityEntry.getPosX());
          blockEntityNbt.putInt("y", blockEntityEntry.getPosY());
          blockEntityNbt.putInt("z", blockEntityEntry.getPosZ());
        }

        ProtocolUtils.writeBinaryTag(buf, version, blockEntityNbt);
      }
      if (version.greaterThan(ProtocolVersion.MINECRAFT_1_17_1)) {
        long[] mask = create117Mask();
        if (version.lessThan(ProtocolVersion.MINECRAFT_1_20)) {
          buf.writeBoolean(true); // Trust edges.
        }
        ProtocolUtils.writeVarInt(buf, mask.length); // Skylight mask.
        for (long m : mask) {
          buf.writeLong(m);
        }
        ProtocolUtils.writeVarInt(buf, mask.length); // BlockLight mask.
        for (long m : mask) {
          buf.writeLong(m);
        }
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
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_18)) {
      int emptySectionSize = version.noLessThan(ProtocolVersion.MINECRAFT_1_21_5) ? 6 : 8;
      dataLength += (maxSections - nonNullSections) * emptySectionSize;
    }

    ByteBuf data = Unpooled.buffer(dataLength);
    for (int pass = 0; pass < 4; ++pass) {
      for (NetworkSection section : sections) {
        if (section != null) {
          section.writeData(data, pass, version);
        } else if (pass == 0 && version.noLessThan(ProtocolVersion.MINECRAFT_1_18)) {
          data.writeShort(0); // Block count = 0.
          data.writeByte(0); // BlockStorage: 0 bit per entry = Single palette.
          ProtocolUtils.writeVarInt(data, Material.AIR.getId()); // Only air block in the palette.
          if (version.lessThan(ProtocolVersion.MINECRAFT_1_21_5)) {
            ProtocolUtils.writeVarInt(data, 0); // BlockStorage: 0 entries.
          }

          data.writeByte(0); // BiomeStorage: 0 bit per entry = Single palette.
          ProtocolUtils.writeVarInt(data, Biome.PLAINS.getId()); // Only Plain biome in the palette.
          if (version.lessThan(ProtocolVersion.MINECRAFT_1_21_5)) {
            ProtocolUtils.writeVarInt(data, 0); // BiomeStorage: 0 entries.
          }
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

  private long[] create117Mask() {
    return BitSet.valueOf(new long[] {mask}).toLongArray();
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return true;
  }
}