package com.velocitypowered.proxy.protocol.data.world;

import com.velocitypowered.api.network.ProtocolVersion;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

public enum WorldVersion {
    LEGACY(EnumSet.range(ProtocolVersion.MINECRAFT_1_7_2, ProtocolVersion.MINECRAFT_1_12_2)),
    MINECRAFT_1_13(ProtocolVersion.MINECRAFT_1_13),
    MINECRAFT_1_13_2(ProtocolVersion.MINECRAFT_1_13_1, ProtocolVersion.MINECRAFT_1_13_2),
    MINECRAFT_1_14(EnumSet.range(ProtocolVersion.MINECRAFT_1_14, ProtocolVersion.MINECRAFT_1_14_4)),
    MINECRAFT_1_15(EnumSet.range(ProtocolVersion.MINECRAFT_1_15, ProtocolVersion.MINECRAFT_1_15_2)),
    MINECRAFT_1_16(ProtocolVersion.MINECRAFT_1_16, ProtocolVersion.MINECRAFT_1_16_1),
    MINECRAFT_1_16_2(EnumSet.range(ProtocolVersion.MINECRAFT_1_16_2, ProtocolVersion.MINECRAFT_1_16_4)),
    MINECRAFT_1_17(EnumSet.range(ProtocolVersion.MINECRAFT_1_17, ProtocolVersion.MINECRAFT_1_18_2)),
    MINECRAFT_1_19(EnumSet.range(ProtocolVersion.MINECRAFT_1_19, ProtocolVersion.MINECRAFT_1_19_1)),
    MINECRAFT_1_19_3(ProtocolVersion.MINECRAFT_1_19_3),
    MINECRAFT_1_19_4(EnumSet.range(ProtocolVersion.MINECRAFT_1_19_4, ProtocolVersion.MINECRAFT_1_20)),
    MINECRAFT_1_20(EnumSet.range(ProtocolVersion.MINECRAFT_1_20, ProtocolVersion.MINECRAFT_1_20_2)),
    MINECRAFT_1_20_3(ProtocolVersion.MINECRAFT_1_20_3),
    MINECRAFT_1_20_5(EnumSet.range(ProtocolVersion.MINECRAFT_1_20_5, ProtocolVersion.MINECRAFT_1_21)),
    MINECRAFT_1_21_2(EnumSet.range(ProtocolVersion.MINECRAFT_1_21_2, ProtocolVersion.MINECRAFT_1_21_2)),
    MINECRAFT_1_21_4(EnumSet.range(ProtocolVersion.MINECRAFT_1_21_4, ProtocolVersion.MAXIMUM_VERSION)),
    MINECRAFT_1_21_5(EnumSet.of(ProtocolVersion.MINECRAFT_1_21_5));

    private static final EnumMap<ProtocolVersion, WorldVersion> MC_VERSION_TO_ITEM_VERSIONS = new EnumMap<>(ProtocolVersion.class);

    static {
        for (WorldVersion version : WorldVersion.values()) {
            for (ProtocolVersion protocolVersion : version.getVersions()) {
                MC_VERSION_TO_ITEM_VERSIONS.put(protocolVersion, version);
            }
        }
    }

    private final Set<ProtocolVersion> versions;

    WorldVersion(ProtocolVersion... versions) {
        this.versions = EnumSet.copyOf(Arrays.asList(versions));
    }

    WorldVersion(Set<ProtocolVersion> versions) {
        this.versions = versions;
    }

    public static WorldVersion parse(String from) {
        return switch (from) {
            case "1.13" -> MINECRAFT_1_13;
            case "1.13.2" -> MINECRAFT_1_13_2;
            case "1.14" -> MINECRAFT_1_14;
            case "1.15" -> MINECRAFT_1_15;
            case "1.16" -> MINECRAFT_1_16;
            case "1.16.2" -> MINECRAFT_1_16_2;
            case "1.17" -> MINECRAFT_1_17;
            case "1.19" -> MINECRAFT_1_19;
            case "1.19.3" -> MINECRAFT_1_19_3;
            case "1.19.4" -> MINECRAFT_1_19_4;
            case "1.20" -> MINECRAFT_1_20;
            case "1.20.3" -> MINECRAFT_1_20_3;
            case "1.20.5" -> MINECRAFT_1_20_5;
            case "1.21.2" -> MINECRAFT_1_21_2;
            case "1.21.4" -> MINECRAFT_1_21_4;
            case "1.21.5" -> MINECRAFT_1_21_5;
            default -> LEGACY;
        };
    }

    public static WorldVersion from(ProtocolVersion protocolVersion) {
        return MC_VERSION_TO_ITEM_VERSIONS.get(protocolVersion);
    }

    public ProtocolVersion getMinSupportedVersion() {
        return this.versions.iterator().next();
    }

    public Set<ProtocolVersion> getVersions() {
        return this.versions;
    }
}
