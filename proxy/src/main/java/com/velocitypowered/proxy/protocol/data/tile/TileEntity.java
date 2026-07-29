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

package com.velocitypowered.proxy.protocol.data.tile;

import com.google.gson.internal.LinkedTreeMap;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.VelocityServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class TileEntity {

  private static final Map<String, TileEntity> MODERN_ID_MAP = new HashMap<>();

  @Getter
  private final String modernId;
  private final Map<TileEntityVersion, Short> versionIds = new EnumMap<>(TileEntityVersion.class);

  @SuppressWarnings("unchecked")
  public static void init() {
    LinkedTreeMap<String, LinkedTreeMap<String, String>> blockEntitiesMapping = VelocityServer.GENERAL_GSON.fromJson(
        new InputStreamReader(
            Objects.requireNonNull(TileEntity.class.getClassLoader().getResourceAsStream("mapping/blockentities_mapping.json")),
            StandardCharsets.UTF_8
        ),
        LinkedTreeMap.class
    );

    blockEntitiesMapping.forEach((modernId, protocols) -> {
      TileEntity TileEntity = new TileEntity(modernId);
      protocols.forEach((key, value) -> TileEntity.versionIds.put(TileEntityVersion.parse(key), Short.parseShort(value)));
      MODERN_ID_MAP.put(modernId, TileEntity);
    });
  }

  public static TileEntity fromModernId(String id) {
    return MODERN_ID_MAP.get(id);
  }

  public int getId(ProtocolVersion version) {
    return getId(TileEntityVersion.from(version));
  }

  public int getId(TileEntityVersion version) {
    Short result = versionIds.get(version);
    if (result == null) {
      throw new IllegalArgumentException("Item " + modernId + " does not exists on " + version);
    }
    return result;
  }

  public boolean isSupportedOn(ProtocolVersion version) {
    return versionIds.containsKey(TileEntityVersion.from(version));
  }

  public boolean isSupportedOn(TileEntityVersion version) {
    return versionIds.containsKey(version);
  }

  public Entry getEntry(int posX, int posY, int posZ, CompoundBinaryTag nbt) {
    return new Entry(posX, posY, posZ, nbt);
  }

  @Getter
  @AllArgsConstructor
  public class Entry {

    private final int posX;
    private final int posY;
    private final int posZ;
    private final CompoundBinaryTag nbt;

    public TileEntity getTileEntity() {
      return TileEntity.this;
    }

    public int getId(ProtocolVersion version) {
      return TileEntity.this.getId(version);
    }

    public int getId(TileEntityVersion version) {
      return TileEntity.this.getId(version);
    }

    public boolean isSupportedOn(ProtocolVersion version) {
      return TileEntity.this.isSupportedOn(version);
    }

    public boolean isSupportedOn(TileEntityVersion version) {
      return TileEntity.this.isSupportedOn(version);
    }
  }
}
