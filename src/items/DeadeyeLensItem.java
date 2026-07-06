package src.items;

import src.entity.Player;

public class DeadeyeLensItem implements Item {
    private static final double CRIT_CHANCE_BONUS = 0.04;
    private static final double CRIT_DAMAGE_BONUS = 0.15;

    @Override
    public String getName() {
        return "Deadeye Lens";
    }

    @Override
    public String getDescription() {
        return "Raises crit chance and crit damage for burst-damage builds.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        player.increaseCriticalChance(CRIT_CHANCE_BONUS);
        player.increaseCriticalDamageMultiplier(CRIT_DAMAGE_BONUS);
    }
}
