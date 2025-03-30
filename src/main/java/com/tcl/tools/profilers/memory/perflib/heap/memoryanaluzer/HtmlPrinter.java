package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.tcl.tools.profilers.memory.perflib.heap.Instance;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Base64;
import javax.imageio.ImageIO;

public final class HtmlPrinter implements Printer {
    private final PrintStream mOutStream;
    private final Escaper mEscaper;
    private static final int MAX_PREVIEW_STRING_LENGTH = 100;

    public HtmlPrinter(Path path) throws FileNotFoundException {
        this(new PrintStream(path.toFile()));
    }

    public HtmlPrinter(PrintStream outStream) {
        this.mOutStream = outStream;
        this.mEscaper = HtmlEscapers.htmlEscaper();
    }

    public void addHeading(int level, String content) {
        this.mOutStream.printf("<h%d>%s</h%1$d>\n", level, this.mEscaper.escape(content));
    }

    public void addParagraph(String content) {
        this.mOutStream.printf("<p>%s</p>\n", this.mEscaper.escape(content));
    }

    public void startTable(String... columnHeadings) {
        this.mOutStream.printf("<table>\n");
        if (columnHeadings.length > 0) {
            this.mOutStream.printf("<tr style='border: 1px solid black;'>\n");
            String[] var2 = columnHeadings;
            int var3 = columnHeadings.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String column = var2[var4];
                this.mOutStream.printf("<th style='border: 1px solid black;'>%s</th>\n", this.mEscaper.escape(column));
            }

            this.mOutStream.printf("</tr>\n");
        }

    }

    public void addRow(String... values) {
        this.mOutStream.printf("<tr>\n");
        String[] var2 = values;
        int var3 = values.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String value = var2[var4];
            this.mOutStream.printf("<td>%s</td>\n", this.mEscaper.escape(value));
        }

        this.mOutStream.printf("</tr>\n");
    }

    public void endTable() {
        this.mOutStream.printf("</table>\n");
    }

    public void addImage(Instance instance) {
        if (HprofBitmapProvider.canGetBitmapFromInstance(instance)) {
            try {
                HprofBitmapProvider bitmapProvider = new HprofBitmapProvider(instance);
                String configName = bitmapProvider.getBitmapConfigName();
                int width = bitmapProvider.getDimension().width;
                int height = bitmapProvider.getDimension().height;
                byte[] raw = bitmapProvider.getPixelBytes(new Dimension());
                if (!"\"ARGB_8888\"".equals(configName)) {
                    throw new Exception("RGB_565/ALPHA_8 conversion not implemented");
                } else {
                    int[] converted = new int[width * height];

                    for(int i = 0; i < converted.length; ++i) {
                        converted[i] = ((raw[i * 4 + 3] & 255) << 24) + ((raw[i * 4 + 0] & 255) << 16) + ((raw[i * 4 + 1] & 255) << 8) + (raw[i * 4 + 2] & 255);
                    }

                    int imageType = -1;
                    byte var10 = -1;
                    switch(configName.hashCode()) {
                        case 419759970:
                            if (configName.equals("\"RGB_565\"")) {
                                var10 = 1;
                            }
                            break;
                        case 1379270573:
                            if (configName.equals("\"ALPHA_8\"")) {
                                var10 = 2;
                            }
                            break;
                        case 1661702801:
                            if (configName.equals("\"ARGB_8888\"")) {
                                var10 = 0;
                            }
                    }

                    switch(var10) {
                        case 0:
                            imageType = 6;
                            break;
                        case 1:
                            imageType = 8;
                            break;
                        case 2:
                            imageType = 10;
                    }

                    BufferedImage image = new BufferedImage(width, height, imageType);
                    image.setRGB(0, 0, width, height, converted, 0, width);
                    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", byteOutputStream);
                    byteOutputStream.flush();
                    String imageDataString = Base64.getEncoder().encodeToString(byteOutputStream.toByteArray());
                    byteOutputStream.close();
                    this.mOutStream.printf("<img src='data:image/png;base64,%s' \\>\n", imageDataString);
                }
            } catch (Exception var12) {
                var12.printStackTrace();
            }
        }
    }

    public String formatInstance(Instance instance) {
        return instance.toString();
    }

    private String bitmapAsBase64String(BufferedImage image) {
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteOutputStream);
            byteOutputStream.flush();
            String imageDataString = Base64.getEncoder().encodeToString(byteOutputStream.toByteArray());
            byteOutputStream.close();
            return imageDataString;
        } catch (IOException var4) {
            return null;
        }
    }
}
