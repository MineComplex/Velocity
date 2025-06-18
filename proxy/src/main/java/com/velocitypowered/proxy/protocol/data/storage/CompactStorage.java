package com.velocitypowered.proxy.protocol.data.storage;

import com.velocitypowered.api.network.ProtocolVersion;

public interface CompactStorage {

    void set(int index, int value);

    int get(int index);

    void write(Object byteBufObject, ProtocolVersion version);

    int getBitsPerEntry();

    int getDataLength();

    long[] getData();

    CompactStorage copy();

    static int fixBitsPerEntry(ProtocolVersion version, int bitsPerEntry) {
        if (bitsPerEntry < 4) {
            return 4;
        } else if (bitsPerEntry < 9) {
            return bitsPerEntry;
        } else if (version.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) {
            return 13;
        } else if (version.compareTo(ProtocolVersion.MINECRAFT_1_16_4) < 0) {
            return 14;
        } else {
            return 15;
        }
    }
}
