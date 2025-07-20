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

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.natives.NativeSetupException;
import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.encryption.JavaVelocityCipher;
import com.velocitypowered.natives.util.BufferPreference;
import com.velocitypowered.natives.util.Natives;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.netty.MinecraftCompressorAndLengthEncoder;
import com.velocitypowered.proxy.protocol.prepared.dummy.DummyChannelHandlerContext;
import com.velocitypowered.proxy.protocol.prepared.handler.CompressionEventHandler;
import com.velocitypowered.proxy.protocol.prepared.handler.PreparedPacketEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCounted;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PreparedPacketFactory {

  public static final String PREPARED_ENCODER = "fastprepare-encoder";
  public static final String COMPRESSION_HANDLER = "fastprepare-compression-handler";
  private static final boolean DIRECT_BYTEBUF_PREFERRED_FOR_COMPRESSOR;
  private static final boolean JAVA_CIPHER;

  static {
    VelocityCompressor compressor = Natives.compress.get().create(1);
    try {
      BufferPreference bufferType = compressor.preferredBufferType();
      DIRECT_BYTEBUF_PREFERRED_FOR_COMPRESSOR =
          (bufferType.equals(BufferPreference.DIRECT_PREFERRED) || bufferType.equals(BufferPreference.DIRECT_REQUIRED));
      compressor.close();
    } catch (Throwable throwable) {
      if (compressor != null) {
        try {
          compressor.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }
      }
      throw throwable;
    }
    JAVA_CIPHER = (Natives.cipher.get() == JavaVelocityCipher.FACTORY);
  }

  private final Set<StateRegistry> stateRegistries = ConcurrentHashMap.newKeySet();
  private final PreparedPacketConstructor constructor;
  private final boolean releaseReferenceCounted;
  private final ByteBufAllocator preparedPacketAllocator;
  private final ChannelHandlerContext dummyContext;
  private final Map<Thread, MinecraftCompressorAndLengthEncoder> compressionEncoder;
  private boolean enableCompression;
  private int compressionThreshold;
  private int compressionLevel;
  private boolean saveUncompressed;
  private boolean compatibilityMode;

  public PreparedPacketFactory(PreparedPacketConstructor constructor, StateRegistry stateRegistry, boolean enableCompression, int compressionLevel,
                               int compressionThreshold, boolean saveUncompressed, boolean releaseReferenceCounted, boolean compatibilityMode) {
    this(constructor, stateRegistry, enableCompression, compressionLevel, compressionThreshold, saveUncompressed, releaseReferenceCounted,
        compatibilityMode, new PooledByteBufAllocator());
  }

  public PreparedPacketFactory(PreparedPacketConstructor constructor, StateRegistry stateRegistry, boolean enableCompression, int compressionLevel,
                               int compressionThreshold, boolean saveUncompressed, boolean releaseReferenceCounted, boolean compatibilityMode,
                               ByteBufAllocator preparedPacketAllocator) {
    this(constructor, Collections.singleton(stateRegistry), enableCompression, compressionLevel, compressionThreshold, saveUncompressed,
        releaseReferenceCounted, compatibilityMode, preparedPacketAllocator);
  }

  public PreparedPacketFactory(PreparedPacketConstructor constructor, Collection<StateRegistry> stateRegistries, boolean enableCompression,
                               int compressionLevel, int compressionThreshold, boolean saveUncompressed, boolean releaseReferenceCounted,
                               boolean compatibilityMode, ByteBufAllocator preparedPacketAllocator) {
    this.constructor = constructor;
    this.stateRegistries.addAll(stateRegistries);
    this.compressionEncoder = Collections.synchronizedMap(new HashMap<>());
    updateCompressor(enableCompression, compressionLevel, compressionThreshold, saveUncompressed, compatibilityMode);
    this.releaseReferenceCounted = releaseReferenceCounted;
    this.preparedPacketAllocator = preparedPacketAllocator;
    this.dummyContext = new DummyChannelHandlerContext(preparedPacketAllocator);
  }

  public void updateCompressor(boolean enableCompression, int compressionLevel, int compressionThreshold, boolean saveUncompressed,
                               boolean compatibilityMode) {
    this.enableCompression = enableCompression;
    this.compressionLevel = compressionLevel;
    this.compressionThreshold = compressionThreshold;
    this.saveUncompressed = (saveUncompressed && enableCompression);
    this.compatibilityMode = compatibilityMode;
  }

  public void releaseThread(Thread thread) {
    if (compressionEncoder.containsKey(thread)) {
      try {
        compressionEncoder.remove(thread).handlerRemoved(dummyContext);
      } catch (Exception e) {
        throw new NativeSetupException(e);
      }
    }
  }

  private MinecraftCompressorAndLengthEncoder getThreadLocalCompressionEncoder() {
    return compressionEncoder.computeIfAbsent(Thread.currentThread(),
        key -> new MinecraftCompressorAndLengthEncoder(compressionThreshold, Natives.compress.get().create(compressionLevel)));
  }

  public PreparedPacket createPreparedPacket(ProtocolVersion minVersion, ProtocolVersion maxVersion) {
    return constructor.construct(minVersion, maxVersion, this);
  }

  public void encodeId(MinecraftPacket packet, ByteBuf out, ProtocolVersion version) {
    int packetId = Integer.MIN_VALUE;
    for (StateRegistry stateRegistry : stateRegistries) {
      StateRegistry.PacketRegistry packetRegistry = stateRegistry.clientbound;
      StateRegistry.PacketRegistry.ProtocolRegistry protocolRegistry = packetRegistry.getProtocolRegistry(version);
      packetId = protocolRegistry.packetClassToId.getInt(packet.getClass());
      if (packetId != Integer.MIN_VALUE) {
        break;
      }
    }
    if (packetId == Integer.MIN_VALUE) {
      throw new IllegalArgumentException(String.format("Unable to find id for packet of type %s in clientbound protocol %s.", packet
          .getClass().getName(), version));
    }
    ProtocolUtils.writeVarInt(out, packetId);
    packet.encode(out, ProtocolUtils.Direction.CLIENTBOUND, version);
  }

  public ByteBuf compress(ByteBuf packetData, boolean enableCompression) {
    ByteBuf networkPacket;
    try {
      if (enableCompression) {
        MinecraftCompressorAndLengthEncoder encoder = getThreadLocalCompressionEncoder();
        networkPacket = encoder.allocateBuffer(dummyContext, packetData, false);
        encoder.encode(dummyContext, packetData, networkPacket);
      } else {
        int capacity = ProtocolUtils.varIntBytes(packetData.readableBytes()) + packetData.readableBytes();
        networkPacket = JAVA_CIPHER ? preparedPacketAllocator.heapBuffer(capacity) : preparedPacketAllocator.directBuffer(capacity);
        ProtocolUtils.writeVarInt(networkPacket, packetData.readableBytes());
        networkPacket.writeBytes(packetData);
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
    packetData.release();
    return networkPacket;
  }

  public ByteBuf encodeSingle(MinecraftPacket packet, ProtocolVersion version) {
    return encodeSingle(packet, version, enableCompression);
  }

  public ByteBuf encodeSingle(MinecraftPacket packet, ProtocolVersion version, ByteBufAllocator alloc) {
    return encodeSingle(packet, version, enableCompression, releaseReferenceCounted, alloc);
  }

  public ByteBuf encodeSingle(MinecraftPacket packet, ProtocolVersion version, boolean enableCompression) {
    return encodeSingle(packet, version, enableCompression, releaseReferenceCounted, preparedPacketAllocator);
  }

  public ByteBuf encodeSingle(MinecraftPacket packet, ProtocolVersion version, boolean enableCompression, boolean releaseReferenceCounted) {
    return encodeSingle(packet, version, enableCompression, releaseReferenceCounted, preparedPacketAllocator);
  }

  public ByteBuf encodeSingle(MinecraftPacket packet, ProtocolVersion version, boolean enableCompression, ByteBufAllocator alloc) {
    return encodeSingle(packet, version, enableCompression, releaseReferenceCounted, alloc);
  }

  public ByteBuf encodeSingle(MinecraftPacket packet, ProtocolVersion version, boolean enableCompression, boolean releaseReferenceCounted,
                              ByteBufAllocator alloc) {
    ByteBuf packetData;
    if (enableCompression) {
      packetData = DIRECT_BYTEBUF_PREFERRED_FOR_COMPRESSOR ? alloc.directBuffer() : alloc.buffer();
    } else {
      packetData = alloc.directBuffer();
    }
    encodeId(packet, packetData, version);
    if (releaseReferenceCounted && packet instanceof ReferenceCounted referenceCounted) {
      referenceCounted.release();
    }
    return compress(packetData, enableCompression);
  }

  public void inject(Player player, MinecraftConnection connection, ChannelPipeline pipeline) {
    inject(player.isOnlineMode(), connection, pipeline);
  }

  public void inject(boolean onlineMode, MinecraftConnection connection, ChannelPipeline pipeline) {
    pipeline.addAfter("minecraft-encoder", PREPARED_ENCODER, new PreparedPacketEncoder(this, connection
        .getProtocolVersion(), onlineMode));
    pipeline.addFirst(COMPRESSION_HANDLER, new CompressionEventHandler(this));
  }

  public void replace(ChannelPipeline pipeline) {
    pipeline.get(PreparedPacketEncoder.class).setFactory(this);
  }

  public void setShouldSendUncompressed(ChannelPipeline pipeline, boolean shouldSendUncompressed) {
    pipeline.get(PreparedPacketEncoder.class).setShouldSendUncompressed(shouldSendUncompressed);
  }

  public void deject(ChannelPipeline pipeline) {
    if (pipeline.names().contains(PREPARED_ENCODER)) {
      pipeline.remove(PreparedPacketEncoder.class);
      pipeline.remove(CompressionEventHandler.class);
    }
  }

  public boolean isCompressionEnabled() {
    return enableCompression;
  }

  public boolean shouldSaveUncompressed() {
    return saveUncompressed;
  }

  public boolean shouldReleaseReferenceCounted() {
    return releaseReferenceCounted;
  }

  public void addStateRegistries(Collection<StateRegistry> stateRegistries) {
    this.stateRegistries.addAll(stateRegistries);
  }

  public void addStateRegistry(StateRegistry stateRegistry) {
    stateRegistries.add(stateRegistry);
  }
}
