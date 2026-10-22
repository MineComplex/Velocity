/*
 * Copyright (C) 2026 Velocity Contributors
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.config.RegistrySyncPacket;
import com.velocitypowered.proxy.protocol.prepared.handler.PreparedPacketEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.zip.Inflater;
import org.junit.jupiter.api.Test;

class PreparedPacketTest {

  private PreparedPacketFactory factory(boolean compression) {
    return new PreparedPacketFactory(PreparedPacket::new, StateRegistry.CONFIG,
        compression, 1, 0, true, true, false);
  }

  @Test
  void compressedAndUncompressedCachesContainTheSamePacket() throws Exception {
    PreparedPacketFactory factory = factory(true);
    ByteBuf payload = Unpooled.buffer().writeLong(123456789L);
    RegistrySyncPacket packet = new RegistrySyncPacket();
    packet.replace(payload);
    PreparedPacket prepared = factory.createPreparedPacket().prepare(packet).build();
    try {
      assertEquals(0, payload.refCnt(), "The source packet must be released after both encodings");
      ByteBuf plain = prepared.getUncompressedPackets().duplicate();
      assertEquals(plain.readableBytes() - 1, ProtocolUtils.readVarInt(plain));
      byte[] expected = ByteBufUtil.getBytes(plain);
      assertEquals(123456789L, plain.getLong(plain.writerIndex() - Long.BYTES));

      ByteBuf compressed = prepared.getPackets().duplicate();
      int frameLength = ProtocolUtils.readVarInt(compressed);
      assertEquals(frameLength, compressed.readableBytes());
      int decodedLength = ProtocolUtils.readVarInt(compressed);
      assertEquals(expected.length, decodedLength);
      Inflater inflater = new Inflater();
      try {
        inflater.setInput(ByteBufUtil.getBytes(compressed));
        byte[] decoded = new byte[decodedLength];
        assertEquals(decodedLength, inflater.inflate(decoded));
        assertArrayEquals(expected, decoded);
      } finally {
        inflater.end();
      }
      assertNotSame(prepared.getPackets(), prepared.getUncompressedPackets());
    } finally {
      ByteBuf compressed = prepared.getPackets();
      final ByteBuf plain = prepared.getUncompressedPackets();
      prepared.release();
      prepared.release();
      assertEquals(0, compressed.refCnt());
      assertEquals(0, plain.refCnt());
      factory.releaseThread(Thread.currentThread());
    }
  }

  @Test
  void cachedPacketCanBeSentRepeatedlyWithoutConsumingTheCache() {
    PreparedPacketFactory factory = factory(false);
    RegistrySyncPacket packet = new RegistrySyncPacket();
    packet.replace(Unpooled.buffer().writeLong(42));
    PreparedPacket prepared = factory.createPreparedPacket().prepare(packet).build();
    EmbeddedChannel channel = new EmbeddedChannel(new PreparedPacketEncoder(factory, false));
    try {
      assertSame(prepared.getPackets(), prepared.getUncompressedPackets());
      int readerIndex = prepared.getPackets().readerIndex();
      channel.writeOutbound(prepared);
      channel.writeOutbound(prepared);
      ByteBuf first = channel.readOutbound();
      ByteBuf second = channel.readOutbound();
      try {
        assertEquals(first, second);
        first.skipBytes(first.readableBytes());
        assertFalse(first.isReadable());
        assertEquals(readerIndex, prepared.getPackets().readerIndex());
        assertEquals(readerIndex, second.readerIndex());
      } finally {
        first.release();
        second.release();
      }
    } finally {
      channel.finishAndReleaseAll();
      ByteBuf cached = prepared.getPackets();
      prepared.release();
      prepared.release();
      assertEquals(0, cached.refCnt());
    }
  }
}
