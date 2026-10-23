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

package com.velocitypowered.proxy.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.Expose;
import com.velocitypowered.api.proxy.config.ProxyConfig;
import com.velocitypowered.api.util.Favicon;
import com.velocitypowered.proxy.util.AddressUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Velocity's configuration.
 */
public class VelocityConfiguration implements ProxyConfig {

  private static final Logger logger = LogManager.getLogger(VelocityConfiguration.class);

  @Expose
  private String bind = "0.0.0.0:25565";
  @Expose
  private String motd = "<aqua>A Velocity Server";
  @Expose
  private int showMaxPlayers = 500;
  private byte[] forwardingSecret = generateRandomString(12).getBytes(StandardCharsets.UTF_8);
  @Expose
  private boolean announceForge = false;
  @Expose
  private boolean onlineModeKickExistingPlayers = false;
  @Expose
  private boolean samplePlayersInPing = false;
  private final Servers servers;
  @Expose
  private final Advanced advanced;
  @Expose
  private boolean enablePlayerAddressLogging = true;
  private @MonotonicNonNull Component motdAsComponent;
  private @Nullable Favicon favicon;
  @Expose
  private PacketLimiterConfig packetLimiterConfig = PacketLimiterConfig.DEFAULT;

  private VelocityConfiguration(Servers servers, Advanced advanced) {
    this.servers = servers;
    this.advanced = advanced;
  }

  private VelocityConfiguration(String bind, String motd, int showMaxPlayers,
                                boolean announceForge, byte[] forwardingSecret,
                                boolean onlineModeKickExistingPlayers,
                                boolean samplePlayersInPing, boolean enablePlayerAddressLogging, Servers servers,
                                Advanced advanced, PacketLimiterConfig packetLimiterConfig) {
    this.bind = bind;
    this.motd = motd;
    this.showMaxPlayers = showMaxPlayers;
    this.announceForge = announceForge;
    this.forwardingSecret = forwardingSecret;
    this.onlineModeKickExistingPlayers = onlineModeKickExistingPlayers;
    this.samplePlayersInPing = samplePlayersInPing;
    this.enablePlayerAddressLogging = enablePlayerAddressLogging;
    this.servers = servers;
    this.advanced = advanced;
    this.packetLimiterConfig = packetLimiterConfig;
  }

  /**
   * Attempts to validate the configuration.
   *
   * @return {@code true} if the configuration is sound, {@code false} if not
   */
  public boolean validate() {
    boolean valid = true;

    if (bind.isEmpty()) {
      logger.error("'bind' option is empty.");
      valid = false;
    } else {
      try {
        AddressUtil.parseAddress(bind);
      } catch (IllegalArgumentException e) {
        logger.error("'bind' option does not specify a valid IP address.", e);
        valid = false;
      }
    }

    if (forwardingSecret == null || forwardingSecret.length == 0) {
      logger.error("You don't have a forwarding secret set. This is required for security.");
      valid = false;
    }

    if (servers.getServers().isEmpty()) {
      logger.warn("You don't have any servers configured.");
    }

    for (Map.Entry<String, String> entry : servers.getServers().entrySet()) {
      try {
        AddressUtil.parseAddress(entry.getValue());
      } catch (IllegalArgumentException e) {
        logger.error("Server {} does not have a valid IP address.", entry.getKey(), e);
        valid = false;
      }
    }

    for (String s : servers.getAttemptConnectionOrder()) {
      if (!servers.getServers().containsKey(s)) {
        logger.error("Fallback server " + s + " is not registered in your configuration!");
        valid = false;
      }
    }

    try {
      getMotd();
    } catch (Exception e) {
      logger.error("Can't parse your MOTD", e);
      valid = false;
    }

    if (advanced.compressionLevel < -1 || advanced.compressionLevel > 9) {
      logger.error("Invalid compression level {}", advanced.compressionLevel);
      valid = false;
    } else if (advanced.compressionLevel == 0) {
      logger.warn("ALL packets going through the proxy will be uncompressed. This will increase "
          + "bandwidth usage.");
    }

    if (advanced.compressionThreshold < -1) {
      logger.error("Invalid compression threshold {}", advanced.compressionLevel);
      valid = false;
    } else if (advanced.compressionThreshold == 0) {
      logger.warn("ALL packets going through the proxy will be compressed. This will compromise "
          + "throughput and increase CPU usage!");
    }

    if (advanced.loginRatelimit < 0) {
      logger.error("Invalid login ratelimit {}ms", advanced.loginRatelimit);
      valid = false;
    }

    if (advanced.commandRateLimit < 0) {
      logger.error("Invalid command rate limit {}", advanced.commandRateLimit);
      valid = false;
    }

    loadFavicon();

    return valid;
  }

  private void loadFavicon() {
    Path faviconPath = Path.of("server-icon.png");
    if (Files.exists(faviconPath)) {
      try {
        this.favicon = Favicon.create(faviconPath);
      } catch (Exception e) {
        logger.info("Unable to load your server-icon.png, continuing without it.", e);
      }
    }
  }

  public InetSocketAddress getBind() {
    return AddressUtil.parseAndResolveAddress(bind);
  }

  @Override
  public Component getMotd() {
    if (motdAsComponent == null) {
      motdAsComponent = MiniMessage.miniMessage().deserialize(motd);
    }
    return motdAsComponent;
  }

  @Override
  public int getShowMaxPlayers() {
    return showMaxPlayers;
  }

  /**
   * Players are always authenticated, by the token of their client.
   */
  @Override
  public boolean isOnlineMode() {
    return true;
  }

  public byte[] getForwardingSecret() {
    return forwardingSecret.clone();
  }

  @Override
  public Map<String, String> getServers() {
    return servers.getServers();
  }

  @Override
  public List<String> getAttemptConnectionOrder() {
    return servers.getAttemptConnectionOrder();
  }

  @Override
  public int getCompressionThreshold() {
    return advanced.getCompressionThreshold();
  }

  @Override
  public int getCompressionLevel() {
    return advanced.getCompressionLevel();
  }

  @Override
  public int getLoginRatelimit() {
    return advanced.getLoginRatelimit();
  }

  @Override
  public Optional<Favicon> getFavicon() {
    return Optional.ofNullable(favicon);
  }

  @Override
  public boolean isAnnounceForge() {
    return announceForge;
  }

  @Override
  public int getConnectTimeout() {
    return advanced.getConnectionTimeout();
  }

  @Override
  public int getReadTimeout() {
    return advanced.getReadTimeout();
  }

  @Override
  public int getCommandRatelimit() {
    return advanced.getCommandRateLimit();
  }

  @Override
  public int getTabCompleteRatelimit() {
    return advanced.getTabCompleteRateLimit();
  }

  @Override
  public int getKickAfterRateLimitedTabCompletes() {
    return advanced.getKickAfterRateLimitedTabCompletes();
  }

  @Override
  public boolean isForwardCommandsIfRateLimited() {
    return advanced.isForwardCommandsIfRateLimited();
  }

  @Override
  public int getKickAfterRateLimitedCommands() {
    return advanced.getKickAfterRateLimitedCommands();
  }

  public boolean isProxyProtocol() {
    return advanced.isProxyProtocol();
  }

  public void setProxyProtocol(boolean proxyProtocol) {
    advanced.setProxyProtocol(proxyProtocol);
  }

  public boolean useTcpFastOpen() {
    return advanced.isTcpFastOpen();
  }

  public boolean getSamplePlayersInPing() {
    return samplePlayersInPing;
  }

  public boolean isPlayerAddressLoggingEnabled() {
    return enablePlayerAddressLogging;
  }

  public boolean isShowPingRequests() {
    return advanced.isShowPingRequests();
  }

  public boolean isFailoverOnUnexpectedServerDisconnect() {
    return advanced.isFailoverOnUnexpectedServerDisconnect();
  }

  public boolean isAnnounceProxyCommands() {
    return advanced.isAnnounceProxyCommands();
  }

  public boolean isLogCommandExecutions() {
    return advanced.isLogCommandExecutions();
  }

  public boolean isLogPlayerConnections() {
    return advanced.isLogPlayerConnections();
  }

  public boolean isEnableReusePort() {
    return advanced.isEnableReusePort();
  }

  public PacketLimiterConfig getPacketLimiterConfig() {
    return packetLimiterConfig;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("bind", bind)
        .add("motd", motd)
        .add("showMaxPlayers", showMaxPlayers)
        .add("forwardingSecret", forwardingSecret)
        .add("announceForge", announceForge)
        .add("servers", servers)
        .add("advanced", advanced)
        .add("favicon", favicon)
        .add("enablePlayerAddressLogging", enablePlayerAddressLogging)
        .add("packetLimiterConfig", packetLimiterConfig)
        .toString();
  }

  /**
   * Reads the Velocity configuration from {@code path}.
   *
   * @param path the path to read from
   * @return the deserialized Velocity configuration
   * @throws IOException if we could not read from the {@code path}.
   */
  @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
      justification = "I looked carefully and there's no way SpotBugs is right.")
  public static VelocityConfiguration read(Path path) throws IOException {
    URL defaultConfigLocation = VelocityConfiguration.class.getClassLoader()
        .getResource("default-velocity.toml");
    if (defaultConfigLocation == null) {
      throw new RuntimeException("Default configuration file does not exist.");
    }

    // Create the forwarding-secret file on first-time startup if it doesn't exist
    final Path defaultForwardingSecretPath = Path.of("forwarding.secret");
    if (Files.notExists(path) && Files.notExists(defaultForwardingSecretPath)) {
      Files.writeString(defaultForwardingSecretPath, generateRandomString(12));
    }

    try (final CommentedFileConfig config = CommentedFileConfig.builder(path)
        .defaultData(defaultConfigLocation)
        .preserveInsertionOrder()
        .sync()
        .build()
    ) {
      config.load();

      String forwardingSecretString = System.getenv().getOrDefault(
          "VELOCITY_FORWARDING_SECRET", "");
      if (forwardingSecretString.isBlank()) {
        final String forwardSecretFile = config.get("forwarding-secret-file");
        final Path secretPath = forwardSecretFile == null
            ? defaultForwardingSecretPath
            : Path.of(forwardSecretFile);
        if (Files.exists(secretPath)) {
          if (Files.isRegularFile(secretPath)) {
            forwardingSecretString = String.join("", Files.readAllLines(secretPath));
          } else {
            throw new RuntimeException(
                "The file " + forwardSecretFile + " is not a valid file or it is a directory.");
          }
        } else {
          Files.createFile(secretPath);
          Files.writeString(secretPath, forwardingSecretString = generateRandomString(12),
              StandardCharsets.UTF_8);
          logger.info("The forwarding-secret-file does not exist. A new file has been created at {}",
              forwardSecretFile);
        }
      }
      final byte[] forwardingSecret = forwardingSecretString.getBytes(StandardCharsets.UTF_8);
      final String motd = config.getOrElse("motd", "<#09add3>A Velocity Server");

      // Read the rest of the config
      final CommentedConfig serversConfig = config.get("servers");
      final CommentedConfig advancedConfig = config.get("advanced");
      final boolean samplePlayersInPing = config.getOrElse("sample-players-in-ping", false);

      final String bind = config.getOrElse("bind", "0.0.0.0:25565");
      final int maxPlayers = config.getIntOrElse("show-max-players", 500);
      final boolean announceForge = config.getOrElse("announce-forge", true);
      final boolean kickExisting = config.getOrElse("kick-existing-players", false);
      final boolean enablePlayerAddressLogging = config.getOrElse(
          "enable-player-address-logging", true);
      final PacketLimiterConfig packetLimiterConfig = PacketLimiterConfig.fromConfig(config.get("packet-limiter"));

      if (forwardingSecret.length == 0) {
        throw new RuntimeException("The forwarding-secret file must not be empty.");
      }

      return new VelocityConfiguration(
          bind,
          motd,
          maxPlayers,
          announceForge,
          forwardingSecret,
          kickExisting,
          samplePlayersInPing,
          enablePlayerAddressLogging,
          new Servers(serversConfig),
          new Advanced(advancedConfig),
          packetLimiterConfig
      );
    }
  }

  /**
   * Generates a Random String.
   *
   * @param length the required string size.
   * @return a new random string.
   */
  public static String generateRandomString(int length) {
    final String chars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
    final StringBuilder builder = new StringBuilder();
    final Random rnd = new SecureRandom();
    for (int i = 0; i < length; i++) {
      builder.append(chars.charAt(rnd.nextInt(chars.length())));
    }
    return builder.toString();
  }

  public boolean isOnlineModeKickExistingPlayers() {
    return onlineModeKickExistingPlayers;
  }

  private static class Servers {

    private Map<String, String> servers = ImmutableMap.of(
        "lobby", "127.0.0.1:30066",
        "factions", "127.0.0.1:30067",
        "minigames", "127.0.0.1:30068"
    );
    private List<String> attemptConnectionOrder = ImmutableList.of("lobby");

    private Servers() {
    }

    private Servers(CommentedConfig config) {
      if (config != null) {
        Map<String, String> servers = new HashMap<>();
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
          if (entry.getValue() instanceof String) {
            servers.put(cleanServerName(entry.getKey()), entry.getValue());
          } else {
            if (!entry.getKey().equalsIgnoreCase("try")) {
              throw new IllegalArgumentException(
                  "Server entry " + entry.getKey() + " is not a string!");
            }
          }
        }
        this.servers = ImmutableMap.copyOf(servers);
        this.attemptConnectionOrder = config.getOrElse("try", attemptConnectionOrder);
      }
    }

    private Servers(Map<String, String> servers, List<String> attemptConnectionOrder) {
      this.servers = servers;
      this.attemptConnectionOrder = attemptConnectionOrder;
    }

    private Map<String, String> getServers() {
      return servers;
    }

    public void setServers(Map<String, String> servers) {
      this.servers = servers;
    }

    public List<String> getAttemptConnectionOrder() {
      return attemptConnectionOrder;
    }

    public void setAttemptConnectionOrder(List<String> attemptConnectionOrder) {
      this.attemptConnectionOrder = attemptConnectionOrder;
    }

    /**
     * TOML requires keys to match a regex of {@code [A-Za-z0-9_-]} unless it is wrapped in quotes;
     * however, the TOML parser returns the key with the quotes so we need to clean the server name
     * before we pass it onto server registration to keep proper server name behavior.
     *
     * @param name the server name to clean
     * @return the cleaned server name
     */
    private String cleanServerName(String name) {
      return name.replace("\"", "");
    }

    @Override
    public String toString() {
      return "Servers{"
          + "servers=" + servers
          + ", attemptConnectionOrder=" + attemptConnectionOrder
          + '}';
    }
  }

  private static class Advanced {

    @Expose
    private int compressionThreshold = 256;
    @Expose
    private int compressionLevel = -1;
    @Expose
    private int loginRatelimit = 3000;
    @Expose
    private int connectionTimeout = 5000;
    @Expose
    private int readTimeout = 30000;
    @Expose
    private boolean proxyProtocol = false;
    @Expose
    private boolean tcpFastOpen = false;
    @Expose
    private boolean showPingRequests = false;
    @Expose
    private boolean failoverOnUnexpectedServerDisconnect = true;
    @Expose
    private boolean announceProxyCommands = true;
    @Expose
    private boolean logCommandExecutions = false;
    @Expose
    private boolean logPlayerConnections = true;
    @Expose
    private boolean enableReusePort = false;
    @Expose
    private int commandRateLimit = 50;
    @Expose
    private boolean forwardCommandsIfRateLimited = true;
    @Expose
    private int kickAfterRateLimitedCommands = 5;
    @Expose
    private int tabCompleteRateLimit = 50;
    @Expose
    private int kickAfterRateLimitedTabCompletes = 10;

    private Advanced() {
    }

    private Advanced(CommentedConfig config) {
      if (config != null) {
        this.compressionThreshold = config.getIntOrElse("compression-threshold", 256);
        this.compressionLevel = config.getIntOrElse("compression-level", -1);
        this.loginRatelimit = config.getIntOrElse("login-ratelimit", 3000);
        this.connectionTimeout = config.getIntOrElse("connection-timeout", 5000);
        this.readTimeout = config.getIntOrElse("read-timeout", 30000);
        if (config.contains("haproxy-protocol")) {
          this.proxyProtocol = config.getOrElse("haproxy-protocol", false);
        } else {
          this.proxyProtocol = config.getOrElse("proxy-protocol", false);
        }
        this.tcpFastOpen = config.getOrElse("tcp-fast-open", false);
        this.showPingRequests = config.getOrElse("show-ping-requests", false);
        this.failoverOnUnexpectedServerDisconnect = config
            .getOrElse("failover-on-unexpected-server-disconnect", true);
        this.announceProxyCommands = config.getOrElse("announce-proxy-commands", true);
        this.logCommandExecutions = config.getOrElse("log-command-executions", false);
        this.logPlayerConnections = config.getOrElse("log-player-connections", true);
        this.enableReusePort = config.getOrElse("enable-reuse-port", false);
        this.commandRateLimit = config.getIntOrElse("command-rate-limit", 25);
        this.forwardCommandsIfRateLimited = config.getOrElse("forward-commands-if-rate-limited", true);
        this.kickAfterRateLimitedCommands = config.getIntOrElse("kick-after-rate-limited-commands", 0);
        this.tabCompleteRateLimit = config.getIntOrElse("tab-complete-rate-limit", 10); // very lenient
        this.kickAfterRateLimitedTabCompletes = config.getIntOrElse("kick-after-rate-limited-tab-completes", 0);
      }
    }

    public int getCompressionThreshold() {
      return compressionThreshold;
    }

    public int getCompressionLevel() {
      return compressionLevel;
    }

    public int getLoginRatelimit() {
      return loginRatelimit;
    }

    public int getConnectionTimeout() {
      return connectionTimeout;
    }

    public int getReadTimeout() {
      return readTimeout;
    }

    public boolean isProxyProtocol() {
      return proxyProtocol;
    }

    public void setProxyProtocol(boolean proxyProtocol) {
      this.proxyProtocol = proxyProtocol;
    }

    public boolean isTcpFastOpen() {
      return tcpFastOpen;
    }

    public boolean isShowPingRequests() {
      return showPingRequests;
    }

    public boolean isFailoverOnUnexpectedServerDisconnect() {
      return failoverOnUnexpectedServerDisconnect;
    }

    public boolean isAnnounceProxyCommands() {
      return announceProxyCommands;
    }

    public boolean isLogCommandExecutions() {
      return logCommandExecutions;
    }

    public boolean isLogPlayerConnections() {
      return logPlayerConnections;
    }

    public boolean isEnableReusePort() {
      return enableReusePort;
    }

    public int getCommandRateLimit() {
      return commandRateLimit;
    }

    public boolean isForwardCommandsIfRateLimited() {
      return forwardCommandsIfRateLimited;
    }

    public int getKickAfterRateLimitedCommands() {
      return kickAfterRateLimitedCommands;
    }

    public int getTabCompleteRateLimit() {
      return tabCompleteRateLimit;
    }

    public int getKickAfterRateLimitedTabCompletes() {
      return kickAfterRateLimitedTabCompletes;
    }

    @Override
    public String toString() {
      return "Advanced{"
          + "compressionThreshold=" + compressionThreshold
          + ", compressionLevel=" + compressionLevel
          + ", loginRatelimit=" + loginRatelimit
          + ", connectionTimeout=" + connectionTimeout
          + ", readTimeout=" + readTimeout
          + ", proxyProtocol=" + proxyProtocol
          + ", tcpFastOpen=" + tcpFastOpen
          + ", showPingRequests=" + showPingRequests
          + ", failoverOnUnexpectedServerDisconnect=" + failoverOnUnexpectedServerDisconnect
          + ", announceProxyCommands=" + announceProxyCommands
          + ", logCommandExecutions=" + logCommandExecutions
          + ", logPlayerConnections=" + logPlayerConnections
          + ", enableReusePort=" + enableReusePort
          + '}';
    }
  }

  /**
   * Configuration for packet limiting.
   *
   * @param interval                the interval in seconds to measure packets over
   * @param pps                     the maximum number of packets per second allowed
   * @param bytes                   the maximum number of bytes per second allowed
   * @param bytesAfterDecompression the maximum number of decompressed bytes per second allowed
   */
  public record PacketLimiterConfig(int interval, int pps, int bytes, int bytesAfterDecompression) {
    public static PacketLimiterConfig DEFAULT = new PacketLimiterConfig(7, -1, -1, 5242880);

    /**
     * returns a PacketLimiterConfig from a config section, or the default if the section is null.
     *
     * @param config the configuration object to parse
     * @return the packet limiter config, or the default if {@code config} is null
     */
    public static PacketLimiterConfig fromConfig(CommentedConfig config) {
      if (config != null) {
        return new PacketLimiterConfig(
            config.getIntOrElse("interval", DEFAULT.interval()),
            config.getIntOrElse("packets-per-second", DEFAULT.pps()),
            config.getIntOrElse("bytes-per-second", DEFAULT.bytes()),
            config.getIntOrElse("decompressed-bytes-per-second", DEFAULT.bytesAfterDecompression())
        );
      } else {
        return DEFAULT;
      }
    }
  }
}
