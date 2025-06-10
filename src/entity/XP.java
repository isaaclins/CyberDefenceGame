package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import src.utils.GlowRenderer;

public class XP {
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
        Graphics2D g2d = (Graphics2D) g.create();
        Rectangle2D xpShape = new Rectangle2D.Double(x - 2, y - 2, 4, 4);

        GlowRenderer.drawGlow(g2d, xpShape, Color.GREEN, 8);

        g2d.setColor(Color.GREEN);
        g2d.fill(xpShape);
        g2d.dispose();
    }

    public void move() {
        x += velocityX;
        y += velocityY;
        velocityX *= friction;
        velocityY *= friction;
    }

    public void moveTo(double targetX, double targetY) {
        double angle = Math.atan2(targetY - y, targetX - x);
        velocityX += speed * Math.cos(angle);
        velocityY += speed * Math.sin(angle);
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
