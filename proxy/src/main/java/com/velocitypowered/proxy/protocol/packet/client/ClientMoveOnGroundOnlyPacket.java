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
public class ClientMoveOnGroundOnlyPacket implements MinecraftPacket {

    private boolean onGround;
    private boolean collideHorizontally;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
            onGround = buf.readBoolean();
        } else {
            int flags = buf.readUnsignedByte();
            onGround = (flags & 1) != 0;
            collideHorizontally = (flags & 2) != 0;
        }
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
        return 1;
    }

    @Override
    public int expectedMinLength(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        return 1;
    }

}
