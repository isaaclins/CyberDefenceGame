package src.items;

import src.entity.Player;

public class KineticBootsItem implements Item {
    private static final double ACCELERATION_MULTIPLIER = 1.08;
    private static final double DASH_COOLDOWN_MULTIPLIER = 0.92;

    @Override
    public String getName() {
        return "Kinetic Boots";
    }

    @Override
    public String getDescription() {
        return "Improves movement acceleration and lowers dash cooldown.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        player.multiplyAcceleration(ACCELERATION_MULTIPLIER);
        player.reduceDashCooldown(DASH_COOLDOWN_MULTIPLIER);
    }
}
