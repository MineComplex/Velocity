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

package com.velocitypowered.proxy.protocol.data.entity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.data.item.Item;
import com.velocitypowered.proxy.protocol.data.nbt.ItemComponentMap;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.Map;

@AllArgsConstructor
public class EntityMetadata {

  private final Map<Byte, Entry> entries;

  public void encode(ByteBuf buf, ProtocolVersion protocolVersion) {
    entries.forEach((index, value) -> {
      buf.writeByte(index);
      ProtocolUtils.writeVarInt(buf, value.getType(protocolVersion));
      value.encode(buf, protocolVersion);
    });
    buf.writeByte(0xFF);
  }

  public interface Entry {

    void encode(ByteBuf buf, ProtocolVersion protocolVersion);

    int getType(ProtocolVersion protocolVersion);
  }

  @AllArgsConstructor
  public static class SlotEntry implements Entry {

    private final boolean present;
    private final Item item;
    private final int count;
    private final CompoundBinaryTag nbt;
    private final ItemComponentMap map;

    public SlotEntry(Item item, int count, CompoundBinaryTag nbt, ItemComponentMap map) {
      this(true, item, count, nbt, map);
    }

    public SlotEntry() {
      this(false, null, 0, null, null);
    }

    @Override
    public void encode(ByteBuf buf, ProtocolVersion protocolVersion) {
      if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_20_5)) {
        encodeModern(buf, protocolVersion);
      } else {
        encodeLegacy(buf, protocolVersion);
      }
    }

    public void encodeModern(ByteBuf buf, ProtocolVersion protocolVersion) {
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
      buf.writeBoolean(present);

      if (present) {
        ProtocolUtils.writeVarInt(buf, item.getId(protocolVersion));
        buf.writeByte(count);

        if (nbt == null) {
          buf.writeByte(0);
        } else {
          ProtocolUtils.writeBinaryTag(buf, protocolVersion, nbt);
        }
      }
    }

    @Override
    public int getType(ProtocolVersion protocolVersion) {
      if (protocolVersion.noGreaterThan(ProtocolVersion.MINECRAFT_1_19_1)) {
        return 6;
      } else {
        return 7;
      }
    }
  }

  @AllArgsConstructor
  public static class VarIntEntry implements Entry {

    private final int value;

    @Override
    public void encode(ByteBuf buf, ProtocolVersion protocolVersion) {
      ProtocolUtils.writeVarInt(buf, value);
    }

    @Override
    public int getType(ProtocolVersion protocolVersion) {
      return 1;
    }
  }

  @AllArgsConstructor
  public static class ByteEntry implements Entry {

    private final int value;

    @Override
    public void encode(ByteBuf buf, ProtocolVersion protocolVersion) {
      buf.writeByte(value);
    }

    @Override
    public int getType(ProtocolVersion protocolVersion) {
      return 0;
    }
  }
}
