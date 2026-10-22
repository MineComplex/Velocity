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

package com.velocitypowered.proxy.protocol;

import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_1_8;
import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_26_3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.packet.HandshakePacket;
import com.velocitypowered.proxy.protocol.packet.StatusPingPacket;
import org.junit.jupiter.api.Test;

class PacketRegistryTest {

  private StateRegistry.PacketRegistry setupRegistry() {
    StateRegistry.PacketRegistry registry = new StateRegistry.PacketRegistry(
        ProtocolUtils.Direction.CLIENTBOUND, StateRegistry.PLAY);
    registry.register(HandshakePacket.class, HandshakePacket::new,
        new StateRegistry.PacketMapping(0x01, MINECRAFT_26_3, null, false));
    return registry;
  }

  @Test
  void onlySupportedVersionsAreKnown() {
    assertEquals(MINECRAFT_26_3, ProtocolVersion.MINIMUM_VERSION);
    assertTrue(MINECRAFT_26_3.isSupported());
    assertFalse(MINECRAFT_1_8.isSupported());
    assertFalse(ProtocolVersion.SUPPORTED_VERSIONS.contains(MINECRAFT_1_8));
    // Old versions are still recognised so the server list ping can answer them.
    assertEquals(MINECRAFT_1_8, ProtocolVersion.getProtocolVersion(47));
  }

  @Test
  void packetRegistryWorks() {
    StateRegistry.PacketRegistry registry = setupRegistry();
    MinecraftPacket packet = registry.getProtocolRegistry(MINECRAFT_26_3).createPacket(0x01);
    assertNotNull(packet, "Packet was not found in registry");
    assertEquals(HandshakePacket.class, packet.getClass(), "Registry returned wrong class");

    assertEquals(0x01, registry.getProtocolRegistry(MINECRAFT_26_3).getPacketId(packet),
        "Registry did not return the correct packet ID");
    assertNull(registry.getProtocolRegistry(MINECRAFT_26_3).createPacket(0x00),
        "Registry should return null");
  }

  @Test
  void unsupportedVersionsUseFallback() {
    StateRegistry.PacketRegistry registry = setupRegistry();
    assertSame(registry.getProtocolRegistry(MINECRAFT_26_3),
        registry.getProtocolRegistry(MINECRAFT_1_8));

    registry.fallback = false;
    assertThrows(IllegalArgumentException.class,
        () -> registry.getProtocolRegistry(MINECRAFT_1_8));
  }

  @Test
  void failOnNoMappings() {
    StateRegistry.PacketRegistry registry = new StateRegistry.PacketRegistry(
        ProtocolUtils.Direction.CLIENTBOUND, StateRegistry.PLAY);
    assertThrows(IllegalArgumentException.class,
        () -> registry.register(HandshakePacket.class, HandshakePacket::new));
    assertThrows(IllegalArgumentException.class,
        () -> registry.getProtocolRegistry(ProtocolVersion.UNKNOWN)
                .getPacketId(new HandshakePacket()));
  }

  @Test
  void failOnUnsupportedVersionMapping() {
    StateRegistry.PacketRegistry registry = new StateRegistry.PacketRegistry(
        ProtocolUtils.Direction.CLIENTBOUND, StateRegistry.PLAY);
    assertThrows(IllegalArgumentException.class,
        () -> registry.register(HandshakePacket.class, HandshakePacket::new,
            new StateRegistry.PacketMapping(0x00, MINECRAFT_1_8, null, false)));
    assertThrows(IllegalArgumentException.class,
        () -> registry.register(HandshakePacket.class, HandshakePacket::new,
            new StateRegistry.PacketMapping(0x00, MINECRAFT_26_3, MINECRAFT_1_8, false)));
  }

  @Test
  void failOnDuplicate() {
    StateRegistry.PacketRegistry registry = new StateRegistry.PacketRegistry(
        ProtocolUtils.Direction.CLIENTBOUND, StateRegistry.PLAY);
    registry.register(HandshakePacket.class, HandshakePacket::new,
        new StateRegistry.PacketMapping(0x00, MINECRAFT_26_3, null, false));
    assertThrows(IllegalArgumentException.class,
        () -> registry.register(HandshakePacket.class, HandshakePacket::new,
            new StateRegistry.PacketMapping(0x01, MINECRAFT_26_3, null, false)));
    assertThrows(IllegalArgumentException.class,
        () -> registry.register(StatusPingPacket.class, StatusPingPacket::new,
            new StateRegistry.PacketMapping(0x00, MINECRAFT_26_3, null, false)));
  }
}
