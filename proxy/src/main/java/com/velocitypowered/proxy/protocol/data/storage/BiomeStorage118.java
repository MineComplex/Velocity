package com.velocitypowered.proxy.protocol.data.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.chunk.Chunk;
import com.velocitypowered.proxy.protocol.data.world.Biome;
import io.netty.buffer.ByteBuf;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
public class BiomeStorage118 {

    private final ProtocolVersion version;

    private final List<Biome> palette;
    private final Map<Integer, Biome> rawToBiome;
    private CompactStorage storage;

    public BiomeStorage118(ProtocolVersion version) {
        this.version = version;
        palette = Lists.newArrayList();
        rawToBiome = Maps.newHashMap();

        for (Biome biome : Biome.values()) {
            palette.add(biome);
            rawToBiome.put(biome.getId(), biome);
        }

        storage = new BitStorage116(3, Chunk.MAX_BIOMES_PER_SECTION);
    }

    private BiomeStorage118(ProtocolVersion version, List<Biome> palette, Map<Integer, Biome> rawToBiome, CompactStorage storage) {
        this.version = version;
        this.palette = palette;
        this.rawToBiome = rawToBiome;
        this.storage = storage;
    }

    private static int index(int posX, int posY, int posZ) {
        return posY << 4 | posZ << 2 | posX;
    }

    public void set(int posX, int posY, int posZ, @NonNull Biome biome) {
        int id = getIndex(biome);
        storage.set(index(posX, posY, posZ), id);
    }

    public void set(int index, @NonNull Biome biome) {
        int id = getIndex(biome);
        storage.set(index, id);
    }

    @NonNull
    public Biome get(int posX, int posY, int posZ) {
        return get(index(posX, posY, posZ));
    }

    private Biome get(int index) {
        int id = storage.get(index);
        if (storage.getBitsPerEntry() > 8) {
            return rawToBiome.get(id);
        } else {
            return palette.get(id);
        }
    }

    public void write(ByteBuf buf, ProtocolVersion version) {
        buf.writeByte(storage.getBitsPerEntry());
        if (storage.getBitsPerEntry() <= 8) {
            ProtocolUtils.writeVarInt(buf, palette.size());
            for (Biome biome : palette) {
                ProtocolUtils.writeVarInt(buf, biome.getId());
            }
        }

        storage.write(buf, version);
    }

    public int getDataLength() {
        int length = 1;
        if (storage.getBitsPerEntry() <= 8) {
            length += ProtocolUtils.varIntBytes(palette.size());
            for (Biome biome : palette) {
                length += ProtocolUtils.varIntBytes(biome.getId());
            }
        }

        return length + storage.getDataLength();
    }

    public BiomeStorage118 copy() {
        return new BiomeStorage118(version, new ArrayList<>(palette), new HashMap<>(rawToBiome), storage.copy());
    }

    private int getIndex(Biome biome) {
        if (storage.getBitsPerEntry() > 8) {
            int raw = biome.getId();
            rawToBiome.put(raw, biome);
            return raw;
        } else {
            int id = palette.indexOf(biome);
            if (id == -1) {
                if (palette.size() >= (1 << storage.getBitsPerEntry())) {
                    resize(storage.getBitsPerEntry() + 1);
                    return getIndex(biome);
                }

                palette.add(biome);
                id = palette.size() - 1;
            }

            return id;
        }
    }

    private void resize(int newSize) {
        newSize = CompactStorage.fixBitsPerEntry(version, newSize);
        CompactStorage newStorage = new BitStorage116(newSize, Chunk.MAX_BIOMES_PER_SECTION);
        for (int i = 0; i < Chunk.MAX_BIOMES_PER_SECTION; ++i) {
            newStorage.set(i, newSize > 8 ? palette.get(storage.get(i)).getId() : storage.get(i));
        }

        storage = newStorage;
    }

}
