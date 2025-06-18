package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.nbt.BinaryTag;

public class EmptyItemComponent extends ItemComponent<BinaryTag> {

    public EmptyItemComponent(String name) {
        super(name);
    }

    @Override
    public void write(ProtocolVersion version, ByteBuf buffer) {

    }
}
