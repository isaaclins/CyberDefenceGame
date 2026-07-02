package src.utils;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import src.entity.Particle;

public class EffectManager {
    private static final int MAX_PARTICLES = 240;
    private static final int DAMAGE_FLASH_TICKS = 10;
    private static final int WAVE_PULSE_TICKS = 30;
    private static final int LEVEL_PULSE_TICKS = 34;
    private static final Color DAMAGE_FLASH_COLOR = new Color(255, 64, 64);
    private static final Color WAVE_PULSE_COLOR = new Color(64, 220, 255);
    private static final Color LEVEL_PULSE_COLOR = new Color(96, 255, 160);
    private static final Color TELEPORT_WAVE_COLOR = new Color(138, 255, 248);
    private static final Color PROJECTILE_CLASH_CORE_COLOR = new Color(255, 248, 210);
    private static final Color PROJECTILE_CLASH_SPARK_COLOR = new Color(120, 228, 255);
    private static final Color DASH_CORE_COLOR = new Color(220, 250, 255);
    private static final Color DASH_STREAK_COLOR = new Color(96, 232, 255);
    private static final Color CRIT_SPARK_COLOR = new Color(255, 216, 64);
    private static final Color SHIELD_BLOCK_COLOR = new Color(96, 224, 255);
    private static final Color TIME_WARP_TINT_COLOR = new Color(150, 90, 255);
    private static final Color LOW_HEALTH_COLOR = new Color(255, 40, 40);
    private static final Color BOSS_SPAWN_COLOR = new Color(200, 64, 255);
    private static final Stroke OVERLAY_STROKE = new BasicStroke(6f);
    private static final double LOW_HEALTH_PULSE_STEP = 0.09;

    private final List<Particle> particles = new ArrayList<>();

    private int damageFlashTicksRemaining;
    private int wavePulseTicksRemaining;
    private int levelPulseTicksRemaining;
    private int screenShakeTicksRemaining;
    private double screenShakeMagnitude;
    private long tickCounter;

    public void reset() {
        particles.clear();
        damageFlashTicksRemaining = 0;
        wavePulseTicksRemaining = 0;
        levelPulseTicksRemaining = 0;
        screenShakeTicksRemaining = 0;
        screenShakeMagnitude = 0;
        tickCounter = 0;
    }

    public void tick() {
        tickCounter++;
        if (damageFlashTicksRemaining > 0) {
            damageFlashTicksRemaining--;
        }
        if (wavePulseTicksRemaining > 0) {
            wavePulseTicksRemaining--;
        }
        if (levelPulseTicksRemaining > 0) {
            levelPulseTicksRemaining--;
        }
        if (screenShakeTicksRemaining > 0) {
            screenShakeTicksRemaining--;
            screenShakeMagnitude *= 0.84;
        } else {
            screenShakeMagnitude = 0;
        }

        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.tick();
            if (!particle.isAlive()) {
                iterator.remove();
            }
        }
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public void emitMuzzleFlash(double x, double y, double angle, Color color, Random random, int minCount,
            int maxCount, double shakeMagnitude, int shakeDurationTicks) {
        int count = minCount;
        if (maxCount > minCount) {
            count += random.nextInt((maxCount - minCount) + 1);
        }
        for (int i = 0; i < count; i++) {
            double spread = (random.nextDouble() - 0.5) * 0.55;
            double particleAngle = angle + spread;
            double speed = 1.3 + (random.nextDouble() * 2.0);
            double dx = Math.cos(particleAngle) * speed;
            double dy = Math.sin(particleAngle) * speed;
            addParticle(new Particle(x, y, dx, dy, 12 + random.nextInt(7), 3.5 + random.nextDouble(), 0.88, color, 0));
        }
        triggerScreenShake(shakeMagnitude, shakeDurationTicks);
    }

    public void emitHitSparks(double x, double y, Color color, Random random) {
        emitBurst(4 + random.nextInt(3), x, y, 1.2, 2.6, 10, 16, 2.3, 3.6, 0.9, color, 1, random);
    }

    public void emitEnemyDeath(double x, double y, Color color, Random random) {
        emitBurst(12 + random.nextInt(5), x, y, 1.4, 3.2, 16, 26, 3.0, 4.8, 0.92, color, 2, random);
    }

    public void emitXpPickup(double x, double y, Random random) {
        emitBurst(3 + random.nextInt(2), x, y, 0.7, 1.8, 10, 16, 2.0, 3.2, 0.9, Color.GREEN, 1, random);
    }

    public void emitProjectileClash(double x, double y, Random random) {
        emitBurst(8 + random.nextInt(3), x, y, 0.8, 1.6, 8, 14, 2.0, 3.4, 0.9, PROJECTILE_CLASH_CORE_COLOR, 1,
                false, random);
        emitBurst(5 + random.nextInt(3), x, y, 1.0, 2.0, 8, 12, 1.4, 2.4, 0.88, PROJECTILE_CLASH_SPARK_COLOR, 1,
                false, random);
    }

    public void emitLevelUp(double x, double y, Random random) {
        levelPulseTicksRemaining = LEVEL_PULSE_TICKS;
        emitBurst(16 + random.nextInt(5), x, y, 1.1, 2.8, 18, 28, 3.0, 4.8, 0.93, LEVEL_PULSE_COLOR, 3, random);
    }

    public void emitWaveStart(double x, double y, Random random) {
        wavePulseTicksRemaining = WAVE_PULSE_TICKS;
        emitBurst(12 + random.nextInt(4), x, y, 1.0, 2.2, 14, 22, 2.8, 4.0, 0.92, WAVE_PULSE_COLOR, 2, random);
    }

    public void emitTeleportWave(double x, double y, Random random) {
        int count = 16 + random.nextInt(5);
        double baseAngle = random.nextDouble() * Math.PI * 2;
        for (int i = 0; i < count; i++) {
            double angle = baseAngle + ((Math.PI * 2.0 * i) / count);
            double spawnRadius = 4.0 + (random.nextDouble() * 2.0);
            double speed = 1.0 + (random.nextDouble() * 0.45);
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed;
            int lifetime = 12 + random.nextInt(4);
            double size = 2.0 + (random.nextDouble() * 1.4);
            addParticle(new Particle(x + (Math.cos(angle) * spawnRadius), y + (Math.sin(angle) * spawnRadius), dx, dy,
                    lifetime, size, 0.93, TELEPORT_WAVE_COLOR, 2));
        }
    }

    public void emitDash(double x, double y, double directionX, double directionY, Random random) {
        double directionLengthSquared = (directionX * directionX) + (directionY * directionY);
        if (directionLengthSquared <= 0.0001) {
            return;
        }

        double directionLength = Math.sqrt(directionLengthSquared);
        double normalizedDirectionX = directionX / directionLength;
        double normalizedDirectionY = directionY / directionLength;
        double trailDirectionX = -normalizedDirectionX;
        double trailDirectionY = -normalizedDirectionY;
        double perpendicularX = -normalizedDirectionY;
        double perpendicularY = normalizedDirectionX;

        for (int i = 0; i < 12; i++) {
            double spread = (random.nextDouble() - 0.5) * 16.0;
            double speed = 1.8 + (random.nextDouble() * 2.4);
            double lateralDrift = (random.nextDouble() - 0.5) * 0.9;
            double spawnX = x + (normalizedDirectionX * (2.0 + random.nextDouble() * 6.0))
                    + (perpendicularX * spread);
            double spawnY = y + (normalizedDirectionY * (2.0 + random.nextDouble() * 6.0))
                    + (perpendicularY * spread);
            double dx = (trailDirectionX * speed) + (perpendicularX * lateralDrift);
            double dy = (trailDirectionY * speed) + (perpendicularY * lateralDrift);
            Color color = i < 4 ? DASH_CORE_COLOR : DASH_STREAK_COLOR;
            addParticle(new Particle(spawnX, spawnY, dx, dy, 10 + random.nextInt(6), 2.4 + random.nextDouble() * 2.1,
                    0.84, color, 2, false));
        }

        triggerScreenShake(2.8, 5);
    }

    public void emitCritHit(double x, double y, Random random) {
        emitBurst(8 + random.nextInt(4), x, y, 1.8, 3.6, 12, 20, 2.6, 4.2, 0.9, CRIT_SPARK_COLOR, 2, random);
        triggerScreenShake(3.4, 4);
    }

    public void emitShieldBlock(double x, double y, Random random) {
        int count = 14 + random.nextInt(4);
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 * i) / count;
            double speed = 1.6 + (random.nextDouble() * 0.6);
            addParticle(new Particle(x + (Math.cos(angle) * 12.0), y + (Math.sin(angle) * 12.0),
                    Math.cos(angle) * speed, Math.sin(angle) * speed, 14 + random.nextInt(5),
                    2.2 + random.nextDouble(), 0.9, SHIELD_BLOCK_COLOR, 2, false));
        }
        triggerScreenShake(2.2, 4);
    }

    public void emitSplit(double x, double y, Color color, Random random) {
        emitBurst(10 + random.nextInt(4), x, y, 1.6, 3.0, 12, 20, 2.4, 4.0, 0.9, color, 2, random);
    }

    public void emitPowerUpPickup(double x, double y, Color color, Random random) {
        emitBurst(12 + random.nextInt(5), x, y, 1.2, 2.6, 16, 24, 2.6, 4.2, 0.92, color, 2, random);
        triggerScreenShake(2.4, 4);
    }

    public void emitBossSpawn(double x, double y, Random random) {
        int count = 24 + random.nextInt(6);
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 * i) / count;
            double speed = 1.4 + (random.nextDouble() * 1.2);
            addParticle(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, 20 + random.nextInt(8),
                    3.0 + (random.nextDouble() * 2.0), 0.93, BOSS_SPAWN_COLOR, 3));
        }
        triggerScreenShake(5.0, 12);
    }

    public void triggerDamageFlash() {
        damageFlashTicksRemaining = DAMAGE_FLASH_TICKS;
    }

    public Point getShakeOffset(Random random) {
        if (screenShakeTicksRemaining <= 0 || screenShakeMagnitude <= 0) {
            return new Point(0, 0);
        }
        int offsetX = (int) Math.round((random.nextDouble() - 0.5) * screenShakeMagnitude * 2);
        int offsetY = (int) Math.round((random.nextDouble() - 0.5) * screenShakeMagnitude * 2);
        return new Point(offsetX, offsetY);
    }

    public void renderOverlay(Graphics2D g2d, int width, int height, boolean timeWarpActive, boolean lowHealthActive) {
        Composite previousComposite = g2d.getComposite();
        Stroke previousStroke = g2d.getStroke();

        renderTimeWarpTint(g2d, width, height, timeWarpActive);
        renderLowHealthVignette(g2d, width, height, lowHealthActive);
        renderDamageFlash(g2d, width, height);
        renderPulse(g2d, width, height, wavePulseTicksRemaining, WAVE_PULSE_TICKS, WAVE_PULSE_COLOR);
        renderPulse(g2d, width, height, levelPulseTicksRemaining, LEVEL_PULSE_TICKS, LEVEL_PULSE_COLOR);

        g2d.setComposite(previousComposite);
        g2d.setStroke(previousStroke);
    }

    private void renderTimeWarpTint(Graphics2D g2d, int width, int height, boolean timeWarpActive) {
        if (!timeWarpActive) {
            return;
        }

        float pulse = (float) ((Math.sin(tickCounter * 0.12) + 1.0) / 2.0);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f + (0.04f * pulse)));
        g2d.setColor(TIME_WARP_TINT_COLOR);
        g2d.fillRect(0, 0, width, height);
    }

    private void renderLowHealthVignette(Graphics2D g2d, int width, int height, boolean lowHealthActive) {
        if (!lowHealthActive) {
            return;
        }

        float pulse = (float) ((Math.sin(tickCounter * LOW_HEALTH_PULSE_STEP) + 1.0) / 2.0);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f + (0.18f * pulse)));
        g2d.setColor(LOW_HEALTH_COLOR);
        g2d.setStroke(OVERLAY_STROKE);
        g2d.drawRect(4, 4, width - 8, height - 8);
        g2d.drawRect(10, 10, width - 20, height - 20);
    }

    private void renderDamageFlash(Graphics2D g2d, int width, int height) {
        if (damageFlashTicksRemaining <= 0) {
            return;
        }

        float alpha = 0.28f * (damageFlashTicksRemaining / (float) DAMAGE_FLASH_TICKS);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(DAMAGE_FLASH_COLOR);
        g2d.fillRect(0, 0, width, height);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(0.38f, alpha + 0.08f)));
        g2d.setStroke(OVERLAY_STROKE);
        g2d.drawRect(6, 6, width - 12, height - 12);
    }

    private void renderPulse(Graphics2D g2d, int width, int height, int ticksRemaining, int totalTicks, Color color) {
        if (ticksRemaining <= 0) {
            return;
        }

        float ratio = ticksRemaining / (float) totalTicks;
        int maxRadius = Math.max(width, height) / 2;
        int radius = Math.max(12, (int) (maxRadius * (1.0f - (ratio * ratio))));
        int centerX = width / 2;
        int centerY = height / 2;
        int diameter = radius * 2;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.24f * ratio));
        g2d.setColor(color);
        g2d.fillOval(centerX - radius, centerY - radius, diameter, diameter);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * ratio));
        g2d.setStroke(OVERLAY_STROKE);
        g2d.drawOval(centerX - radius, centerY - radius, diameter, diameter);
    }

    private void triggerScreenShake(double magnitude, int durationTicks) {
        screenShakeMagnitude = Math.max(screenShakeMagnitude, magnitude);
        screenShakeTicksRemaining = Math.max(screenShakeTicksRemaining, durationTicks);
    }

    private void emitBurst(int count, double x, double y, double minSpeed, double maxSpeed, int minLifetime,
            int maxLifetime, double minSize, double maxSize, double friction, Color color, int priority,
            Random random) {
        emitBurst(count, x, y, minSpeed, maxSpeed, minLifetime, maxLifetime, minSize, maxSize, friction, color,
                priority, true, random);
    }

    private void emitBurst(int count, double x, double y, double minSpeed, double maxSpeed, int minLifetime,
            int maxLifetime, double minSize, double maxSize, double friction, Color color, int priority,
            boolean activatesRoomWindow, Random random) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = minSpeed + (random.nextDouble() * (maxSpeed - minSpeed));
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed;
            int lifetime = minLifetime + random.nextInt((maxLifetime - minLifetime) + 1);
            double size = minSize + (random.nextDouble() * (maxSize - minSize));
            addParticle(new Particle(x, y, dx, dy, lifetime, size, friction, color, priority, activatesRoomWindow));
        }
    }

    private void addParticle(Particle particle) {
        if (particles.size() >= MAX_PARTICLES) {
            int removalIndex = findParticleToReplace(particle.getPriority());
            if (removalIndex < 0) {
                return;
            }
            particles.remove(removalIndex);
        }
        particles.add(particle);
    }

    private int findParticleToReplace(int incomingPriority) {
        for (int priority = 0; priority <= incomingPriority; priority++) {
            for (int i = 0; i < particles.size(); i++) {
                if (particles.get(i).getPriority() == priority) {
                    return i;
                }
            }
        }
        return -1;
    }
}
