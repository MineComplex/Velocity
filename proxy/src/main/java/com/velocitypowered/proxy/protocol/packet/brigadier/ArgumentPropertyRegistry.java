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

import static com.velocitypowered.proxy.protocol.packet.brigadier.ArgumentIdentifier.id;
import static com.velocitypowered.proxy.protocol.packet.brigadier.DoubleArgumentPropertySerializer.DOUBLE;
import static com.velocitypowered.proxy.protocol.packet.brigadier.EmptyArgumentPropertySerializer.EMPTY;
import static com.velocitypowered.proxy.protocol.packet.brigadier.FloatArgumentPropertySerializer.FLOAT;
import static com.velocitypowered.proxy.protocol.packet.brigadier.IntegerArgumentPropertySerializer.INTEGER;
import static com.velocitypowered.proxy.protocol.packet.brigadier.LongArgumentPropertySerializer.LONG;
import static com.velocitypowered.proxy.protocol.packet.brigadier.ModArgumentPropertySerializer.MOD;
import static com.velocitypowered.proxy.protocol.packet.brigadier.StringArgumentPropertySerializer.STRING;

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
    ArgumentIdentifier identifier = readIdentifier(buf);

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
      writeIdentifier(buf, property.getIdentifier());
      if (property.getResult() != null) {
        property.getSerializer().serialize(property.getResult(), buf, protocolVersion);
      }
    } else if (type instanceof ModArgumentProperty) {
      ModArgumentProperty property = (ModArgumentProperty) type;
      writeIdentifier(buf, property.getIdentifier());
      buf.writeBytes(property.getData());
    } else {
      ArgumentPropertySerializer serializer = byClass.get(type.getClass());
      ArgumentIdentifier id = classToId.get(type.getClass());
      if (serializer == null || id == null) {
        throw new IllegalArgumentException("Don't know how to serialize "
            + type.getClass().getName());
      }
      writeIdentifier(buf, id);
      serializer.serialize(type, buf, protocolVersion);
    }
  }

  /**
   * Writes the {@link ArgumentIdentifier} to a buffer.
   *
   * @param buf             the buffer to write to
   * @param identifier      the identifier to write
   */
  public static void writeIdentifier(ByteBuf buf, ArgumentIdentifier identifier) {
    ProtocolUtils.writeVarInt(buf, identifier.getId());
  }

  /**
   * Reads the {@link ArgumentIdentifier} from a buffer.
   *
   * @param buf             the buffer to write to
   * @return the identifier read from the buffer
   */
  public static @NotNull ArgumentIdentifier readIdentifier(ByteBuf buf) {
    int id = ProtocolUtils.readVarInt(buf);
    for (ArgumentIdentifier i : byIdentifier.keySet()) {
      if (i.getId() == id) {
        return i;
      }
    }
    throw new IllegalArgumentException("Argument type identifier " + id + " unknown.");
  }

  static {
    // Base Brigadier argument types
    register(id("brigadier:bool", 0), BoolArgumentType.class,
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
    register(id("brigadier:float", 1), FloatArgumentType.class, FLOAT);
    register(id("brigadier:double", 2), DoubleArgumentType.class, DOUBLE);
    register(id("brigadier:integer", 3), IntegerArgumentType.class, INTEGER);
    register(id("brigadier:long", 4), LongArgumentType.class, LONG);
    register(id("brigadier:string", 5), StringArgumentType.class, STRING);

    empty(id("minecraft:entity", 6), ByteArgumentPropertySerializer.BYTE);
    empty(id("minecraft:game_profile", 7));
    empty(id("minecraft:block_pos", 8));
    empty(id("minecraft:column_pos", 9));
    empty(id("minecraft:vec3", 10));
    empty(id("minecraft:vec2", 11));
    empty(id("minecraft:block_state", 12));
    empty(id("minecraft:block_predicate", 13));
    empty(id("minecraft:item_stack", 14));
    empty(id("minecraft:item_predicate", 15));
    empty(id("minecraft:component", 18));
    empty(id("minecraft:style", 19));
    empty(id("minecraft:message", 20));
    empty(id("minecraft:nbt_compound_tag", 21));
    empty(id("minecraft:nbt_tag", 22));
    empty(id("minecraft:nbt_path", 23));
    empty(id("minecraft:objective", 24));
    empty(id("minecraft:objective_criteria", 25));
    empty(id("minecraft:operation", 26));
    empty(id("minecraft:particle", 27));
    empty(id("minecraft:angle", 28));
    empty(id("minecraft:rotation", 29));
    empty(id("minecraft:scoreboard_slot", 30));
    empty(id("minecraft:score_holder", 31),
        ByteArgumentPropertySerializer.BYTE);
    empty(id("minecraft:swizzle", 32));
    empty(id("minecraft:team", 33));
    empty(id("minecraft:item_slot", 34));
    empty(id("minecraft:item_slots", 35));
    empty(id("minecraft:resource_location", 36));
    empty(id("minecraft:function", 37));
    empty(id("minecraft:entity_anchor", 38));
    empty(id("minecraft:int_range", 39));
    empty(id("minecraft:float_range", 40));
    empty(id("minecraft:dimension", 41));
    empty(id("minecraft:gamemode", 42));

    empty(id("minecraft:time", 43), TimeArgumentSerializer.TIME);

    register(id("minecraft:resource_or_tag", 44), RegistryKeyArgument.class, RegistryKeyArgumentSerializer.REGISTRY);
    register(id("minecraft:resource_or_tag_key", 45),
        RegistryKeyArgumentList.ResourceOrTagKey.class,
        RegistryKeyArgumentList.ResourceOrTagKey.Serializer.REGISTRY);
    register(id("minecraft:resource", 46),
        RegistryKeyArgument.class, RegistryKeyArgumentSerializer.REGISTRY);
    register(id("minecraft:resource_key", 47),
        RegistryKeyArgumentList.ResourceKey.class,
        RegistryKeyArgumentList.ResourceKey.Serializer.REGISTRY);
    register(id("minecraft:resource_selector", 48),
        RegistryKeyArgumentList.ResourceSelector.class,
        RegistryKeyArgumentList.ResourceSelector.Serializer.REGISTRY);

    empty(id("minecraft:template_mirror", 49));
    empty(id("minecraft:template_rotation", 50));
    empty(id("minecraft:heightmap", 51));

    empty(id("minecraft:uuid", 61));

    empty(id("minecraft:loot_table", 52));
    empty(id("minecraft:loot_predicate", 53));
    empty(id("minecraft:loot_modifier", 54));

    empty(id("minecraft:hex_color", 17));
    empty(id("minecraft:dialog", 58));
    empty(id("minecraft:team_color", 16));
    empty(id("minecraft:context_float_provider", 55));
    empty(id("minecraft:context_int_provider", 56));
    empty(id("minecraft:slot_source", 57));
    empty(id("minecraft:feature", 59));
    empty(id("minecraft:swing_animation", 60));

    // Crossstitch support
    register(id("crossstitch:mod_argument", -256), ModArgumentProperty.class, MOD);
  }
}