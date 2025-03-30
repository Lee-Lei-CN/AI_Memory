package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.google.common.collect.ImmutableMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class BitmapDecoder {
    public static final String BITMAP_FQCN = "android.graphics.Bitmap";
    public static final String BITMAP_DRAWABLE_FQCN = "android.graphics.drawable.BitmapDrawable";
    protected static final Map<String, BitmapExtractor> SUPPORTED_FORMATS = ImmutableMap.of("\"ARGB_8888\"", new ARGB8888_BitmapExtractor(), "\"RGB_565\"", new RGB565_BitmapExtractor(), "\"ALPHA_8\"", new ALPHA8_BitmapExtractor());
    private static final int MAX_DIMENSION = 1024;

    public BitmapDecoder() {
    }

    public static BufferedImage getBitmap(BitmapDataProvider dataProvider) throws Exception {
        String config = dataProvider.getBitmapConfigName();
        if (config == null) {
            throw new RuntimeException("Unable to determine bitmap configuration");
        } else {
            BitmapExtractor bitmapExtractor = (BitmapExtractor)SUPPORTED_FORMATS.get(config);
            if (bitmapExtractor == null) {
                throw new RuntimeException("Unsupported bitmap configuration: " + config);
            } else {
                Dimension size = dataProvider.getDimension();
                if (size == null) {
                    throw new RuntimeException("Unable to determine image dimensions.");
                } else {
                    if (size.width > 1024 || size.height > 1024) {
                        boolean couldDownsize = dataProvider.downsizeBitmap(size);
                        if (!couldDownsize) {
                            throw new RuntimeException("Unable to create scaled bitmap");
                        }

                        size = dataProvider.getDimension();
                        if (size == null) {
                            throw new RuntimeException("Unable to obtained scaled bitmap's dimensions");
                        }
                    }

                    return bitmapExtractor.getImage(size.width, size.height, dataProvider.getPixelBytes(size));
                }
            }
        }
    }

    private static class ALPHA8_BitmapExtractor implements BitmapExtractor {
        private ALPHA8_BitmapExtractor() {
        }

        public BufferedImage getImage(int width, int height, byte[] rgb) {
            BufferedImage bufferedImage = new BufferedImage(width, height, 2);

            for(int y = 0; y < height; ++y) {
                int stride = y * width;

                for(int x = 0; x < width; ++x) {
                    int index = stride + x;
                    int value = rgb[index];
                    int rgba = value << 24 | 16711680 | '\uff00' | 255;
                    bufferedImage.setRGB(x, y, rgba);
                }
            }

            return bufferedImage;
        }
    }

    private static class RGB565_BitmapExtractor implements BitmapExtractor {
        private RGB565_BitmapExtractor() {
        }

        public BufferedImage getImage(int width, int height, byte[] rgb) {
            int bytesPerPixel = 2;
            BufferedImage bufferedImage = new BufferedImage(width, height, 2);

            for(int y = 0; y < height; ++y) {
                int stride = y * width;

                for(int x = 0; x < width; ++x) {
                    int index = (stride + x) * bytesPerPixel;
                    int value = rgb[index] & 255 | rgb[index + 1] << 8 & '\uff00';
                    int r = (value >>> 11 & 31) * 255 / 31;
                    int g = (value >>> 5 & 63) * 255 / 63;
                    int b = (value & 31) * 255 / 31;
                    int a = 255;
                    int rgba = a << 24 | r << 16 | g << 8 | b;
                    bufferedImage.setRGB(x, y, rgba);
                }
            }

            return bufferedImage;
        }
    }

    private static class ARGB8888_BitmapExtractor implements BitmapExtractor {
        private ARGB8888_BitmapExtractor() {
        }

        public BufferedImage getImage(int width, int height, byte[] rgba) {
            BufferedImage bufferedImage = new BufferedImage(width, height, 2);

            for(int y = 0; y < height; ++y) {
                int stride = y * width;

                for(int x = 0; x < width; ++x) {
                    int i = (stride + x) * 4;
                    long rgb = 0L;
                    rgb |= ((long)rgba[i] & 255L) << 16;
                    rgb |= ((long)rgba[i + 1] & 255L) << 8;
                    rgb |= (long)rgba[i + 2] & 255L;
                    rgb |= ((long)rgba[i + 3] & 255L) << 24;
                    bufferedImage.setRGB(x, y, (int)(rgb & 4294967295L));
                }
            }

            return bufferedImage;
        }
    }

    private interface BitmapExtractor {
        BufferedImage getImage(int w, int h, byte[] data);
    }

    public interface BitmapDataProvider {
        String getBitmapConfigName() throws Exception;

        Dimension getDimension() throws Exception;

        boolean downsizeBitmap(Dimension newSize) throws Exception;

        byte[] getPixelBytes(Dimension size) throws Exception;
    }
}
