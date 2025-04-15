package com.my.buyrecipes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.util.*;

public class Main {
    static Map<String, BufferedImage> templates = new HashMap<>();

    public static void main(String[] args) throws Exception {
        String path = null;
        if (args.length < 1) {
            path = "checkFolder";            //System.out.println("Укажите путь к папке с изображениями");
            //return;
        }

        loadTemplates();

        File folder = new File(path);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".png"));

        if (files == null) {
            System.out.println("Папка не найдена или пуста");
            return;
        }

        //temp
        int problem_not_found = 0;
        int problem_false_found = 0;
        int common_problem = 0;

        for (File file : files) {
            BufferedImage img = ImageIO.read(file);
            String result = recognizeCards(img);
            if (!file.getName().equals(result.replace("_grey","").replace("_dark","") + ".png")) {
                System.out.println("=== ПРОБЛЕМА ====== Не совпало");
                common_problem++;
            }
            System.out.println(file.getName() + " - " + result);
        }
        //System.out.println("================ НЕ НАШЛО:" + problem_not_found + "   ЛОЖНОЕ:" + problem_false_found);
        System.out.println("================ ПРОБЛЕМ:" + common_problem);
    }

    static void loadTemplates() throws Exception {
        File tmplDir = new File("templates");
        for (File f : tmplDir.listFiles()) {
            if (f.getName().endsWith(".png")) {
                String name = f.getName().replace(".png", "");
                templates.put(name, ImageIO.read(f));
            }
        }
    }

    static String recognizeCards(BufferedImage img) {
        //int w = img.getWidth();
        //int h = img.getHeight();
        //int y = h / 3;
        int cardWidth = 53;//w / 8;
        int cardHeight = 76;//w / 8;
        int shift = 72;//73-34mln;74-62mln;75-88mln;                                         //70-40млн; 71-9млн; 72-4млн; 73-34млн; 74-63млн              //old 65, 68, 70  75!, 73, 74! 72:)  71!

        int x = 147;
        int y = 591;

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            //if (x + cardWidth > w) break;

            BufferedImage sub = img.getSubimage(x + i * shift, y, cardWidth, cardHeight);
            String bestMatch = findBestMatch(sub);
            if (bestMatch != null) result.append(bestMatch);
        }

        return result.toString();
    }

    static String findBestMatch(BufferedImage image) {
        String best = null;
        double minDiff = Double.MAX_VALUE;

        //todo temp
        List<Map.Entry<String, BufferedImage>> sortedTemplates = new ArrayList<>(templates.entrySet());
        sortedTemplates.sort(Map.Entry.comparingByKey());

        for (Map.Entry<String, BufferedImage> entry : templates.entrySet()) {
            BufferedImage template = entry.getValue();

/*            BufferedImage normImage = normalizeImage(image);
            BufferedImage normTemplate = normalizeImage(template);*/

            double diff = imageDiff(image, template);
            if (diff < minDiff) {
                if (diff < 8450) {  //7450 - 53проблемы//15766 threshhold
                    minDiff = diff;
                    best = entry.getKey();
                }
            }
        }

        return best;
    }

    static double imageDiff(BufferedImage image, BufferedImage template) {
        int w = Math.min(image.getWidth(), template.getWidth());
        int h = Math.min(image.getHeight(), template.getHeight());
        double total = 0;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Color colorImage = new Color(image.getRGB(x, y));
                Color colorTemplate = new Color(template.getRGB(x, y));
                int dr = colorImage.getRed() - colorTemplate.getRed();
                int dg = colorImage.getGreen() - colorTemplate.getGreen();
                int db = colorImage.getBlue() - colorTemplate.getBlue();
                total += dr*dr + dg*dg + db*dg;

/*                int grayImage = (colorImage.getRed() + colorImage.getGreen() + colorImage.getBlue()) / 3;
                int grayTemplate = (colorTemplate.getRed() + colorTemplate.getGreen() + colorTemplate.getBlue()) / 3;
                int diff = grayImage - grayTemplate;
                total += diff * diff;*/
            }
        }

        return total / (w * h);
    }

    static BufferedImage normalizeImage(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage norm = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        int min = 255, max = 0;

        int[][] gray = new int[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Color color = new Color(img.getRGB(x, y));
                int g = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                gray[x][y] = g;
                if (g < min) min = g;
                if (g > max) max = g;
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int normG = (gray[x][y] - min) * 255 / (max - min + 1); // +1 чтобы избежать деления на 0
                int rgb = new Color(normG, normG, normG).getRGB();
                norm.setRGB(x, y, rgb);
            }
        }

        return norm;
    }
}
