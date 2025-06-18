package com.velocitypowered.proxy.protocol.data.storage;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.block.BlockStorage;
import com.velocitypowered.proxy.protocol.data.chunk.Chunk;
import io.netty.buffer.ByteBuf;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
public class BlockStorage19 implements BlockStorage {

    private final ProtocolVersion version;
    private final List<Block> palette;
    private final Map<Short, Block> rawToBlock;

    private CompactStorage storage;

    public BlockStorage19(ProtocolVersion version) {
        this.version = version;
        palette = Lists.newArrayList();
        rawToBlock = Maps.newHashMap();

        palette.add(Block.AIR);
        rawToBlock.put(Block.AIR.getBlockStateId(version), Block.AIR);

        storage = createStorage(4);
    }

    private BlockStorage19(ProtocolVersion version, List<Block> palette, Map<Short, Block> rawToBlock, CompactStorage storage) {
        this.version = version;
        this.palette = palette;
        this.rawToBlock = rawToBlock;
        this.storage = storage;
    }

    @Override
    public void write(Object byteBufObject, ProtocolVersion version, int pass) {
        Preconditions.checkArgument(byteBufObject instanceof ByteBuf);
        ByteBuf buf = (ByteBuf) byteBufObject;
        buf.writeByte(storage.getBitsPerEntry());
        if (storage.getBitsPerEntry() > 8) {
            if (this.version.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) {
                ProtocolUtils.writeVarInt(buf, 0);
            }
        } else {
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
        storage.set(BlockStorage.index(posX, posY, posZ), id);
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
                    int bitsPerEntry = CompactStorage.fixBitsPerEntry(version, storage.getBitsPerEntry() + 1);
                    CompactStorage newStorage = createStorage(bitsPerEntry);
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

    private CompactStorage createStorage(int bitsPerEntry) {
        return version.compareTo(ProtocolVersion.MINECRAFT_1_16) < 0
                ? new BitStorage19(bitsPerEntry, Chunk.MAX_BLOCKS_PER_SECTION)
                : new BitStorage116(bitsPerEntry, Chunk.MAX_BLOCKS_PER_SECTION);
    }

    @NonNull
    @Override
    public Block get(int posX, int posY, int posZ) {
        int id = storage.get(BlockStorage.index(posX, posY, posZ));
        if (storage.getBitsPerEntry() > 8) {
            return rawToBlock.get((short) id);
        } else {
            return palette.get(id);
        }
    }

    @Override
    public int getDataLength(ProtocolVersion version) {
        int length = 1;
        if (storage.getBitsPerEntry() > 8) {
            if (this.version.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) {
                length += 1;
            }
        } else {
            length += ProtocolUtils.varIntBytes(palette.size());
            for (Block state : palette) {
                length += ProtocolUtils.varIntBytes(state.getBlockStateId(this.version));
            }
        }

        return length + storage.getDataLength();
    }

    @Override
    public BlockStorage copy() {
        return new BlockStorage19(version, new ArrayList<>(palette), new HashMap<>(rawToBlock), storage.copy());
    }

}
