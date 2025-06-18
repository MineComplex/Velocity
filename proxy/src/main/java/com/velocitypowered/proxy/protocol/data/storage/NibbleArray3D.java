/*
 * This file is part of MCProtocolLib, licensed under the MIT License (MIT).
 *
 * Copyright (C) 2013-2021 Steveice10
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.velocitypowered.proxy.protocol.data.storage;

import com.velocitypowered.proxy.protocol.data.block.BlockStorage;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class NibbleArray3D {

    private final byte[] data;

    public NibbleArray3D(int size) {
        data = new byte[size >> 1];
    }

    public NibbleArray3D(int size, int defaultValue) {
        data = new byte[size >> 1];
        fill(defaultValue);
    }

    public NibbleArray3D(byte[] array) {
        data = array;
    }

    public int get(int posX, int posY, int posZ) {
        int key = BlockStorage.index(posX, posY, posZ);
        int index = key >> 1;
        return (key & 1) == 0 ? data[index] & 15 : data[index] >> 4 & 15;
    }

    public void set(int posX, int posY, int posZ, int value) {
        set(BlockStorage.index(posX, posY, posZ), value);
    }

    public void set(int key, int val) {
        int index = key >> 1;
        if ((key & 1) == 0) {
            data[index] = (byte) (data[index] & 240 | val & 15);
        } else {
            data[index] = (byte) (data[index] & 15 | (val & 15) << 4);
        }
    }

    public void fill(int value) {
        for (int index = 0; index < data.length << 1; ++index) {
            set(index, value);
        }
    }

    public NibbleArray3D copy() {
        return new NibbleArray3D(Arrays.copyOf(data, data.length));
    }
}
