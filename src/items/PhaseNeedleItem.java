package src.items;

import src.entity.Player;

public class PhaseNeedleItem implements Item {
    private static final double DAMAGE_MULTIPLIER = 1.06;

    @Override
    public String getName() {
        return "Phase Needle";
    }

    @Override
    public String getDescription() {
        return "Adds bullet pierce and a small damage bump for line-clear builds.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        player.increasePierceCount(1);
        if (player.getGun() != null) {
            player.getGun().setDamage(player.getGun().getDamage() * DAMAGE_MULTIPLIER);
        }
    }
}
