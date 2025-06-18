package com.velocitypowered.proxy.protocol.packet.client;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"entityId", "hand"})
public class ClientAnimationPacket implements MinecraftPacket {

    private int entityId = -1;
    private int hand = MAIN_HAND;
    private LegacyAnimationType type = LegacyAnimationType.SWING_ARM;

    public static int MAIN_HAND = 0;

    @Override
    public void encode(ByteBuf bytebuf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decode(@NotNull ByteBuf bytebuf, ProtocolUtils.Direction direction, @NotNull ProtocolVersion version) {
        if (version.lessThan(ProtocolVersion.MINECRAFT_1_8)) {
            entityId = bytebuf.readInt();
            type = LegacyAnimationType.getById(bytebuf.readByte());
        } else if (version.greaterThan(ProtocolVersion.MINECRAFT_1_8)) {
            // Only 1.9+ clients have an offhand
            hand = ProtocolUtils.readVarInt(bytebuf);
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        handler.handleGeneric(this);
        return true;
    }

    @Getter
    public enum LegacyAnimationType {
        NO_ANIMATION,
        SWING_ARM,
        DAMAGE_ANIMATION,
        LEAVE_BED,
        EAT_FOOD,
        CRITICAL_EFFECT,
        MAGIC_CRITICAL_EFFECT,
        UNKNOWN(102),
        CROUCH(104),
        @SuppressWarnings("SpellCheckingInspection")
        UNCROUCH(105);

        private final int id;

        LegacyAnimationType(int id) {
            this.id = id;
        }

        LegacyAnimationType() {
            this.id = ordinal();
        }

        public static LegacyAnimationType getById(int id) {
            if (id >= 0 && id <= 7) {
                return LegacyAnimationType.values()[id];
            }
            return switch (id) {
                case 102 -> UNKNOWN;
                case 104 -> CROUCH;
                case 105 -> UNCROUCH;
                default -> throw new IllegalArgumentException("Unknown type with id: " + id);
            };
        }
    }
}
