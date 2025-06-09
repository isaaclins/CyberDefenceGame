package src.entity;

import java.awt.Color;
import java.awt.Graphics;

public class Pellet {
    private double x, y;
    private double velocityX, velocityY;
    private double damage;
    private double size;
    private double knockback;

    public Pellet(double x, double y, double angle, double speed, double damage, double size, double knockback) {
        this.x = x;
        this.y = y;
        this.velocityX = speed * Math.cos(angle);
        this.velocityY = speed * Math.sin(angle);
        this.damage = damage;
        this.size = size;
        this.knockback = knockback;
    }

    public void move() {
        x += velocityX;
        y += velocityY;
    }

    public void render(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int) (x - size / 2), (int) (y - size / 2), (int) size, (int) size);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDamage() {
        return damage;
    }

    public double getKnockback() {
        return knockback;
    }
}
