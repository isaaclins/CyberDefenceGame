package src.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class FloatingText {
    private static final Font TEXT_FONT = new Font("Arial", Font.BOLD, 18);

    private final String text;
    private final Color color;
    private final int maxLifetime;
    private double x;
    private double y;
    private double velocityY;
    private int lifetime;

    public FloatingText(double x, double y, String text, Color color, int lifetime) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.velocityY = -0.7;
    }

    public void tick() {
        y += velocityY;
        velocityY *= 0.97;
        lifetime--;
    }

    public boolean isAlive() {
        return lifetime > 0;
    }

    public void render(Graphics2D g2d) {
        if (lifetime <= 0) {
            return;
        }

        int alpha = (int) Math.max(0, Math.min(255, (255.0 * lifetime) / Math.max(1, maxLifetime)));
        g2d.setFont(TEXT_FONT);
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int drawX = (int) Math.round(x - (textWidth / 2.0));
        int drawY = (int) Math.round(y);
        g2d.setColor(new Color(0, 0, 0, Math.min(220, alpha)));
        g2d.drawString(text, drawX + 1, drawY + 1);
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.drawString(text, drawX, drawY);
    }
}
