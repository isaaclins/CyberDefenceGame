package src.entity;

import java.awt.Color;
import java.awt.Graphics;

public class Particle {
    private double x, y;
    private double dx, dy;
    private int lifetime;
    private final int maxLifetime;
    private double size;
    private final double friction;
    private Color color;
    private final int priority;

    public Particle(double x, double y, double dx, double dy, int lifetime, double size, double friction, Color color,
            int priority) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.size = size;
        this.friction = friction;
        this.color = color;
        this.priority = priority;
    }

    public void tick() {
        x += dx;
        y += dy;
        dx *= friction;
        dy *= friction;
        size = Math.max(1.0, size * 0.97);
        lifetime--;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isAlive() {
        return lifetime > 0;
    }

    public void render(Graphics g) {
        int alpha = (int) Math.max(0, Math.min(255, (255.0 * lifetime) / Math.max(1, maxLifetime)));
        int drawSize = (int) Math.max(1, Math.round(size));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.fillRect((int) Math.round(x - (drawSize / 2.0)), (int) Math.round(y - (drawSize / 2.0)), drawSize, drawSize);
    }

    public int getPriority() {
        return priority;
    }
}
