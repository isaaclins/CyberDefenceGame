package src.upgrades;

import src.entity.Player;

public class MoveSpeedUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Move Speed Up";
    }

    @Override
    public String getDescription() {
        return "Increases movement speed slightly";
    }

    @Override
    public void apply(Player player) {
        player.increaseAcceleration(0.08);
    }
}
