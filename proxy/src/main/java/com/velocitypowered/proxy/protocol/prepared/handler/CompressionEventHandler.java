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
