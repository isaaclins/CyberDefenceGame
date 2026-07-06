package src.items;

import src.entity.Player;

public class BulwarkCoreItem implements Item {
    @Override
    public String getName() {
        return "Bulwark Core";
    }

    @Override
    public String getDescription() {
        return "Grants a shield charge. Every second stack also adds a heart.";
    }

    @Override
    public void apply(Player player) {
        int stacks = addStack(player);
        player.addShieldCharge();
        if (stacks % 2 == 0) {
            player.increaseMaxHealth(1);
        }
    }
}
