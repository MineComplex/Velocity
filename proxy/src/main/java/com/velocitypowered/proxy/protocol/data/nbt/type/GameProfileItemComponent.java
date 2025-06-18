package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class GameProfileItemComponent extends ItemComponent<GameProfile> {

    private static final UUID ZERO = new UUID(0, 0);

    public GameProfileItemComponent(String name) {
        super(name);
    }

    @Override
    public void write(ProtocolVersion version, ByteBuf buffer) {
        buffer.writeBoolean(!getValue().getName().isEmpty());
        if (!getValue().getName().isEmpty()) {
            ProtocolUtils.writeString(buffer, getValue().getName());
        }

        buffer.writeBoolean(!getValue().getId().equals(ZERO));
        if (!getValue().getId().equals(ZERO)) {
            ProtocolUtils.writeUuid(buffer, getValue().getId());
        }

        ProtocolUtils.writeProperties(buffer, getValue().getProperties());
    }
}
