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

package com.velocitypowered.proxy.protocol.prepared.handler;

import com.velocitypowered.proxy.protocol.VelocityConnectionEvent;
import com.velocitypowered.proxy.protocol.prepared.PreparedPacketFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CompressionEventHandler extends ChannelInboundHandlerAdapter {

  private final PreparedPacketFactory factory;

  public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
    if (event instanceof VelocityConnectionEvent velocityEvent) {
      if (velocityEvent == VelocityConnectionEvent.COMPRESSION_ENABLED) {
        factory.setShouldSendUncompressed(ctx.pipeline(), false);
      } else if (velocityEvent == VelocityConnectionEvent.COMPRESSION_DISABLED) {
        factory.setShouldSendUncompressed(ctx.pipeline(), true);
      }
    }
    ctx.fireUserEventTriggered(event);
  }
}
