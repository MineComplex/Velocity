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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a respawn packet sent by the server when the player changes dimensions or respawns.
 * The packet contains information about the new dimension, difficulty, gamemode, and more.
 */
@Getter
@Setter
@ToString
public class RespawnPacket implements MinecraftPacket {

  private int dimension;
  private long partialHashedSeed;
  private int gamemode;
  private byte dataToKeep;
  private DimensionInfo dimensionInfo;
  private int previousGamemode;
  private @Nullable Pair<String, Long> lastDeathPosition;
  private int portalCooldown;
  private int seaLevel;

  public RespawnPacket() {
  }

  public RespawnPacket(int dimension, long partialHashedSeed, int gamemode, byte dataToKeep,
                       DimensionInfo dimensionInfo, int previousGamemode,
                       @Nullable Pair<String, Long> lastDeathPosition, int portalCooldown,
                       int seaLevel) {
    this.dimension = dimension;
    this.partialHashedSeed = partialHashedSeed;
    this.gamemode = gamemode;
    this.dataToKeep = dataToKeep;
    this.dimensionInfo = dimensionInfo;
    this.previousGamemode = previousGamemode;
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
        joinGame.getGamemode(), (byte) 0, joinGame.getDimensionInfo(),
        joinGame.getPreviousGamemode(), joinGame.getLastDeathPosition(),
        joinGame.getPortalCooldown(), joinGame.getSeaLevel());
  }

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    this.dimension = ProtocolUtils.readVarInt(buf);
    String levelName = ProtocolUtils.readString(buf);
    this.partialHashedSeed = buf.readLong();
    this.gamemode = ProtocolUtils.readVarInt(buf);
    this.previousGamemode = ProtocolUtils.readVarInt(buf);
    boolean isDebug = buf.readBoolean();
    boolean isFlat = buf.readBoolean();
    this.dimensionInfo = new DimensionInfo(levelName, isFlat, isDebug);
    if (buf.readBoolean()) {
      this.lastDeathPosition = Pair.of(ProtocolUtils.readString(buf), buf.readLong());
    }
    this.portalCooldown = ProtocolUtils.readVarInt(buf);
    this.seaLevel = ProtocolUtils.readVarInt(buf);
    this.dataToKeep = buf.readByte();
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    ProtocolUtils.writeVarInt(buf, dimension);
    ProtocolUtils.writeString(buf, dimensionInfo.getLevelName());
    buf.writeLong(partialHashedSeed);
    ProtocolUtils.writeVarInt(buf, this.gamemode);
    ProtocolUtils.writeVarInt(buf, this.previousGamemode);
    buf.writeBoolean(dimensionInfo.isDebugType());
    buf.writeBoolean(dimensionInfo.isFlat());

    // optional death location
    if (lastDeathPosition != null) {
      buf.writeBoolean(true);
      ProtocolUtils.writeString(buf, lastDeathPosition.key());
      buf.writeLong(lastDeathPosition.value());
    } else {
      buf.writeBoolean(false);
    }

    ProtocolUtils.writeVarInt(buf, portalCooldown);
    ProtocolUtils.writeVarInt(buf, seaLevel);
    buf.writeByte(dataToKeep);
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }
}
