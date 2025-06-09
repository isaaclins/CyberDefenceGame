package src.entity;

import java.awt.Color;
import java.awt.Graphics;

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
        g.setColor(Color.GREEN);
        g.fillRect((int) x - 2, (int) y - 2, 4, 4);
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
