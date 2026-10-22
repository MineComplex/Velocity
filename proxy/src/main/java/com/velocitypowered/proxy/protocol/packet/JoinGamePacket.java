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

import com.google.common.collect.ImmutableSet;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.registry.DimensionInfo;
import com.velocitypowered.proxy.protocol.*;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a packet sent to the client when they successfully join a game in Minecraft.
 * This packet contains all the necessary information to initialize the client state,
 * including the player's entity ID, game mode, dimension, world settings, and more.
 */
@Getter
@Setter
@ToString
public class JoinGamePacket implements MinecraftPacket {

  private int entityId;
  private int gamemode;
  private int dimension;
  private long partialHashedSeed;
  @Setter
  private boolean isHardcore;
  private int maxPlayers;
  private int viewDistance;
  private boolean reducedDebugInfo;
  private boolean showRespawnScreen;
  private boolean doLimitedCrafting;
  private ImmutableSet<String> levelNames;
  private DimensionInfo dimensionInfo;
  private int previousGamemode;
  private int simulationDistance;
  private @Nullable Pair<String, Long> lastDeathPosition;
  private int portalCooldown;
  private int seaLevel;
  private boolean onlineMode;
  private boolean enforcesSecureChat;

  @Override
  public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    this.entityId = buf.readInt();
    this.isHardcore = buf.readBoolean();

    this.levelNames = ImmutableSet.copyOf(ProtocolUtils.readStringArray(buf));

    this.maxPlayers = ProtocolUtils.readVarInt(buf);

    this.viewDistance = ProtocolUtils.readVarInt(buf);
    this.simulationDistance = ProtocolUtils.readVarInt(buf);

    this.reducedDebugInfo = buf.readBoolean();
    this.showRespawnScreen = buf.readBoolean();
    this.doLimitedCrafting = buf.readBoolean();

    this.dimension = ProtocolUtils.readVarInt(buf);
    String levelName = ProtocolUtils.readString(buf);
    this.partialHashedSeed = buf.readLong();

    this.gamemode = ProtocolUtils.readVarInt(buf);
    this.previousGamemode = ProtocolUtils.readVarInt(buf); // game mode + 1 or 0

    boolean isDebug = buf.readBoolean();
    boolean isFlat = buf.readBoolean();
    this.dimensionInfo = new DimensionInfo(levelName, isFlat, isDebug);

    // optional death location
    if (buf.readBoolean()) {
      this.lastDeathPosition = Pair.of(ProtocolUtils.readString(buf), buf.readLong());
    }

    this.portalCooldown = ProtocolUtils.readVarInt(buf);
    this.seaLevel = ProtocolUtils.readVarInt(buf);
    this.onlineMode = buf.readBoolean();
    this.enforcesSecureChat = buf.readBoolean();
  }

  @Override
  public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
    buf.writeInt(entityId);
    buf.writeBoolean(isHardcore);

    ProtocolUtils.writeStringArray(buf, levelNames.toArray(String[]::new));

    ProtocolUtils.writeVarInt(buf, maxPlayers);

    ProtocolUtils.writeVarInt(buf, viewDistance);
    ProtocolUtils.writeVarInt(buf, simulationDistance);

    buf.writeBoolean(reducedDebugInfo);
    buf.writeBoolean(showRespawnScreen);
    buf.writeBoolean(doLimitedCrafting);

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
    buf.writeBoolean(this.onlineMode);
    buf.writeBoolean(this.enforcesSecureChat);
  }

  @Override
  public boolean handle(MinecraftSessionHandler handler) {
    return handler.handle(this);
  }
}
