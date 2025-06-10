package src.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

public class GlowRenderer {

    public static void drawGlow(Graphics2D g2d, Shape shape, Color color, int intensity) {
        g2d.setColor(color);
        for (int i = intensity; i >= 1; i--) {
            float alpha = 0.1f * (1.0f - (float) i / intensity);
            g2d.setStroke(new BasicStroke(i * 2));
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g2d.draw(shape);
        }
    }
}
