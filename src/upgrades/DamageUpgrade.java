package src.upgrades;

import src.entity.Player;

public class DamageUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Damage Up";
    }

    @Override
    public String getDescription() {
        return "Increases gun damage by 10%";
    }

    @Override
    public void apply(Player player) {
        if (player.getGun() != null) {
            player.getGun().setDamage(player.getGun().getDamage() * 1.1);
        }
    }
}
