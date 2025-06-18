package com.velocitypowered.proxy.protocol.packet.client;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.*;

import java.util.UUID;

@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientPlayerChatSessionPacket implements MinecraftPacket {

    private UUID holderId;
    private IdentifiedKey playerKey;

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        holderId = ProtocolUtils.readUuid(byteBuf);
        playerKey = ProtocolUtils.readPlayerKey(protocolVersion, byteBuf);
    }

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        ProtocolUtils.writeUuid(byteBuf, holderId);
        ProtocolUtils.writePlayerKey(byteBuf, playerKey);
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        // LimboAPI hook - skip server-side signature verification if enabled
        if (minecraftSessionHandler instanceof ClientPlaySessionHandler) {
            return true;
        }

        return false;
    }

}
