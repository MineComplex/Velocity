package com.velocitypowered.proxy.protocol.data.entity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.item.Item;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponentMap;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.Map;

@AllArgsConstructor
public class EntityMetadata {

    private final Map<Byte, Entry> entries;

    public void encode(ByteBuf buf, ProtocolVersion protocolVersion) {
        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_8) <= 0) {
            entries.forEach((index, value) -> {
                buf.writeByte((index & 0x1F) | (value.getType(protocolVersion) << 5));
                value.encode(buf, protocolVersion);
            });
            buf.writeByte(0x7F);
        } else {
            entries.forEach((index, value) -> {
                buf.writeByte(index);
                ProtocolUtils.writeVarInt(buf, value.getType(protocolVersion));
                value.encode(buf, protocolVersion);
            });
            buf.writeByte(0xFF);
        }
    }

    public interface Entry {

        void encode(ByteBuf buf, ProtocolVersion protocolVersion);

        int getType(ProtocolVersion protocolVersion);
    }

    @AllArgsConstructor
    public static class SlotEntry implements Entry {

        private final boolean present;
        private final Item item;
        private final int count;
        private final int data;
        private final CompoundBinaryTag nbt;
        private final ItemComponentMap map;

        public SlotEntry(Item item, int count, int data, CompoundBinaryTag nbt, ItemComponentMap map) {
            this(true, item, count, data, nbt, map);
        }

        public SlotEntry() {
            this(false, null, 0, 0, null, null);
        }

        @Override
        public void encode(ByteBuf buf, ProtocolVersion protocolVersion) {
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_20_5) >= 0) {
                encodeModern(buf, protocolVersion);
            } else {
                encodeLegacy(buf, protocolVersion);
            }
        }

        public void encodeModern(ByteBuf buf, ProtocolVersion protocolVersion) {
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

        public void encodeLegacy(ByteBuf buf, ProtocolVersion protocolVersion) {
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_13_2) >= 0) {
                buf.writeBoolean(present);
            }

            if (!present && protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_13_2) < 0) {
                buf.writeShort(-1);
            }

            if (present) {
                if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_13_2) < 0) {
                    buf.writeShort(item.getId(protocolVersion));
                } else {
                    ProtocolUtils.writeVarInt(buf, item.getId(protocolVersion));
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
        public int getType(ProtocolVersion protocolVersion) {
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_12_2) <= 0) {
                return 5;
            } else if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_19_1) <= 0) {
                return 6;
            } else {
                return 7;
            }
        }
    }

    @AllArgsConstructor
    public static class VarIntEntry implements Entry {

        private final int value;

        @Override
        public void encode(ByteBuf buf, ProtocolVersion protocolVersion) {
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_8) <= 0) {
                buf.writeInt(value);
            } else {
                ProtocolUtils.writeVarInt(buf, value);
            }
        }

        @Override
        public int getType(ProtocolVersion protocolVersion) {
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_8) <= 0) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    @AllArgsConstructor
    public static class ByteEntry implements Entry {

        private final int value;

        @Override
        public void encode(ByteBuf buf, ProtocolVersion protocolVersion) {
            buf.writeByte(value);
        }

        @Override
        public int getType(ProtocolVersion protocolVersion) {
            return 0;
        }
    }
}
