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

import static com.velocitypowered.api.network.ProtocolVersion.*;

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
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 8163;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_7)) {
      return 8173;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_11)) {
      return 9250;
    }
    return 9451;
  }, protocolVersion -> (double) 0.75f),
  TRAPDOOR(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 11293;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_7)) {
      return 11303;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_11)) {
      return 12380;
    }
    return 12582;
  }, protocolVersion -> 0.1875),
  END_PORTAL_FRAME(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 8185;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_7)) {
      return 8195;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_11)) {
      return 9272;
    }
    return 9473;
  }, protocolVersion -> 0.8125),
  DAYLIGHT_SENSOR(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_2)) {
      return 9462;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 10006;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_7)) {
      return 10016;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_11)) {
      return 11093;
    }
    return 11295;
  }, protocolVersion -> 0.375),
  COBBLESTONE_WALL(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 8696;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_7)) {
      return 8706;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_11)) {
      return 9783;
    }
    return 9984;
  }, protocolVersion -> 1.5),
  STONE_SLABS(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 12110;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_7)) {
      return 12120;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_11)) {
      return 13197;
    }
    return 13399;
  }, protocolVersion -> 0.5),
  WHITE_CARPET(protocolVersion -> {
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_4)) {
      return 11607;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_7)) {
      return 11617;
    }
    if (protocolVersion.noGreaterThan(MINECRAFT_1_21_11)) {
      return 12694;
    }
    return 12896;
  }, protocolVersion -> 0.0625);

  private final Function<ProtocolVersion, Integer> id;
  private final Function<ProtocolVersion, Double> blockHeight;
}
