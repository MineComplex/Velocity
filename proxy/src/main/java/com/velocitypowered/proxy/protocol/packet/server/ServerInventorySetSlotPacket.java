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

package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.item.Item;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponentMap;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.checkerframework.checker.nullness.qual.Nullable;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerInventorySetSlotPacket implements MinecraftPacket {

  private int windowId;
  private int slot;
  private Item item;
  private int count;
  private int data;
  @Nullable
  private CompoundBinaryTag nbt;
  @Nullable
  private ItemComponentMap map;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    throw new IllegalStateException();
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_20_5)) {
      encodeModern(buf, protocolVersion);
    } else {
      encodeLegacy(buf, protocolVersion);
    }
  }

  public void encodeModern(ByteBuf buf, ProtocolVersion protocolVersion) {
    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
      ProtocolUtils.writeVarInt(buf, windowId);
    } else {
      buf.writeByte(windowId);
    }
    ProtocolUtils.writeVarInt(buf, 0);
    buf.writeShort(slot);

    int id = item.getId(protocolVersion);
    if (id == 0) {
      ProtocolUtils.writeVarInt(buf, 0);
    } else {
      ProtocolUtils.writeVarInt(buf, count);
      ProtocolUtils.writeVarInt(buf, id);

      if (map != null) {
        map.write(protocolVersion, buf);
      } else {
        ProtocolUtils.writeVarInt(buf, 0);
        ProtocolUtils.writeVarInt(buf, 0);
      }
    }
  }

  public void encodeLegacy(ByteBuf buf, ProtocolVersion protocolVersion) {
    buf.writeByte(windowId);

    if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_17_1)) {
      ProtocolUtils.writeVarInt(buf, 0); // State Id.
    }

    buf.writeShort(slot);
    int id = item.getId(protocolVersion);
    boolean present = id > 0;

    buf.writeBoolean(present);

    if (present) {
      ProtocolUtils.writeVarInt(buf, id);
      buf.writeByte(count);

      if (nbt == null) {
        buf.writeByte(0);
      } else {
        ProtocolUtils.writeBinaryTag(buf, protocolVersion, nbt);
      }
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return true;
  }

}
