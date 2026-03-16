package src.entity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.Collections;
import java.util.List;

import src.utils.GlowRenderer;

public class MutantEnemy extends Enemy {
    private static final int HEALTH = 160;
    private static final double SPEED = 0.11;
    private static final double SIZE = 24;
    private static final int DAMAGE = 1;
    private static final int XP_DROP_AMOUNT = 42;
    private static final Color COLOR = new Color(156, 255, 116);
    private static final Color AURA_COLOR = new Color(128, 255, 96);
    private static final Color CORE_COLOR = new Color(235, 255, 208);
    private static final Stroke AURA_STROKE = new BasicStroke(4f);
    private static final double AURA_RADIUS = 172.0;
    private static final int EXPOSURE_THRESHOLD_TICKS = 75;
    private static final int EXPOSURE_RESET_TICKS = 24;
    private static final int RADIATION_DAMAGE = 1;

    private int animationTicks;

    public MutantEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
        animationTicks = 0;
    }

    @Override
    public List<EnemyBullet> updateBehavior(double playerX, double playerY) {
        animationTicks++;
        moveToPlayer(playerX, playerY);
        return Collections.emptyList();
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();

        double auraPulse = 0.5 + (0.5 * Math.sin(animationTicks * 0.09));
        double outerRadius = AURA_RADIUS + (auraPulse * 10.0);
        double innerRadius = (AURA_RADIUS * 0.64) + (auraPulse * 7.0);
        Ellipse2D.Double outerAura = new Ellipse2D.Double(x - outerRadius, y - outerRadius, outerRadius * 2.0,
                outerRadius * 2.0);
        Ellipse2D.Double innerAura = new Ellipse2D.Double(x - innerRadius, y - innerRadius, innerRadius * 2.0,
                innerRadius * 2.0);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f + (float) (auraPulse * 0.04)));
        g2d.setColor(AURA_COLOR);
        g2d.fill(outerAura);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f + (float) (auraPulse * 0.04)));
        g2d.fill(innerAura);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.42f));
        g2d.setStroke(AURA_STROKE);
        GlowRenderer.drawGlow(g2d, outerAura, AURA_COLOR, 5);
        g2d.setColor(AURA_COLOR);
        g2d.draw(outerAura);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
        GlowRenderer.drawGlow(g2d, innerAura, CORE_COLOR, 4);
        g2d.setColor(CORE_COLOR);
        g2d.draw(innerAura);

        g2d.setComposite(oldComposite);
        g2d.setStroke(oldStroke);

        super.render(g);
    }

    @Override
    public double getRenderRadius() {
        return AURA_RADIUS + 8.0;
    }

    public double getAuraRadius() {
        return AURA_RADIUS;
    }

    public int getExposureThresholdTicks() {
        return EXPOSURE_THRESHOLD_TICKS;
    }

    public int getExposureResetTicks() {
        return EXPOSURE_RESET_TICKS;
    }

    public int getRadiationDamage() {
        return RADIATION_DAMAGE;
    }

    public Color getAuraColor() {
        return AURA_COLOR;
    }
}
