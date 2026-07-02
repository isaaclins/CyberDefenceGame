package src.utils;

import src.upgrades.Upgrade;
import src.upgrades.CritChanceUpgrade;
import src.upgrades.DamageUpgrade;
import src.upgrades.FireRateUpgrade;
import src.upgrades.MagazineSizeUpgrade;
import src.upgrades.MaxHealthUpgrade;
import src.upgrades.MoveSpeedUpgrade;
import src.upgrades.PiercingUpgrade;
import src.upgrades.ReloadSpeedUpgrade;
import src.upgrades.RicochetUpgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpgradeManager {
    private List<Upgrade> allUpgrades;

    public UpgradeManager() {
        allUpgrades = new ArrayList<>();
        allUpgrades.add(new DamageUpgrade());
        allUpgrades.add(new FireRateUpgrade());
        allUpgrades.add(new ReloadSpeedUpgrade());
        allUpgrades.add(new MagazineSizeUpgrade());
        allUpgrades.add(new MaxHealthUpgrade());
        allUpgrades.add(new MoveSpeedUpgrade());
        allUpgrades.add(new CritChanceUpgrade());
        allUpgrades.add(new PiercingUpgrade());
        allUpgrades.add(new RicochetUpgrade());
    }

    public List<Upgrade> getRandomUpgrades(int count) {
        List<Upgrade> availableUpgrades = new ArrayList<>(allUpgrades);
        Collections.shuffle(availableUpgrades);
        return availableUpgrades.subList(0, Math.min(count, availableUpgrades.size()));
    }
}
