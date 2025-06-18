package com.velocitypowered.proxy.protocol.prepared.dummy;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import io.netty.channel.Channel;

public class DummyClosedMinecraftConnection extends MinecraftConnection {

    public DummyClosedMinecraftConnection(Channel channel, VelocityServer server) {
        super(channel, server);
    }

    @Override
    public boolean isClosed() {
        return true;
    }
}
