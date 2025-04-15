package com.my.buyrecipes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class TemplateCutter {
    static int startX = 147;
    static int startY = 591;
    static int cardWidth = 53;
    static int cardHeight = 76;
    static int shift = 72;

    public static void main(String[] args) throws Exception {
        File inputFolder = new File("inputForTemplates");
        File outputFolder = new File("templates");

        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            throw new Exception("Папка inputForTemplates не найдена");
        }

        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        Set<String> savedCards = new HashSet<>();

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

                if (savedCards.contains(card)) continue;

                int x = startX + i * shift;
                if (x + cardWidth > original.getWidth()) {
                    System.out.println("⚠️ Пропуск " + card + " в " + filename + ": вылетает за границы изображения");
                    continue;
                }

                BufferedImage sub = original.getSubimage(x, startY, cardWidth, cardHeight);

                String outputName = card /*+ "_from_" + nameWithoutExt*/ + ".png";
                File outFile = new File(outputFolder, outputName);
                ImageIO.write(sub, "png", outFile);

                savedCards.add(card);
                System.out.println("✅ Сохранён шаблон: " + outputName);
            }
        }

        System.out.println("\n🎉 Готово. Уникальных шаблонов сохранено: " + savedCards.size());
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
