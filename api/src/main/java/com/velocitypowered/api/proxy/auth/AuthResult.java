/*
 * Copyright (C) 2026 Velocity Contributors
 *
 * The Velocity API is licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in the api top-level directory.
 */

package com.velocitypowered.api.proxy.auth;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.util.GameProfile;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The result of {@link AuthHandler#authenticate}: the profile of the authenticated player, or
 * the reason to disconnect.
 */
public final class AuthResult {

  private final @Nullable GameProfile profile;
  private final @Nullable Component reason;

  private AuthResult(@Nullable GameProfile profile, @Nullable Component reason) {
    this.profile = profile;
    this.reason = reason;
  }

  /**
   * Lets the player in with the given profile.
   *
   * @param profile the profile of the authenticated player
   * @return the result
   */
  public static AuthResult allowed(GameProfile profile) {
    Preconditions.checkNotNull(profile, "profile");
    return new AuthResult(profile, null);
  }

  /**
   * Disconnects the player.
   *
   * @param reason the reason shown to the player
   * @return the result
   */
  public static AuthResult denied(Component reason) {
    Preconditions.checkNotNull(reason, "reason");
    return new AuthResult(null, reason);
  }

  /**
   * Returns whether the player is authenticated.
   *
   * @return true if the player is let in
   */
  public boolean isAllowed() {
    return profile != null;
  }

  /**
   * Returns the profile of the authenticated player.
   *
   * @return the profile, empty if the player is denied
   */
  public Optional<GameProfile> getProfile() {
    return Optional.ofNullable(profile);
  }

  /**
   * Returns the reason to disconnect the player.
   *
   * @return the reason, empty if the player is let in
   */
  public Optional<Component> getReason() {
    return Optional.ofNullable(reason);
  }

  @Override
  public String toString() {
    return profile != null ? "allowed " + profile.getName() : "denied";
  }
}
