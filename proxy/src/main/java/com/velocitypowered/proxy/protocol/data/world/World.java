package com.velocitypowered.proxy.protocol.data.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.chunk.Chunk;
import com.velocitypowered.proxy.protocol.data.tile.TileEntity;
import lombok.Getter;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
public class World {

    private final Map<Long, Chunk> chunks = Maps.newHashMap();
    private final List<List<Chunk>> distanceChunkMap = Lists.newArrayList();
    @NonNull
    private final Dimension dimension;
    private final Biome defaultBiome;

    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;
    private final float yaw;
    private final float pitch;

    public World(@NonNull Dimension dimension, double posX, double posY, double posZ, float yaw, float pitch) {
        this.dimension = dimension;
        this.defaultBiome = dimension.getDefaultBiome();
        this.spawnX = posX;
        this.spawnY = posY;
        this.spawnZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;

        getChunkOrNew((int) posX, (int) posZ);
    }

    private static long getChunkIndex(int posX, int posZ) {
        return (long) posX << 32 | posZ & 0xFFFFFFFFL;
    }

    private static int getChunkXZ(int pos) {
        return pos >> 4;
    }

    private static int getChunkCoordinate(int pos) {
        return pos & 15;
    }

    public void setBlock(int posX, int posY, int posZ, @Nullable Block block) {
        getChunkOrNew(posX, posZ).setBlock(getChunkCoordinate(posX), posY, getChunkCoordinate(posZ), block);
    }

    public void setTileEntity(int posX, int posY, int posZ, @Nullable CompoundBinaryTag nbt, @Nullable TileEntity tileEntity) {
        getChunkOrNew(posX, posZ).setTileEntity(getChunkCoordinate(posX), posY, getChunkCoordinate(posZ), nbt, tileEntity);
    }

    @NonNull
    public Block getBlock(int posX, int posY, int posZ) {
        return chunkAction(posX, posZ, chunk -> chunk.getBlock(getChunkCoordinate(posX), posY, getChunkCoordinate(posZ)), () -> Block.AIR);
    }

    public void setBiome2d(int posX, int posZ, @NonNull Biome biome) {
        getChunkOrNew(posX, posZ).setBiome2D(getChunkCoordinate(posX), getChunkCoordinate(posZ), biome);
    }

    public void setBiome3d(int posX, int posY, int posZ, @NonNull Biome biome) {
        getChunkOrNew(posX, posZ).setBiome3D(getChunkCoordinate(posX), posY, getChunkCoordinate(posZ), biome);
    }

    public Biome getBiome(int posX, int posY, int posZ) {
        return chunkAction(posX, posZ, chunk -> chunk.getBiome(posX, posY, posZ), () -> Biome.PLAINS);
    }

    public byte getBlockLight(int posX, int posY, int posZ) {
        return chunkAction(posX, posZ, chunk -> chunk.getBlockLight(getChunkCoordinate(posX), posY, getChunkCoordinate(posZ)), () -> (byte) 0);
    }

    public void setBlockLight(int posX, int posY, int posZ, byte light) {
        getChunkOrNew(posX, posZ).setBlockLight(getChunkCoordinate(posX), posY, getChunkCoordinate(posZ), light);
    }

    public void fillBlockLight(int level) {
        for (Chunk chunk : chunks.values()) {
            chunk.fillBlockLight(level);
        }
    }

    public void fillSkyLight(int level) {
        for (Chunk chunk : chunks.values()) {
            chunk.fillSkyLight(level);
        }
    }

    public List<Chunk> getChunks() {
        return ImmutableList.copyOf(chunks.values());
    }

    public List<List<Chunk>> getOrderedChunks() {
        return distanceChunkMap.stream()
                .map(Collections::unmodifiableList)
                .toList();
    }

    private int getDistanceToSpawn(Chunk chunk) {
        int diffX = getChunkXZ((int) spawnX) - chunk.getPosX();
        int diffZ = getChunkXZ((int) spawnZ) - chunk.getPosZ();
        return (int) Math.sqrt((diffX * diffX) + (diffZ * diffZ));
    }

    @Nullable
    public Chunk getChunk(int posX, int posZ) {
        return chunks.get(getChunkIndex(getChunkXZ(posX), getChunkXZ(posZ)));
    }

    public Chunk getChunkOrNew(int posX, int posZ) {
        posX = getChunkXZ(posX);
        posZ = getChunkXZ(posZ);

        // Modern Sodium versions don't load chunks if their "neighbours" are unloaded.
        // We are fixing this problem there by generating all the "neighbours".
        for (int chunkX = posX - 1; chunkX <= posX + 1; ++chunkX) {
            for (int chunkZ = posZ - 1; chunkZ <= posZ + 1; ++chunkZ) {
                localCreateChunk(chunkX, chunkZ);
            }
        }

        return chunks.get(getChunkIndex(posX, posZ));
    }

    private void localCreateChunk(int posX, int posZ) {
        long index = getChunkIndex(posX, posZ);
        if (!chunks.containsKey(index)) {
            Chunk chunk = new Chunk(posX, posZ, defaultBiome);

            chunks.put(index, chunk);

            int distance = getDistanceToSpawn(chunk);
            for (int i = distanceChunkMap.size(); i <= distance; i++) {
                distanceChunkMap.add(new LinkedList<>());
            }

            distanceChunkMap.get(distance).add(chunk);
        }
    }

    private <T> T chunkAction(int posX, int posZ, Function<Chunk, T> function, Supplier<T> ifNull) {
        Chunk chunk = getChunk(posX, posZ);
        if (chunk == null) {
            return ifNull.get();
        }

        return function.apply(chunk);
    }
}
