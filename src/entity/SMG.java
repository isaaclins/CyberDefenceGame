package src.entity;

import java.util.ArrayList;

public class SMG extends Gun {
    private static final double BULLET_SIZE = 10;
    private static final double BULLET_DAMAGE = 20;
    private static final double BULLET_SPEED = 5;
    private static final int RELOAD_TICKS = 72;
    private static final int SHOT_COOLDOWN_TICKS = 2;
    private static final int MAGAZINE_SIZE = 30;
    private static final double SPREAD = 0.25;
    private static final double KNOCKBACK = 2;
    private static final int BULLETS_PER_SHOT = 5;

    public SMG() {
        super(BULLET_SIZE, BULLET_DAMAGE, BULLET_SPEED, RELOAD_TICKS, SHOT_COOLDOWN_TICKS, MAGAZINE_SIZE, SPREAD,
                KNOCKBACK,
                BULLETS_PER_SHOT);
    }

    @Override
    public ArrayList<Pellet> shoot(double x, double y, double angle) {
        ArrayList<Pellet> pellets = new ArrayList<>();
        if (beginShot()) {
            double spreadAngle = angle + ((RANDOM.nextDouble() - 0.5) * spread);
            pellets.add(new Pellet(x, y, spreadAngle, bulletSpeed, bulletDamage, bulletSize, knockback));
        }
        return pellets;
    }
}
