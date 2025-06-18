/*
 * Copyright (C) 2025 Sonar Contributors
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

package com.velocitypowered.proxy.protocol.packet.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServerSetPassengersPacket implements MinecraftPacket {

    private int entityId, passengerId;

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        // You can find this in the EntityAttach packet,
        // which was later replaced by SetPassengers in 1.9+
        if (protocolVersion.noGreaterThan(ProtocolVersion.MINECRAFT_1_8)) {
            byteBuf.writeInt(passengerId);
            byteBuf.writeInt(entityId);
            byteBuf.writeByte(0); // leash
            return;
        }

        ProtocolUtils.writeVarInt(byteBuf, entityId);
        ProtocolUtils.writeVarInt(byteBuf, 1); // passenger count
        ProtocolUtils.writeVarInt(byteBuf, passengerId);
    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        return true;
    }
}
