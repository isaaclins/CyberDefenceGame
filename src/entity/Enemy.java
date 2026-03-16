package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import src.utils.GlowRenderer;

public abstract class Enemy {
    private static final Stroke FACING_STROKE = new java.awt.BasicStroke(1f);

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
        double dx = playerX - x;
        double dy = playerY - y;
        double distanceSquared = (dx * dx) + (dy * dy);
        if (distanceSquared <= 0.0001) {
            return;
        }

        double distance = Math.sqrt(distanceSquared);
        velocityX += speed * (dx / distance);
        velocityY += speed * (dy / distance);
        facingAngle = Math.atan2(dy, dx);
    }

    public List<EnemyBullet> updateBehavior(double playerX, double playerY) {
        moveToPlayer(playerX, playerY);
        return Collections.emptyList();
    }

    public void applyKnockback(double knockbackX, double knockbackY) {
        this.velocityX += knockbackX;
        this.velocityY += knockbackY;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        Stroke oldStroke = g2d.getStroke();

        g2d.translate(x, y);
        g2d.rotate(facingAngle);

        Rectangle2D enemyShape = new Rectangle2D.Double(-size / 2, -size / 2, size, size);

        GlowRenderer.drawGlow(g2d, enemyShape, color, 5);
        g2d.setColor(color);
        g2d.fill(enemyShape);

        g2d.setColor(Color.RED);
        g2d.setStroke(FACING_STROKE);
        g2d.drawLine(0, 0, (int) (size / 2), 0);

        g2d.setStroke(oldStroke);
        g2d.setTransform(oldTransform);
    }

    public void teleportTo(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0;
        this.velocityY = 0;
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

    public double getSize() {
        return size;
    }

    public double getRenderRadius() {
        return size / 2.0;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
