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

import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.Arrays;

@Getter
public class MapCanvas {

  private final byte[][] canvas;
  private final int width;
  private final int height;

  public MapCanvas(int width, int height) {
    this.width = width;
    this.height = height;
    this.canvas = new byte[width * height][MapData.MAP_SIZE];
    Arrays.stream(canvas).forEach(e -> Arrays.fill(e, (byte) 0));
  }

  public void drawImage(BufferedImage image, int width, int height) {
    drawImageCraft(MapPalette.imageToBytes(image), width, height);
  }

  public void drawImageCraft(int[] craftBytes, int width, int height) {
    for (int canvasY = 0; canvasY < this.height; canvasY++) {
      for (int canvasX = 0; canvasX < this.width; canvasX++) {
        int canvas = this.canvas.length - 1 - canvasY * this.width - canvasX;
        for (int mapY = 0; mapY < MapData.MAP_DIM_SIZE; mapY++) {
          int imageY = canvasY * MapData.MAP_DIM_SIZE + mapY;
          if (imageY >= height) {
            return;
          }

          for (int mapX = 0; mapX < MapData.MAP_DIM_SIZE; mapX++) {
            int imageX = canvasX * MapData.MAP_DIM_SIZE + mapX;
            if (imageX >= width) {
              break;
            }

            byte color = (byte) craftBytes[imageY * width + imageX];
            if (color != MapPalette.TRANSPARENT) {
              this.canvas[canvas][mapY * MapData.MAP_DIM_SIZE + mapX] = color;
            }
          }
        }
      }
    }
  }

  public MapData getMapData(int index, MapPalette.MapVersion version) {
    byte[] convertedCanvas = new byte[MapData.MAP_SIZE];
    return new MapData(MapPalette.convertImage(canvas[index], convertedCanvas, version));
  }

}
