/*
 * Copyright (C) 2021 - 2024 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file contains some parts of Velocity, licensed under the AGPLv3 License (AGPLv3).
 *
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.protocol.netty;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;

import java.util.List;

public class MinecraftLimitedCompressDecoder extends MinecraftCompressDecoder {

    private final int threshold;
    private final int maxSingleLength;
    private final VelocityCompressor compressor;
    @Setter
    private int uncompressedCap;

    public MinecraftLimitedCompressDecoder(int threshold, int maxPacketLengthToSuppress, int maxSinglePacketLength, VelocityCompressor compressor) {
        super(threshold, compressor);
        this.threshold = threshold;
        this.uncompressedCap = maxPacketLengthToSuppress;
        this.maxSingleLength = maxSinglePacketLength;
        this.compressor = compressor;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int claimedUncompressedSize = ProtocolUtils.readVarInt(in);
        if (claimedUncompressedSize == 0) {
            out.add(in.retain());
        } else {
            if (claimedUncompressedSize > maxSingleLength) {
                ctx.close();
            } else {
                if (claimedUncompressedSize >= threshold && claimedUncompressedSize <= uncompressedCap) {
                    ByteBuf compatibleIn = MoreByteBufUtils.ensureCompatible(ctx.alloc(), compressor, in);
                    ByteBuf uncompressed = MoreByteBufUtils.preferredBuffer(ctx.alloc(), compressor, claimedUncompressedSize);
                    try {
                        compressor.inflate(compatibleIn, uncompressed, claimedUncompressedSize);
                        out.add(uncompressed);
                    } catch (Exception e) {
                        uncompressed.release();
                        throw e;
                    } finally {
                        compatibleIn.release();
                    }
                }
            }
        }
    }
}
