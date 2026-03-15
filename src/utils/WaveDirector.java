package src.utils;

import java.util.Random;

import src.entity.BigEnemy;
import src.entity.Enemy;
import src.entity.NormalEnemy;
import src.entity.SmallEnemy;

public class WaveDirector {
    private static final int INTER_WAVE_TICKS = 60 * 5;
    private static final int EARLY_WAVE_ONE_TOTAL = 4;
    private static final int EARLY_WAVE_STEP = 2;
    private static final int SMALL_XP_DROP = 10;
    private static final int NORMAL_XP_DROP = 20;
    private static final int BIG_XP_DROP = 50;

    private int waveNumber;
    private int enemiesToSpawn;
    private int activeEnemyCap;
    private int spawnCooldownTicks;
    private int interWaveTicksRemaining;
    private boolean waveActive;

    public WaveDirector() {
        reset();
    }

    public void reset() {
        waveNumber = 0;
        enemiesToSpawn = 0;
        activeEnemyCap = 6;
        spawnCooldownTicks = 0;
        interWaveTicksRemaining = 0;
        waveActive = false;
    }

    public void startRun() {
        reset();
        startNextWave();
    }

    public WaveTickResult tick(int activeEnemies) {
        boolean spawnRequested = false;
        boolean waveStarted = false;
        boolean waveCleared = false;

        if (interWaveTicksRemaining > 0) {
            interWaveTicksRemaining--;
            if (interWaveTicksRemaining == 0 && activeEnemies == 0) {
                startNextWave();
                waveStarted = true;
            }
            return new WaveTickResult(spawnRequested, waveStarted, waveCleared);
        }

        if (!waveActive) {
            if (activeEnemies == 0) {
                startNextWave();
                waveStarted = true;
            }
            return new WaveTickResult(spawnRequested, waveStarted, waveCleared);
        }

        if (spawnCooldownTicks > 0) {
            spawnCooldownTicks--;
        }

        if (enemiesToSpawn > 0 && activeEnemies < activeEnemyCap && spawnCooldownTicks == 0) {
            enemiesToSpawn--;
            spawnCooldownTicks = getSpawnDelayTicks(waveNumber);
            spawnRequested = true;
        }

        if (enemiesToSpawn == 0 && activeEnemies == 0) {
            waveActive = false;
            interWaveTicksRemaining = INTER_WAVE_TICKS;
            waveCleared = true;
        }

        return new WaveTickResult(spawnRequested, waveStarted, waveCleared);
    }

    public Enemy createEnemyNearPlayer(double playerX, double playerY, Random random) {
        double spawnRadius = 280 + (random.nextDouble() * 200);
        double spawnAngle = random.nextDouble() * Math.PI * 2;
        double spawnX = playerX + (Math.cos(spawnAngle) * spawnRadius);
        double spawnY = playerY + (Math.sin(spawnAngle) * spawnRadius);

        int roll = random.nextInt(getEnemyWeightTotal());
        int smallWeight = getSmallEnemyWeight();
        int normalWeight = getNormalEnemyWeight();

        if (roll < smallWeight) {
            return new SmallEnemy(spawnX, spawnY);
        }
        if (roll < smallWeight + normalWeight) {
            return new NormalEnemy(spawnX, spawnY);
        }
        return new BigEnemy(spawnX, spawnY);
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public int getInterWaveTicksRemaining() {
        return interWaveTicksRemaining;
    }

    public boolean isWaveActive() {
        return waveActive;
    }

    public int getEstimatedXpPerKill() {
        int totalWeight = getEnemyWeightTotal();
        if (totalWeight <= 0) {
            return SMALL_XP_DROP;
        }

        int weightedXp = (getSmallEnemyWeight() * SMALL_XP_DROP) + (getNormalEnemyWeight() * NORMAL_XP_DROP)
                + (getBigEnemyWeight() * BIG_XP_DROP);
        return Math.max(1, Math.round(weightedXp / (float) totalWeight));
    }

    private void startNextWave() {
        waveNumber++;
        enemiesToSpawn = getTotalEnemiesForWave(waveNumber);
        activeEnemyCap = getConcurrentEnemyCap(waveNumber);
        spawnCooldownTicks = 0;
        interWaveTicksRemaining = 0;
        waveActive = true;
    }

    private int getTotalEnemiesForWave(int wave) {
        if (wave <= 4) {
            return EARLY_WAVE_ONE_TOTAL + ((wave - 1) * EARLY_WAVE_STEP);
        }
        return 10 + ((wave - 4) * 3);
    }

    private int getConcurrentEnemyCap(int wave) {
        if (wave < 3) {
            return 6;
        }
        if (wave < 5) {
            return 8;
        }
        int cap = 10 + (((wave - 5) / 3) * 2);
        return Math.min(20, cap);
    }

    private int getSpawnDelayTicks(int wave) {
        return Math.max(18, 48 - (wave * 2));
    }

    private int getEnemyWeightTotal() {
        return getSmallEnemyWeight() + getNormalEnemyWeight() + getBigEnemyWeight();
    }

    private int getSmallEnemyWeight() {
        if (waveNumber <= 2) {
            return 65;
        }
        if (waveNumber <= 4) {
            return 50;
        }
        if (waveNumber <= 7) {
            return 40;
        }
        return 30;
    }

    private int getNormalEnemyWeight() {
        if (waveNumber <= 2) {
            return 35;
        }
        if (waveNumber <= 4) {
            return 38;
        }
        if (waveNumber <= 7) {
            return 40;
        }
        return 45;
    }

    private int getBigEnemyWeight() {
        if (waveNumber <= 2) {
            return 0;
        }
        if (waveNumber <= 4) {
            return 12;
        }
        if (waveNumber <= 7) {
            return 20;
        }
        return 25;
    }

    public static final class WaveTickResult {
        private final boolean spawnRequested;
        private final boolean waveStarted;
        private final boolean waveCleared;

        public WaveTickResult(boolean spawnRequested, boolean waveStarted, boolean waveCleared) {
            this.spawnRequested = spawnRequested;
            this.waveStarted = waveStarted;
            this.waveCleared = waveCleared;
        }

        public boolean isSpawnRequested() {
            return spawnRequested;
        }

        public boolean isWaveStarted() {
            return waveStarted;
        }

        public boolean isWaveCleared() {
            return waveCleared;
        }
    }
}
