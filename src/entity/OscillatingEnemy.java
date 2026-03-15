package src.entity;

import java.awt.Color;

public abstract class OscillatingEnemy extends Enemy {
    private final double phaseOffset;
    private final double strafeStrength;
    private final double waveStep;
    private double waveAngle;

    public OscillatingEnemy(double x, double y, int health, Color color, double speed, double size, int damage,
            int xpDropAmount, double phaseOffset, double strafeStrength, double waveStep) {
        super(x, y, health, color, speed, size, damage, xpDropAmount);
        this.phaseOffset = phaseOffset;
        this.strafeStrength = strafeStrength;
        this.waveStep = waveStep;
        this.waveAngle = 0;
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
        double forwardX = dx / distance;
        double forwardY = dy / distance;
        double perpendicularX = -forwardY;
        double perpendicularY = forwardX;

        waveAngle += waveStep;
        double waveOffset = Math.sin(waveAngle + phaseOffset);

        velocityX += (forwardX * speed) + (perpendicularX * strafeStrength * waveOffset);
        velocityY += (forwardY * speed) + (perpendicularY * strafeStrength * waveOffset);

        clampVelocity();
        facingAngle = Math.atan2(velocityY, velocityX);
    }

    private void clampVelocity() {
        double maxVelocity = (speed * 8.5) + (strafeStrength * 2.5);
        double velocitySquared = (velocityX * velocityX) + (velocityY * velocityY);
        if (velocitySquared <= maxVelocity * maxVelocity) {
            return;
        }

        double velocityMagnitude = Math.sqrt(velocitySquared);
        velocityX = (velocityX / velocityMagnitude) * maxVelocity;
        velocityY = (velocityY / velocityMagnitude) * maxVelocity;
    }
}
