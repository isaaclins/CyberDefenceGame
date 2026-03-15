package src.utils;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

public class GlowRenderer {
    private static final int MAX_GLOW_PASSES = 5;
    private static final BasicStroke[] GLOW_STROKES = {
            null,
            new BasicStroke(2f),
            new BasicStroke(4f),
            new BasicStroke(6f),
            new BasicStroke(8f),
            new BasicStroke(10f)
    };
    private static final float[] GLOW_ALPHAS = { 0f, 0.10f, 0.08f, 0.06f, 0.045f, 0.03f };

    public static void drawGlow(Graphics2D g2d, Shape shape, Color color, int intensity) {
        int passes = Math.max(1, Math.min(MAX_GLOW_PASSES, intensity));
        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();

        g2d.setColor(color);
        for (int i = passes; i >= 1; i--) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, GLOW_ALPHAS[i]));
            g2d.setStroke(GLOW_STROKES[i]);
            g2d.draw(shape);
        }

        g2d.setComposite(oldComposite);
        g2d.setStroke(oldStroke);
    }
}
