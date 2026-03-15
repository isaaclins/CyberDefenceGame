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
    protected int reloadStartAmmo;
    protected int reloadTicksTotal;
    protected boolean reloadStartedSinceLastCheck;

    public static final class TickResult {
        private static final TickResult NONE = new TickResult(false, false);
        private static final TickResult AMMO_INSERTED = new TickResult(true, false);
        private static final TickResult RELOAD_COMPLETED = new TickResult(false, true);
        private static final TickResult AMMO_INSERTED_AND_COMPLETED = new TickResult(true, true);

        private final boolean ammoInserted;
        private final boolean reloadCompleted;

        private TickResult(boolean ammoInserted, boolean reloadCompleted) {
            this.ammoInserted = ammoInserted;
            this.reloadCompleted = reloadCompleted;
        }

        public static TickResult of(boolean ammoInserted, boolean reloadCompleted) {
            if (ammoInserted && reloadCompleted) {
                return AMMO_INSERTED_AND_COMPLETED;
            }
            if (ammoInserted) {
                return AMMO_INSERTED;
            }
            if (reloadCompleted) {
                return RELOAD_COMPLETED;
            }
            return NONE;
        }

        public boolean isAmmoInserted() {
            return ammoInserted;
        }

        public boolean isReloadCompleted() {
            return reloadCompleted;
        }
    }

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
        this.reloadStartAmmo = currentAmmo;
        this.reloadTicksTotal = 0;
        this.reloadStartedSinceLastCheck = false;
    }

    public abstract ArrayList<Pellet> shoot(double x, double y, double angle);

    public TickResult tick() {
        boolean ammoInserted = false;
        boolean reloadCompleted = false;

        if (shotCooldownTicksRemaining > 0) {
            shotCooldownTicksRemaining--;
        }
        if (reloadTicksRemaining > 0) {
            int ammoBeforeTick = currentAmmo;
            reloadTicksRemaining--;
            currentAmmo = Math.max(currentAmmo, getReloadDisplayAmmo());
            ammoInserted = currentAmmo > ammoBeforeTick;
            if (reloadTicksRemaining == 0) {
                currentAmmo = magazineSize;
                reloading = false;
                reloadCompleted = true;
            }
        }
        return TickResult.of(ammoInserted, reloadCompleted);
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

    protected boolean beginReload() {
        if (reloading || currentAmmo >= magazineSize) {
            return false;
        }

        reloading = true;
        reloadStartAmmo = currentAmmo;
        reloadTicksTotal = Math.max(1, reloadDurationTicks);
        reloadTicksRemaining = reloadTicksTotal;
        reloadStartedSinceLastCheck = true;
        return true;
    }

    public boolean reload() {
        return beginReload();
    }

    public boolean consumeReloadStarted() {
        boolean reloadStarted = reloadStartedSinceLastCheck;
        reloadStartedSinceLastCheck = false;
        return reloadStarted;
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

    private int getReloadDisplayAmmo() {
        if (!reloading || reloadTicksTotal <= 0) {
            return currentAmmo;
        }

        int missingAmmo = magazineSize - reloadStartAmmo;
        if (missingAmmo <= 0) {
            return currentAmmo;
        }

        double progress = 1.0 - (reloadTicksRemaining / (double) reloadTicksTotal);
        int loadedAmmo = (int) Math.floor(progress * missingAmmo);
        return Math.min(magazineSize, reloadStartAmmo + loadedAmmo);
    }
}
