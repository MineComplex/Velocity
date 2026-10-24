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

package com.velocitypowered.proxy.protocol.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.concurrent.atomic.LongAdder;

/**
 * Counts the bytes of the connections of players since the start of the proxy.
 */
@ChannelHandler.Sharable
public final class BandwidthHandler extends ChannelDuplexHandler {

  public static final BandwidthHandler INSTANCE = new BandwidthHandler();

  private static final LongAdder INCOMING = new LongAdder();
  private static final LongAdder OUTGOING = new LongAdder();

  private BandwidthHandler() {
  }

  /**
   * Bytes received from players.
   */
  public static long getIncomingBytes() {
    return INCOMING.sum();
  }

  /**
   * Bytes sent to players.
   */
  public static long getOutgoingBytes() {
    return OUTGOING.sum();
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
    if (msg instanceof ByteBuf buf) {
      INCOMING.add(buf.readableBytes());
    }
    ctx.fireChannelRead(msg);
  }

  @Override
  public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
    if (msg instanceof ByteBuf buf) {
      OUTGOING.add(buf.readableBytes());
    }
    ctx.write(msg, promise);
  }
}
