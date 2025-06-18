package com.velocitypowered.proxy.protocol.data.nbt;

import com.velocitypowered.api.network.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class ItemComponent<T> {

    private final String name;
    private T value;

    public abstract void write(ProtocolVersion version, ByteBuf buffer);

    public ItemComponent<T> setValue(T value) {
        this.value = value;
        return this;
    }

}
