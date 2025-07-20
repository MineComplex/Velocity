/*
 * Copyright (C) 2025 Velocity Contributors
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

package com.velocitypowered.proxy.protocol.data.world;

import com.velocitypowered.api.network.ProtocolVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag.Builder;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum Biome {

  PLAINS(
      "minecraft:plains",
      1,
      new Element(
          true, 0.125F, 0.8F, 0.05F, 0.4F, "plains",
          Effects.builder(7907327, 329011, 12638463, 415920)
              .moodSound(Effects.MoodSound.of(6000, 2.0, 8, "minecraft:ambient.cave"))
              .build()
      )
  ),
  SWAMP(
      "minecraft:swamp",
      6,
      new Element(
          true, -0.2F, 0.8F, 0.1F, 0.9F, "swamp",
          Effects.builder(7907327, 329011, 12638463, 415920)
              .grassColorModifier("swamp")
              .foliageColor(6975545)
              .moodSound(Effects.MoodSound.of(6000, 2.0, 8, "minecraft:ambient.cave"))
              .build()
      )
  ),
  SWAMP_HILLS(
      "minecraft:swamp_hills",
      134,
      new Element(
          true, -0.1F, 0.8F, 0.3F, 0.9F, "swamp",
          Effects.builder(7907327, 329011, 12638463, 415920)
              .grassColorModifier("swamp")
              .foliageColor(6975545)
              .moodSound(Effects.MoodSound.of(6000, 2.0, 8, "minecraft:ambient.cave"))
              .build()
      )
  ),
  NETHER_WASTES(
      "minecraft:nether_wastes",
      8,
      new Element(false, 0.1f, 2.0f, 0.2f, 0.0f, "nether",
          Effects.builder(7254527, 329011, 3344392, 4159204)
              .moodSound(Effects.MoodSound.of(6000, 2.0, 8, "minecraft:ambient.nether_wastes.mood"))
              .build()
      )
  ),
  THE_END(
      "minecraft:the_end",
      9,
      new Element(false, 0.1f, 0.5f, 0.2f, 0.5f, "the_end",
          Effects.builder(0, 10518688, 12638463, 4159204)
              .moodSound(Effects.MoodSound.of(6000, 2.0, 8, "minecraft:ambient.cave"))
              .build()
      )
  );

  private final String name;
  private final int id;
  private final Element element;

  public static CompoundBinaryTag getRegistry(ProtocolVersion version) {
    return CompoundBinaryTag.builder()
        .putString("type", "minecraft:worldgen/biome")
        .put("value", ListBinaryTag.from(Arrays.stream(Biome.values()).map(biome -> biome.encodeBiome(version)).collect(Collectors.toList())))
        .build();
  }

  public CompoundBinaryTag encodeBiome(ProtocolVersion version) {
    return CompoundBinaryTag.builder()
        .putString("name", this.name)
        .putInt("id", this.id)
        .put("element", this.element.encode(version))
        .build();
  }

  @ToString
  @AllArgsConstructor
  public static class Element {

    public final boolean hasPrecipitation;
    public final float depth;
    public final float temperature;
    public final float scale;
    public final float downfall;
    public final String category;
    public final Effects effects;

    public CompoundBinaryTag encode(ProtocolVersion version) {
      Builder tagBuilder = CompoundBinaryTag.builder()
          .putFloat("depth", depth)
          .putFloat("temperature", temperature)
          .putFloat("scale", scale).putFloat("downfall", downfall)
          .putString("category", category)
          .put("effects", effects.encode());

      if (version.lessThan(ProtocolVersion.MINECRAFT_1_19_4)) {
        tagBuilder.putString("precipitation", hasPrecipitation ? "rain" : "none");
      } else {
        tagBuilder.putBoolean("has_precipitation", hasPrecipitation);
      }

      return tagBuilder.build();
    }

  }

  @Getter
  @ToString
  @AllArgsConstructor
  public static class Effects {

    private final int skyColor;
    private final int waterFogColor;
    private final int fogColor;
    private final int waterColor;

    @Nullable
    private final Integer foliageColor;
    @Nullable
    private final String grassColorModifier;
    @Nullable
    private final Music music;
    @Nullable
    private final String ambientSound;
    @Nullable
    private final AdditionsSound additionsSound;
    @Nullable
    private final MoodSound moodSound;
    @Nullable
    private final Particle particle;

    public static EffectsBuilder builder(int skyColor, int waterFogColor, int fogColor, int waterColor) {
      return new EffectsBuilder()
          .skyColor(skyColor)
          .waterFogColor(waterFogColor)
          .fogColor(fogColor)
          .waterColor(waterColor);
    }

    public CompoundBinaryTag encode() {
      Builder result = CompoundBinaryTag.builder();

      result.putInt("sky_color", skyColor);
      result.putInt("water_fog_color", waterColor);
      result.putInt("fog_color", fogColor);
      result.putInt("water_color", waterColor);

      if (foliageColor != null) {
        result.putInt("foliage_color", foliageColor);
      }

      if (grassColorModifier != null) {
        result.putString("grass_color_modifier", grassColorModifier);
      }

      if (music != null) {
        result.put("music", music.encode());
      }

      if (ambientSound != null) {
        result.putString("ambient_sound", ambientSound);
      }

      if (additionsSound != null) {
        result.put("additions_sound", additionsSound.encode());
      }

      if (moodSound != null) {
        result.put("mood_sound", moodSound.encode());
      }

      if (particle != null) {
        result.put("particle", particle.encode());
      }

      return result.build();
    }

    @Getter
    @ToString
    @AllArgsConstructor
    public static final class MoodSound {

      private final int tickDelay;
      private final double offset;
      private final int blockSearchExtent;
      @NonNull
      private final String sound;

      public static MoodSound of(int tickDelay, double offset, int blockSearchExtent, @NonNull String sound) {
        return new MoodSound(tickDelay, offset, blockSearchExtent, sound);
      }

      public CompoundBinaryTag encode() {
        return CompoundBinaryTag.builder()
            .putInt("tick_delay", this.tickDelay)
            .putDouble("offset", this.offset)
            .putInt("block_search_extent", this.blockSearchExtent)
            .putString("sound", this.sound)
            .build();
      }

    }

    @Getter
    @ToString
    @AllArgsConstructor
    public static final class Music {

      private final boolean replaceCurrentMusic;
      @NonNull
      private final String sound;
      private final int maxDelay;
      private final int minDelay;

      public static Music of(boolean replaceCurrentMusic, @NonNull String sound, int maxDelay, int minDelay) {
        return new Music(replaceCurrentMusic, sound, maxDelay, minDelay);
      }

      public CompoundBinaryTag encode() {
        return CompoundBinaryTag.builder()
            .putBoolean("replace_current_music", this.replaceCurrentMusic)
            .putString("sound", this.sound)
            .putInt("max_delay", this.maxDelay)
            .putInt("min_delay", this.minDelay)
            .build();
      }

    }

    @ToString
    @Getter
    @AllArgsConstructor
    public static final class AdditionsSound {

      @NonNull
      private final String sound;
      private final double tickChance;

      public static AdditionsSound of(@NonNull String sound, double tickChance) {
        return new AdditionsSound(sound, tickChance);
      }

      public CompoundBinaryTag encode() {
        return CompoundBinaryTag.builder()
            .putString("sound", this.sound)
            .putDouble("tick_chance", this.tickChance)
            .build();
      }

    }

    @ToString
    @Getter
    @AllArgsConstructor
    public static final class Particle {

      private final float probability;
      @NonNull
      private final ParticleOptions options;

      public static Particle of(float probability, @NonNull ParticleOptions options) {
        return new Particle(probability, options);
      }

      public CompoundBinaryTag encode() {
        return CompoundBinaryTag.builder()
            .putFloat("probability", this.probability)
            .put("options", this.options.encode())
            .build();
      }

      @Getter
      @ToString
      @AllArgsConstructor
      public static class ParticleOptions {

        @NonNull
        private final String type;

        public CompoundBinaryTag encode() {
          return CompoundBinaryTag.builder()
              .putString("type", this.type)
              .build();
        }

      }
    }

    @ToString
    public static class EffectsBuilder {

      private int skyColor;
      private int waterFogColor;
      private int fogColor;
      private int waterColor;
      private Integer foliageColor;
      private String grassColorModifier;
      private Music music;
      private String ambientSound;
      private AdditionsSound additionsSound;
      private MoodSound moodSound;
      private Particle particle;

      public EffectsBuilder skyColor(int skyColor) {
        this.skyColor = skyColor;
        return this;
      }

      public EffectsBuilder waterFogColor(int waterFogColor) {
        this.waterFogColor = waterFogColor;
        return this;
      }

      public EffectsBuilder fogColor(int fogColor) {
        this.fogColor = fogColor;
        return this;
      }

      public EffectsBuilder waterColor(int waterColor) {
        this.waterColor = waterColor;
        return this;
      }

      public EffectsBuilder foliageColor(Integer foliageColor) {
        this.foliageColor = foliageColor;
        return this;
      }

      public EffectsBuilder grassColorModifier(String grassColorModifier) {
        this.grassColorModifier = grassColorModifier;
        return this;
      }

      public EffectsBuilder music(Music music) {
        this.music = music;
        return this;
      }

      public EffectsBuilder ambientSound(String ambientSound) {
        this.ambientSound = ambientSound;
        return this;
      }

      public EffectsBuilder additionsSound(AdditionsSound additionsSound) {
        this.additionsSound = additionsSound;
        return this;
      }

      public EffectsBuilder moodSound(MoodSound moodSound) {
        this.moodSound = moodSound;
        return this;
      }

      public EffectsBuilder particle(Particle particle) {
        this.particle = particle;
        return this;
      }

      public Effects build() {
        return new Effects(
            skyColor,
            waterFogColor,
            fogColor,
            waterColor,
            foliageColor,
            grassColorModifier,
            music,
            ambientSound,
            additionsSound,
            moodSound,
            particle
        );
      }

    }
  }
}
