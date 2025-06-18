package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;

public class DyedColorItemComponent extends ItemComponent<Pair<Integer, Boolean>> {

    public DyedColorItemComponent(String name) {
        super(name);
    }

    @Override
    public void write(ProtocolVersion version, ByteBuf buffer) {
        buffer.writeInt(getValue().left());
        buffer.writeBoolean(getValue().right());
    }
}
