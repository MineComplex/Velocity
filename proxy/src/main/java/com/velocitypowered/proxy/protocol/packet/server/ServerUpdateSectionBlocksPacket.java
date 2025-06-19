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
import com.velocitypowered.proxy.protocol.data.block.BlockUpdate;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"blockUpdates"})
public class ServerUpdateSectionBlocksPacket implements MinecraftPacket {

  private int sectionX, sectionZ;
  private BlockUpdate[] blockUpdates;

  @Override
  public void encode(ByteBuf bytebuf, ProtocolUtils.Direction direction, @NotNull ProtocolVersion protocolVersion) {
    if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_16_2)) {
      bytebuf.writeInt(sectionX);
      bytebuf.writeInt(sectionZ);

      if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_8)) {
        bytebuf.writeShort(blockUpdates.length);
        bytebuf.writeInt(4 * blockUpdates.length);
      } else {
        ProtocolUtils.writeVarInt(bytebuf, blockUpdates.length);
      }

      for (BlockUpdate block : blockUpdates) {
        bytebuf.writeShort(block.getLegacyBlockState());
        int blockId = block.getBlockType().getId().apply(protocolVersion);
        if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_13)) {
          ProtocolUtils.writeVarInt(bytebuf, blockId);
        } else {
          int shiftedBlockId = blockId << 4;
          if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_8)) {
            bytebuf.writeShort(shiftedBlockId);
          } else {
            ProtocolUtils.writeVarInt(bytebuf, shiftedBlockId);
          }
        }
      }
    } else {
      // We only need one Y position
      int sectionY = blockUpdates[0].getPosition().getY() >> 4;

      // https://wiki.vg/Protocol#Update_Section_Blocks
      bytebuf.writeLong(((sectionX & 0x3FFFFFL) << 42) | (sectionY & 0xFFFFF) | ((sectionZ & 0x3FFFFFL) << 20));

      // 1.20+ don't have light update suppression
      if (protocolVersion.lessThan(ProtocolVersion.MINECRAFT_1_20)) {
        bytebuf.writeBoolean(true); // suppress light updates
      }

      ProtocolUtils.writeVarInt(bytebuf, blockUpdates.length);

      for (BlockUpdate block : blockUpdates) {
        // https://wiki.vg/Protocol#Update_Section_Blocks
        int shiftedBlockId = block.getBlockType().getId().apply(protocolVersion) << 12;
        long positionIdValue = shiftedBlockId | block.getBlockState();
        ProtocolUtils.writeVarLong(bytebuf, positionIdValue);
      }
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return true;
  }

  @Override
  public void decode(ByteBuf bytebuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
    throw new UnsupportedOperationException();
  }
}
