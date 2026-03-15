package src.entity;

import java.util.ArrayList;
import java.util.Random;

public abstract class Gun {
    protected static final Random RANDOM = new Random();

    protected double bulletSize;
    protected double bulletDamage;
    protected double bulletSpeed;
    protected int reloadDurationTicks;
    protected int shotCooldownTicks;
    protected int magazineSize;
    protected double spread;
    protected double knockback;
    protected int bulletsPerShot;
    protected boolean reloading;
    protected int currentAmmo;
    protected int reloadTicksRemaining;
    protected int shotCooldownTicksRemaining;

    public Gun(double bulletSize, double bulletDamage, double bulletSpeed, int reloadDurationTicks, int shotCooldownTicks,
            int magazineSize, double spread, double knockback, int bulletsPerShot) {
        this.bulletSize = bulletSize;
        this.bulletDamage = bulletDamage;
        this.bulletSpeed = bulletSpeed;
        this.reloadDurationTicks = reloadDurationTicks;
        this.shotCooldownTicks = shotCooldownTicks;
        this.magazineSize = magazineSize;
        this.spread = spread;
        this.knockback = knockback;
        this.bulletsPerShot = bulletsPerShot;
        this.currentAmmo = magazineSize;
        this.reloading = false;
        this.reloadTicksRemaining = 0;
        this.shotCooldownTicksRemaining = 0;
    }

    public abstract ArrayList<Pellet> shoot(double x, double y, double angle);

    public boolean tick() {
        if (shotCooldownTicksRemaining > 0) {
            shotCooldownTicksRemaining--;
        }
        if (reloadTicksRemaining > 0) {
            reloadTicksRemaining--;
            if (reloadTicksRemaining == 0) {
                currentAmmo = magazineSize;
                reloading = false;
                return true;
            }
        }
        return false;
    }

    protected boolean beginShot() {
        if (reloading) {
            return false;
        }
        if (shotCooldownTicksRemaining > 0) {
            return false;
        }
        if (currentAmmo <= 0) {
            beginReload();
            return false;
        }

        currentAmmo--;
        shotCooldownTicksRemaining = shotCooldownTicks;
        if (currentAmmo == 0) {
            beginReload();
        }
        return true;
    }

    protected void beginReload() {
        if (reloading) {
            return;
        }
        reloading = true;
        reloadTicksRemaining = reloadDurationTicks;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public boolean isReloading() {
        return reloading;
    }

    public double getSpread() {
        return spread;
    }

    public double getDamage() {
        return bulletDamage;
    }

    public void setDamage(double damage) {
        this.bulletDamage = damage;
    }

    public double getReloadSpeed() {
        return reloadDurationTicks;
    }

    public void setReloadSpeed(double reloadSpeed) {
        this.reloadDurationTicks = Math.max(1, (int) Math.round(reloadSpeed));
    }

    public int getShotCooldownTicks() {
        return shotCooldownTicks;
    }

    public void setShotCooldownTicks(int shotCooldownTicks) {
        this.shotCooldownTicks = Math.max(1, shotCooldownTicks);
    }

    public void increaseMagazineSize(int amount) {
        if (amount <= 0) {
            return;
        }
        magazineSize += amount;
        currentAmmo = Math.min(magazineSize, currentAmmo + amount);
    }
}
