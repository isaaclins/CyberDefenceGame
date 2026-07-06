package src.screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

// Shared palette, fonts, and drawing helpers for the clean-minimal menus.
// Keeping this in one place keeps the four screens visually consistent.
public final class UiTheme {
    private UiTheme() {
    }

    public static final Color BACKGROUND = new Color(11, 14, 18);
    public static final Color PANEL = new Color(24, 30, 38);
    public static final Color PANEL_SELECTED = new Color(30, 42, 52);
    public static final Color TEXT = new Color(232, 236, 241);
    public static final Color MUTED = new Color(138, 147, 160);
    public static final Color ACCENT = new Color(92, 224, 255);
    public static final Color BORDER = new Color(58, 66, 78);
    public static final Color DANGER = new Color(255, 96, 96);

    public static final Font TITLE = new Font("Arial", Font.BOLD, 34);
    public static final Font HEADING = new Font("Arial", Font.BOLD, 22);
    public static final Font LABEL = new Font("Arial", Font.BOLD, 16);
    public static final Font BODY = new Font("Arial", Font.PLAIN, 13);
    public static final Font SMALL = new Font("Arial", Font.PLAIN, 11);
    public static final Font HINT = new Font("Arial", Font.PLAIN, 12);

    public static void enableAntialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    // Cubic ease-out: fast start, gentle settle. Good default for UI entrances.
    public static double easeOut(double t) {
        double clamped = clamp01(t);
        return 1.0 - Math.pow(1.0 - clamped, 3);
    }

    public static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampChannel(alpha));
    }

    public static Color mix(Color from, Color to, double t) {
        double k = clamp01(t);
        int r = (int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * k);
        int g = (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * k);
        int b = (int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * k);
        int a = (int) Math.round(from.getAlpha() + (to.getAlpha() - from.getAlpha()) * k);
        return new Color(clampChannel(r), clampChannel(g), clampChannel(b), clampChannel(a));
    }

    private static int clampChannel(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static void drawCenteredString(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, centerX - (fm.stringWidth(text) / 2), baselineY);
    }

    // Faint grid only. The top-level window background stays transparent.
    public static void drawBackdrop(Graphics2D g, int width, int height) {
        g.setColor(new Color(255, 255, 255, 7));
        int spacing = 36;
        for (int x = spacing; x < width; x += spacing) {
            g.drawLine(x, 0, x, height);
        }
        for (int y = spacing; y < height; y += spacing) {
            g.drawLine(0, y, width, y);
        }
    }

    // Intentionally no full-window scrim: the background should remain transparent.
    public static void drawScrim(Graphics2D g, int width, int height, double progress) {
    }

    // A selectable row/card. Highlight blends toward the accent as focus grows (0..1).
    public static void drawOptionBox(Graphics2D g, int x, int y, int width, int height, double focus) {
        double f = clamp01(focus);
        g.setColor(mix(PANEL, PANEL_SELECTED, f));
        g.fillRoundRect(x, y, width, height, 12, 12);

        // Accent marker bar on the left grows in as the option gains focus.
        int barHeight = (int) Math.round((height - 12) * f);
        if (barHeight > 0) {
            g.setColor(ACCENT);
            g.fillRoundRect(x + 2, y + (height - barHeight) / 2, 3, barHeight, 3, 3);
        }

        g.setColor(mix(BORDER, ACCENT, f));
        g.drawRoundRect(x, y, width, height, 12, 12);
    }
}
