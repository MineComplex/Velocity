/*
 * Copyright (C) 2025 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    if (entry == null || entry.getProfile() == null || !getPlayer().getUniqueId().equals(entry.getProfile().getId())) {
      return entry;
    }

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
