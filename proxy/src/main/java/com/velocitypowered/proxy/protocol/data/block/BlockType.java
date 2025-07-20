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

package com.velocitypowered.proxy.protocol.data.block;

import com.velocitypowered.api.network.ProtocolVersion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_16_4;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_18_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_19_1;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_19_3;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_19_4;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_20;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_21;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_21_2;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_21_4;

// TODO: load mappings from a separate file
@Getter
@SuppressWarnings("unused")
@RequiredArgsConstructor
public enum BlockType {
  // Useful resources:
  // - https://github.com/PrismarineJS/minecraft-data/blob/master/data/pc/
  // - https://pokechu22.github.io/Burger/
  // - https://github.com/ViaVersion/Mappings/tree/main/mappings
  //STONE(protocolVersion -> 1, 1),
  ENCHANTMENT_TABLE(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_16_4)) {
      return 5136;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_18_2)) {
      return 5333;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_1)) {
      return 5719;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
      return 7159;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_4)) {
      return 7385;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
      return 7389;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_2)) {
      return 7619;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 8163;
    }
    return 8173;
  }, protocolVersion -> (double) 0.75f),
  TRAPDOOR(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_16_4)) {
      return 7556;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_18_2)) {
      return 7802;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_1)) {
      return 8293;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
      return 9937;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_4)) {
      return 10269;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_20)) {
      return 10273;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
      return 10414;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_2)) {
      return 10749;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 11293;
    }
    return 11303;
  }, protocolVersion -> 0.1875),
  END_PORTAL_FRAME(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_16_4)) {
      return 5157;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_18_2)) {
      return 5358;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_1)) {
      return 5744;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
      return 7184;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_4)) {
      return 7410;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
      return 7414;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_2)) {
      return 7644;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 8185;
    }
    return 8195;
  }, protocolVersion -> 0.8125),
  DAYLIGHT_SENSOR(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_16_4)) {
      return 6698;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_18_2)) {
      return 6916;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_1)) {
      return 7327;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
      return 8811;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_4)) {
      return 9063;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_20)) {
      return 9067;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
      return 9207;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_2)) {
      return 9462;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 10006;
    }
    return 10016;
  }, protocolVersion -> 0.375),
  COBBLESTONE_WALL(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_16_4)) {
      return 5664;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_18_2)) {
      return 5866;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_1)) {
      return 6252;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
      return 7692;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_4)) {
      return 7918;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
      return 7922;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_2)) {
      return 8152;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 8696;
    }
    return 8706;
  }, protocolVersion -> 1.5),
  STONE_SLABS(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_16_4)) {
      return 8349;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_18_2)) {
      return 8595;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_1)) {
      return 9092;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
      return 10748;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_4)) {
      return 11086;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_20)) {
      return 11090;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
      return 11231;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_2)) {
      return 11566;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 12110;
    }
    return 12120;
  }, protocolVersion -> 0.5),
  WHITE_CARPET(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_16_4)) {
      return 7870;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_18_2)) {
      return 8116;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_1)) {
      return 8607;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_3)) {
      return 10251;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_19_4)) {
      return 10583;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_20)) {
      return 10587;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21)) {
      return 10728;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_2)) {
      return 11063;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 11607;
    }
    return 11617;
  }, protocolVersion -> 0.0625);

  private final Function<ProtocolVersion, Integer> id;
  private final Function<ProtocolVersion, Double> blockHeight;
}
