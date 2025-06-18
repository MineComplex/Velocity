package com.velocitypowered.proxy.protocol.packet.client;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.ClientLimboSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.*;

@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientTeleportConfirmPacket implements MinecraftPacket {

    private int teleportId;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        teleportId = ProtocolUtils.readVarInt(buf);
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        if (handler instanceof ClientLimboSessionHandler client) {
            return client.handle(this);
        } else {
            return true;
        }
    }

    @Override
    public int expectedMaxLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        return 5;
    }

    @Override
    public int expectedMinLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        return 1;
    }

}
