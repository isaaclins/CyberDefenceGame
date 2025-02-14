package src.entity;

import java.awt.Color;
import java.awt.Graphics;

public class Pellet {
    private double x, y;
    private double targetX, targetY;
    private double velocityX, velocityY;
    private final double speed = 5.0;

    public Pellet(double startX, double startY, double targetX, double targetY) {
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        double angle = Math.atan2(targetY - startY, targetX - startX);
        velocityX = speed * Math.cos(angle);
        velocityY = speed * Math.sin(angle);
    }

    public void move() {
        x += velocityX;
        y += velocityY;
    }

    public void render(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)x - 3, (int)y - 3, 6, 6);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
