package com.velocitypowered.proxy.protocol.data.chunk.section;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.block.BlockStorage;
import com.velocitypowered.proxy.protocol.data.storage.BlockStorage19;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
@AllArgsConstructor
public class BlockSection {

    private final BlockStorage blocks;

    @Getter
    private long lastUpdate = System.nanoTime();

    public BlockSection() {
        this(new BlockStorage19(ProtocolVersion.MINECRAFT_1_17));
    }

    public void setBlockAt(int posX, int posY, int posZ, @Nullable Block block) {
        this.checkIndexes(posX, posY, posZ);
        this.blocks.set(posX, posY, posZ, block == null ? Block.AIR : block);
        this.lastUpdate = System.nanoTime();
    }

    public Block getBlockAt(int posX, int posY, int posZ) {
        this.checkIndexes(posX, posY, posZ);
        return this.blocks.get(posX, posY, posZ);
    }

    private void checkIndexes(int posX, int posY, int posZ) {
        Preconditions.checkArgument(this.checkIndex(posX), "x should be between 0 and 15");
        Preconditions.checkArgument(this.checkIndex(posY), "y should be between 0 and 15");
        Preconditions.checkArgument(this.checkIndex(posZ), "z should be between 0 and 15");
    }

    private boolean checkIndex(int pos) {
        return pos >= 0 && pos <= 15;
    }

    public BlockSection getSnapshot() {
        return new BlockSection(this.blocks.copy(), this.lastUpdate);
    }

}
