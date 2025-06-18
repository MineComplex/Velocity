package com.velocitypowered.proxy.protocol.data.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
public final class BlockUpdate {
    private final BlockPosition position;
    private final BlockType blockType;
    private final int legacyBlockState, blockState;

    public BlockUpdate(@NotNull BlockPosition position, BlockType blockType) {
        this.position = position;
        this.blockType = blockType;
        final int x = position.getX() - (position.getChunkX() << 4);
        final int y = position.getY();
        final int z = position.getZ() - (position.getChunkZ() << 4);
        this.legacyBlockState = x << 12 | z << 8 | y;
        this.blockState = x << 8 | z << 4 | y - ((y >> 4) << 4);
    }

    @Getter
    @RequiredArgsConstructor
    public static final class BlockPosition {
        private final int x, y, z;
        private final int chunkX, chunkZ;
    }
}
