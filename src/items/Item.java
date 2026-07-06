package src.items;

import src.entity.Player;
import src.upgrades.Upgrade;

public interface Item extends Upgrade {
    default String getStackKey() {
        return getClass().getName();
    }

    default String getName(Player player) {
        int stacks = player.getItemStack(getStackKey());
        if (stacks <= 0) {
            return getName();
        }
        return getName() + " x" + stacks;
    }

    default String getDescription(Player player) {
        int nextStack = player.getItemStack(getStackKey()) + 1;
        return getDescription() + " Next stack: " + nextStack + ".";
    }

    default int addStack(Player player) {
        return player.addItemStack(getStackKey());
    }
}
