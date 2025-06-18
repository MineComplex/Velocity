package com.velocitypowered.proxy.protocol.data.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * For MapData packet.
 */
@ToString
@Getter
@AllArgsConstructor
public class MapData {

    public static final int MAP_DIM_SIZE = 128;
    public static final int MAP_SIZE = MAP_DIM_SIZE * MAP_DIM_SIZE; // 128² == 16384

    private final int columns;
    private final int rows;
    private final int x;
    private final int y;
    private final byte[] data;

    public MapData(byte[] data) {
        this(0, data);
    }

    public MapData(int posX, byte[] data) {
        this(MAP_DIM_SIZE, MAP_DIM_SIZE, posX, 0, data);
    }

}
