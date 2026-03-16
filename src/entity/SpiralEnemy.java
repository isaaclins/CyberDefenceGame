package src.entity;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

public class SpiralEnemy extends Enemy {
    private static final int HEALTH = 50;
    private static final double SPEED = 0.12;
    private static final double SIZE = 30;
    private static final int DAMAGE = 1;
    private static final int XP_DROP_AMOUNT = 45;
    private static final Color COLOR = new Color(130, 255, 210);
    private static final Color BULLET_COLOR = new Color(255, 120, 90);
    private static final int SHOT_COOLDOWN_TICKS = 20;
    private static final double BULLET_SPEED = 1.0;
    private static final int BULLET_DAMAGE = 1;
    private static final double BULLET_SIZE = 8;
    private static final double SPIRAL_STEP = Math.PI / 18.0;
    private static final double VISUAL_SPIN_STEP = Math.PI / 90.0;

    private int shotCooldownTicks;
    private double shotAngle;

    public SpiralEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
        shotCooldownTicks = SHOT_COOLDOWN_TICKS;
        shotAngle = 0.0;
    }

    @Override
    public void move() {
        velocityX = 0.0;
        velocityY = 0.0;
    }

    @Override
    public List<EnemyBullet> updateBehavior(double playerX, double playerY) {
        facingAngle += VISUAL_SPIN_STEP;

        if (shotCooldownTicks > 0) {
            shotCooldownTicks--;
            return Collections.emptyList();
        }

        double bulletAngle = shotAngle;
        double spawnDistance = (size / 2.0) + (BULLET_SIZE / 2.0);
        double bulletX = x + (Math.cos(bulletAngle) * spawnDistance);
        double bulletY = y + (Math.sin(bulletAngle) * spawnDistance);

        shotAngle += SPIRAL_STEP;
        shotCooldownTicks = SHOT_COOLDOWN_TICKS;

        return Collections.singletonList(new EnemyBullet(bulletX, bulletY, bulletAngle, BULLET_SPEED, BULLET_DAMAGE,
                BULLET_SIZE, BULLET_COLOR));
    }
}
