package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServerEntityAnimationPacket implements MinecraftPacket {

    private int entityId;
    private Type type;

    @Override
    public void encode(ByteBuf bytebuf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        ProtocolUtils.writeVarInt(bytebuf, entityId);
        bytebuf.writeByte(type.ordinal());
    }

    @Override
    public void decode(ByteBuf bytebuf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }

    public enum Type {
        SWING_MAIN_ARM,
        HURT,
        WAKE_UP,
        // 1.9+?
        SWING_OFF_HAND, // Eat food on 1.7
        CRITICAL_HIT,
        MAGIC_CRITICAL_HIT;
        // unknown (102), crouch (104), uncrouch(105) only exist on 1.7 and unused here
    }
}
