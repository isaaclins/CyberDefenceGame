package src.items;

import src.entity.Player;

public class DrumFeederItem implements Item {
    private static final int MAGAZINE_BONUS = 2;
    private static final double COOLDOWN_MULTIPLIER = 0.94;

    @Override
    public String getName() {
        return "Drum Feeder";
    }

    @Override
    public String getDescription() {
        return "Adds magazine size and slightly lowers shot cooldown for rapid-fire builds.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        if (player.getGun() == null) {
            return;
        }

        player.getGun().increaseMagazineSize(MAGAZINE_BONUS);
        player.getGun().setShotCooldownTicks(
            Math.max(
                1,
                (int) Math.round(
                    player.getGun().getShotCooldownTicks() *
                    COOLDOWN_MULTIPLIER
                )
            )
        );
    }
}
