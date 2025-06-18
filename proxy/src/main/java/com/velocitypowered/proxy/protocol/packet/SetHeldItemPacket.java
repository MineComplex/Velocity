package com.velocitypowered.proxy.protocol.packet;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SetHeldItemPacket implements MinecraftPacket {

    private int slot;

    @Override
    public void encode(@NotNull ByteBuf bytebuf, ProtocolUtils.Direction direction, @NotNull ProtocolVersion protocolVersion) {
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_4)) {
            ProtocolUtils.writeVarInt(bytebuf, slot);
        } else {
            bytebuf.writeByte(slot);
        }
    }

    @Override
    public void decode(@NotNull ByteBuf bytebuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        slot = bytebuf.readShort();
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        handler.handleGeneric(this);
        return true;
    }
}
