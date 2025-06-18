package com.velocitypowered.proxy.protocol.prepared.handler;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.prepared.PreparedPacket;
import com.velocitypowered.proxy.protocol.prepared.PreparedPacketFactory;
import com.velocitypowered.proxy.protocol.prepared.dummy.DummyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

@Getter
@Setter
public class PreparedPacketEncoder extends ChannelOutboundHandlerAdapter {

    private final ProtocolVersion protocolVersion;
    private final Function<ByteBuf, ByteBuf> duplicateFunction;
    private PreparedPacketFactory factory;
    private boolean shouldSendUncompressed = true;

    public PreparedPacketEncoder(PreparedPacketFactory factory, ProtocolVersion protocolVersion, boolean shouldCopy) {
        this.factory = factory;
        this.protocolVersion = protocolVersion;
        this.duplicateFunction = shouldCopy ? ByteBuf::copy : ByteBuf::retainedDuplicate;
    }

    public PreparedPacketEncoder(PreparedPacketFactory factory, ProtocolVersion protocolVersion, Function<ByteBuf, ByteBuf> duplicateFunction) {
        this.factory = factory;
        this.protocolVersion = protocolVersion;
        this.duplicateFunction = duplicateFunction;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof DummyPacket)
            return;

        if (msg instanceof PreparedPacket preparedPacket) {
            if (factory.isCompatibilityMode()) {
                ByteBuf uncompressedPacket = preparedPacket.getUncompressedPackets(protocolVersion);
                if (uncompressedPacket == null)
                    throw new IllegalStateException("Failed to find compatible uncompressed packet for " + protocolVersion);
                ByteBuf uncompressed = uncompressedPacket.slice();
                while (uncompressed.isReadable()) {
                    ctx.write(uncompressed.readBytes(ProtocolUtils.readVarInt(uncompressed)), promise);
                    if (!promise.isVoid())
                        promise = ctx.newPromise();
                }
                return;
            }
            ByteBuf cachedPacket = isSendUncompressed() ? preparedPacket.getUncompressedPackets(protocolVersion) : preparedPacket.getPackets(protocolVersion);
            if (cachedPacket == null)
                throw new IllegalStateException("Current PreparedPacket is not prepared for " + protocolVersion);
            ctx.write(duplicateFunction.apply(cachedPacket), promise);
        } else if (msg instanceof MinecraftPacket && !factory.isCompatibilityMode()) {
            if (isSendUncompressed()) {
                ctx.write(factory.encodeSingle((MinecraftPacket) msg, protocolVersion, false, ctx.alloc()), promise);
            } else {
                ctx.write(factory.encodeSingle((MinecraftPacket) msg, protocolVersion, ctx.alloc()), promise);
            }
        } else {
            ctx.write(msg, promise);
        }
    }

    public boolean isSendUncompressed() {
        return factory.shouldSaveUncompressed() && shouldSendUncompressed;
    }
}
