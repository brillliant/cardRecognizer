package com.my.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class TemplateCutter1 {
    public static void main(String[] args) throws Exception {
        // Путь к оригинальному изображению
        File inputFile = new File("inputForTemplates/10c3s2d.png");

        //System.out.println("Текущая рабочая директория: " + new File(".").getAbsolutePath());

        if (!inputFile.exists()) {
            throw new Exception("Input file missing");
        }
        // Загрузка изображения
        BufferedImage original = ImageIO.read(inputFile);

        // Координаты и размер шаблона (без рамки)
        int x = 147;
        int y = 591;
        int width = 53;
        int height = 76;

        // Вырезаем область
        BufferedImage template = original.getSubimage(x, y, width, height);

        // Сохраняем шаблон
        ImageIO.write(template, "png", new File("templates/template_10c.png"));

        System.out.println("Шаблон сохранён как template_10c.png");
    }
}

