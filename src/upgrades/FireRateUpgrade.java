package src.upgrades;

import src.entity.Player;

public class FireRateUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Fire Rate Up";
    }

    @Override
    public String getDescription() {
        return "Reduces time between shots by 10%";
    }

    @Override
    public void apply(Player player) {
        if (player.getGun() != null) {
            int currentCooldown = player.getGun().getShotCooldownTicks();
            player.getGun().setShotCooldownTicks(Math.max(1, (int) Math.round(currentCooldown * 0.9)));
        }
    }
}
