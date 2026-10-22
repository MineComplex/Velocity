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
  ENCHANTMENT_TABLE(protocolVersion -> 11185, protocolVersion -> (double) 0.75f),
  TRAPDOOR(protocolVersion -> 14429, protocolVersion -> 0.1875),
  END_PORTAL_FRAME(protocolVersion -> 11207, protocolVersion -> 0.8125),
  DAYLIGHT_SENSOR(protocolVersion -> 13062, protocolVersion -> 0.375),
  COBBLESTONE_WALL(protocolVersion -> 11726, protocolVersion -> 1.5),
  STONE_SLABS(protocolVersion -> 15252, protocolVersion -> 0.5),
  WHITE_CARPET(protocolVersion -> 14743, protocolVersion -> 0.0625);

  private final Function<ProtocolVersion, Integer> id;
  private final Function<ProtocolVersion, Double> blockHeight;
}
