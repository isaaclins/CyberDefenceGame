package src.items;

import src.entity.Player;

public class MagnetBloomItem implements Item {
    private static final double PICKUP_RADIUS_MULTIPLIER = 1.16;
    private static final double ATTRACTION_RADIUS_MULTIPLIER = 1.20;
    private static final double XP_GAIN_BONUS = 0.08;

    @Override
    public String getName() {
        return "Magnet Bloom";
    }

    @Override
    public String getDescription() {
        return "Expands XP pickup range and adds bonus XP from pickups.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        player.increasePickupAndAttractionRadius(
            PICKUP_RADIUS_MULTIPLIER,
            ATTRACTION_RADIUS_MULTIPLIER
        );
        player.increaseXpGainMultiplier(XP_GAIN_BONUS);
    }
}
