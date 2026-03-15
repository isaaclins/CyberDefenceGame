package src.entity;

import java.util.ArrayList;

public class Sniper extends Gun {
    private static final double BULLET_SIZE = 8;
    private static final double BULLET_DAMAGE = 50;
    private static final double BULLET_SPEED = 30;
    private static final int RELOAD_TICKS = 120;
    private static final int SHOT_COOLDOWN_TICKS = 60;
    private static final int MAGAZINE_SIZE = 3;
    private static final double SPREAD = 0.05;
    private static final double KNOCKBACK = 12;
    private static final int BULLETS_PER_SHOT = 1;

    public Sniper() {
        super(BULLET_SIZE, BULLET_DAMAGE, BULLET_SPEED, RELOAD_TICKS, SHOT_COOLDOWN_TICKS, MAGAZINE_SIZE, SPREAD,
                KNOCKBACK,
                BULLETS_PER_SHOT);
    }

    @Override
    public ArrayList<Pellet> shoot(double x, double y, double angle) {
        ArrayList<Pellet> pellets = new ArrayList<>();
        if (beginShot()) {
            for (int i = 0; i < bulletsPerShot; i++) {
                pellets.add(new Pellet(x, y, angle, bulletSpeed, bulletDamage, bulletSize, knockback));
            }
        }
        return pellets;
    }
}
