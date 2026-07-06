package src.utils;

import src.items.BulwarkCoreItem;
import src.items.ChargeCapacitorItem;
import src.items.ChronoAnchorItem;
import src.items.DeadeyeLensItem;
import src.items.DrumFeederItem;
import src.items.Item;
import src.items.KineticBootsItem;
import src.items.MagnetBloomItem;
import src.items.PhaseNeedleItem;
import src.items.RicochetCatalystItem;
import src.items.ScatterMatrixItem;
import src.items.VelocityRoundsItem;
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
    private List<Item> allItems;

    public UpgradeManager() {
        allUpgrades = new ArrayList<>();
        allItems = new ArrayList<>();
        allUpgrades.add(new DamageUpgrade());
        allUpgrades.add(new FireRateUpgrade());
        allUpgrades.add(new ReloadSpeedUpgrade());
        allUpgrades.add(new MagazineSizeUpgrade());
        allUpgrades.add(new MaxHealthUpgrade());
        allUpgrades.add(new MoveSpeedUpgrade());
        allUpgrades.add(new CritChanceUpgrade());
        allUpgrades.add(new PiercingUpgrade());
        allUpgrades.add(new RicochetUpgrade());

        registerItem(new VelocityRoundsItem());
        registerItem(new RicochetCatalystItem());
        registerItem(new ChargeCapacitorItem());
        registerItem(new DrumFeederItem());
        registerItem(new PhaseNeedleItem());
        registerItem(new DeadeyeLensItem());
        registerItem(new BulwarkCoreItem());
        registerItem(new KineticBootsItem());
        registerItem(new MagnetBloomItem());
        registerItem(new ScatterMatrixItem());
        registerItem(new ChronoAnchorItem());
    }

    public List<Upgrade> getRandomUpgrades(int count) {
        List<Upgrade> availableUpgrades = new ArrayList<>(allUpgrades);
        Collections.shuffle(availableUpgrades);
        return availableUpgrades.subList(0, Math.min(count, availableUpgrades.size()));
    }

    public List<Upgrade> getRandomItems(int count) {
        List<Item> availableItems = new ArrayList<>(allItems);
        Collections.shuffle(availableItems);
        return new ArrayList<Upgrade>(availableItems.subList(0, Math.min(count, availableItems.size())));
    }

    private void registerItem(Item item) {
        allItems.add(item);
        allUpgrades.add(item);
    }
}
