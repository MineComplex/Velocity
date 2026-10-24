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

import com.velocitypowered.api.network.ProtocolState;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.packet.AvailableCommandsPacket;
import com.velocitypowered.proxy.protocol.packet.BossBarPacket;
import com.velocitypowered.proxy.protocol.packet.BundleDelimiterPacket;
import com.velocitypowered.proxy.protocol.packet.ClientSettingsPacket;
import com.velocitypowered.proxy.protocol.packet.ClientboundCookieRequestPacket;
import com.velocitypowered.proxy.protocol.packet.ClientboundPostEffectsPacket;
import com.velocitypowered.proxy.protocol.packet.ClientboundSoundEntityPacket;
import com.velocitypowered.proxy.protocol.packet.ClientboundStopSoundPacket;
import com.velocitypowered.proxy.protocol.packet.ClientboundStoreCookiePacket;
import com.velocitypowered.proxy.protocol.packet.DialogClearPacket;
import com.velocitypowered.proxy.protocol.packet.DialogShowPacket;
import com.velocitypowered.proxy.protocol.packet.DisconnectPacket;
import com.velocitypowered.proxy.protocol.packet.EncryptionRequestPacket;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import com.velocitypowered.proxy.protocol.packet.HandshakePacket;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooterPacket;
import com.velocitypowered.proxy.protocol.packet.JoinGamePacket;
import com.velocitypowered.proxy.protocol.packet.KeepAlivePacket;
import com.velocitypowered.proxy.protocol.packet.LoginAcknowledgedPacket;
import com.velocitypowered.proxy.protocol.packet.LoginPluginMessagePacket;
import com.velocitypowered.proxy.protocol.packet.LoginPluginResponsePacket;
import com.velocitypowered.proxy.protocol.packet.PingIdentifyPacket;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfoPacket;
import com.velocitypowered.proxy.protocol.packet.RemoveResourcePackPacket;
import com.velocitypowered.proxy.protocol.packet.ResourcePackRequestPacket;
import com.velocitypowered.proxy.protocol.packet.ResourcePackResponsePacket;
import com.velocitypowered.proxy.protocol.packet.RespawnPacket;
import com.velocitypowered.proxy.protocol.packet.ServerDataPacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginSuccessPacket;
import com.velocitypowered.proxy.protocol.packet.ServerboundCookieResponsePacket;
import com.velocitypowered.proxy.protocol.packet.ServerboundCustomClickActionPacket;
import com.velocitypowered.proxy.protocol.packet.ServerboundPlayerLoadedPacket;
import com.velocitypowered.proxy.protocol.packet.SetCompressionPacket;
import com.velocitypowered.proxy.protocol.packet.StatusPingPacket;
import com.velocitypowered.proxy.protocol.packet.StatusRequestPacket;
import com.velocitypowered.proxy.protocol.packet.StatusResponsePacket;
import com.velocitypowered.proxy.protocol.packet.TabCompleteRequestPacket;
import com.velocitypowered.proxy.protocol.packet.TabCompleteResponsePacket;
import com.velocitypowered.proxy.protocol.packet.TransferPacket;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfoPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ChatAcknowledgementPacket;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerChatCompletionPacket;
import com.velocitypowered.proxy.protocol.packet.chat.SystemChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommandPacket;
import com.velocitypowered.proxy.protocol.packet.chat.session.UnsignedPlayerCommandPacket;
import com.velocitypowered.proxy.protocol.packet.client.ClientPlayerChatSessionPacket;
import com.velocitypowered.proxy.protocol.packet.config.ActiveFeaturesPacket;
import com.velocitypowered.proxy.protocol.packet.config.ClientboundCustomReportDetailsPacket;
import com.velocitypowered.proxy.protocol.packet.config.ClientboundServerLinksPacket;
import com.velocitypowered.proxy.protocol.packet.config.CodeOfConductAcceptPacket;
import com.velocitypowered.proxy.protocol.packet.config.CodeOfConductPacket;
import com.velocitypowered.proxy.protocol.packet.config.FinishedUpdatePacket;
import com.velocitypowered.proxy.protocol.packet.config.KnownPacksPacket;
import com.velocitypowered.proxy.protocol.packet.config.RegistrySyncPacket;
import com.velocitypowered.proxy.protocol.packet.config.StartUpdatePacket;
import com.velocitypowered.proxy.protocol.packet.config.TagsUpdatePacket;
import com.velocitypowered.proxy.protocol.packet.title.TitleActionbarPacket;
import com.velocitypowered.proxy.protocol.packet.title.TitleClearPacket;
import com.velocitypowered.proxy.protocol.packet.title.TitleSubtitlePacket;
import com.velocitypowered.proxy.protocol.packet.title.TitleTextPacket;
import com.velocitypowered.proxy.protocol.packet.title.TitleTimesPacket;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_26_3;
import static com.velocitypowered.proxy.protocol.ProtocolUtils.Direction;
import static com.velocitypowered.proxy.protocol.ProtocolUtils.Direction.CLIENTBOUND;
import static com.velocitypowered.proxy.protocol.ProtocolUtils.Direction.SERVERBOUND;

/**
 * Registry of all Minecraft protocol states and the packets for each state.
 */
public enum StateRegistry {

  HANDSHAKE {
    {
      serverbound.register(HandshakePacket.class, HandshakePacket::new,
          map(0x00, false));
    }
  },
  STATUS {
    {
      serverbound.register(
          StatusRequestPacket.class, () -> StatusRequestPacket.INSTANCE,
          map(0x00, false));
      serverbound.register(StatusPingPacket.class, StatusPingPacket::new,
          map(0x01, false));

      clientbound.register(
          StatusResponsePacket.class, StatusResponsePacket::new,
          map(0x00, false));
      clientbound.register(StatusPingPacket.class, StatusPingPacket::new,
          map(0x01, false));
    }
  },
  CONFIG {
    {
      serverbound.register(
          ClientSettingsPacket.class, ClientSettingsPacket::new,
          map(0x00, false));
      serverbound.register(
          ServerboundCookieResponsePacket.class, ServerboundCookieResponsePacket::new,
          map(0x01, false));
      serverbound.register(
          PluginMessagePacket.class, PluginMessagePacket::new,
          map(0x02, false));
      serverbound.register(
          FinishedUpdatePacket.class, () -> FinishedUpdatePacket.INSTANCE,
          map(0x03, false));
      serverbound.register(KeepAlivePacket.class, KeepAlivePacket::new,
          map(0x04, false));
      serverbound.register(
          PingIdentifyPacket.class, PingIdentifyPacket::new,
          map(0x05, false));
      serverbound.register(
          ResourcePackResponsePacket.class,
          ResourcePackResponsePacket::new,
          map(0x06, false));
      serverbound.register(
          KnownPacksPacket.class,
          KnownPacksPacket::new,
          map(0x07, false));
      serverbound.register(ServerboundCustomClickActionPacket.class, ServerboundCustomClickActionPacket::new,
          map(0x08, false));
      serverbound.register(
          CodeOfConductAcceptPacket.class,
          () -> CodeOfConductAcceptPacket.INSTANCE,
          map(0x09, false));

      clientbound.register(
          ClientboundCookieRequestPacket.class, ClientboundCookieRequestPacket::new,
          map(0x00, false));
      clientbound.register(
          PluginMessagePacket.class, PluginMessagePacket::new,
          map(0x01, false));
      clientbound.register(
          DisconnectPacket.class, () -> new DisconnectPacket(this),
          map(0x02, false));
      clientbound.register(
          FinishedUpdatePacket.class, () -> FinishedUpdatePacket.INSTANCE,
          map(0x03, false));
      clientbound.register(KeepAlivePacket.class, KeepAlivePacket::new,
          map(0x04, false));
      clientbound.register(
          PingIdentifyPacket.class, PingIdentifyPacket::new,
          map(0x05, false));
      clientbound.register(
          RegistrySyncPacket.class, RegistrySyncPacket::new,
          map(0x07, false));
      clientbound.register(
          RemoveResourcePackPacket.class, RemoveResourcePackPacket::new,
          map(0x08, false));
      clientbound.register(ResourcePackRequestPacket.class, ResourcePackRequestPacket::new,
          map(0x09, false));
      clientbound.register(
          ClientboundPostEffectsPacket.class, ClientboundPostEffectsPacket::new,
          map(0x0A, false));
      clientbound.register(
          ClientboundStoreCookiePacket.class, ClientboundStoreCookiePacket::new,
          map(0x0B, false));
      clientbound.register(TransferPacket.class, TransferPacket::new,
          map(0x0C, false));
      clientbound.register(ActiveFeaturesPacket.class, ActiveFeaturesPacket::new,
          map(0x0D, false));
      clientbound.register(TagsUpdatePacket.class, TagsUpdatePacket::new,
          map(0x0E, false));
      clientbound.register(KnownPacksPacket.class, KnownPacksPacket::new,
          map(0x0F, false));
      clientbound.register(ClientboundCustomReportDetailsPacket.class, ClientboundCustomReportDetailsPacket::new,
          map(0x10, false));
      clientbound.register(ClientboundServerLinksPacket.class, ClientboundServerLinksPacket::new,
          map(0x11, false));
      clientbound.register(DialogClearPacket.class, () -> DialogClearPacket.INSTANCE,
          map(0x12, false));
      clientbound.register(DialogShowPacket.class, () -> new DialogShowPacket(this),
          map(0x13, false));
      clientbound.register(CodeOfConductPacket.class, CodeOfConductPacket::new,
          map(0x14, false));
    }
  },
  PLAY {
    {
      serverbound.fallback = false;
      clientbound.fallback = false;

      serverbound.register(TabCompleteRequestPacket.class, TabCompleteRequestPacket::new,
          map(0x0F, false));
      serverbound.register(
          ChatAcknowledgementPacket.class,
          ChatAcknowledgementPacket::new,
          map(0x06, false));
      serverbound.register(SessionPlayerCommandPacket.class, SessionPlayerCommandPacket::new,
          map(0x08, false));
      serverbound.register(UnsignedPlayerCommandPacket.class, UnsignedPlayerCommandPacket::new,
          map(0x07, false));
      serverbound.register(
          SessionPlayerChatPacket.class,
          SessionPlayerChatPacket::new,
          map(0x09, false));
      serverbound.register(
          ClientSettingsPacket.class,
          ClientSettingsPacket::new,
          map(0x0E, false));
      serverbound.register(
          ServerboundCookieResponsePacket.class, ServerboundCookieResponsePacket::new,
          map(0x15, false));
      serverbound.register(
          ServerboundPlayerLoadedPacket.class,
          () -> ServerboundPlayerLoadedPacket.INSTANCE,
          map(0x2C, false));
      serverbound.register(
          PluginMessagePacket.class,
          PluginMessagePacket::new,
          map(0x16, false));
      serverbound.register(
          KeepAlivePacket.class,
          KeepAlivePacket::new,
          map(0x1C, false));
      serverbound.register(
          ResourcePackResponsePacket.class,
          ResourcePackResponsePacket::new,
          map(0x32, false));
      serverbound.register(
          FinishedUpdatePacket.class, () -> FinishedUpdatePacket.INSTANCE,
          map(0x10, false));
      serverbound.register(
          ClientPlayerChatSessionPacket.class, ClientPlayerChatSessionPacket::new,
          map(0x0A, false));

      clientbound.register(
          BossBarPacket.class,
          BossBarPacket::new,
          map(0x09, false));
      clientbound.register(TabCompleteResponsePacket.class, TabCompleteResponsePacket::new,
          map(0x0F, false));
      clientbound.register(
          AvailableCommandsPacket.class,
          AvailableCommandsPacket::new,
          map(0x10, false));
      clientbound.register(
          ClientboundCookieRequestPacket.class, ClientboundCookieRequestPacket::new,
          map(0x15, false));
      clientbound.register(
          ClientboundPostEffectsPacket.class, ClientboundPostEffectsPacket::new,
          map(0x53, false));
      clientbound.register(
          ClientboundSoundEntityPacket.class, ClientboundSoundEntityPacket::new,
          map(0x76, true));
      clientbound.register(
          ClientboundStopSoundPacket.class, ClientboundStopSoundPacket::new,
          map(0x79, true));
      clientbound.register(
          PluginMessagePacket.class,
          PluginMessagePacket::new,
          map(0x18, false));
      clientbound.register(
          DisconnectPacket.class,
          () -> new DisconnectPacket(this),
          map(0x20, false));
      clientbound.register(
          KeepAlivePacket.class,
          KeepAlivePacket::new,
          map(0x2D, false));
      clientbound.register(
          JoinGamePacket.class,
          JoinGamePacket::new,
          map(0x32, false));
      clientbound.register(
          RespawnPacket.class,
          RespawnPacket::new,
          map(0x54, true));
      clientbound.register(
          RemoveResourcePackPacket.class,
          RemoveResourcePackPacket::new,
          map(0x51, false));
      clientbound.register(
          ResourcePackRequestPacket.class,
          ResourcePackRequestPacket::new,
          map(0x52, false));
      clientbound.register(
          HeaderAndFooterPacket.class,
          HeaderAndFooterPacket::new,
          map(0x7D, true));
      clientbound.register(TitleSubtitlePacket.class, TitleSubtitlePacket::new,
          map(0x72, true));
      clientbound.register(
          TitleTextPacket.class,
          TitleTextPacket::new,
          map(0x74, true));
      clientbound.register(
          TitleActionbarPacket.class,
          TitleActionbarPacket::new,
          map(0x59, true));
      clientbound.register(
          TitleTimesPacket.class,
          TitleTimesPacket::new,
          map(0x75, true));
      clientbound.register(
          TitleClearPacket.class,
          TitleClearPacket::new,
          map(0x0E, true));
      clientbound.register(RemovePlayerInfoPacket.class, RemovePlayerInfoPacket::new,
          map(0x46, false));
      clientbound.register(
          UpsertPlayerInfoPacket.class,
          UpsertPlayerInfoPacket::new,
          map(0x47, false));
      clientbound.register(
          ClientboundStoreCookiePacket.class, ClientboundStoreCookiePacket::new,
          map(0x7A, false));
      clientbound.register(
          SystemChatPacket.class,
          SystemChatPacket::new,
          map(0x7C, true));
      clientbound.register(
          PlayerChatCompletionPacket.class,
          PlayerChatCompletionPacket::new,
          map(0x17, true));
      clientbound.register(
          ServerDataPacket.class,
          ServerDataPacket::new,
          map(0x58, false));
      clientbound.register(
          StartUpdatePacket.class,
          () -> StartUpdatePacket.INSTANCE,
          map(0x78, false));
      clientbound.register(
          BundleDelimiterPacket.class,
          () -> BundleDelimiterPacket.INSTANCE,
          map(0x00, false));
      clientbound.register(
          TransferPacket.class,
          TransferPacket::new,
          map(0x84, false));
      clientbound.register(
          ClientboundCustomReportDetailsPacket.class,
          ClientboundCustomReportDetailsPacket::new,
          map(0x8B, false));
      clientbound.register(
          ClientboundServerLinksPacket.class,
          ClientboundServerLinksPacket::new,
          map(0x8C, false));
    }
  },
  LOGIN {
    {
      serverbound.register(ServerLoginPacket.class,
          ServerLoginPacket::new,
          map(0x00, false));
      serverbound.register(
          EncryptionResponsePacket.class, EncryptionResponsePacket::new,
          map(0x01, false));
      serverbound.register(
          LoginPluginResponsePacket.class, LoginPluginResponsePacket::new,
          map(0x02, false));
      serverbound.register(
          LoginAcknowledgedPacket.class, LoginAcknowledgedPacket::new,
          map(0x03, false));
      serverbound.register(
          ServerboundCookieResponsePacket.class, ServerboundCookieResponsePacket::new,
          map(0x04, false));

      clientbound.register(
          DisconnectPacket.class, () -> new DisconnectPacket(this),
          map(0x00, false));
      clientbound.register(
          EncryptionRequestPacket.class, EncryptionRequestPacket::new,
          map(0x01, false));
      clientbound.register(
          ServerLoginSuccessPacket.class, ServerLoginSuccessPacket::new,
          map(0x02, false));
      clientbound.register(
          SetCompressionPacket.class, SetCompressionPacket::new,
          map(0x03, false));
      clientbound.register(
          LoginPluginMessagePacket.class,
          LoginPluginMessagePacket::new,
          map(0x04, false));
      clientbound.register(
          ClientboundCookieRequestPacket.class, ClientboundCookieRequestPacket::new,
          map(0x05, false));
    }
  };

  public static final int STATUS_ID = 1;
  public static final int LOGIN_ID = 2;
  public static final int TRANSFER_ID = 3;
  protected final PacketRegistry clientbound = new PacketRegistry(CLIENTBOUND, this);
  protected final PacketRegistry serverbound = new PacketRegistry(SERVERBOUND, this);

  public StateRegistry.PacketRegistry.ProtocolRegistry getProtocolRegistry(Direction direction,
                                                                           ProtocolVersion version) {
    return (direction == SERVERBOUND ? serverbound : clientbound).getProtocolRegistry(version);
  }

  /**
   * Gets the API representation of the StateRegistry.
   *
   * @return the API representation
   */
  public ProtocolState toProtocolState() {
    return switch (this) {
      case HANDSHAKE -> ProtocolState.HANDSHAKE;
      case STATUS -> ProtocolState.STATUS;
      case LOGIN -> ProtocolState.LOGIN;
      case CONFIG -> ProtocolState.CONFIGURATION;
      default -> ProtocolState.PLAY;
    };
  }

  /**
   * Packet registry.
   */
  public static class PacketRegistry {

    private final Direction direction;
    private final StateRegistry registry;
    private final ProtocolRegistry protocolRegistry = new ProtocolRegistry(MINECRAFT_26_3);
    boolean fallback = true;

    PacketRegistry(Direction direction, StateRegistry registry) {
      this.direction = direction;
      this.registry = registry;

    }

    /**
     * Returns protocol registry by @param version.
     */
    public ProtocolRegistry getProtocolRegistry(final ProtocolVersion version) {
      if (version == MINECRAFT_26_3 || fallback) {
        return protocolRegistry;
      }
      throw new IllegalArgumentException("Could not find data for protocol version " + version);
    }

    /**
     * Register packet class using its construct or and mappings.
     */
    public <P extends MinecraftPacket> void register(Class<P> clazz, Supplier<P> packetSupplier,
                                                     PacketMapping... mappings) {
      if (mappings.length != 1) {
        throw new IllegalArgumentException("Exactly one mapping must be provided for 26.3.");
      }
      PacketMapping mapping = mappings[0];
      if (protocolRegistry.packetIdToSupplier.containsKey(mapping.id)) {
        throw new IllegalArgumentException("Packet ID " + mapping.id + " is already registered");
      }
      if (protocolRegistry.packetClassToId.containsKey(clazz)) {
        throw new IllegalArgumentException(clazz.getSimpleName() + " is already registered");
      }
      if (!mapping.encodeOnly) {
        protocolRegistry.packetIdToSupplier.put(mapping.id, packetSupplier);
      }
      protocolRegistry.packetClassToId.put(clazz, mapping.id);
    }

    /**
     * Protocol registry.
     */
    public class ProtocolRegistry {

      public final ProtocolVersion version;
      final IntObjectMap<Supplier<? extends MinecraftPacket>> packetIdToSupplier =
          new IntObjectHashMap<>(16, 0.5f);
      final Object2IntMap<Class<? extends MinecraftPacket>> packetClassToId =
          new Object2IntOpenHashMap<>(16, 0.5f);

      /**
       * Protocol registry.
       */
      public ProtocolRegistry(final ProtocolVersion version) {
        this.version = version;
        this.packetClassToId.defaultReturnValue(Integer.MIN_VALUE);
      }

      /**
       * Attempts to create a packet from the specified {@code id}.
       *
       * @param id the packet ID
       * @return the packet instance, or {@code null} if the ID is not registered
       */
      public @Nullable MinecraftPacket createPacket(final int id) {
        final Supplier<? extends MinecraftPacket> supplier = this.packetIdToSupplier.get(id);
        if (supplier == null) {
          return null;
        }
        return supplier.get();
      }

      /**
       * Attempts to look up the packet ID for an {@code packet}.
       *
       * @param packet the packet to look up
       * @return the packet ID
       * @throws IllegalArgumentException if the packet ID is not found
       */
      public int getPacketId(final MinecraftPacket packet) {
        final int id = this.packetClassToId.getInt(packet.getClass());
        if (id == Integer.MIN_VALUE) {
          throw new IllegalArgumentException(String.format(
              "Unable to find id for packet of type %s in %s protocol %s phase %s",
              packet.getClass().getName(), PacketRegistry.this.direction,
              this.version, PacketRegistry.this.registry
          ));
        }
        return id;
      }

      /**
       * Checks if the registry contains a packet with the specified {@code id}.
       *
       * @param packet the packet to check
       * @return {@code true} if the packet is registered, {@code false} otherwise
       */
      public boolean containsPacket(final MinecraftPacket packet) {
        return this.packetClassToId.containsKey(packet.getClass());
      }
    }
  }

  /**
   * Packet mapping.
   */
  public record PacketMapping(int id, boolean encodeOnly) {
  }

  private static PacketMapping map(int id, boolean encodeOnly) {
    return new PacketMapping(id, encodeOnly);
  }

}
