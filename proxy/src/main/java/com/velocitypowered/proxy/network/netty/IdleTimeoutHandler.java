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

package com.velocitypowered.proxy.network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 * Closes the connection when nothing is read from it or nothing is written to it for the timeouts.
 * Unlike {@link io.netty.handler.timeout.ReadTimeoutHandler} it fires no exception, so connections
 * which have just gone silent, like bots during an attack, are closed without an error in the log.
 */
public class IdleTimeoutHandler extends IdleStateHandler {

  /**
   * Creates the handler.
   *
   * @param readTimeout nothing is read from the connection for this time, 0 disables it
   * @param writeTimeout no write to the connection has completed for this time, 0 disables it
   * @param unit unit of the timeouts
   */
  public IdleTimeoutHandler(long readTimeout, long writeTimeout, TimeUnit unit) {
    super(false, readTimeout, writeTimeout, 0, unit);
  }

  @Override
  protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent event) {
    if (ctx.channel().isActive()) {
      ctx.close();
    }
  }
}
