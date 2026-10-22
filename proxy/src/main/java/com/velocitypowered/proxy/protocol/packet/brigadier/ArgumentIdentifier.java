/*
 * Copyright (C) 2022-2023 Velocity Contributors
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

package com.velocitypowered.proxy.protocol.packet.brigadier;

import com.google.common.base.Preconditions;

public record ArgumentIdentifier(String identifier, int id) {

  public ArgumentIdentifier {
    Preconditions.checkNotNull(identifier);
  }

  public static ArgumentIdentifier id(String identifier, int id) {
    return new ArgumentIdentifier(identifier, id);
  }

  public String getIdentifier() {
    return identifier;
  }

  public int getId() {
    return id;
  }
}
