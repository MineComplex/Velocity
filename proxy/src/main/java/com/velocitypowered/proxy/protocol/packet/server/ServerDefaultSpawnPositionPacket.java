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
public class ServerDefaultSpawnPositionPacket implements MinecraftPacket {

    private final int posX;
    private final int posY;
    private final int posZ;
    private final float angle;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_8) < 0) {
            buf.writeInt(posX);
            buf.writeInt(posY);
            buf.writeInt(posZ);
        } else {
            long location;
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_14) < 0) {
                location = ((posX & 0x3FFFFFFL) << 38) | ((posY & 0xFFFL) << 26) | (posZ & 0x3FFFFFFL);
            } else {
                location = ((posX & 0x3FFFFFFL) << 38) | ((posZ & 0x3FFFFFFL) << 12) | (posY & 0xFFFL);
            }

            buf.writeLong(location);

            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_17) >= 0) {
                buf.writeFloat(angle);
            }
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }

}
