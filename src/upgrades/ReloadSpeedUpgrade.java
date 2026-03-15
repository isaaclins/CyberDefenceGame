package src.upgrades;

import src.entity.Player;

public class ReloadSpeedUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Reload Speed Up";
    }

    @Override
    public String getDescription() {
        return "Reduces reload time by 15%";
    }

    @Override
    public void apply(Player player) {
        if (player.getGun() != null) {
            player.getGun().setReloadSpeed(player.getGun().getReloadSpeed() * 0.85);
        }
    }
}
