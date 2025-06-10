package src.upgrades;

import src.entity.Player;

public interface Upgrade {
    String getName();

    String getDescription();

    void apply(Player player);
}
