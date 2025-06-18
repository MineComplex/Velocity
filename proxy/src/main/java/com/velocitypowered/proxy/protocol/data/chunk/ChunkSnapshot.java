package com.velocitypowered.proxy.protocol.data.chunk;

import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.chunk.section.BlockSection;
import com.velocitypowered.proxy.protocol.data.chunk.section.LightSection;
import com.velocitypowered.proxy.protocol.data.tile.TileEntity;
import com.velocitypowered.proxy.protocol.data.world.Biome;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChunkSnapshot {

    private final int posX;
    private final int posZ;
    private final boolean fullChunk;
    private final BlockSection[] sections;
    private final LightSection[] light;
    private final Biome[] biomes;
    private final List<TileEntity.Entry> tileEntityEntries;

    public Block getBlock(int posX, int posY, int posZ) {
        BlockSection section = sections[posY >> 4];
        return section == null ? Block.AIR : section.getBlockAt(posX, posY & 15, posZ);
    }

}
