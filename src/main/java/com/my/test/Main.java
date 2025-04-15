package com.my.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class Main {
    static final Map<String, BufferedImage> rankTemplates = new HashMap<>();
    static final Map<String, BufferedImage> suitTemplates = new HashMap<>();

    static final int startX = 147;
    static final int startY = 591;
    static final int rankWidth = 53;
    static final int rankHeight = 27;
    static final int shift = 72;
    static final int suitOffsetY = 41;
    static final int suitOffsetX = 20;
    static final int suitHeight = 35;
    static final int suitWidth = 33;

    public static void main(String[] args) throws Exception {
        final String path = args.length > 0 ? args[0] : "checkFolder";
        loadTemplates();

        final File[] files = new File(path).listFiles((dir, name) -> name.endsWith(".png"));
        if (files == null) throw new NoSuchFileException("File missing");

        for (final File file : files) {
            final String result = recognizeCards(ImageIO.read(file));
            System.out.println(file.getName() + " -> " + result.replaceAll("(_gray|-red|-black)", ""));
        }
    }

    static void loadTemplates() throws IOException {
        loadTemplateFolder("templates/rank", rankTemplates);
        loadTemplateFolder("templates/suit", suitTemplates);
    }

    static void loadTemplateFolder(String path, Map<String, BufferedImage> targetMap) throws IOException {
        for (final File f : Objects.requireNonNull(new File(path).listFiles((d, n) -> n.endsWith(".png")))) {
            targetMap.put(f.getName().replace(".png", ""), ImageIO.read(f));
        }
    }

    static String recognizeCards(BufferedImage bufferedImage) {
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            final int x = startX + i * shift;
            final BufferedImage rankImg = bufferedImage.getSubimage(x, startY, rankWidth, rankHeight);
            final BufferedImage suitImg = bufferedImage.getSubimage(x + suitOffsetX, startY + suitOffsetY, suitWidth, suitHeight);

            final String bestRank = findBestMatch(rankImg, rankTemplates);
            final String bestSuit = findBestMatch(suitImg, suitTemplates);

            if (bestRank != null && bestSuit != null) result.append(bestRank).append(bestSuit);
        }
        return result.toString();
    }

    static String findBestMatch(BufferedImage image, Map<String, BufferedImage> templates) {
        String bestMatch = null;
        double minDiff = Double.MAX_VALUE;

        for (Map.Entry<String, BufferedImage> entry : templates.entrySet()) {
            final double diff = calculateImagesDifference(image, entry.getValue());
            if (diff < minDiff && diff < 15000.0) {
                minDiff = diff;
                bestMatch = entry.getKey();
            }
        }
        return bestMatch;
    }

    static double calculateImagesDifference(BufferedImage image, BufferedImage template) {
        final int w = Math.min(image.getWidth(), template.getWidth());
        final int h = Math.min(image.getHeight(), template.getHeight());
        double total = 0;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final Color c1 = new Color(image.getRGB(x, y));
                final Color c2 = new Color(template.getRGB(x, y));
                total += Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getGreen() - c2.getGreen(), 2)
                        + Math.pow(c1.getBlue() - c2.getBlue(), 2);
            }
        }
        return total / (w * h);
    }
}