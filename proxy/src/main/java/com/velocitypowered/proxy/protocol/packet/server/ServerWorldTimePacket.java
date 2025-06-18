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
public class ServerWorldTimePacket implements MinecraftPacket {

    private final long worldAge;
    private final long timeOfDay;

    public ServerWorldTimePacket() {
        throw new IllegalStateException();
    }

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buf.writeLong(worldAge);
        buf.writeLong(timeOfDay);

        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
            buf.writeBoolean(false); // no ticking
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }

}
