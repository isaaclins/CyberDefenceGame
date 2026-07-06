package src.items;

import src.entity.Player;

public class RicochetCatalystItem implements Item {
    private static final double RICOCHET_DAMAGE_BONUS = 0.35;

    @Override
    public String getName() {
        return "Ricochet Catalyst";
    }

    @Override
    public String getDescription() {
        return "Each ricochet multiplies that bullet's remaining damage by 1.5x. Stacks add more ramp.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        player.increaseRicochetCount(1);
        player.increaseRicochetDamageMultiplier(RICOCHET_DAMAGE_BONUS);
    }
}
