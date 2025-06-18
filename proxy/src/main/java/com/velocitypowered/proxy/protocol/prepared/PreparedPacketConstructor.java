package com.velocitypowered.proxy.protocol.prepared;

import com.velocitypowered.api.network.ProtocolVersion;

public interface PreparedPacketConstructor {

    PreparedPacket construct(ProtocolVersion minVersion, ProtocolVersion maxVersion, PreparedPacketFactory factory);

}
