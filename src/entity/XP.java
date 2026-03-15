package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import src.utils.GlowRenderer;

public class XP {
    private static final Color XP_COLOR = new Color(96, 255, 128);

    private double x, y;
    private int amount;
    private double velocityX, velocityY;
    private final double speed = 1.0;
    private final double friction = 0.95;

    public XP(double x, double y, int amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Rectangle2D xpShape = new Rectangle2D.Double(x - 2, y - 2, 4, 4);

        GlowRenderer.drawGlow(g2d, xpShape, XP_COLOR, 4);

        g2d.setColor(XP_COLOR);
        g2d.fill(xpShape);
    }

    public void move() {
        x += velocityX;
        y += velocityY;
        velocityX *= friction;
        velocityY *= friction;
    }

    public void moveTo(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distanceSquared = (dx * dx) + (dy * dy);
        if (distanceSquared <= 0.0001) {
            return;
        }

        double distance = Math.sqrt(distanceSquared);
        velocityX += speed * (dx / distance);
        velocityY += speed * (dy / distance);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getAmount() {
        return amount;
    }
}
