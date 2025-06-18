package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerRemoveEntitiesPacket implements MinecraftPacket {

    private int entityId;

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        // No idea why Mojang decided that this is a good idea, but whatever
        if (!protocolVersion.equals(ProtocolVersion.MINECRAFT_1_17)) {
            ProtocolUtils.writeVarInt(byteBuf, 1); // size
        }

        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_8)) {
            byteBuf.writeInt(entityId);
        } else {
            ProtocolUtils.writeVarInt(byteBuf, entityId);
        }
    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }
}
