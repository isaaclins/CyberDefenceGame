package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import src.utils.GlowRenderer;

public class Chest {
    private static final Color GLOW_COLOR = new Color(255, 196, 64);
    private static final Color LID_COLOR = new Color(164, 92, 36);
    private static final Color BODY_COLOR = new Color(116, 64, 32);
    private static final Color BAND_COLOR = new Color(222, 168, 72);
    private static final Color LOCK_COLOR = new Color(255, 232, 128);
    private static final double COLLISION_RADIUS = 24.0;
    private static final double BODY_WIDTH = 32.0;
    private static final double BODY_HEIGHT = 24.0;
    private static final double PULSE_STEP = 0.08;

    private double x;
    private double y;
    private int ageTicks;

    public Chest(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void tick() {
        ageTicks++;
    }

    public void translatePosition(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        double bobOffset = Math.sin(ageTicks * PULSE_STEP) * 1.5;
        double left = x - (BODY_WIDTH / 2.0);
        double top = y - (BODY_HEIGHT / 2.0) + bobOffset;
        RoundRectangle2D body = new RoundRectangle2D.Double(left, top + 6, BODY_WIDTH, BODY_HEIGHT - 4, 4, 4);

        GlowRenderer.drawGlow(g2d, body, GLOW_COLOR, 7);

        g2d.setColor(BODY_COLOR);
        g2d.fill(body);
        g2d.setColor(LID_COLOR);
        g2d.fill(new RoundRectangle2D.Double(left, top, BODY_WIDTH, 12, 8, 8));

        g2d.setColor(BAND_COLOR);
        g2d.fill(new Rectangle2D.Double(x - 3, top + 1, 6, BODY_HEIGHT + 1));
        g2d.draw(new Rectangle2D.Double(left + 2, top + 11, BODY_WIDTH - 4, 1));

        g2d.setColor(new Color(62, 36, 24));
        g2d.draw(new RoundRectangle2D.Double(left, top, BODY_WIDTH, BODY_HEIGHT + 2, 6, 6));

        g2d.setColor(LOCK_COLOR);
        g2d.fill(new RoundRectangle2D.Double(x - 5, top + 13, 10, 9, 3, 3));
        g2d.setColor(new Color(90, 58, 28));
        g2d.draw(new RoundRectangle2D.Double(x - 5, top + 13, 10, 9, 3, 3));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getCollisionRadius() {
        return COLLISION_RADIUS;
    }
}
