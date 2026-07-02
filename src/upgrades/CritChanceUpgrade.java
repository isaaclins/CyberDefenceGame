package src.upgrades;

import src.entity.Player;

public class CritChanceUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Deadeye";
    }

    @Override
    public String getDescription() {
        return "Increases critical hit chance by 6%. Crits deal double damage.";
    }

    @Override
    public void apply(Player player) {
        player.increaseCriticalChance(0.06);
    }
}
