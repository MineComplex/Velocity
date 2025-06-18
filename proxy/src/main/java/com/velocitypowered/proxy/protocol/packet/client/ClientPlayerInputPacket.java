package com.velocitypowered.proxy.protocol.packet.client;

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
public class ClientPlayerInputPacket implements MinecraftPacket {

    private float sideways, forward;
    private boolean jump, sneak;

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
            byte mask = byteBuf.readByte();
            jump = (mask & 16) != 0;
            sneak = (mask & 32) != 0;
            return;
        }

        sideways = byteBuf.readFloat();
        forward = byteBuf.readFloat();

        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_8)) {
            jump = byteBuf.readBoolean();
            sneak = byteBuf.readBoolean();
        } else {
            byte flags = byteBuf.readByte();
            jump = (flags & 0x01) != 0;
            sneak = (flags & 0x02) != 0;
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        handler.handleGeneric(this);
        return true;
    }
}
