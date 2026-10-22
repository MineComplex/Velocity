/*
 * Copyright (C) 2025 Velocity Contributors
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

package com.velocitypowered.proxy.protocol.prepared;

import com.velocitypowered.proxy.protocol.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.ReferenceCounted;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class PreparedPacket {

  private final PreparedPacketFactory factory;
  private ByteBuf packets;
  private ByteBuf uncompressedPackets;
  private boolean disposed;

  public PreparedPacket prepare(MinecraftPacket packet) {
    if (packet == null) {
      return this;
    }

    ByteBuf buf = factory.encodeSingle(packet, factory.isCompressionEnabled(), false);
    if (packets == null) {
      packets = factory.getPreparedPacketAllocator().directBuffer();
    }
    try {
      packets.writeBytes(buf);
    } finally {
      buf.release();
    }

    if (factory.shouldSaveUncompressed()) {
      if (packet instanceof ByteBufHolder holder) {
        holder.content().resetReaderIndex();
      }
      ByteBuf uncompressed = factory.encodeSingle(packet, false, false);
      if (uncompressedPackets == null) {
        uncompressedPackets = factory.getPreparedPacketAllocator().directBuffer();
      }
      try {
        uncompressedPackets.writeBytes(uncompressed);
      } finally {
        uncompressed.release();
      }
    }

    if (factory.shouldReleaseReferenceCounted() && packet instanceof ReferenceCounted counted) {
      counted.release();
    }
    return this;
  }

  public PreparedPacket prepare(MinecraftPacket[] packets) {
    return packets == null ? this : prepare(Arrays.asList(packets));
  }

  public PreparedPacket prepare(List<? extends MinecraftPacket> packets) {
    if (packets != null) {
      packets.forEach(this::prepare);
    }
    return this;
  }

  public ByteBuf getPackets() {
    return packets;
  }

  public ByteBuf getUncompressedPackets() {
    return uncompressedPackets;
  }

  public PreparedPacket build() {
    if (packets != null) {
      packets.capacity(packets.readableBytes());
    }
    if (uncompressedPackets == null) {
      uncompressedPackets = packets;
    } else {
      uncompressedPackets.capacity(uncompressedPackets.readableBytes());
    }
    return this;
  }

  public void release() {
    if (disposed) {
      return;
    }
    disposed = true;
    if (packets != null && packets.refCnt() != 0) {
      packets.release();
    }
    if (uncompressedPackets != null && uncompressedPackets != packets && uncompressedPackets.refCnt() != 0) {
      uncompressedPackets.release();
    }
  }
}
