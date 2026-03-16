package src.entity;

import java.awt.Color;
import java.awt.Graphics;

public class Pellet {
    private static final Color PELLET_COLOR = new Color(255, 225, 80);
    private static final Color PELLET_CORE_COLOR = new Color(255, 255, 220);

    private double x, y;
    private double previousX, previousY;
    private double velocityX, velocityY;
    private double damage;
    private double size;
    private double knockback;

    public Pellet(double x, double y, double angle, double speed, double damage, double size, double knockback) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.velocityX = speed * Math.cos(angle);
        this.velocityY = speed * Math.sin(angle);
        this.damage = damage;
        this.size = size;
        this.knockback = knockback;
    }

    public void move() {
        previousX = x;
        previousY = y;
        x += velocityX;
        y += velocityY;
    }

    public void render(Graphics g) {
        int drawSize = Math.max(2, (int) Math.round(size));
        int drawX = (int) Math.round(x - (drawSize / 2.0));
        int drawY = (int) Math.round(y - (drawSize / 2.0));
        int coreSize = Math.max(1, drawSize / 2);
        int coreOffset = (drawSize - coreSize) / 2;

        g.setColor(PELLET_COLOR);
        g.fillOval(drawX, drawY, drawSize, drawSize);
        g.setColor(PELLET_CORE_COLOR);
        g.fillOval(drawX + coreOffset, drawY + coreOffset, coreSize, coreSize);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getPreviousX() {
        return previousX;
    }

    public double getPreviousY() {
        return previousY;
    }

    public double getDamage() {
        return damage;
    }

    public double getKnockback() {
        return knockback;
    }

    public double getSize() {
        return size;
    }
}
