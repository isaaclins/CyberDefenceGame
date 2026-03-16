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

public class WarperEnemy extends Enemy {
    private static final int HEALTH = 420;
    private static final double SPEED = 0.06;
    private static final double SIZE = 42;
    private static final int DAMAGE = 2;
    private static final int XP_DROP_AMOUNT = 70;
    private static final int WARP_COOLDOWN_TICKS = 330;
    private static final int WARP_CHARGE_TICKS = 78;
    private static final Color COLOR = new Color(78, 214, 255);
    private static final Color CORE_COLOR = new Color(232, 250, 255);
    private static final Color CHARGE_COLOR = new Color(120, 255, 240);
    private static final Stroke SHELL_STROKE = new BasicStroke(3f);
    private static final Stroke CHARGE_STROKE = new BasicStroke(2.5f);

    private int warpCooldownTicks;
    private int warpChargeTicksRemaining;
    private int animationTicks;
    private boolean chargingWarp;
    private boolean pendingWarp;

    public WarperEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
        warpCooldownTicks = WARP_COOLDOWN_TICKS;
        warpChargeTicksRemaining = 0;
        animationTicks = 0;
        chargingWarp = false;
        pendingWarp = false;
    }

    public List<EnemyBullet> updateBehavior(double playerX, double playerY, int activeEnemyCount) {
        animationTicks++;

        if (pendingWarp) {
            return Collections.emptyList();
        }

        if (chargingWarp) {
            facingAngle += 0.04;
            velocityX *= 0.78;
            velocityY *= 0.78;
            warpChargeTicksRemaining--;
            if (warpChargeTicksRemaining <= 0) {
                chargingWarp = false;
                pendingWarp = true;
                warpCooldownTicks = WARP_COOLDOWN_TICKS;
            }
            return Collections.emptyList();
        }

        moveToPlayer(playerX, playerY);
        if (warpCooldownTicks > 0) {
            warpCooldownTicks = Math.max(0, warpCooldownTicks - getCooldownTickStep(activeEnemyCount));
        } else {
            chargingWarp = true;
            warpChargeTicksRemaining = WARP_CHARGE_TICKS;
        }
        return Collections.emptyList();
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();

        double pulse = 0.5 + (0.5 * Math.sin(animationTicks * 0.08));
        double shellRadius = (size / 2.0) + (pulse * 1.8);
        Ellipse2D.Double shell = new Ellipse2D.Double(x - shellRadius, y - shellRadius, shellRadius * 2.0,
                shellRadius * 2.0);
        Ellipse2D.Double core = new Ellipse2D.Double(x - (size * 0.18), y - (size * 0.18), size * 0.36, size * 0.36);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.16f));
        g2d.setColor(COLOR);
        g2d.fill(new Ellipse2D.Double(x - ((size / 2.0) + 6.0), y - ((size / 2.0) + 6.0), size + 12.0, size + 12.0));

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
        GlowRenderer.drawGlow(g2d, shell, COLOR, 7);
        g2d.setColor(COLOR);
        g2d.fill(shell);
        g2d.setColor(CORE_COLOR);
        g2d.setStroke(SHELL_STROKE);
        g2d.draw(shell);

        double chargeProgress = getChargeProgress();
        if (chargeProgress > 0.0) {
            double ringRadius = (size / 2.0) + 10.0 + (chargeProgress * 16.0) + (pulse * 2.0);
            Ellipse2D.Double chargeRing = new Ellipse2D.Double(x - ringRadius, y - ringRadius, ringRadius * 2.0,
                    ringRadius * 2.0);
            float chargeAlpha = (float) Math.min(0.8, 0.22 + (chargeProgress * 0.42));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, chargeAlpha));
            GlowRenderer.drawGlow(g2d, chargeRing, CHARGE_COLOR, 5);
            g2d.setColor(CHARGE_COLOR);
            g2d.setStroke(CHARGE_STROKE);
            g2d.draw(chargeRing);
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.96f));
        GlowRenderer.drawGlow(g2d, core, CORE_COLOR, 3);
        g2d.setColor(CORE_COLOR);
        g2d.fill(core);
        g2d.setColor(Color.WHITE);
        g2d.drawLine((int) Math.round(x), (int) Math.round(y),
                (int) Math.round(x + (Math.cos(facingAngle) * size * 0.28)),
                (int) Math.round(y + (Math.sin(facingAngle) * size * 0.28)));

        g2d.setComposite(oldComposite);
        g2d.setStroke(oldStroke);
    }

    @Override
    public double getRenderRadius() {
        return (size / 2.0) + 28.0;
    }

    public boolean hasPendingWarp() {
        return pendingWarp;
    }

    public boolean consumePendingWarp() {
        if (!pendingWarp) {
            return false;
        }
        pendingWarp = false;
        return true;
    }

    private double getChargeProgress() {
        if (pendingWarp) {
            return 1.0;
        }
        if (!chargingWarp) {
            return 0.0;
        }
        return 1.0 - (warpChargeTicksRemaining / (double) WARP_CHARGE_TICKS);
    }

    private int getCooldownTickStep(int activeEnemyCount) {
        int crowdedWaveStep = 1 + (Math.max(0, activeEnemyCount - 1) / 4);
        return Math.min(4, crowdedWaveStep);
    }
}
