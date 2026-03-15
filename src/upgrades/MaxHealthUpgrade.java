package src.upgrades;

import src.entity.Player;

public class MaxHealthUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Max Health Up";
    }

    @Override
    public String getDescription() {
        return "Adds 1 heart and heals it";
    }

    @Override
    public void apply(Player player) {
        player.increaseMaxHealth(1);
    }
}
