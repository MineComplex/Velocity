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

package com.velocitypowered.proxy.protocol.packet.brigadier;

import static com.velocitypowered.api.network.ProtocolVersion.MINECRAFT_26_3;
import static com.velocitypowered.proxy.protocol.packet.brigadier.ArgumentIdentifier.id;
import static com.velocitypowered.proxy.protocol.packet.brigadier.ArgumentIdentifier.mapSet;
import static com.velocitypowered.proxy.protocol.packet.brigadier.DoubleArgumentPropertySerializer.DOUBLE;
import static com.velocitypowered.proxy.protocol.packet.brigadier.EmptyArgumentPropertySerializer.EMPTY;
import static com.velocitypowered.proxy.protocol.packet.brigadier.FloatArgumentPropertySerializer.FLOAT;
import static com.velocitypowered.proxy.protocol.packet.brigadier.IntegerArgumentPropertySerializer.INTEGER;
import static com.velocitypowered.proxy.protocol.packet.brigadier.LongArgumentPropertySerializer.LONG;
import static com.velocitypowered.proxy.protocol.packet.brigadier.ModArgumentPropertySerializer.MOD;
import static com.velocitypowered.proxy.protocol.packet.brigadier.StringArgumentPropertySerializer.STRING;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ArgumentPropertyRegistry {

  private ArgumentPropertyRegistry() {
    throw new AssertionError();
  }

  private static final Map<ArgumentIdentifier, ArgumentPropertySerializer<?>> byIdentifier =
      new HashMap<>();
  private static final Map<Class<? extends ArgumentType>,
      ArgumentPropertySerializer<?>> byClass = new HashMap<>();
  private static final Map<Class<? extends ArgumentType>, ArgumentIdentifier> classToId =
      new HashMap<>();

  private static <T extends ArgumentType<?>> void register(ArgumentIdentifier identifier,
                                                           Class<T> klazz, ArgumentPropertySerializer<T> serializer) {
    byIdentifier.put(identifier, serializer);
    byClass.put(klazz, serializer);
    classToId.put(klazz, identifier);
  }

  private static <T> void empty(ArgumentIdentifier identifier) {
    empty(identifier, EMPTY);
  }

  private static <T> void empty(ArgumentIdentifier identifier,
                                ArgumentPropertySerializer<T> serializer) {
    byIdentifier.put(identifier, serializer);
  }

  /**
   * Deserializes the {@link ArgumentType}.
   *
   * @param buf the buffer to deserialize
   * @return the deserialized {@link ArgumentType}
   */
  public static ArgumentType<?> deserialize(ByteBuf buf, ProtocolVersion protocolVersion) {
    ArgumentIdentifier identifier = readIdentifier(buf, protocolVersion);

    ArgumentPropertySerializer<?> serializer = byIdentifier.get(identifier);
    Object result = serializer.deserialize(buf, protocolVersion);

    if (result instanceof ArgumentType) {
      return (ArgumentType<?>) result;
    } else {
      return new PassthroughProperty(identifier, serializer, result);
    }
  }

  /**
   * Serializes the {@code type} into the provided {@code buf}.
   *
   * @param buf  the buffer to serialize into
   * @param type the type to serialize
   */
  public static void serialize(ByteBuf buf, ArgumentType<?> type,
                               ProtocolVersion protocolVersion) {
    if (type instanceof PassthroughProperty) {
      PassthroughProperty property = (PassthroughProperty) type;
      writeIdentifier(buf, property.getIdentifier(), protocolVersion);
      if (property.getResult() != null) {
        property.getSerializer().serialize(property.getResult(), buf, protocolVersion);
      }
    } else if (type instanceof ModArgumentProperty) {
      ModArgumentProperty property = (ModArgumentProperty) type;
      writeIdentifier(buf, property.getIdentifier(), protocolVersion);
      buf.writeBytes(property.getData());
    } else {
      ArgumentPropertySerializer serializer = byClass.get(type.getClass());
      ArgumentIdentifier id = classToId.get(type.getClass());
      if (serializer == null || id == null) {
        throw new IllegalArgumentException("Don't know how to serialize "
            + type.getClass().getName());
      }
      writeIdentifier(buf, id, protocolVersion);
      serializer.serialize(type, buf, protocolVersion);
    }
  }

  /**
   * Writes the {@link ArgumentIdentifier} to a version-specific buffer.
   *
   * @param buf             the buffer to write to
   * @param identifier      the identifier to write
   * @param protocolVersion the protocol version to use
   */
  public static void writeIdentifier(ByteBuf buf, ArgumentIdentifier identifier,
                                     ProtocolVersion protocolVersion) {
    Integer id = identifier.getIdByProtocolVersion(protocolVersion);
    Preconditions.checkNotNull(id, "Don't know how to serialize type " + identifier);
    ProtocolUtils.writeVarInt(buf, id);
  }

  /**
   * Reads the {@link ArgumentIdentifier} from a version-specific buffer.
   *
   * @param buf             the buffer to write to
   * @param protocolVersion the protocol version to use
   * @return the identifier read from the buffer
   */
  public static @NotNull ArgumentIdentifier readIdentifier(ByteBuf buf, ProtocolVersion protocolVersion) {
    int id = ProtocolUtils.readVarInt(buf);
    for (ArgumentIdentifier i : byIdentifier.keySet()) {
      Integer v = i.getIdByProtocolVersion(protocolVersion);
      if (v != null && v == id) {
        return i;
      }
    }
    throw new IllegalArgumentException("Argument type identifier " + id + " unknown.");
  }

  static {
    // Base Brigadier argument types
    register(id("brigadier:bool", mapSet(MINECRAFT_26_3, 0)), BoolArgumentType.class,
        new ArgumentPropertySerializer<>() {
          @Override
          public BoolArgumentType deserialize(ByteBuf buf, ProtocolVersion protocolVersion) {
            return BoolArgumentType.bool();
          }

          @Override
          public void serialize(BoolArgumentType object, ByteBuf buf,
                                ProtocolVersion protocolVersion) {

          }
        });
    register(id("brigadier:float", mapSet(MINECRAFT_26_3, 1)), FloatArgumentType.class, FLOAT);
    register(id("brigadier:double", mapSet(MINECRAFT_26_3, 2)), DoubleArgumentType.class, DOUBLE);
    register(id("brigadier:integer", mapSet(MINECRAFT_26_3, 3)), IntegerArgumentType.class, INTEGER);
    register(id("brigadier:long", mapSet(MINECRAFT_26_3, 4)), LongArgumentType.class, LONG);
    register(id("brigadier:string", mapSet(MINECRAFT_26_3, 5)), StringArgumentType.class, STRING);

    empty(id("minecraft:entity", mapSet(MINECRAFT_26_3, 6)), ByteArgumentPropertySerializer.BYTE);
    empty(id("minecraft:game_profile", mapSet(MINECRAFT_26_3, 7)));
    empty(id("minecraft:block_pos", mapSet(MINECRAFT_26_3, 8)));
    empty(id("minecraft:column_pos", mapSet(MINECRAFT_26_3, 9)));
    empty(id("minecraft:vec3", mapSet(MINECRAFT_26_3, 10)));
    empty(id("minecraft:vec2", mapSet(MINECRAFT_26_3, 11)));
    empty(id("minecraft:block_state", mapSet(MINECRAFT_26_3, 12)));
    empty(id("minecraft:block_predicate", mapSet(MINECRAFT_26_3, 13)));
    empty(id("minecraft:item_stack", mapSet(MINECRAFT_26_3, 14)));
    empty(id("minecraft:item_predicate", mapSet(MINECRAFT_26_3, 15)));
    empty(id("minecraft:component", mapSet(MINECRAFT_26_3, 18)));
    empty(id("minecraft:style", mapSet(MINECRAFT_26_3, 19)));
    empty(id("minecraft:message", mapSet(MINECRAFT_26_3, 20)));
    empty(id("minecraft:nbt_compound_tag", mapSet(MINECRAFT_26_3, 21)));
    empty(id("minecraft:nbt_tag", mapSet(MINECRAFT_26_3, 22)));
    empty(id("minecraft:nbt_path", mapSet(MINECRAFT_26_3, 23)));
    empty(id("minecraft:objective", mapSet(MINECRAFT_26_3, 24)));
    empty(id("minecraft:objective_criteria", mapSet(MINECRAFT_26_3, 25)));
    empty(id("minecraft:operation", mapSet(MINECRAFT_26_3, 26)));
    empty(id("minecraft:particle", mapSet(MINECRAFT_26_3, 27)));
    empty(id("minecraft:angle", mapSet(MINECRAFT_26_3, 28)));
    empty(id("minecraft:rotation", mapSet(MINECRAFT_26_3, 29)));
    empty(id("minecraft:scoreboard_slot", mapSet(MINECRAFT_26_3, 30)));
    empty(id("minecraft:score_holder", mapSet(MINECRAFT_26_3, 31)),
        ByteArgumentPropertySerializer.BYTE);
    empty(id("minecraft:swizzle", mapSet(MINECRAFT_26_3, 32)));
    empty(id("minecraft:team", mapSet(MINECRAFT_26_3, 33)));
    empty(id("minecraft:item_slot", mapSet(MINECRAFT_26_3, 34)));
    empty(id("minecraft:item_slots", mapSet(MINECRAFT_26_3, 35)));
    empty(id("minecraft:resource_location", mapSet(MINECRAFT_26_3, 36)));
    empty(id("minecraft:function", mapSet(MINECRAFT_26_3, 37)));
    empty(id("minecraft:entity_anchor", mapSet(MINECRAFT_26_3, 38)));
    empty(id("minecraft:int_range", mapSet(MINECRAFT_26_3, 39)));
    empty(id("minecraft:float_range", mapSet(MINECRAFT_26_3, 40)));
    empty(id("minecraft:dimension", mapSet(MINECRAFT_26_3, 41)));
    empty(id("minecraft:gamemode", mapSet(MINECRAFT_26_3, 42)));

    empty(id("minecraft:time", mapSet(MINECRAFT_26_3, 43)), TimeArgumentSerializer.TIME);

    register(id("minecraft:resource_or_tag", mapSet(MINECRAFT_26_3, 44)), RegistryKeyArgument.class, RegistryKeyArgumentSerializer.REGISTRY);
    register(id("minecraft:resource_or_tag_key", mapSet(MINECRAFT_26_3, 45)),
        RegistryKeyArgumentList.ResourceOrTagKey.class,
        RegistryKeyArgumentList.ResourceOrTagKey.Serializer.REGISTRY);
    register(id("minecraft:resource", mapSet(MINECRAFT_26_3, 46)),
        RegistryKeyArgument.class, RegistryKeyArgumentSerializer.REGISTRY);
    register(id("minecraft:resource_key", mapSet(MINECRAFT_26_3, 47)),
        RegistryKeyArgumentList.ResourceKey.class,
        RegistryKeyArgumentList.ResourceKey.Serializer.REGISTRY);
    register(id("minecraft:resource_selector", mapSet(MINECRAFT_26_3, 48)),
        RegistryKeyArgumentList.ResourceSelector.class,
        RegistryKeyArgumentList.ResourceSelector.Serializer.REGISTRY);

    empty(id("minecraft:template_mirror", mapSet(MINECRAFT_26_3, 49)));
    empty(id("minecraft:template_rotation", mapSet(MINECRAFT_26_3, 50)));
    empty(id("minecraft:heightmap", mapSet(MINECRAFT_26_3, 51)));

    empty(id("minecraft:uuid", mapSet(MINECRAFT_26_3, 61)));

    empty(id("minecraft:loot_table", mapSet(MINECRAFT_26_3, 52)));
    empty(id("minecraft:loot_predicate", mapSet(MINECRAFT_26_3, 53)));
    empty(id("minecraft:loot_modifier", mapSet(MINECRAFT_26_3, 54)));

    empty(id("minecraft:hex_color", mapSet(MINECRAFT_26_3, 17)));
    empty(id("minecraft:dialog", mapSet(MINECRAFT_26_3, 58)));
    empty(id("minecraft:team_color", mapSet(MINECRAFT_26_3, 16)));
    empty(id("minecraft:context_float_provider", mapSet(MINECRAFT_26_3, 55)));
    empty(id("minecraft:context_int_provider", mapSet(MINECRAFT_26_3, 56)));
    empty(id("minecraft:slot_source", mapSet(MINECRAFT_26_3, 57)));
    empty(id("minecraft:feature", mapSet(MINECRAFT_26_3, 59)));
    empty(id("minecraft:swing_animation", mapSet(MINECRAFT_26_3, 60)));

    // Crossstitch support
    register(id("crossstitch:mod_argument", mapSet(MINECRAFT_26_3, -256)), ModArgumentProperty.class, MOD);
  }
}