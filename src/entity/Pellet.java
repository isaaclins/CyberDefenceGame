package src.entity;

import java.awt.Color;
import java.awt.Graphics;

public class Pellet {
    private double x, y;
    private double velocityX, velocityY;
    private final double speed = 10.0;

    public Pellet(double startX, double startY, double targetX, double targetY) {
        this.x = startX;
        this.y = startY;
        double angle = Math.atan2(targetY - startY, targetX - startX);
        this.velocityX = speed * Math.cos(angle);
        this.velocityY = speed * Math.sin(angle);
    }

    public void move() {
        x += velocityX;
        y += velocityY;
    }

    public void render(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int) x - 5, (int) y - 5, 10, 10);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
