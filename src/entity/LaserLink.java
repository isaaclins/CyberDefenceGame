package src.entity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import src.utils.GlowRenderer;

public class LaserLink {
    private static final Color ACTIVE_COLOR = new Color(255, 60, 60);
    private static final Color FADING_COLOR = new Color(255, 96, 96);
    private static final Stroke LASER_STROKE = new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final int FADE_TICKS = 10;

    private LaserTwinEnemy firstTwin;
    private LaserTwinEnemy secondTwin;
    private double previousStartX;
    private double previousStartY;
    private double previousEndX;
    private double previousEndY;
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private boolean active;
    private int fadeTicksRemaining;

    public LaserLink(LaserTwinEnemy firstTwin, LaserTwinEnemy secondTwin) {
        this.firstTwin = firstTwin;
        this.secondTwin = secondTwin;
        active = true;
        fadeTicksRemaining = 0;
        syncEndpoints();
        previousStartX = startX;
        previousStartY = startY;
        previousEndX = endX;
        previousEndY = endY;
    }

    public LaserTwinEnemy getFirstTwin() {
        return firstTwin;
    }

    public LaserTwinEnemy getSecondTwin() {
        return secondTwin;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isExpired() {
        return !active && fadeTicksRemaining <= 0;
    }

    public boolean references(LaserTwinEnemy twin) {
        return twin != null && (twin == firstTwin || twin == secondTwin);
    }

    public void breakLink() {
        if (!active) {
            return;
        }

        syncEndpoints();
        active = false;
        fadeTicksRemaining = FADE_TICKS;
        firstTwin = null;
        secondTwin = null;
    }

    public void tick() {
        if (active) {
            previousStartX = startX;
            previousStartY = startY;
            previousEndX = endX;
            previousEndY = endY;
            syncEndpoints();
            return;
        }

        if (!active && fadeTicksRemaining > 0) {
            fadeTicksRemaining--;
        }
    }

    public double getPreviousStartX() {
        return previousStartX;
    }

    public double getPreviousStartY() {
        return previousStartY;
    }

    public double getPreviousEndX() {
        return previousEndX;
    }

    public double getPreviousEndY() {
        return previousEndY;
    }

    public double getStartX() {
        syncEndpoints();
        return startX;
    }

    public double getStartY() {
        syncEndpoints();
        return startY;
    }

    public double getEndX() {
        syncEndpoints();
        return endX;
    }

    public double getEndY() {
        syncEndpoints();
        return endY;
    }

    public void render(Graphics g) {
        if (!active && fadeTicksRemaining <= 0) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();

        Line2D.Double beam = new Line2D.Double(getStartX(), getStartY(), getEndX(), getEndY());
        float alpha = active ? 0.94f : (fadeTicksRemaining / (float) FADE_TICKS);
        if (!active && (fadeTicksRemaining % 4) < 2) {
            alpha *= 0.35f;
        }

        if (alpha > 0f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            GlowRenderer.drawGlow(g2d, beam, active ? ACTIVE_COLOR : FADING_COLOR, 4);
            g2d.setColor(active ? ACTIVE_COLOR : FADING_COLOR);
            g2d.setStroke(LASER_STROKE);
            g2d.draw(beam);
        }

        g2d.setComposite(oldComposite);
        g2d.setStroke(oldStroke);
    }

    private void syncEndpoints() {
        if (!active || firstTwin == null || secondTwin == null) {
            return;
        }

        startX = firstTwin.getX();
        startY = firstTwin.getY();
        endX = secondTwin.getX();
        endY = secondTwin.getY();
    }
}
