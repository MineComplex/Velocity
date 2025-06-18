package com.velocitypowered.proxy.protocol.data.block;

import com.velocitypowered.api.network.ProtocolVersion;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface BlockStorage {

    static int index(int posX, int posY, int posZ) {
        return posY << 8 | posZ << 4 | posX;
    }

    void write(Object byteBufObject, ProtocolVersion version, int pass);

    void set(int posX, int posY, int posZ, @NonNull Block block);

    @NonNull
    Block get(int posX, int posY, int posZ);

    int getDataLength(ProtocolVersion version);

    BlockStorage copy();
}
