package src.entity;

import src.entity.Gun;
import src.entity.SMG;
import src.entity.Shotgun;
import src.entity.Sniper;

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

    private int health;
    private int maxHealth;

    private Gun gun;
    private LevelingSystem levelingSystem;
    private final double pickupRadius = 50.0;
    private final double attractionRadius = 150.0;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.gunAngle = 0;
        this.targetGunAngle = 0;
        this.gun = null;
        this.levelingSystem = new LevelingSystem();
        this.maxHealth = 5;
        this.health = this.maxHealth;
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

    public java.util.ArrayList<Pellet> shoot() {
        return gun.shoot(gunX, gunY, gunAngle);
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
        angleDifference = Math.atan2(Math.sin(angleDifference), Math.cos(angleDifference));
        gunAngle += angleDifference * gunFriction;
        updateGunPosition();
    }

    public void spinGun() {
        gunAngle += spinSpeed;
        updateGunPosition();
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
        }
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

    public double getGunAngle() {
        return gunAngle;
    }

    public Gun getGun() {
        return gun;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public LevelingSystem getLevelingSystem() {
        return levelingSystem;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getPickupRadius() {
        return pickupRadius;
    }

    public double getAttractionRadius() {
        return attractionRadius;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setGun(Gun gun) {
        this.gun = gun;
    }
}
