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
public class ServerExperiencePacket implements MinecraftPacket {

    private final float expBar;
    private final int level;
    private final int totalExp;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buf.writeFloat(expBar);
        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_8) < 0) {
            buf.writeShort(level);
            buf.writeShort(totalExp);
        } else {
            ProtocolUtils.writeVarInt(buf, level);
            ProtocolUtils.writeVarInt(buf, totalExp);
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }

}
