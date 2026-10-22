/*
 * Copyright (C) 2018-2023 Velocity Contributors
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

package com.velocitypowered.proxy.util;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.proxy.protocol.util.PluginMessageUtil;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Registry for channels recognized by the proxy.
 */
public class VelocityChannelRegistrar implements ChannelRegistrar {

  private final Map<String, ChannelIdentifier> identifierMap = new ConcurrentHashMap<>();

  @Override
  public void register(ChannelIdentifier... identifiers) {
    for (ChannelIdentifier identifier : identifiers) {
      Preconditions.checkArgument(identifier instanceof LegacyChannelIdentifier
          || identifier instanceof MinecraftChannelIdentifier, "identifier is unknown");
    }

    for (ChannelIdentifier identifier : identifiers) {
      if (identifier instanceof MinecraftChannelIdentifier) {
        identifierMap.put(identifier.getId(), identifier);
      } else {
        String rewritten = PluginMessageUtil.transformLegacyToModernChannel(identifier.getId());
        identifierMap.put(identifier.getId(), identifier);
        identifierMap.put(rewritten, identifier);
      }
    }
  }

  @Override
  public void unregister(ChannelIdentifier... identifiers) {
    for (ChannelIdentifier identifier : identifiers) {
      Preconditions.checkArgument(identifier instanceof LegacyChannelIdentifier
              || identifier instanceof MinecraftChannelIdentifier,
          "identifier is unknown");
    }

    for (ChannelIdentifier identifier : identifiers) {
      if (identifier instanceof MinecraftChannelIdentifier) {
        identifierMap.remove(identifier.getId());
      } else {
        String rewritten = PluginMessageUtil.transformLegacyToModernChannel(identifier.getId());
        identifierMap.remove(identifier.getId());
        identifierMap.remove(rewritten);
      }
    }
  }

  /**
   * Returns all channel IDs to announce to clients and servers.
   *
   * @return the channel IDs
   */
  public Collection<ChannelIdentifier> getModernChannelIds() {
    Collection<ChannelIdentifier> ids = new HashSet<>();
    for (ChannelIdentifier value : identifierMap.values()) {
      if (value instanceof MinecraftChannelIdentifier) {
        ids.add(value);
      } else {
        ids.add(MinecraftChannelIdentifier.from(PluginMessageUtil.transformLegacyToModernChannel(value.getId())));
      }
    }
    return ids;
  }

  public @Nullable ChannelIdentifier getFromId(String id) {
    return identifierMap.get(id);
  }
}
