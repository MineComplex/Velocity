package com.velocitypowered.proxy.tablist.rewriting;

import com.google.common.collect.Maps;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;

import java.util.Map;
import java.util.UUID;

public interface RewritingTabList {
    
    Map<Player, UUID> INITIAL_ID = Maps.newConcurrentMap();

    Player getPlayer();

    default TabListEntry rewriteEntry(TabListEntry entry) {
        if (entry == null || entry.getProfile() == null || !getPlayer().getUniqueId().equals(entry.getProfile().getId()))
            return entry;

        TabListEntry.Builder builder = TabListEntry.builder();
        builder.tabList(entry.getTabList());
        builder.profile(new GameProfile(rewriteUuid(entry.getProfile().getId()),
                entry.getProfile().getName(), entry.getProfile().getProperties()));
        builder.listed(entry.isListed());
        builder.latency(entry.getLatency());
        builder.gameMode(entry.getGameMode());
        entry.getDisplayNameComponent().ifPresent(builder::displayName);
        builder.chatSession(entry.getChatSession());
        builder.listOrder(entry.getListOrder());
        builder.showHat(entry.isShowHat());

        return builder.build();
    }

    default UUID rewriteUuid(UUID uuid) {
        if (getPlayer().getUniqueId().equals(uuid)) {
            return INITIAL_ID.getOrDefault(getPlayer(), uuid);
        }

        return uuid;
    }
}
