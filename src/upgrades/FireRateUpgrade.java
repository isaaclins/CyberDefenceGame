package src.upgrades;

import src.entity.Player;

public class FireRateUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Fire Rate Up";
    }

    @Override
    public String getDescription() {
        return "Increases fire rate by 10%";
    }

    @Override
    public void apply(Player player) {
        if (player.getGun() != null) {
            player.getGun().setReloadSpeed(player.getGun().getReloadSpeed() * 0.9);
        }
    }
}
