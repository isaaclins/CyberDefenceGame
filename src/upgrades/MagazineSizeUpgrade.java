package src.upgrades;

import src.entity.Player;

public class MagazineSizeUpgrade implements Upgrade {
    @Override
    public String getName() {
        return "Magazine Up";
    }

    @Override
    public String getDescription() {
        return "Adds 2 bullets to the magazine";
    }

    @Override
    public void apply(Player player) {
        if (player.getGun() != null) {
            player.getGun().increaseMagazineSize(2);
        }
    }
}
