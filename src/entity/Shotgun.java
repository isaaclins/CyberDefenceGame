package src.entity;

import java.util.ArrayList;
import java.util.Random;

public class Shotgun extends Gun {
    private static final double BULLET_SIZE = 6;
    private static final double BULLET_DAMAGE = 15;
    private static final double BULLET_SPEED = 12;
    private static final double RELOAD_SPEED = 1500;
    private static final int MAGAZINE_SIZE = 5;
    private static final double SPREAD = 0.4;
    private static final double KNOCKBACK = 5;
    private static final int BULLETS_PER_SHOT = 6;

    public Shotgun() {
        super(BULLET_SIZE, BULLET_DAMAGE, BULLET_SPEED, RELOAD_SPEED, MAGAZINE_SIZE, SPREAD, KNOCKBACK,
                BULLETS_PER_SHOT);
    }

    @Override
    public ArrayList<Pellet> shoot(double x, double y, double angle) {
        ArrayList<Pellet> pellets = new ArrayList<>();
        if (currentAmmo > 0 && !reloading) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShotTime > 500) { // 500ms fire rate
                for (int i = 0; i < bulletsPerShot; i++) {
                    Random rand = new Random();
                    double spreadAngle = angle + (rand.nextDouble() - 0.5) * spread;
                    pellets.add(new Pellet(x, y, spreadAngle, bulletSpeed, bulletDamage, bulletSize, knockback));
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
