package src.upgrades;

import src.entity.Player;

public class PiercingUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Piercing Rounds";
    }

    @Override
    public String getDescription() {
        return "Bullets punch through one additional enemy.";
    }

    @Override
    public void apply(Player player) {
        player.increasePierceCount(1);
    }
}
