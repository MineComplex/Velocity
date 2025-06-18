package com.velocitypowered.proxy.protocol.data.tag;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.data.block.Block;
import com.velocitypowered.proxy.protocol.data.item.Item;
import com.velocitypowered.proxy.protocol.data.world.WorldVersion;
import com.velocitypowered.proxy.protocol.packet.server.ServerTagsPacket;
import lombok.experimental.UtilityClass;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class TagManager {

    private final Map<String, Integer> FLUIDS = new HashMap<>();
    private final Map<WorldVersion, ServerTagsPacket> VERSION_MAP = new EnumMap<>(WorldVersion.class);

    @SuppressWarnings("unchecked")
    public void init() {
        Gson gson = new Gson();
        LinkedTreeMap<String, String> fluids = gson.fromJson(
                new InputStreamReader(
                        Objects.requireNonNull(TagManager.class.getClassLoader().getResourceAsStream("mapping/fluids.json")),
                        StandardCharsets.UTF_8
                ),
                LinkedTreeMap.class
        );

        fluids.forEach((id, protocolId) -> FLUIDS.put(id, Integer.valueOf(protocolId)));

        LinkedTreeMap<String, LinkedTreeMap<String, List<String>>> tags = gson.fromJson(
                new InputStreamReader(
                        Objects.requireNonNull(TagManager.class.getClassLoader().getResourceAsStream("mapping/tags.json")),
                        StandardCharsets.UTF_8
                ),
                LinkedTreeMap.class
        );

        for (WorldVersion version : WorldVersion.values()) {
            VERSION_MAP.put(version, localGetTagsForVersion(tags, version));
        }
    }

    public ServerTagsPacket getUpdateTagsPacket(ProtocolVersion version) {
        return VERSION_MAP.get(WorldVersion.from(version));
    }

    public ServerTagsPacket getUpdateTagsPacket(WorldVersion version) {
        return VERSION_MAP.get(version);
    }

    private ServerTagsPacket localGetTagsForVersion(LinkedTreeMap<String, LinkedTreeMap<String, List<String>>> defaultTags,
                                                           WorldVersion version) {
        Map<String, Map<String, List<Integer>>> tags = new LinkedTreeMap<>();
        defaultTags.forEach((tagType, defaultTagList) -> {
            LinkedTreeMap<String, List<Integer>> tagList = new LinkedTreeMap<>();
            switch (tagType) {
                case "minecraft:block": {
                    defaultTagList.forEach((tagName, blockList) ->
                            tagList.put(tagName, blockList.stream()
                                    .map(e -> Block.fromModernId(e, Map.of()))
                                    .filter(e -> e.isSupportedOn(version))
                                    .map(e -> (int) e.getBlockId(version))
                                    .collect(Collectors.toList())));
                    break;
                }
                case "minecraft:fluid": {
                    defaultTagList.forEach((tagName, fluidList) ->
                            tagList.put(tagName, fluidList.stream().map(FLUIDS::get).collect(Collectors.toList())));
                    break;
                }
                case "minecraft:item": {
                    defaultTagList.forEach((tagName, itemList) ->
                            tagList.put(tagName, itemList.stream()
                                    .map(Item::fromModernId)
                                    .filter(item -> item.isSupportedOn(version))
                                    .map(item -> (int) item.getId(version))
                                    .collect(Collectors.toList())));
                    break;
                }
                default: {
                    defaultTagList.forEach((tagName, entryList) -> {
                        if (!entryList.isEmpty()) {
                            throw new IllegalStateException("The " + tagType + " tag type is not supported yet.");
                        }

                        tagList.put(tagName, List.of());
                    });
                    break;
                }
            }

            tags.put(tagType, tagList);
        });

        return new ServerTagsPacket(tags);
    }
}
