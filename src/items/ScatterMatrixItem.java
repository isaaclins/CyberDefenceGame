package src.items;

import src.entity.Player;

public class ScatterMatrixItem implements Item {
    private static final double SPREAD_MULTIPLIER = 1.16;
    private static final double SIZE_MULTIPLIER = 1.05;
    private static final double KNOCKBACK_MULTIPLIER = 1.10;

    @Override
    public String getName() {
        return "Scatter Matrix";
    }

    @Override
    public String getDescription() {
        return "Adds one projectile per shot with wider, heavier close-range volleys.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        if (player.getGun() == null) {
            return;
        }

        player.getGun().increaseBulletsPerShot(1);
        player.getGun().multiplySpread(SPREAD_MULTIPLIER);
        player.getGun().multiplyBulletSize(SIZE_MULTIPLIER);
        player.getGun().multiplyKnockback(KNOCKBACK_MULTIPLIER);
    }
}
