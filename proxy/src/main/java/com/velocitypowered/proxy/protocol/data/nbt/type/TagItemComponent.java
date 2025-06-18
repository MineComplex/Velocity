package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.nbt.BinaryTag;

public class TagItemComponent extends ItemComponent<BinaryTag> {

    public TagItemComponent(String name) {
        super(name);
    }

    @Override
    public void write(ProtocolVersion version, ByteBuf buffer) {
        ProtocolUtils.writeBinaryTag(buffer, version, getValue());
    }
}
