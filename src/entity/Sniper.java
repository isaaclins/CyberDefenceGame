package src.entity;

import java.util.ArrayList;

public class Sniper extends Gun {
    private static final double BULLET_SIZE = 8;
    private static final double BULLET_DAMAGE = 50;
    private static final double BULLET_SPEED = 30;
    private static final double RELOAD_SPEED = 2000;
    private static final int MAGAZINE_SIZE = 3;
    private static final double SPREAD = 0.05;
    private static final double KNOCKBACK = 12;
    private static final int BULLETS_PER_SHOT = 1;

    public Sniper() {
        super(BULLET_SIZE, BULLET_DAMAGE, BULLET_SPEED, RELOAD_SPEED, MAGAZINE_SIZE, SPREAD, KNOCKBACK,
                BULLETS_PER_SHOT);
    }

    @Override
    public ArrayList<Pellet> shoot(double x, double y, double angle) {
        ArrayList<Pellet> pellets = new ArrayList<>();
        if (currentAmmo > 0 && !reloading) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShotTime > 1000) { // 1000ms fire rate
                for (int i = 0; i < bulletsPerShot; i++) {
                    // Sniper has very low spread, so we can ignore it for the main bullet
                    pellets.add(new Pellet(x, y, angle, bulletSpeed, bulletDamage, bulletSize, knockback));
                }
                lastShotTime = currentTime;
                currentAmmo--;
                if (currentAmmo == 0) {
                    reload();
                }
            }
        }
        return pellets;
    }
}
