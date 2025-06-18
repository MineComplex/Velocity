package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

public class ComponentItemComponent extends ItemComponent<Component> {

    public ComponentItemComponent(String name) {
        super(name);
    }

    @Override
    public void write(ProtocolVersion version, ByteBuf buffer) {
        new ComponentHolder(version, getValue()).write(buffer);
    }
}
