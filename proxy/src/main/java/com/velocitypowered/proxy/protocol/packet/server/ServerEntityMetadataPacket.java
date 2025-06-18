package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.entity.EntityMetadata;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class ServerEntityMetadataPacket implements MinecraftPacket {

    private final int entityId;
    private final Function<ProtocolVersion, EntityMetadata> metadata;

    public ServerEntityMetadataPacket(int entityId, EntityMetadata metadata) {
        this(entityId, protocolVersion -> metadata);
    }

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_7_6) <= 0) {
            buf.writeInt(entityId);
        } else {
            ProtocolUtils.writeVarInt(buf, entityId);
        }
        metadata.apply(protocolVersion).encode(buf, protocolVersion);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }
}
