package com.velocitypowered.proxy.protocol.data.chunk;

import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.chunk.section.BlockSection;
import com.velocitypowered.proxy.protocol.data.chunk.section.LightSection;
import com.velocitypowered.proxy.protocol.data.tile.TileEntity;
import com.velocitypowered.proxy.protocol.data.world.Biome;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.IntRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ToString
public class Chunk {

    public static final int MAX_BLOCKS_PER_SECTION = 16 * 16 * 16;
    public static final int MAX_BIOMES_PER_SECTION = 4 * 4 * 4;

    @ToString.Include
    @Getter
    private final int posX;
    @ToString.Include
    @Getter
    private final int posZ;

    private final BlockSection[] sections = new BlockSection[16];
    private final LightSection[] light = new LightSection[18];
    private final Biome[] biomes = new Biome[1024];
    private final List<TileEntity.Entry> tileEntityEntries = new ArrayList<>();

    public Chunk(int posX, int posZ) {
        this(posX, posZ, Biome.PLAINS);
    }

    public Chunk(int posX, int posZ, Biome defaultBiome) {
        this.posX = posX;
        this.posZ = posZ;

        for (int i = 0; i < light.length; ++i) {
            light[i] = new LightSection();
        }

        Arrays.fill(biomes, defaultBiome);
    }

    private static int getBiomeIndex(int posX, int posY, int posZ) {
        return (posY >> 2 & 63) << 4 | (posZ >> 2 & 3) << 2 | posX >> 2 & 3;
    }

    private static int getSectionIndex(int posY) {
        return posY >> 4;
    }

    public void setBlock(int posX, int posY, int posZ, @Nullable Block block) {
        getSection(posY).setBlockAt(posX, posY & 15, posZ, block);
    }

    public void setTileEntity(int posX, int posY, int posZ, @Nullable CompoundBinaryTag nbt, @Nullable TileEntity tileEntity) {
        if (tileEntity == null) {
            tileEntityEntries.removeIf(entry -> entry.getPosX() == posX && entry.getPosY() == posY && entry.getPosZ() == posZ);
            return;
        }

        tileEntityEntries.add(tileEntity.getEntry(posX, posY, posZ, nbt));
    }

    public void setTileEntity(TileEntity.Entry tileEntityEntry) {
        tileEntityEntries.add(tileEntityEntry);
    }

    private BlockSection getSection(int posY) {
        int sectionIndex = getSectionIndex(posY);
        BlockSection section = sections[sectionIndex];
        if (section == null) {
            section = new BlockSection();
            sections[sectionIndex] = section;
        }

        return section;
    }

    @NonNull
    public Block getBlock(int posX, int posY, int posZ) {
        BlockSection section = sections[getSectionIndex(posY)];
        if (section == null) {
            return Block.AIR;
        } else {
            return section.getBlockAt(posX, posY & 15, posZ);
        }
    }

    public void setBiome2D(int posX, int posZ, @NonNull Biome biome) {
        for (int posY = 0; posY < 256; posY += 4) {
            setBiome3D(posX, posY, posZ, biome);
        }
    }

    public void setBiome3D(int posX, int posY, int posZ, @NonNull Biome biome) {
        biomes[getBiomeIndex(posX, posY, posZ)] = biome;
    }

    @NonNull
    public Biome getBiome(int posX, int posY, int posZ) {
        return biomes[getBiomeIndex(posX, posY, posZ)];
    }

    public void setBlockLight(int posX, int posY, int posZ, byte light) {
        getLightSection(posY).setBlockLight(posX, posY & 15, posZ, light);
    }

    public byte getBlockLight(int posX, int posY, int posZ) {
        return getLightSection(posY).getBlockLight(posX, posY & 15, posZ);
    }

    public void setSkyLight(int posX, int posY, int posZ, byte light) {
        getLightSection(posY).setSkyLight(posX, posY & 15, posZ, light);
    }

    public byte getSkyLight(int posX, int posY, int posZ) {
        return getLightSection(posY).getSkyLight(posX, posY & 15, posZ);
    }

    private LightSection getLightSection(int posY) {
        return light[posY < 0 ? 0 : getSectionIndex(posY) + 1];
    }

    public void fillBlockLight(@IntRange(from = 0, to = 15) int level) {
        for (LightSection lightSection : light) {
            lightSection.getBlockLight().fill(level);
        }
    }

    public void fillSkyLight(@IntRange(from = 0, to = 15) int level) {
        for (LightSection lightSection : light) {
            lightSection.getSkyLight().fill(level);
        }
    }

    public ChunkSnapshot getFullChunkSnapshot() {
        return createSnapshot(true, 0);
    }

    public ChunkSnapshot getPartialChunkSnapshot(long previousUpdate) {
        return createSnapshot(false, previousUpdate);
    }

    private ChunkSnapshot createSnapshot(boolean full, long previousUpdate) {
        BlockSection[] sectionsSnapshot = new BlockSection[sections.length];
        for (int i = 0; i < sections.length; ++i) {
            if (sections[i] != null && sections[i].getLastUpdate() > previousUpdate) {
                sectionsSnapshot[i] = sections[i].getSnapshot();
            }
        }

        LightSection[] lightSnapshot = new LightSection[light.length];
        for (int i = 0; i < lightSnapshot.length; ++i) {
            if (light[i].getLastUpdate() > previousUpdate) {
                lightSnapshot[i] = light[i].copy();
            }
        }

        return new ChunkSnapshot(posX, posZ, full, sectionsSnapshot, lightSnapshot,
                Arrays.copyOf(biomes, biomes.length), List.copyOf(tileEntityEntries));
    }

}
