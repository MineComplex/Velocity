package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServerMapDataPacket implements MinecraftPacket {

    private int mapId;
    private byte[] buffer;
    private int x, y;
    private int scaling;
    private boolean locked;

    @Override
    public void encode(@NotNull ByteBuf byteBuf, @NotNull ProtocolUtils.Direction direction, @NotNull ProtocolVersion protocolVersion) {
        ProtocolUtils.writeVarInt(byteBuf, mapId); // item damage

        if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_8)) {
            byteBuf.writeShort(buffer.length + 3);
            byteBuf.writeByte(0);
            byteBuf.writeByte(x);
            byteBuf.writeByte(y);
            byteBuf.writeBytes(buffer);
            return;
        }

        byteBuf.writeByte(scaling);

        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_9)
                && protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_17)) {
            byteBuf.writeBoolean(false); // no icon
        }

        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_14)) {
            byteBuf.writeBoolean(locked);
        }

        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_17)) {
            byteBuf.writeBoolean(false); // no icon
        } else {
            ProtocolUtils.writeVarInt(byteBuf, 0); // no icon
        }

        byteBuf.writeByte(128); // rows
        byteBuf.writeByte(128); // columns
        byteBuf.writeByte(x);
        byteBuf.writeByte(y);

        ProtocolUtils.writeVarInt(byteBuf, buffer.length);
        byteBuf.writeBytes(buffer);
    }

    @Override
    public void decode(ByteBuf byteBuf, @NotNull ProtocolUtils.Direction direction,ProtocolVersion protocolVersion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }

}