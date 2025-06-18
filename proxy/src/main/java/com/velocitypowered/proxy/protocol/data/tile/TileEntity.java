package com.velocitypowered.proxy.protocol.data.tile;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.velocitypowered.api.network.ProtocolVersion;
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

    private static final Gson GSON = new Gson();

    private static final Map<String, TileEntity> MODERN_ID_MAP = new HashMap<>();

    @Getter
    private final String modernId;
    private final Map<TileEntityVersion, Integer> versionIds = new EnumMap<>(TileEntityVersion.class);

    @SuppressWarnings("unchecked")
    public static void init() {
        LinkedTreeMap<String, LinkedTreeMap<String, String>> blockEntitiesMapping = GSON.fromJson(
                new InputStreamReader(
                        Objects.requireNonNull(TileEntity.class.getClassLoader().getResourceAsStream("mapping/blockentities_mapping.json")), StandardCharsets.UTF_8
                ),
                LinkedTreeMap.class
        );

        blockEntitiesMapping.forEach((modernId, protocols) -> {
            TileEntity TileEntity = new TileEntity(modernId);
            protocols.forEach((key, value) -> TileEntity.versionIds.put(TileEntityVersion.parse(key), Integer.parseInt(value)));
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
        return versionIds.get(version);
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
