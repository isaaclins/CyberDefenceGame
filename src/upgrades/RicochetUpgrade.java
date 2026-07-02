package src.upgrades;

import src.entity.Player;

public class RicochetUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Ricochet";
    }

    @Override
    public String getDescription() {
        return "Bullets bounce toward one additional nearby enemy.";
    }

    @Override
    public void apply(Player player) {
        player.increaseRicochetCount(1);
    }
}
