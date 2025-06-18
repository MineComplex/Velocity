package com.velocitypowered.proxy.connection.client;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.packet.client.*;

public interface ClientLimboSessionHandler extends MinecraftSessionHandler {
    boolean handle(ClientMovePacket packet);

    boolean handle(ClientMovePositionOnlyPacket packet);

    boolean handle(ClientMoveRotationOnlyPacket packet);

    boolean handle(ClientMoveOnGroundOnlyPacket packet);

    boolean handle(ClientTeleportConfirmPacket packet);
}
