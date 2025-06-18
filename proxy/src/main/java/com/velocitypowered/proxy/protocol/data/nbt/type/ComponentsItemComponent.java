package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

import java.util.List;

public class ComponentsItemComponent extends ItemComponent<List<Component>> {

    public ComponentsItemComponent(String name) {
        super(name);
    }

    @Override
    public void write(ProtocolVersion version, ByteBuf buffer) {
        ProtocolUtils.writeVarInt(buffer, getValue().size());
        for (Component component : getValue()) {
            new ComponentHolder(version, component).write(buffer);
        }
    }
}
