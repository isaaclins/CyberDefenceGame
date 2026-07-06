package src.items;

import src.entity.Player;

public class ChronoAnchorItem implements Item {
    private static final int TIME_WARP_DURATION_BONUS_TICKS = 90;

    @Override
    public String getName() {
        return "Chrono Anchor";
    }

    @Override
    public String getDescription() {
        return "Extends future Time Warp power-ups for control-focused runs.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        player.addTimeWarpDurationBonus(TIME_WARP_DURATION_BONUS_TICKS);
    }
}
