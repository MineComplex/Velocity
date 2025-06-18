package com.velocitypowered.proxy.protocol.data.nbt;

import com.google.common.collect.Lists;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ItemComponentMap {

    private final List<ItemComponent<?>> addedComponents = Lists.newArrayList();
    private final List<ItemComponent<?>> removedComponents = Lists.newArrayList();

    public <T> ItemComponentMap add(ProtocolVersion version, String name, T value) {
        addedComponents.add((ItemComponent<?>) ItemComponentManager.createComponent(version, name).setValue(value));
        return this;
    }

    public ItemComponentMap remove(ProtocolVersion version, String name) {
        removedComponents.add(ItemComponentManager.createComponent(version, name));
        return null;
    }

    public List<ItemComponent<?>> getAdded() {
        return addedComponents;
    }

    public List<ItemComponent<?>> getRemoved() {
        return removedComponents;
    }

    public void read(ProtocolVersion version, Object buffer) {
        // TODO: implement
        throw new UnsupportedOperationException("read");
    }

    public void write(ProtocolVersion version, Object buffer) {
        ByteBuf buf = (ByteBuf) buffer;

        ProtocolUtils.writeVarInt(buf, getAdded().size());
        ProtocolUtils.writeVarInt(buf, getRemoved().size());

        for (ItemComponent<?> component : addedComponents) {
            ProtocolUtils.writeVarInt(buf, ItemComponentManager.getId(component.getName(), version));
            component.write(version, buf);
        }

        for (ItemComponent<?> component : removedComponents) {
            ProtocolUtils.writeVarInt(buf, ItemComponentManager.getId(component.getName(), version));
        }
    }
}
