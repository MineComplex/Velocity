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
public class ServerViewPositionPacket implements MinecraftPacket {

    private final int posX;
    private final int posZ;

    public ServerViewPositionPacket() {
        throw new IllegalStateException();
    }

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        ProtocolUtils.writeVarInt(buf, posX);
        ProtocolUtils.writeVarInt(buf, posZ);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }
}
