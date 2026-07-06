package src.items;

import src.entity.Player;

public class ChargeCapacitorItem implements Item {
    private static final double CHARGE_POWER_BONUS = 0.75;

    @Override
    public String getName() {
        return "Charge Capacitor";
    }

    @Override
    public String getDescription() {
        return "Hold shoot to charge one bigger, harder shot. Stacks make charged shots hit harder.";
    }

    @Override
    public void apply(Player player) {
        addStack(player);
        player.enableChargedShots(CHARGE_POWER_BONUS);
    }
}
