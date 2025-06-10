package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import src.utils.GlowRenderer;

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
        Graphics2D g2d = (Graphics2D) g.create();
        Ellipse2D.Double pelletShape = new Ellipse2D.Double(x - size / 2, y - size / 2, size, size);

        GlowRenderer.drawGlow(g2d, pelletShape, Color.YELLOW, 10);

        g2d.setColor(Color.YELLOW);
        g2d.fill(pelletShape);
        g2d.dispose();
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
