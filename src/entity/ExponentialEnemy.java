package src.entity;

import java.awt.Color;

public class ExponentialEnemy extends Enemy {
    private static final int HEALTH = 10;
    private static final double BASE_SPEED = 0.015;
    private static final double SIZE = 22;
    private static final int DAMAGE = 1;
    private static final int XP_DROP_AMOUNT = 35;
    private static final int TICKS_PER_SECOND = 60;
    private static final Color COLOR = new Color(255, 240, 110);

    private int ageTicks;

    public ExponentialEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, BASE_SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
        ageTicks = 0;
    }

    @Override
    public void moveToPlayer(double playerX, double playerY) {
        double dx = playerX - x;
        double dy = playerY - y;
        double distanceSquared = (dx * dx) + (dy * dy);
        if (distanceSquared <= 0.0001) {
            return;
        }

        double distance = Math.sqrt(distanceSquared);
        double currentSpeed = getCurrentSpeed();
        velocityX += currentSpeed * (dx / distance);
        velocityY += currentSpeed * (dy / distance);
        facingAngle = Math.atan2(dy, dx);
        ageTicks++;
    }

    private double getCurrentSpeed() {
        double elapsedSeconds = ageTicks / (double) TICKS_PER_SECOND;
        return speed * Math.pow(2.0, elapsedSeconds);
    }
}
