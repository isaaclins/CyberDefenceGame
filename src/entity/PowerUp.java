package src.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import src.utils.GlowRenderer;

public class PowerUp {
    public enum Type {
        OVERDRIVE("Overdrive", new Color(255, 196, 64)),
        SHIELD("Shield", new Color(96, 224, 255)),
        TIME_WARP("Time Warp", new Color(196, 120, 255)),
        MAGNET("Magnet", new Color(120, 255, 140));

        private final String label;
        private final Color color;

        Type(String label, Color color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public Color getColor() {
            return color;
        }
    }

    private static final int LIFETIME_TICKS = 600;
    private static final int BLINK_THRESHOLD_TICKS = 150;
    private static final int BLINK_INTERVAL_TICKS = 8;
    private static final double BASE_SIZE = 12.0;
    private static final double PULSE_AMPLITUDE = 2.5;
    private static final double PULSE_STEP = 0.09;
    private static final double SPIN_STEP = 0.035;

    private final Type type;
    private double x;
    private double y;
    private int lifetimeTicksRemaining;
    private int ageTicks;

    public PowerUp(Type type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.lifetimeTicksRemaining = LIFETIME_TICKS;
        this.ageTicks = 0;
    }

    public void tick() {
        ageTicks++;
        lifetimeTicksRemaining--;
    }

    public void translatePosition(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public boolean isExpired() {
        return lifetimeTicksRemaining <= 0;
    }

    public void render(Graphics g) {
        if (isBlinkedOut()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        double size = BASE_SIZE + (Math.sin(ageTicks * PULSE_STEP) * PULSE_AMPLITUDE);
        g2d.translate(x, y);
        g2d.rotate((Math.PI / 4.0) + (ageTicks * SPIN_STEP));

        Rectangle2D shape = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
        GlowRenderer.drawGlow(g2d, shape, type.getColor(), 6);
        g2d.setColor(type.getColor());
        g2d.fill(shape);

        double coreSize = size * 0.45;
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(-coreSize / 2, -coreSize / 2, coreSize, coreSize));

        g2d.setTransform(oldTransform);
    }

    public Type getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    private boolean isBlinkedOut() {
        if (lifetimeTicksRemaining > BLINK_THRESHOLD_TICKS) {
            return false;
        }
        return (lifetimeTicksRemaining / BLINK_INTERVAL_TICKS) % 2 == 0;
    }
}
