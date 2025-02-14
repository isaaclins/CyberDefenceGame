package src.entity;

import java.awt.Color;
import java.awt.Graphics;

public class Enemy {
    private double x, y;
    private double velocityX, velocityY;
    private int health;
    private Color color;
    private final double friction = 0.90;

    public Enemy(double x, double y, int health, Color color) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.color = color;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    public void move() {
        x += velocityX;
        y += velocityY;

        velocityX *= friction;
        velocityY *= friction;
    }

    public void applyKnockback(double knockbackX, double knockbackY) {
        this.velocityX += knockbackX;
        this.velocityY += knockbackY;
    }

    public void render(Graphics g) {
        g.setColor(color);
        g.fillRect((int) x - 10, (int) y - 10, 20, 20);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
