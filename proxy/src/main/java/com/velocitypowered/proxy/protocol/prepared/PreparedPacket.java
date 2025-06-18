package com.velocitypowered.proxy.protocol.prepared;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.ReferenceCounted;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class PreparedPacket {

    private final ByteBuf[] packets = new ByteBuf[ProtocolVersion.values().length];
    private final ProtocolVersion minVersion;
    private final ProtocolVersion maxVersion;
    private final PreparedPacketFactory factory;
    private ByteBuf[] uncompressedPackets;
    private boolean disposed;

    public PreparedPacket(ProtocolVersion minVersion, ProtocolVersion maxVersion, PreparedPacketFactory factory) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.factory = factory;
    }

    public <T> PreparedPacket prepare(T packet) {
        if (packet == null)
            return this;

        return prepare((version) -> packet, ProtocolVersion.MINIMUM_VERSION, ProtocolVersion.MAXIMUM_VERSION);
    }

    public <T> PreparedPacket prepare(T[] packets) {
        return prepare(Arrays.asList(packets));
    }

    public <T> PreparedPacket prepare(List<T> packets) {
        if (packets == null)
            return this;

        for (T packet : packets)
            prepare((version) -> packet, ProtocolVersion.MINIMUM_VERSION, ProtocolVersion.MAXIMUM_VERSION);

        return this;
    }

    public <T> PreparedPacket prepare(T packet, ProtocolVersion from) {
        if (packet == null)
            return this;

        return prepare((version) -> packet, from, ProtocolVersion.MAXIMUM_VERSION);
    }

    public <T> PreparedPacket prepare(T packet, ProtocolVersion from, ProtocolVersion to) {
        if (packet == null)
            return this;

        return prepare((version) -> packet, from, to);
    }

    public <T> PreparedPacket prepare(T[] packets, ProtocolVersion from) {
        return prepare(Arrays.asList(packets), from);
    }

    public <T> PreparedPacket prepare(T[] packets, ProtocolVersion from, ProtocolVersion to) {
        return prepare(Arrays.asList(packets), from, to);
    }

    public <T> PreparedPacket prepare(List<T> packets, ProtocolVersion from) {
        if (packets == null)
            return this;

        for (T packet : packets)
            prepare(packet, from);

        return this;
    }

    public <T> PreparedPacket prepare(List<T> packets, ProtocolVersion from, ProtocolVersion to) {
        if (packets == null)
            return this;

        for (T packet : packets)
            prepare(packet, from, to);

        return this;
    }

    public <T> PreparedPacket prepare(Function<ProtocolVersion, T> packet) {
        return prepare(packet, ProtocolVersion.MINIMUM_VERSION, ProtocolVersion.MAXIMUM_VERSION);
    }

    public <T> PreparedPacket prepare(Function<ProtocolVersion, T> packet, ProtocolVersion from) {
        return prepare(packet, from, ProtocolVersion.MAXIMUM_VERSION);
    }

    public <T> PreparedPacket prepare(Function<ProtocolVersion, T> packet, ProtocolVersion originalFrom, ProtocolVersion originalTo) {
        ProtocolVersion from = originalFrom.compareTo(minVersion) > 0 ? originalFrom : minVersion;
        ProtocolVersion to = originalTo.compareTo(maxVersion) < 0 ? originalTo : maxVersion;
        if (from.compareTo(to) > 0) {
            return this;
        }

        for (ProtocolVersion protocolVersion : EnumSet.range(from, to)) {
            T minecraftPacket = packet.apply(protocolVersion);
            Preconditions.checkArgument(minecraftPacket instanceof MinecraftPacket);
            MinecraftPacket castedMinecraftPacket = (MinecraftPacket) minecraftPacket;
            ByteBuf buf = factory.encodeSingle(castedMinecraftPacket, protocolVersion, factory.isCompressionEnabled(), false);
            int versionKey = protocolVersion.ordinal();
            if (packets[versionKey] == null) {
                packets[versionKey] = factory.getPreparedPacketAllocator().directBuffer();
            }

            packets[versionKey].writeBytes(buf);
            buf.release();

            if (factory.shouldSaveUncompressed()) {
                if (minecraftPacket instanceof ByteBufHolder byteBufHolder) {
                    byteBufHolder.content().resetReaderIndex();
                }

                ByteBuf buf2 = factory.encodeSingle(castedMinecraftPacket, protocolVersion, false, false);

                if (uncompressedPackets == null) {
                    uncompressedPackets = new ByteBuf[ProtocolVersion.values().length];
                }

                if (uncompressedPackets[versionKey] == null) {
                    uncompressedPackets[versionKey] = factory.getPreparedPacketAllocator().directBuffer();
                }

                uncompressedPackets[versionKey].writeBytes(buf2);
                buf2.release();
            }

            if (factory.shouldReleaseReferenceCounted() && minecraftPacket instanceof ReferenceCounted referenceCounted) {
                referenceCounted.release();
            }
        }

        return this;
    }

    public ByteBuf getPackets(ProtocolVersion version) {
        return packets[version.ordinal()];
    }

    public ByteBuf getUncompressedPackets(ProtocolVersion version) {
        return uncompressedPackets[version.ordinal()];
    }

    public PreparedPacket build() {
        if (uncompressedPackets == null)
            uncompressedPackets = packets;

        buildPacketArray(packets);
        buildPacketArray(uncompressedPackets);

        return this;
    }

    private void buildPacketArray(ByteBuf[] packetArray) {
        ByteBuf prevBuf = null;
        for (int i = 0, packetsLength = packetArray.length; i < packetsLength; i++) {
            ByteBuf buf = packetArray[i];
            if (buf != null) {
                if (buf == prevBuf) {
                    packetArray[i] = prevBuf;
                } else if (buf.equals(prevBuf)) {
                    buf.release();
                    packetArray[i] = prevBuf;
                } else {
                    packetArray[i] = buf.capacity(buf.readableBytes());
                    prevBuf = buf;
                }
            }
        }
    }

    public void release() {
        if (disposed)
            return;

        disposed = true;
        for (ByteBuf packet : packets) {
            if (packet != null) {
                if (packet.refCnt() != 0) {
                    packet.release();
                }
            }
        }

        if (uncompressedPackets != null && packets != uncompressedPackets) {
            for (ByteBuf packet : uncompressedPackets) {
                if (packet != null) {
                    if (packet.refCnt() != 0) {
                        packet.release();
                    }
                }
            }
        }
    }
}
