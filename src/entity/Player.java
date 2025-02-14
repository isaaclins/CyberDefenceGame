package src.entity;

public class Player {
    private double x, y;
    private double velocityX = 0, velocityY = 0;
    private final double acceleration = 0.5;
    private final double friction = 0.90;
    private double gunX, gunY;
    private double gunAngle;
    private double targetGunAngle;
    private final double gunRadius = 20.0;
    private final double spinSpeed = 0.1;
    private final double gunFriction = 0.1;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.gunAngle = 0;
        this.targetGunAngle = 0;
        updateGunPosition();
    }

    public void move(boolean upPressed, boolean downPressed, boolean leftPressed, boolean rightPressed) {
        if (upPressed)
            velocityY -= acceleration;
        if (downPressed)
            velocityY += acceleration;
        if (leftPressed)
            velocityX -= acceleration;
        if (rightPressed)
            velocityX += acceleration;

        x += velocityX;
        y += velocityY;

        velocityX *= friction;
        velocityY *= friction;
        updateGunPosition();
    }

    public void updateGunPosition() {
        gunX = x + gunRadius * Math.cos(gunAngle);
        gunY = y + gunRadius * Math.sin(gunAngle);
    }

    public void updateGunAngle(double targetX, double targetY) {
        targetGunAngle = Math.atan2(targetY - y, targetX - x);
    }

    public void smoothGunTransition() {
        double angleDifference = targetGunAngle - gunAngle;
        angleDifference = Math.atan2(Math.sin(angleDifference), Math.cos(angleDifference)); // Normalize angle difference
        gunAngle += angleDifference * gunFriction;
        updateGunPosition();
    }

    public void spinGun() {
        gunAngle += spinSpeed;
        updateGunPosition();
    }

    public void applyKnockback(double knockbackX, double knockbackY) {
        this.velocityX += knockbackX;
        this.velocityY += knockbackY;
    }

    public double getGunX() {
        return gunX;
    }

    public double getGunY() {
        return gunY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
