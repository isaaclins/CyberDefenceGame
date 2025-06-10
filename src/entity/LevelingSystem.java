package src.entity;

public class LevelingSystem {
    private int xp;
    private int level;
    private int xpToNextLevel;

    public LevelingSystem() {
        this.xp = 0;
        this.level = 1;
        this.xpToNextLevel = 100;
    }

    public void addXp(int amount) {
        this.xp += amount;
        while (this.xp >= xpToNextLevel) {
            levelUp();
        }
    }

    private void levelUp() {
        this.level++;
        this.xp -= xpToNextLevel;
        this.xpToNextLevel = (int) (xpToNextLevel * 1.5);
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public int getXpToNextLevel() {
        return xpToNextLevel;
    }
}
