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

import com.google.common.base.Preconditions;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class BitStorage19 implements CompactStorage {

    private final long[] data;
    private final int bitsPerEntry;
    private final int size;
    private final long maxEntryValue;

    public BitStorage19(int bitsPerEntry, int size) {
        this(bitsPerEntry, new long[((size * bitsPerEntry - 1) >> 6) + 1]);
    }

    public BitStorage19(int bitsPerEntry, long[] data) {
        if (bitsPerEntry < 4) {
            bitsPerEntry = 4;
        }

        this.bitsPerEntry = bitsPerEntry;
        this.data = data;

        size = (data.length << 6) / bitsPerEntry;
        maxEntryValue = (1L << bitsPerEntry) - 1;
    }

    @Override
    public void set(int index, int value) {
        if (index < 0 || index > size - 1) {
            throw new IndexOutOfBoundsException();
        } else if (value < 0 || value > maxEntryValue) {
            throw new IllegalArgumentException("Value cannot be outside of accepted range.");
        } else {
            int bitIndex = index * bitsPerEntry;
            int startIndex = bitIndex >> 6;
            int endIndex = ((index + 1) * bitsPerEntry - 1) >> 6;
            int startBitSubIndex = bitIndex & 63;
            data[startIndex] = data[startIndex] & ~(maxEntryValue << startBitSubIndex) | ((long) value & maxEntryValue) << startBitSubIndex;
            if (startIndex != endIndex) {
                int endBitSubIndex = 64 - startBitSubIndex;
                data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | ((long) value & maxEntryValue) >> endBitSubIndex;
            }
        }
    }

    @Override
    public int get(int index) {
        if (index < 0 || index > size - 1) {
            throw new IndexOutOfBoundsException();
        } else {
            int bitIndex = index * bitsPerEntry;
            int startIndex = bitIndex >> 6;
            int endIndex = ((index + 1) * bitsPerEntry - 1) >> 6;
            int startBitSubIndex = bitIndex & 63;
            if (startIndex == endIndex) {
                return (int) (data[startIndex] >>> startBitSubIndex & maxEntryValue);
            } else {
                int endBitSubIndex = 64 - startBitSubIndex;
                return (int) ((data[startIndex] >>> startBitSubIndex | data[endIndex] << endBitSubIndex) & maxEntryValue);
            }
        }
    }

    @Override
    public void write(Object byteBufObject, ProtocolVersion version) {
        Preconditions.checkArgument(byteBufObject instanceof ByteBuf);
        ByteBuf buf = (ByteBuf) byteBufObject;
        ProtocolUtils.writeVarInt(buf, data.length);
        for (long l : data) {
            buf.writeLong(l);
        }
    }

    @Override
    public int getDataLength() {
        return ProtocolUtils.varIntBytes(data.length) + data.length * 8;
    }

    @Override
    public CompactStorage copy() {
        return new BitStorage19(bitsPerEntry, Arrays.copyOf(data, data.length));
    }
}
