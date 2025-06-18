package com.velocitypowered.proxy.protocol.packet.client;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.*;

@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientInteractPacket implements MinecraftPacket {

    private int entityId;
    private int type;
    private float targetX;
    private float targetY;
    private float targetZ;
    private int hand;
    private boolean sneaking;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_7_6) > 0) {
            entityId = ProtocolUtils.readVarInt(buf);
            type = ProtocolUtils.readVarInt(buf);
            if (type == 2) {
                targetX = buf.readFloat();
                targetY = buf.readFloat();
                targetZ = buf.readFloat();
            }
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_8) > 0) {
                if (type == 0 || type == 2) {
                    hand = ProtocolUtils.readVarInt(buf);
                }
                if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_15_2) > 0) {
                    sneaking = buf.readBoolean();
                }
            }
        } else {
            entityId = buf.readInt();
            type = buf.readByte();
        }
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        handler.handleGeneric(this);
        return true;
    }

}
