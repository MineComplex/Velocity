package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class StringsItemComponent extends ItemComponent<List<String>> {

    public StringsItemComponent(String name) {
        super(name);
    }

    @Override
    public void write(ProtocolVersion version, ByteBuf buffer) {
        ProtocolUtils.writeVarInt(buffer, getValue().size());
        for (String string : getValue()) {
            ProtocolUtils.writeString(buffer, string);
        }
    }
}
