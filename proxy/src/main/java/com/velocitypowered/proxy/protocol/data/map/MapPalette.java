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

package com.velocitypowered.proxy.protocol.data.map;


import java.awt.image.BufferedImage;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MapPalette {

  /**
   * @deprecated Use {@link java.awt.Color#WHITE} instead.
   */
  @Deprecated
  public static final byte WHITE = 34;
  public static final byte TRANSPARENT = 0;
  private static final byte[] MAIN_BUFFER = readBuffer("/mapping/colors_main_map");
  private static final byte[] REMAP_BUFFER = readBuffer("/mapping/colors_minecraft_1_17_map");

  private static byte[] readBuffer(String filename) {
    try (InputStream stream = MapPalette.class.getResourceAsStream(filename)) {
      return Objects.requireNonNull(stream).readAllBytes();
    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  /**
   * Convert an image to palette indices for 26.3.
   *
   * @param image the image to convert
   * @return the palette indices
   */
  public static int[] imageToBytes(BufferedImage image) {
    int[] result = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    return imageToBytes(result, result);
  }

  /**
   * Convert an image to a byte[] using the palette.
   *
   * @param image   The image to convert.
   * @return A byte[] containing the pixels of the image.
   */
  public static int[] imageToBytes(int[] image) {
    return imageToBytes(image, new int[image.length]);
  }

  /**
   * Convert an image to a byte[] using the palette.
   *
   * @param from    The image to convert.
   * @param to      Output image.
   * @return A byte[] containing the pixels of the image.
   */
  public static int[] imageToBytes(int[] from, int[] to) {
    for (int i = 0; i < from.length; ++i) {
      to[i] = tryFastMatchColor(from[i]);
    }

    return to;
  }

  /**
   * Convert an image to a byte[] using the palette.
   *
   * @param from    The image to convert.
   * @param to      Output image.
   * @return A byte[] containing the pixels of the image.
   */
  public static byte[] imageToBytes(int[] from, byte[] to) {
    for (int i = 0; i < from.length; ++i) {
      to[i] = tryFastMatchColor(from[i]);
    }

    return to;
  }

  /**
   * Get the index of the closest matching color in the palette to the given
   * color. Uses caching and downscaling of color values.
   *
   * @param rgb The Color to match.
   * @return The index in the palette.
   */
  public static byte tryFastMatchColor(int rgb) {
    if (getAlpha(rgb) < 128) {
      return TRANSPARENT;
    }
    return MAIN_BUFFER[rgb & 0xFFFFFF];
  }

  private static int getAlpha(int rgb) {
    return (rgb & 0xFF000000) >>> 24;
  }

  /**
   * Copy palette indices to the 26.3 canvas palette
   *
   * @param image   The image to convert.
   * @return A byte[] containing the pixels of the image.
   */
  public static int[] convertImage(int[] image) {
    return convertImage(image, new int[image.length]);
  }

  /**
   * Copy palette indices to the 26.3 canvas palette
   *
   * @param from    The image to convert.
   * @param to      Output image.
   * @return A byte[] containing the pixels of the image.
   */
  public static int[] convertImage(int[] from, int[] to) {
    byte[] remapBuffer = REMAP_BUFFER;
    for (int i = 0; i < from.length; ++i) {
      to[i] = remapByte(remapBuffer, (byte) from[i]);
    }

    return to;
  }

  /**
   * Copy palette indices to the 26.3 canvas palette
   *
   * @param from    The image to convert.
   * @param to      Output image.
   * @return A byte[] containing the pixels of the image.
   */
  public static byte[] convertImage(byte[] from, byte[] to) {
    byte[] remapBuffer = REMAP_BUFFER;
    for (int i = 0; i < from.length; ++i) {
      to[i] = remapByte(remapBuffer, from[i]);
    }

    return to;
  }

  /**
   * Copy palette indices to the 26.3 canvas palette
   *
   * @param from    The image to convert.
   * @param to      Output image.
   * @return A byte[] containing the pixels of the image.
   */
  public static byte[] convertImage(int[] from, byte[] to) {
    byte[] remapBuffer = REMAP_BUFFER;
    for (int i = 0; i < from.length; ++i) {
      to[i] = remapByte(remapBuffer, (byte) from[i]);
    }

    return to;
  }

  private static byte remapByte(byte[] remapBuffer, byte oldByte) {
    return remapBuffer[Byte.toUnsignedInt(oldByte)];
  }

}
