package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import src.utils.GlowRenderer;

public class EnemyBullet {
    private final double velocityX;
    private final double velocityY;
    private final int damage;
    private final double size;
    private final Color color;
    private double x;
    private double y;
    private double previousX;
    private double previousY;

    public EnemyBullet(double x, double y, double angle, double speed, int damage, double size, Color color) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.velocityX = speed * Math.cos(angle);
        this.velocityY = speed * Math.sin(angle);
        this.damage = damage;
        this.size = size;
        this.color = color;
    }

    public void move() {
        previousX = x;
        previousY = y;
        x += velocityX;
        y += velocityY;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Ellipse2D bulletShape = new Ellipse2D.Double(x - (size / 2.0), y - (size / 2.0), size, size);
        GlowRenderer.drawGlow(g2d, bulletShape, color, 4);
        g2d.setColor(color);
        g2d.fill(bulletShape);
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

    public int getDamage() {
        return damage;
    }

    public double getSize() {
        return size;
    }
}
