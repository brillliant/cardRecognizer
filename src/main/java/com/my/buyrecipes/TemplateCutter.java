package com.my.buyrecipes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.util.*;

public class TemplateCutter {
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
        boolean saveAll = true;

        File inputFolder = new File("inputForTemplates");
        File outputRankFolder = new File("templates/rank");
        File outputSuitFolder = new File("templates/suit");

        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            throw new Exception("Папка inputForTemplates не найдена");
        }

        Set<String> savedRanks = new HashSet<>();
        Set<Character> savedSuits = new HashSet<>();
        Map<String, Integer> rankCounters = new HashMap<>();
        Map<Character, Integer> suitCounters = new HashMap<>();

        File[] files = inputFolder.listFiles((dir, name) -> name.endsWith(".png"));
        if (files == null || files.length == 0) {
            throw new Exception("Нет PNG-файлов в папке inputForTemplates");
        }

        for (File file : files) {
            String filename = file.getName();
            String nameWithoutExt = filename.replace(".png", "");
            List<String> cards = splitCards(nameWithoutExt);
            BufferedImage original = ImageIO.read(file);

            for (int i = 0; i < cards.size(); i++) {
                String card = cards.get(i);
                String rank = card.substring(0, card.length() - 1);
                char suit = card.charAt(card.length() - 1);

                int x = startX + i * shift;

                if (x + cardWidth > original.getWidth()) {
                    System.out.println("⚠️ Пропуск " + card + " в " + filename + ": выходит за границы изображения");
                    continue;
                }

                // Вырезаем изображение ранга
                BufferedImage rankImage = original.getSubimage(x, startY, cardWidth, rankHeight);
                String color = getRankColor(rankImage);
                String rankKey = rank + "-" + color;

                if (saveAll) {
                    int count = rankCounters.getOrDefault(rankKey, 0) + 1;
                    rankCounters.put(rankKey, count);
                    File outRank = new File(outputRankFolder, rankKey + "-" + count + ".png");
                    ImageIO.write(rankImage, "png", outRank);
                    System.out.println("✅ Сохранён rank: " + outRank.getName());
                } else {
                    if (!savedRanks.contains(rankKey)) {
                        File outRank = new File(outputRankFolder, rankKey + ".png");
                        ImageIO.write(rankImage, "png", outRank);
                        savedRanks.add(rankKey);
                        System.out.println("✅ Сохранён rank: " + rankKey + ".png");
                    }
                }

                // Вырезаем изображение масти
                if (saveAll) {
                    int count = suitCounters.getOrDefault(suit, 0) + 1;
                    suitCounters.put(suit, count);
                    File outSuit = new File(outputSuitFolder, suit + "-" + count + ".png");
                    BufferedImage suitImage = original.getSubimage(x + suitOffsetX, startY + suitOffsetY, suitWidth, suitHeight);
                    ImageIO.write(suitImage, "png", outSuit);
                    System.out.println("✅ Сохранён suit: " + outSuit.getName());
                } else {
                    if (!savedSuits.contains(suit)) {
                        BufferedImage suitImage = original.getSubimage(x + suitOffsetX, startY + suitOffsetY, suitWidth, suitHeight);
                        File outSuit = new File(outputSuitFolder, suit + ".png");
                        ImageIO.write(suitImage, "png", outSuit);
                        savedSuits.add(suit);
                        System.out.println("✅ Сохранён suit: " + suit + ".png");
                    }
                }
            }
        }

        System.out.println("\n🎉 Готово. " +
                (saveAll
                        ? "Сохранены все шаблоны"
                        : "Уникальных рангов: " + savedRanks.size() + ", мастей: " + savedSuits.size()));
    }

    static String getRankColor(BufferedImage image) {
        int red = 0, black = 0;
        int w = image.getWidth();
        int h = image.getHeight();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Color color = new Color(image.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                if (r > 100 && g < 80 && b < 80) red++;
                else if (r < 100 && g < 100 && b < 100) black++;
            }
        }

        return red > black ? "red" : "black";
    }

    static List<String> splitCards(String name) {
        Set<String> ranks = Set.of("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");
        Set<Character> suits = Set.of('c', 'd', 'h', 's');

        List<String> cards = new ArrayList<>();
        int i = 0;

        while (i < name.length()) {
            String rank;
            if (i + 2 <= name.length() && name.startsWith("10", i)) {
                rank = "10";
                i += 2;
            } else {
                rank = name.substring(i, i + 1);
                i += 1;
            }

            if (i >= name.length()) break;

            char suit = name.charAt(i);
            i += 1;

            if (ranks.contains(rank) && suits.contains(suit)) {
                cards.add(rank + suit);
            } else {
                System.out.println("⚠️ Невалидная карта: " + rank + suit);
            }
        }

        return cards;
    }
}
