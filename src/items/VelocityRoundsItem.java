package src.items;

import src.entity.Player;

public class VelocityRoundsItem implements Item {
    private static final double DAMAGE_PER_SPEED_POINT = 0.015;
    private static final double BULLET_SPEED_MULTIPLIER = 1.08;

    @Override
    public String getName() {
        return "Velocity Rounds";
    }

    @Override
    public String getDescription() {
        return "Faster bullets deal more damage. Stacks increase the speed scaling.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        player.increaseVelocityDamageScale(DAMAGE_PER_SPEED_POINT);
        if (player.getGun() != null) {
            player.getGun().multiplyBulletSpeed(BULLET_SPEED_MULTIPLIER);
        }
    }
}
