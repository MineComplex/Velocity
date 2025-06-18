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
public class ServerPlayerAbilitiesPacket implements MinecraftPacket {

    private final byte flags;
    private final float walkSpeed;
    private final float flySpeed;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buf.writeByte(flags);
        buf.writeFloat(flySpeed);
        buf.writeFloat(walkSpeed);
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }

}
