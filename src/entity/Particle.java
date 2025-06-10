package src.entity;

import java.awt.Color;
import java.awt.Graphics;

public class Particle {
    private double x, y;
    private double dx, dy;
    private int lifetime;
    private Color color;

    public Particle(double x, double y, double dx, double dy, int lifetime, Color color) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.lifetime = lifetime;
        this.color = color;
    }

    public void tick() {
        x += dx;
        y += dy;
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
        g.setColor(color);
        g.fillRect((int) x, (int) y, 2, 2);
    }
}
