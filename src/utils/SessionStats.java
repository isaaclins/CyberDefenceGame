package src.utils;

public class SessionStats {
    private static final int TICKS_PER_SECOND = 60;

    private long elapsedTicks;
    private int enemiesDefeated;
    private int highestLevel;
    private int highestCombo;
    private int powerUpsCollected;

    public SessionStats() {
        reset();
    }

    public void reset() {
        elapsedTicks = 0;
        enemiesDefeated = 0;
        highestLevel = 1;
        highestCombo = 0;
        powerUpsCollected = 0;
    }

    public void tick() {
        elapsedTicks++;
    }

    public void recordEnemyDefeated() {
        enemiesDefeated++;
    }

    public void updateLevel(int currentLevel) {
        if (currentLevel > highestLevel) {
            highestLevel = currentLevel;
        }
    }

    public void updateCombo(int currentStreak) {
        if (currentStreak > highestCombo) {
            highestCombo = currentStreak;
        }
    }

    public void recordPowerUpCollected() {
        powerUpsCollected++;
    }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public int getHighestCombo() {
        return highestCombo;
    }

    public int getPowerUpsCollected() {
        return powerUpsCollected;
    }

    public int getHighestLevel() {
        return highestLevel;
    }

    public String getFormattedSurvivalTime() {
        long totalSeconds = elapsedTicks / TICKS_PER_SECOND;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
