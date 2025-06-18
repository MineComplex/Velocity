package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@ToString
@AllArgsConstructor
public class ServerTagsPacket implements MinecraftPacket {

    private final Map<String, Map<String, List<Integer>>> tags;

    private static void writeTagList(ByteBuf buf, Map<String, List<Integer>> tagList) {
        ProtocolUtils.writeVarInt(buf, tagList.size());
        tagList.forEach((tagId, blockList) -> {
            ProtocolUtils.writeString(buf, tagId);
            ProtocolUtils.writeVarInt(buf, blockList.size());
            blockList.forEach(blockId -> ProtocolUtils.writeVarInt(buf, blockId));
        });
    }

    public Map<String, Map<String, int[]>> toVelocityTags() {
        Map<String, Map<String, int[]>> newTags = new LinkedHashMap<>();
        for (Entry<String, Map<String, List<Integer>>> entry : tags.entrySet()) {
            Map<String, int[]> tagRegistry = new LinkedHashMap<>();

            for (Entry<String, List<Integer>> tagEntry : entry.getValue().entrySet()) {
                tagRegistry.put(tagEntry.getKey(),
                        tagEntry.getValue().stream().mapToInt(Integer::intValue).toArray());
            }

            newTags.put(entry.getKey(), tagRegistry);
        }

        return newTags;
    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        if (version.compareTo(ProtocolVersion.MINECRAFT_1_17) >= 0) {
            ProtocolUtils.writeVarInt(buf, tags.size());
            tags.forEach((tagType, tagList) -> {
                ProtocolUtils.writeString(buf, tagType);
                writeTagList(buf, tagList);
            });
        } else {
            writeTagList(buf, tags.get("minecraft:block"));
            writeTagList(buf, tags.get("minecraft:item"));
            writeTagList(buf, tags.get("minecraft:fluid"));
            if (version.compareTo(ProtocolVersion.MINECRAFT_1_14) >= 0) {
                writeTagList(buf, tags.get("minecraft:entity_type"));
            }
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }
}
