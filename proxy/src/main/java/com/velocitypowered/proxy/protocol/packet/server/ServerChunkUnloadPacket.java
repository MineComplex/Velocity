package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class ServerChunkUnloadPacket implements MinecraftPacket {

    private final int posX;
    private final int posZ;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buf.writeLong(protocolVersion.noGreaterThan(ProtocolVersion.MINECRAFT_1_20)
                ? (((long) posX & 0xFFFFFFFFL) << 32 | (long) posZ & 0xFFFFFFFFL)
                : (((long) posZ & 0xFFFFFFFFL) << 32 | (long) posX & 0xFFFFFFFFL) // >=1.20.2
        );
        if (protocolVersion.noGreaterThan(ProtocolVersion.MINECRAFT_1_8)) {
            // essentially, it's ChunkData, but written in a way that the chunk gets unloaded
            buf.writeBoolean(true);
            buf.writeShort(0);
            if (protocolVersion == ProtocolVersion.MINECRAFT_1_8) {
                ProtocolUtils.writeVarInt(buf, 0);
            } else {
                buf.writeShort(0);
                buf.writeInt(0);
            }
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        throw new IllegalStateException();
    }
}
