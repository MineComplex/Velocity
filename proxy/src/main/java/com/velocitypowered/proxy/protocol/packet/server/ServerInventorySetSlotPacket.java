package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.item.Item;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponentMap;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.ToString;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.checkerframework.checker.nullness.qual.Nullable;

@ToString
@AllArgsConstructor
public class ServerInventorySetSlotPacket implements MinecraftPacket {

    private final int windowId;
    private final int slot;
    private final Item item;
    private final int count;
    private final int data;
    @Nullable
    private final CompoundBinaryTag nbt;
    @Nullable
    private final ItemComponentMap map;

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_20_5) >= 0) {
            encodeModern(buf, direction, protocolVersion);
        } else {
            encodeLegacy(buf, direction, protocolVersion);
        }
    }

    public void encodeModern(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
            ProtocolUtils.writeVarInt(buf, windowId);
        } else {
            buf.writeByte(windowId);
        }
        ProtocolUtils.writeVarInt(buf, 0);
        buf.writeShort(slot);

        int id = item.getId(protocolVersion);
        if (id == 0) {
            ProtocolUtils.writeVarInt(buf, 0);
        } else {
            ProtocolUtils.writeVarInt(buf, count);
            ProtocolUtils.writeVarInt(buf, id);

            if (map != null) {
                map.write(protocolVersion, buf);
            } else {
                ProtocolUtils.writeVarInt(buf, 0);
                ProtocolUtils.writeVarInt(buf, 0);
            }
        }
    }

    public void encodeLegacy(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buf.writeByte(windowId);

        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_17_1) >= 0) {
            ProtocolUtils.writeVarInt(buf, 0); // State Id.
        }

        buf.writeShort(slot);
        int id = item.getId(protocolVersion);
        boolean present = id > 0;

        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_13_2) >= 0) {
            buf.writeBoolean(present);
        }

        if (!present && protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_13_2) < 0) {
            buf.writeShort(-1);
        }

        if (present) {
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_13_2) < 0) {
                buf.writeShort(id);
            } else {
                ProtocolUtils.writeVarInt(buf, id);
            }
            buf.writeByte(count);
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) {
                buf.writeShort(data);
            }

            if (nbt == null) {
                if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_8) < 0) {
                    buf.writeShort(-1);
                } else {
                    buf.writeByte(0);
                }
            } else {
                ProtocolUtils.writeBinaryTag(buf, protocolVersion, nbt);
            }
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }

}
