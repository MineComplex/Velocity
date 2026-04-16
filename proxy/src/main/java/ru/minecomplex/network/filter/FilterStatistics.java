/*
 * Copyright (C) 2026 Velocity Contributors
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

package ru.minecomplex.network.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import ru.minecomplex.network.util.LagUtil;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public final class FilterStatistics {

  private final Cache<Integer, Byte> LOGINS_PER_SECOND = Caffeine.newBuilder()
          .expireAfterWrite(Duration.ofSeconds(1))
          .ticker(Ticker.systemTicker())
          .build();

  private final Cache<Integer, Byte> CONNECTIONS_PER_SECOND = Caffeine.newBuilder()
          .expireAfterWrite(Duration.ofSeconds(1))
          .ticker(Ticker.systemTicker())
          .build();

  private final AtomicInteger ACTION_COUNTER = new AtomicInteger(Integer.MIN_VALUE);

  /**
   * Helper methods that make it easier to count new statistics
   */

  @ApiStatus.Internal
  public void countConnection() {
    CONNECTIONS_PER_SECOND.put(ACTION_COUNTER.getAndIncrement(), (byte) 0);
  }

  @ApiStatus.Internal
  public void countLogin() {
    LOGINS_PER_SECOND.put(ACTION_COUNTER.getAndIncrement(), (byte) 0);
    totalJoinedPlayers++;
  }

  // Cache all per-session statistics
  private int totalJoinedPlayers;
  @Getter
  public int totalAttemptedVerifications;
  @Getter
  public int totalSuccessfulVerifications;
  @Getter
  public int totalFailedVerifications;
  public long totalBlacklistedPlayers;

  public long totalIncomingTraffic;
  public long totalOutgoingTraffic;
  public long perSecondIncomingTraffic;
  public long perSecondOutgoingTraffic;
  private String perSecondIncomingTrafficFormatted;
  private String perSecondOutgoingTrafficFormatted;

  public void cleanUpCaches() {
    LOGINS_PER_SECOND.cleanUp();
    CONNECTIONS_PER_SECOND.cleanUp();
  }

  public void hitEverySecond() {
    totalIncomingTraffic += perSecondIncomingTraffic;
    totalOutgoingTraffic += perSecondOutgoingTraffic;
    perSecondIncomingTrafficFormatted = LagUtil.formatMemory(perSecondIncomingTraffic);
    perSecondOutgoingTrafficFormatted = LagUtil.formatMemory(perSecondOutgoingTraffic);
    perSecondIncomingTraffic = 0L;
    perSecondOutgoingTraffic = 0L;
  }

  public long getConnectionsPerSecond() {
    return CONNECTIONS_PER_SECOND.estimatedSize();
  }

  public long getLoginsPerSecond() {
    return LOGINS_PER_SECOND.estimatedSize();
  }

  public long getCurrentIncomingBandwidth() {
    return perSecondIncomingTraffic;
  }

  public long getCurrentOutgoingBandwidth() {
    return perSecondOutgoingTraffic;
  }

  public long getTotalIncomingBandwidth() {
    return totalIncomingTraffic;
  }

  public long getTotalOutgoingBandwidth() {
    return totalOutgoingTraffic;
  }

  public String getPerSecondIncomingBandwidthFormatted() {
    return perSecondIncomingTrafficFormatted;
  }

  public String getPerSecondOutgoingBandwidthFormatted() {
    return perSecondOutgoingTrafficFormatted;
  }

  public int getTotalPlayersJoined() {
    return totalJoinedPlayers;
  }

  public long getTotalBlacklistSize() {
    return totalBlacklistedPlayers;
  }
}
