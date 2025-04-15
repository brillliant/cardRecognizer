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
    static int cardWidth = 53;
    static int shift = 72;

    static int rankHeight = 27;
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
            System.out.println("Папка не найдена или пуста");
            return;
        }

        int problems = 0;

        for (File file : files) {
            BufferedImage img = ImageIO.read(file);
            String result = recognizeCards(img);
            String expected = file.getName().replace(".png", "");//.replace("_grey", "").replace("_dark", "");

            if (!result
                    .replace("_gray", "")
                    .replace("_dark", "")
                    .replace("-red", "")
                    .replace("-black", "")
                    .equals(expected)) {
                System.out.println("=== ❌ ПРОБЛЕМА ===");
                problems++;
            }
            System.out.println(file.getName() + " -> " + result
                    .replace("_gray", "")
                    .replace("_dark", "")
                    .replace("-red", "")
                    .replace("-black", "") + "      " + result);
        }

        System.out.println("\n================ ПРОБЛЕМ: " + problems);
    }

    static void loadTemplates() throws Exception {
        File rankDir = new File("templates/rank");
        for (File f : rankDir.listFiles((d, n) -> n.endsWith(".png"))) {
            String name = f.getName().replace(".png", "");
            rankTemplates.put(name, ImageIO.read(f));
        }

        File suitDir = new File("templates/suit");
        for (File f : suitDir.listFiles((d, n) -> n.endsWith(".png"))) {
            String name = f.getName().replace(".png", "");
            suitTemplates.put(name, ImageIO.read(f));
        }
    }

    static String recognizeCards(BufferedImage img) throws Exception {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            int x = startX + i * shift;

            if (x + cardWidth > img.getWidth()) break;

            BufferedImage rankImg = img.getSubimage(x, startY, cardWidth, rankHeight);
            BufferedImage suitImg = img.getSubimage(x + suitOffsetX, startY + suitOffsetY, suitWidth, suitHeight);

            String bestRank = findBestMatch(rankImg, rankTemplates, 15000.0);
            String bestSuit = findBestMatch(suitImg, suitTemplates, 15000.0);

            if (bestRank != null && bestSuit != null) {
                result.append(bestRank).append(bestSuit);
            } /*else {
                result.append("??");
            }*/
        }

        return result.toString();
    }

    static String findBestMatch(BufferedImage image, Map<String, BufferedImage> templates, double threshold) throws Exception {
        String bestMatch = null;
        double minDiff = Double.MAX_VALUE;

        //temp
        //saveImage(image, "png", new File("image.png"));

        for (Map.Entry<String, BufferedImage> entry : templates.entrySet()) {
            //temp
            //saveImage(entry.getValue(), "png", new File("template.png"));

            double diff = imageDiff(image, entry.getValue());
            if (diff < minDiff && diff < threshold) {
                minDiff = diff;
                bestMatch = entry.getKey();
            }
        }

        //temp
        if (bestMatch == null) {
            saveImage(image, "png", new File("image.png"));
        }

        return bestMatch;
    }

    static double imageDiff(BufferedImage image, BufferedImage template) {
        int w = Math.min(image.getWidth(), template.getWidth());
        int h = Math.min(image.getHeight(), template.getHeight());
        double total = 0;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Color c1 = new Color(image.getRGB(x, y));
                Color c2 = new Color(template.getRGB(x, y));
                int dr = c1.getRed() - c2.getRed();
                int dg = c1.getGreen() - c2.getGreen();
                int db = c1.getBlue() - c2.getBlue();
                total += dr * dr + dg * dg + db * db;
            }
        }

        return total / (w * h);
    }

    //todo temp for debug
    static void saveImage(BufferedImage image, String format, File outputFile) throws Exception {
        if (!ImageIO.write(image, format, outputFile)) {
            throw new Exception("❌ Ошибка сохранения изображения: " + outputFile.getAbsolutePath());
        }
    }
}
