package com.velocitypowered.proxy.protocol.data.nbt.type;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponent;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;

import java.util.List;

public class EnchantmentsItemComponent extends ItemComponent<Pair<List<Pair<Integer, Integer>>, Boolean>> {

    public EnchantmentsItemComponent(String name) {
        super(name);
    }

    @Override
    public void write(ProtocolVersion version, ByteBuf buffer) {
        ProtocolUtils.writeVarInt(buffer, getValue().left().size());
        for (Pair<Integer, Integer> enchantment : getValue().left()) {
            ProtocolUtils.writeVarInt(buffer, enchantment.left());
            ProtocolUtils.writeVarInt(buffer, enchantment.right());
        }
        buffer.writeBoolean(getValue().right());
    }
}
