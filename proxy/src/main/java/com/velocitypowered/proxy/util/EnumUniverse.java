package com.velocitypowered.proxy.util;

import java.util.HashMap;
import java.util.Map;

public class EnumUniverse {

  public static <T extends Enum<T>> Map<String, T> createProtocolLookup(T[] values) {
    Map<String, T> lookup = new HashMap<>();
    for (T value : values) {
      if (value.name().startsWith("MINECRAFT_")) {
        lookup.put(value.name().substring("MINECRAFT_".length()).replace("_", "."), value);
      }
    }
    return lookup;
  }
}
