package src.entity;

import java.util.HashMap;
import java.util.Map;
import src.entity.Gun;

public class Player {
    private static final double DIRECTION_EPSILON = 0.0001;
    private static final double DASH_SPEED = 38.0;
    private static final int BASE_DASH_COOLDOWN_TICKS = 90;
    private static final double BASE_CRITICAL_CHANCE = 0.05;
    private static final double BASE_CRITICAL_DAMAGE_MULTIPLIER = 2.0;
    private static final int MAX_SHIELD_CHARGES = 3;

    private double x, y;
    private double previousX, previousY;
    private double velocityX = 0, velocityY = 0;
    private double acceleration = 0.5;
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
    private final double pickupRadius = 65.0;
    private final double attractionRadius = 190.0;
    private final Map<String, Integer> itemStacks = new HashMap<>();
    private double lastMoveDirectionX;
    private double lastMoveDirectionY;
    private boolean hasMoveDirection;
    private int dashCooldownTicksRemaining;
    private int dashCooldownTicks = BASE_DASH_COOLDOWN_TICKS;
    private double criticalChance = BASE_CRITICAL_CHANCE;
    private double criticalDamageMultiplier = BASE_CRITICAL_DAMAGE_MULTIPLIER;
    private int shieldCharges;
    private int pierceCount;
    private int ricochetCount;
    private double velocityDamageScale;
    private double ricochetDamageMultiplier = 1.0;
    private boolean chargedShotsEnabled;
    private double chargedShotPowerScale;
    private double pickupRadiusMultiplier = 1.0;
    private double attractionRadiusMultiplier = 1.0;
    private double xpGainMultiplier = 1.0;
    private int timeWarpDurationBonusTicks;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.previousX = startX;
        this.previousY = startY;
        this.gunAngle = 0;
        this.targetGunAngle = 0;
        this.gun = null;
        this.levelingSystem = new LevelingSystem();
        this.maxHealth = 5;
        this.health = this.maxHealth;
        updateGunPosition();
    }

    public void move(boolean upPressed, boolean downPressed, boolean leftPressed, boolean rightPressed) {
        updateLastMoveDirection(upPressed, downPressed, leftPressed, rightPressed);

        if (upPressed)
            velocityY -= acceleration;
        if (downPressed)
            velocityY += acceleration;
        if (leftPressed)
            velocityX -= acceleration;
        if (rightPressed)
            velocityX += acceleration;

        previousX = x;
        previousY = y;
        x += velocityX;
        y += velocityY;

        velocityX *= friction;
        velocityY *= friction;
        updateGunPosition();
    }

    public boolean tryDash(boolean upPressed, boolean downPressed, boolean leftPressed, boolean rightPressed) {
        updateLastMoveDirection(upPressed, downPressed, leftPressed, rightPressed);
        if (dashCooldownTicksRemaining > 0 || !hasMoveDirection) {
            return false;
        }

        velocityX += lastMoveDirectionX * DASH_SPEED;
        velocityY += lastMoveDirectionY * DASH_SPEED;
        dashCooldownTicksRemaining = dashCooldownTicks;
        return true;
    }

    public boolean tickDashCooldown() {
        if (dashCooldownTicksRemaining <= 0) {
            return false;
        }

        dashCooldownTicksRemaining--;
        return dashCooldownTicksRemaining == 0;
    }

    public java.util.ArrayList<Pellet> shoot() {
        if (gun == null) {
            return new java.util.ArrayList<>();
        }
        return gun.shoot(gunX, gunY, gunAngle);
    }

    public java.util.ArrayList<Pellet> shootFromCenter(double angle) {
        if (gun == null) {
            return new java.util.ArrayList<>();
        }
        return gun.shoot(x, y, angle);
    }

    public Gun.TickResult tickGun() {
        if (gun == null) {
            return Gun.TickResult.of(false, false);
        }
        return gun.tick();
    }

    public void updateGunPosition() {
        gunX = x + gunRadius * Math.cos(gunAngle);
        gunY = y + gunRadius * Math.sin(gunAngle);
    }

    public void updateGunAngle(double targetX, double targetY) {
        targetGunAngle = Math.atan2(targetY - y, targetX - x);
    }

    public double aimGunDirectlyAt(double targetX, double targetY) {
        updateGunAngle(targetX, targetY);
        gunAngle = targetGunAngle;
        updateGunPosition();
        return gunAngle;
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

    public int heal(int amount) {
        if (amount <= 0 || health >= maxHealth) {
            return 0;
        }

        int healedAmount = Math.min(amount, maxHealth - health);
        health += healedAmount;
        return healedAmount;
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

    public double getPreviousX() {
        return previousX;
    }

    public double getPreviousY() {
        return previousY;
    }

    public double getPickupRadius() {
        return pickupRadius * pickupRadiusMultiplier;
    }

    public double getAttractionRadius() {
        return attractionRadius * attractionRadiusMultiplier;
    }

    public int getDashCooldownTicksRemaining() {
        return dashCooldownTicksRemaining;
    }

    public int getDashCooldownTicks() {
        return dashCooldownTicks;
    }

    public double getDashChargeRatio() {
        if (dashCooldownTicks <= 0) {
            return 1.0;
        }
        return 1.0 - (dashCooldownTicksRemaining / (double) dashCooldownTicks);
    }

    public boolean isDashReady() {
        return dashCooldownTicksRemaining <= 0 && hasMoveDirection;
    }

    public double getLastMoveDirectionX() {
        return lastMoveDirectionX;
    }

    public double getLastMoveDirectionY() {
        return lastMoveDirectionY;
    }

    public void setX(double x) {
        this.x = x;
        previousX = x;
        updateGunPosition();
    }

    public void setY(double y) {
        this.y = y;
        previousY = y;
        updateGunPosition();
    }

    public void teleportTo(double x, double y) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.velocityX = 0;
        this.velocityY = 0;
        updateGunPosition();
    }

    public void setGun(Gun gun) {
        this.gun = gun;
    }

    public boolean reloadGun() {
        return gun != null && gun.reload();
    }

    public boolean consumeReloadStarted() {
        return gun != null && gun.consumeReloadStarted();
    }

    public int addItemStack(String stackKey) {
        int nextStacks = getItemStack(stackKey) + 1;
        itemStacks.put(stackKey, nextStacks);
        return nextStacks;
    }

    public int getItemStack(String stackKey) {
        Integer stacks = itemStacks.get(stackKey);
        if (stacks == null) {
            return 0;
        }
        return stacks;
    }

    public void increaseMaxHealth(int amount) {
        if (amount <= 0) {
            return;
        }
        maxHealth += amount;
        health = Math.min(maxHealth, health + amount);
    }

    public void multiplyAcceleration(double factor) {
        if (factor <= 0) {
            return;
        }
        acceleration = Math.max(0.1, acceleration * factor);
    }

    public double getCriticalChance() {
        return criticalChance;
    }

    public double getCriticalDamageMultiplier() {
        return criticalDamageMultiplier;
    }

    public void increaseCriticalChance(double amount) {
        if (amount <= 0) {
            return;
        }
        criticalChance = Math.min(1.0, criticalChance + amount);
    }

    public void increaseCriticalDamageMultiplier(double amount) {
        if (amount <= 0.0) {
            return;
        }
        criticalDamageMultiplier += amount;
    }

    public int getShieldCharges() {
        return shieldCharges;
    }

    public boolean addShieldCharge() {
        if (shieldCharges >= MAX_SHIELD_CHARGES) {
            return false;
        }
        shieldCharges++;
        return true;
    }

    public boolean consumeShieldCharge() {
        if (shieldCharges <= 0) {
            return false;
        }
        shieldCharges--;
        return true;
    }

    public int getPierceCount() {
        return pierceCount;
    }

    public void increasePierceCount(int amount) {
        if (amount <= 0) {
            return;
        }
        pierceCount += amount;
    }

    public int getRicochetCount() {
        return ricochetCount;
    }

    public void increaseRicochetCount(int amount) {
        if (amount <= 0) {
            return;
        }
        ricochetCount += amount;
    }

    public void increaseVelocityDamageScale(double amount) {
        if (amount <= 0.0) {
            return;
        }
        velocityDamageScale += amount;
    }

    public void increasePickupAndAttractionRadius(double pickupMultiplier, double attractionMultiplier) {
        if (pickupMultiplier > 0.0) {
            pickupRadiusMultiplier *= pickupMultiplier;
        }
        if (attractionMultiplier > 0.0) {
            attractionRadiusMultiplier *= attractionMultiplier;
        }
    }

    public void increaseXpGainMultiplier(double amount) {
        if (amount <= 0.0) {
            return;
        }
        xpGainMultiplier += amount;
    }

    public double getXpGainMultiplier() {
        return xpGainMultiplier;
    }

    public void reduceDashCooldown(double factor) {
        if (factor <= 0.0 || factor >= 1.0) {
            return;
        }
        dashCooldownTicks = Math.max(24, (int) Math.round(dashCooldownTicks * factor));
        dashCooldownTicksRemaining = Math.min(dashCooldownTicksRemaining, dashCooldownTicks);
    }

    public void addTimeWarpDurationBonus(int ticks) {
        if (ticks <= 0) {
            return;
        }
        timeWarpDurationBonusTicks += ticks;
    }

    public int getTimeWarpDurationTicks(int baseDurationTicks) {
        return Math.max(1, baseDurationTicks + timeWarpDurationBonusTicks);
    }

    public double getRicochetDamageMultiplier() {
        return ricochetDamageMultiplier;
    }

    public void increaseRicochetDamageMultiplier(double amount) {
        if (amount <= 0.0) {
            return;
        }
        ricochetDamageMultiplier += amount;
    }

    public void enableChargedShots(double powerScaleBonus) {
        chargedShotsEnabled = true;
        chargedShotPowerScale += Math.max(0.0, powerScaleBonus);
    }

    public boolean hasChargedShots() {
        return chargedShotsEnabled;
    }

    public void applyShotPerks(Pellet pellet, double chargeRatio) {
        applyVelocityDamage(pellet);
        applyChargedShot(pellet, chargeRatio);
    }

    private void applyVelocityDamage(Pellet pellet) {
        if (velocityDamageScale <= 0.0) {
            return;
        }
        pellet.multiplyDamage(1.0 + (pellet.getSpeed() * velocityDamageScale));
    }

    private void applyChargedShot(Pellet pellet, double chargeRatio) {
        if (!chargedShotsEnabled || chargeRatio <= 0.0) {
            return;
        }

        double clampedCharge = Math.max(0.0, Math.min(1.0, chargeRatio));
        double scale = Math.max(1.0, chargedShotPowerScale);
        pellet.multiplyDamage(1.0 + (2.0 * clampedCharge * scale));
        pellet.multiplySize(1.0 + (1.6 * clampedCharge));
        pellet.multiplyKnockback(1.0 + clampedCharge);
    }

    private void updateLastMoveDirection(boolean upPressed, boolean downPressed, boolean leftPressed,
            boolean rightPressed) {
        double inputX = 0.0;
        double inputY = 0.0;
        if (leftPressed) {
            inputX -= 1.0;
        }
        if (rightPressed) {
            inputX += 1.0;
        }
        if (upPressed) {
            inputY -= 1.0;
        }
        if (downPressed) {
            inputY += 1.0;
        }

        double magnitudeSquared = (inputX * inputX) + (inputY * inputY);
        if (magnitudeSquared <= DIRECTION_EPSILON) {
            return;
        }

        double magnitude = Math.sqrt(magnitudeSquared);
        lastMoveDirectionX = inputX / magnitude;
        lastMoveDirectionY = inputY / magnitude;
        hasMoveDirection = true;
    }
}
