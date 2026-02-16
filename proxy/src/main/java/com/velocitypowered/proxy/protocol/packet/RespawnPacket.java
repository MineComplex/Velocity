/*
 * Copyright (C) 2018-2023 Velocity Contributors
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

package com.velocitypowered.proxy.protocol.packet;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.registry.DimensionInfo;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.checkerframework.checker.nullness.qual.Nullable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
/**
 * Represents a respawn packet sent by the server when the player changes dimensions or respawns.
 * The packet contains information about the new dimension, difficulty, gamemode, and more.
 */
public class RespawnPacket implements MinecraftPacket {

  private int dimension;
  private long partialHashedSeed;
  private short difficulty;
  private short gamemode;
  private String levelType = "";
  private byte dataToKeep; // 1.16+
  private DimensionInfo dimensionInfo; // 1.16-1.16.1
  private short previousGamemode; // 1.16+
  private CompoundBinaryTag currentDimensionData; // 1.16.2+
  private @Nullable Pair<String, Long> lastDeathPosition; // 1.19+
  private int portalCooldown; // 1.20+
  private int seaLevel; // 1.21.2+

  public RespawnPacket() {
  }

  public RespawnPacket(int dimension, long partialHashedSeed, short difficulty, short gamemode,
                       String levelType, byte dataToKeep, DimensionInfo dimensionInfo,
                       short previousGamemode, CompoundBinaryTag currentDimensionData,
                       @Nullable Pair<String, Long> lastDeathPosition, int portalCooldown,
                       int seaLevel) {
    this.dimension = dimension;
    this.partialHashedSeed = partialHashedSeed;
    this.difficulty = difficulty;
    this.gamemode = gamemode;
    this.levelType = levelType;
    this.dataToKeep = dataToKeep;
    this.dimensionInfo = dimensionInfo;
    this.previousGamemode = previousGamemode;
    this.currentDimensionData = currentDimensionData;
    this.lastDeathPosition = lastDeathPosition;
    this.portalCooldown = portalCooldown;
    this.seaLevel = seaLevel;
  }

  /**
   * Creates a new {@code RespawnPacket} from a {@link JoinGamePacket}.
   *
   * @param joinGame the {@code JoinGamePacket} to use
   * @return a new {@code RespawnPacket} based on the provided {@code JoinGamePacket}
   */
  public static RespawnPacket fromJoinGame(JoinGamePacket joinGame) {
    return new RespawnPacket(joinGame.getDimension(), joinGame.getPartialHashedSeed(),
        joinGame.getDifficulty(), joinGame.getGamemode(), joinGame.getLevelType(),
        (byte) 0, joinGame.getDimensionInfo(), joinGame.getPreviousGamemode(),
        joinGame.getCurrentDimensionData(), joinGame.getLastDeathPosition(),
        joinGame.getPortalCooldown(), joinGame.getSeaLevel());
  }

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    String dimensionKey = "";
    String levelName = null;
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_16)) {
      if (version.noLessThan(ProtocolVersion.MINECRAFT_1_16_2)
          && version.lessThan(ProtocolVersion.MINECRAFT_1_19)) {
        this.currentDimensionData = ProtocolUtils.readCompoundTag(buf, version, BinaryTagIO.reader());
        dimensionKey = ProtocolUtils.readString(buf);
      } else {
        if (version.noLessThan(ProtocolVersion.MINECRAFT_1_20_5)) {
          dimension = ProtocolUtils.readVarInt(buf);
        } else {
          dimensionKey = ProtocolUtils.readString(buf);
        }
        levelName = ProtocolUtils.readString(buf);
      }
    } else {
      this.dimension = buf.readInt();
    }
    if (version.noGreaterThan(ProtocolVersion.MINECRAFT_1_13_2)) {
      this.difficulty = buf.readUnsignedByte();
    }
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_15)) {
      this.partialHashedSeed = buf.readLong();
    }
    this.gamemode = buf.readByte();
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_16)) {
      this.previousGamemode = buf.readByte();
      boolean isDebug = buf.readBoolean();
      boolean isFlat = buf.readBoolean();
      this.dimensionInfo = new DimensionInfo(dimensionKey, levelName, isFlat, isDebug, version);
      if (version.lessThan(ProtocolVersion.MINECRAFT_1_19_3)) {
        this.dataToKeep = (byte) (buf.readBoolean() ? 1 : 0);
      } else if (version.lessThan(ProtocolVersion.MINECRAFT_1_20_2)) {
        this.dataToKeep = buf.readByte();
      }
    } else {
      this.levelType = ProtocolUtils.readString(buf, 16);
    }
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_19) && buf.readBoolean()) {
      this.lastDeathPosition = Pair.of(ProtocolUtils.readString(buf), buf.readLong());
    }
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_20)) {
      this.portalCooldown = ProtocolUtils.readVarInt(buf);
    }
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
      this.seaLevel = ProtocolUtils.readVarInt(buf);
    }
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_20_2)) {
      this.dataToKeep = buf.readByte();
    }
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_16)) {
      if (version.noLessThan(ProtocolVersion.MINECRAFT_1_16_2)
          && version.lessThan(ProtocolVersion.MINECRAFT_1_19)) {
        ProtocolUtils.writeBinaryTag(buf, version, currentDimensionData);
        ProtocolUtils.writeString(buf, dimensionInfo.getRegistryIdentifier());
      } else {
        if (version.noLessThan(ProtocolVersion.MINECRAFT_1_20_5)) {
          ProtocolUtils.writeVarInt(buf, dimension);
        } else {
          ProtocolUtils.writeString(buf, dimensionInfo.getRegistryIdentifier());
        }
        ProtocolUtils.writeString(buf, dimensionInfo.getLevelName());
      }
    } else {
      buf.writeInt(dimension);
    }
    if (version.noGreaterThan(ProtocolVersion.MINECRAFT_1_13_2)) {
      buf.writeByte(difficulty);
    }
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_15)) {
      buf.writeLong(partialHashedSeed);
    }
    buf.writeByte(gamemode);
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_16)) {
      buf.writeByte(previousGamemode);
      buf.writeBoolean(dimensionInfo.isDebugType());
      buf.writeBoolean(dimensionInfo.isFlat());
      if (version.lessThan(ProtocolVersion.MINECRAFT_1_19_3)) {
        buf.writeBoolean(dataToKeep != 0);
      } else if (version.lessThan(ProtocolVersion.MINECRAFT_1_20_2)) {
        buf.writeByte(dataToKeep);
      }
    } else {
      ProtocolUtils.writeString(buf, levelType);
    }

    // optional death location
    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_19)) {
      if (lastDeathPosition != null) {
        buf.writeBoolean(true);
        ProtocolUtils.writeString(buf, lastDeathPosition.key());
        buf.writeLong(lastDeathPosition.value());
      } else {
        buf.writeBoolean(false);
      }
    }

    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_20)) {
      ProtocolUtils.writeVarInt(buf, portalCooldown);
    }

    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_21_2)) {
      ProtocolUtils.writeVarInt(buf, seaLevel);
    }

    if (version.noLessThan(ProtocolVersion.MINECRAFT_1_20_2)) {
      buf.writeByte(dataToKeep);
    }
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }
}
