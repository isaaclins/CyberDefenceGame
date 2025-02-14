package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Enemy {
    private double x, y;
    private double velocityX, velocityY;
    private int health;
    private Color color;
    private final double friction = 0.90;
    private final double speed = 0.1;
    private double facingAngle;

    public Enemy(double x, double y, int health, Color color) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.color = color;
        this.velocityX = 0;
        this.velocityY = 0;
        this.facingAngle = 0;
    }

    public void move() {
        x += velocityX;
        y += velocityY;

        velocityX *= friction;
        velocityY *= friction;
    }

    public void moveToPlayer(double playerX, double playerY) {
        double angle = Math.atan2(playerY - y, playerX - x);
        velocityX += speed * Math.cos(angle);
        velocityY += speed * Math.sin(angle);
        facingAngle = angle;
    }

    public void applyKnockback(double knockbackX, double knockbackY) {
        this.velocityX += knockbackX;
        this.velocityY += knockbackY;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        g2d.translate(x, y);
        g2d.rotate(facingAngle);

        g2d.setColor(color);
        g2d.fillRect(-10, -10, 20, 20);

        // Optionally, you can draw a line to indicate the facing direction
        g2d.setColor(Color.RED);
        g2d.drawLine(0, 0, 20, 0);

        g2d.setTransform(oldTransform);
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
