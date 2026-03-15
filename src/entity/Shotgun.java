package src.entity;

import java.util.ArrayList;

public class Shotgun extends Gun {
    private static final double BULLET_SIZE = 6;
    private static final double BULLET_DAMAGE = 15;
    private static final double BULLET_SPEED = 12;
    private static final int RELOAD_TICKS = 90;
    private static final int SHOT_COOLDOWN_TICKS = 30;
    private static final int MAGAZINE_SIZE = 5;
    private static final double SPREAD = 0.4;
    private static final double KNOCKBACK = 5;
    private static final int BULLETS_PER_SHOT = 6;

    public Shotgun() {
        super(BULLET_SIZE, BULLET_DAMAGE, BULLET_SPEED, RELOAD_TICKS, SHOT_COOLDOWN_TICKS, MAGAZINE_SIZE, SPREAD,
                KNOCKBACK,
                BULLETS_PER_SHOT);
    }

    @Override
    public ArrayList<Pellet> shoot(double x, double y, double angle) {
        ArrayList<Pellet> pellets = new ArrayList<>();
        if (beginShot()) {
            for (int i = 0; i < bulletsPerShot; i++) {
                double spreadAngle = angle + ((RANDOM.nextDouble() - 0.5) * spread);
                pellets.add(new Pellet(x, y, spreadAngle, bulletSpeed, bulletDamage, bulletSize, knockback));
            }
        }
        return pellets;
    }
}
