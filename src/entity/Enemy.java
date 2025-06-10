package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import src.utils.GlowRenderer;

public abstract class Enemy {
    protected double x, y;
    protected double velocityX, velocityY;
    protected int health;
    protected Color color;
    protected final double friction = 0.90;
    protected double speed;
    protected double size;
    protected int damage;
    protected int xpDropAmount;
    protected double facingAngle;

    public Enemy(double x, double y, int health, Color color, double speed, double size, int damage, int xpDropAmount) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.color = color;
        this.speed = speed;
        this.size = size;
        this.damage = damage;
        this.xpDropAmount = xpDropAmount;
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

        Rectangle2D enemyShape = new Rectangle2D.Double(-size / 2, -size / 2, size, size);

        // Use a copy of g2d for glow to avoid stroke issues
        Graphics2D g2dGlow = (Graphics2D) g2d.create();
        GlowRenderer.drawGlow(g2dGlow, enemyShape, color, 15);
        g2dGlow.dispose();

        g2d.setColor(color);
        g2d.fill(enemyShape);

        g2d.setColor(Color.RED);
        g2d.setStroke(new java.awt.BasicStroke(1));
        g2d.drawLine(0, 0, (int) (size / 2), 0);

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

    public int getDamage() {
        return damage;
    }

    public int getXpDropAmount() {
        return xpDropAmount;
    }

    public Color getColor() {
        return color;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
