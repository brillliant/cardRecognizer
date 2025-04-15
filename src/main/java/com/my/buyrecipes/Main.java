package com.my.buyrecipes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.util.*;

public class Main {
    static Map<String, BufferedImage> rankTemplates = new HashMap<>();
    static Map<String, BufferedImage> suitTemplates = new HashMap<>();

    static int startX = 147;
    static int startY = 591;
    static int rankWidth = 53;
    static int rankHeight = 27;
    static int shift = 72;
    static int suitOffsetY = 41;
    static int suitOffsetX = 20;
    static int suitHeight = 35;
    static int suitWidth = 33;

    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "checkFolder";

        loadTemplates();

        File folder = new File(path);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".png"));

        if (files == null) {
            System.out.println("Folder is missing");
            return;
        }

        for (File file : files) {
            BufferedImage img = ImageIO.read(file);
            String result = recognizeCards(img);

            System.out.println(file.getName() + " -> " + result
                    .replace("_gray", "")
                    .replace("-red", "")
                    .replace("-black", ""));
        }
    }

    static void loadTemplates() throws Exception {
        File rankDir = new File("templates/rank");
        for (File f : Objects.requireNonNull(rankDir.listFiles((d, n) -> n.endsWith(".png")))) {
            rankTemplates.put(f.getName().replace(".png", ""), ImageIO.read(f));
        }

        File suitDir = new File("templates/suit");
        for (File f : Objects.requireNonNull(suitDir.listFiles((d, n) -> n.endsWith(".png")))) {
            suitTemplates.put(f.getName().replace(".png", ""), ImageIO.read(f));
        }
    }

    static String recognizeCards(BufferedImage bufferedImage) throws Exception {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            int x = startX + i * shift;

            if (x + rankWidth > bufferedImage.getWidth()) break;

            BufferedImage rankImg = bufferedImage.getSubimage(x, startY, rankWidth, rankHeight);
            BufferedImage suitImg = bufferedImage.getSubimage(x + suitOffsetX, startY + suitOffsetY, suitWidth, suitHeight);

            String bestRank = findBestMatch(rankImg, rankTemplates);
            String bestSuit = findBestMatch(suitImg, suitTemplates);

            if (bestRank != null && bestSuit != null) {
                result.append(bestRank).append(bestSuit);
            }
        }
        return result.toString();
    }

    static String findBestMatch(BufferedImage image, Map<String, BufferedImage> templates) throws Exception {
        String bestMatch = null;
        double minDiff = Double.MAX_VALUE;

        for (Map.Entry<String, BufferedImage> entry : templates.entrySet()) {
            double diff = imageDiff(image, entry.getValue());
            if (diff < minDiff && diff < 15000.0) {
                minDiff = diff;
                bestMatch = entry.getKey();
            }
        }
        return bestMatch;
    }

    static double imageDiff(BufferedImage image, BufferedImage template) {
        int w = Math.min(image.getWidth(), template.getWidth());
        int h = Math.min(image.getHeight(), template.getHeight());
        double total = 0;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Color imageColor = new Color(image.getRGB(x, y));
                Color templateColor = new Color(template.getRGB(x, y));
                int differenceRed = imageColor.getRed() - templateColor.getRed();
                int differenceGreen = imageColor.getGreen() - templateColor.getGreen();
                int differenceBlue = imageColor.getBlue() - templateColor.getBlue();
                total += differenceRed * differenceRed + differenceGreen * differenceGreen + differenceBlue * differenceBlue;
            }
        }
        return total / (w * h);
    }
}
