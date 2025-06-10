package src.entity;

import java.util.ArrayList;

public abstract class Gun {
    protected double bulletSize;
    protected double bulletDamage;
    protected double bulletSpeed;
    protected double reloadSpeed;
    protected int magazineSize;
    protected double spread;
    protected double knockback;
    protected int bulletsPerShot;
    protected long lastShotTime;
    protected boolean reloading;
    protected int currentAmmo;

    public Gun(double bulletSize, double bulletDamage, double bulletSpeed, double reloadSpeed,
            int magazineSize, double spread, double knockback, int bulletsPerShot) {
        this.bulletSize = bulletSize;
        this.bulletDamage = bulletDamage;
        this.bulletSpeed = bulletSpeed;
        this.reloadSpeed = reloadSpeed;
        this.magazineSize = magazineSize;
        this.spread = spread;
        this.knockback = knockback;
        this.bulletsPerShot = bulletsPerShot;
        this.currentAmmo = magazineSize;
        this.lastShotTime = 0;
        this.reloading = false;
    }

    public abstract ArrayList<Pellet> shoot(double x, double y, double angle);

    public void reload() {
        if (!reloading) {
            reloading = true;
            // In a real game, you might use a timer here.
            // For now, we'll just simulate the reload time.
            new Thread(() -> {
                try {
                    Thread.sleep((long) reloadSpeed);
                    currentAmmo = magazineSize;
                    reloading = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
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
        return reloadSpeed;
    }

    public void setReloadSpeed(double reloadSpeed) {
        this.reloadSpeed = reloadSpeed;
    }
}
