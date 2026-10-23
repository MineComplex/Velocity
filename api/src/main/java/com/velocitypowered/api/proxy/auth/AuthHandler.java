/*
 * Copyright (C) 2026 Velocity Contributors
 *
 * The Velocity API is licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in the api top-level directory.
 */

package com.velocitypowered.api.proxy.auth;

import com.velocitypowered.api.proxy.InboundConnection;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Authenticates players logging into the proxy. There is no Mojang session server: once
 * encryption is enabled, the proxy asks the client for its token on the {@value #TOKEN_CHANNEL}
 * login channel and passes it to this handler.
 *
 * <p>A plugin sets the handler with
 * {@link com.velocitypowered.api.proxy.ProxyServer#setAuthHandler(AuthHandler)}. While no
 * handler is set, nobody can join.</p>
 */
@FunctionalInterface
public interface AuthHandler {

  /**
   * Login plugin channel on which the client sends its token as UTF-8 bytes.
   */
  String TOKEN_CHANNEL = "minecomplex:auth";

  /**
   * Authenticates a player. It is called on the event loop of the connection, so slow checks
   * must run asynchronously.
   *
   * @param connection the connection logging in, already encrypted
   * @param username the name sent by the client
   * @param token the token sent by the client, or {@code null} if it sent none
   * @return the result, which may complete on any thread
   */
  CompletableFuture<AuthResult> authenticate(InboundConnection connection, String username,
      @Nullable String token);
}
