package src.utils;

import src.upgrades.Upgrade;
import src.upgrades.DamageUpgrade;
import src.upgrades.FireRateUpgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpgradeManager {
    private List<Upgrade> allUpgrades;

    public UpgradeManager() {
        allUpgrades = new ArrayList<>();
        allUpgrades.add(new DamageUpgrade());
        allUpgrades.add(new FireRateUpgrade());
        // Add other upgrades here
    }

    public List<Upgrade> getRandomUpgrades(int count) {
        List<Upgrade> availableUpgrades = new ArrayList<>(allUpgrades);
        Collections.shuffle(availableUpgrades);
        return availableUpgrades.subList(0, Math.min(count, availableUpgrades.size()));
    }
}
