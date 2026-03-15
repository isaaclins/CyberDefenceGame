package src.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import src.entity.BigEnemy;
import src.entity.CosinusEnemy;
import src.entity.Enemy;
import src.entity.ExponentialEnemy;
import src.entity.NormalEnemy;
import src.entity.SmallEnemy;
import src.entity.SinusEnemy;

public class WaveDirector {
    private static final int INTER_WAVE_TICKS = 60 * 5;
    private static final int EARLY_WAVE_ONE_TOTAL = 4;
    private static final int EARLY_WAVE_STEP = 2;
    private static final int SIN_COS_UNLOCK_WAVE = 2;
    private static final int SMALL_XP_DROP = 10;
    private static final int NORMAL_XP_DROP = 20;
    private static final int SINUS_XP_DROP = 24;
    private static final int COSINUS_XP_DROP = 24;
    private static final int EXPONENTIAL_XP_DROP = 35;
    private static final int BIG_XP_DROP = 50;
    private static final double PAIR_SEPARATION = 28.0;

    private int waveNumber;
    private int enemiesToSpawn;
    private int activeEnemyCap;
    private int spawnCooldownTicks;
    private int interWaveTicksRemaining;
    private boolean waveActive;
    private boolean sinCosPairPending;

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
        sinCosPairPending = false;
    }

    public void startRun() {
        reset();
        startNextWave();
    }

    public WaveTickResult tick(int activeEnemies, double playerX, double playerY, Random random) {
        List<Enemy> spawnedEnemies = Collections.emptyList();
        boolean waveStarted = false;
        boolean waveCleared = false;

        if (interWaveTicksRemaining > 0) {
            interWaveTicksRemaining--;
            if (interWaveTicksRemaining == 0 && activeEnemies == 0) {
                startNextWave();
                waveStarted = true;
            }
            return new WaveTickResult(spawnedEnemies, waveStarted, waveCleared);
        }

        if (!waveActive) {
            if (activeEnemies == 0) {
                startNextWave();
                waveStarted = true;
            }
            return new WaveTickResult(spawnedEnemies, waveStarted, waveCleared);
        }

        if (spawnCooldownTicks > 0) {
            spawnCooldownTicks--;
        }

        boolean hasRoomForSpawn = activeEnemies < activeEnemyCap;
        boolean hasRoomForPair = activeEnemies <= activeEnemyCap - 2;

        if (enemiesToSpawn > 0 && hasRoomForSpawn && spawnCooldownTicks == 0
                && (!sinCosPairPending || hasRoomForPair || enemiesToSpawn < 2)) {
            spawnedEnemies = createSpawnBatchNearPlayer(playerX, playerY, random);
            enemiesToSpawn -= spawnedEnemies.size();
            spawnCooldownTicks = getSpawnDelayTicks(waveNumber);
        }

        if (enemiesToSpawn == 0 && activeEnemies == 0) {
            waveActive = false;
            interWaveTicksRemaining = INTER_WAVE_TICKS;
            waveCleared = true;
        }

        return new WaveTickResult(spawnedEnemies, waveStarted, waveCleared);
    }

    private List<Enemy> createSpawnBatchNearPlayer(double playerX, double playerY, Random random) {
        if (sinCosPairPending && enemiesToSpawn >= 2) {
            sinCosPairPending = false;
            return createSinCosPairNearPlayer(playerX, playerY, random);
        }

        return Collections.singletonList(createSingleEnemyNearPlayer(playerX, playerY, random));
    }

    private Enemy createSingleEnemyNearPlayer(double playerX, double playerY, Random random) {
        double spawnRadius = 280 + (random.nextDouble() * 200);
        double spawnAngle = random.nextDouble() * Math.PI * 2;
        double spawnX = playerX + (Math.cos(spawnAngle) * spawnRadius);
        double spawnY = playerY + (Math.sin(spawnAngle) * spawnRadius);

        int roll = random.nextInt(getSingleEnemyWeightTotal());
        int smallWeight = getSmallEnemyWeight();
        int normalWeight = getNormalEnemyWeight();
        int exponentialWeight = getExponentialEnemyWeight();

        if (roll < smallWeight) {
            return new SmallEnemy(spawnX, spawnY);
        }
        if (roll < smallWeight + normalWeight) {
            return new NormalEnemy(spawnX, spawnY);
        }
        if (roll < smallWeight + normalWeight + exponentialWeight) {
            return new ExponentialEnemy(spawnX, spawnY);
        }
        return new BigEnemy(spawnX, spawnY);
    }

    private List<Enemy> createSinCosPairNearPlayer(double playerX, double playerY, Random random) {
        double spawnRadius = 300 + (random.nextDouble() * 180);
        double spawnAngle = random.nextDouble() * Math.PI * 2;
        double baseX = playerX + (Math.cos(spawnAngle) * spawnRadius);
        double baseY = playerY + (Math.sin(spawnAngle) * spawnRadius);
        double perpendicularX = -Math.sin(spawnAngle);
        double perpendicularY = Math.cos(spawnAngle);

        List<Enemy> spawnedEnemies = new ArrayList<>(2);
        spawnedEnemies.add(new SinusEnemy(baseX + (perpendicularX * PAIR_SEPARATION),
                baseY + (perpendicularY * PAIR_SEPARATION)));
        spawnedEnemies.add(new CosinusEnemy(baseX - (perpendicularX * PAIR_SEPARATION),
                baseY - (perpendicularY * PAIR_SEPARATION)));
        return spawnedEnemies;
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
        int singleEnemyWeightTotal = getSingleEnemyWeightTotal();
        if (singleEnemyWeightTotal <= 0) {
            return SMALL_XP_DROP;
        }

        int regularEnemyKills = getTotalEnemiesForWave(waveNumber) - getGuaranteedSinCosKills();
        if (regularEnemyKills < 1) {
            regularEnemyKills = 1;
        }

        int weightedSingleXp = (getSmallEnemyWeight() * SMALL_XP_DROP) + (getNormalEnemyWeight() * NORMAL_XP_DROP)
                + (getExponentialEnemyWeight() * EXPONENTIAL_XP_DROP) + (getBigEnemyWeight() * BIG_XP_DROP);
        int regularXpPerKill = Math.max(1, Math.round(weightedSingleXp / (float) singleEnemyWeightTotal));
        int totalEnemies = Math.max(1, getTotalEnemiesForWave(waveNumber));
        int totalEstimatedXp = (regularEnemyKills * regularXpPerKill) + getGuaranteedSinCosXp();
        return Math.max(1, Math.round(totalEstimatedXp / (float) totalEnemies));
    }

    private void startNextWave() {
        waveNumber++;
        enemiesToSpawn = getTotalEnemiesForWave(waveNumber);
        activeEnemyCap = getConcurrentEnemyCap(waveNumber);
        spawnCooldownTicks = 0;
        interWaveTicksRemaining = 0;
        waveActive = true;
        sinCosPairPending = waveNumber >= SIN_COS_UNLOCK_WAVE && enemiesToSpawn >= 2;
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

    private int getSingleEnemyWeightTotal() {
        return getSmallEnemyWeight() + getNormalEnemyWeight() + getExponentialEnemyWeight() + getBigEnemyWeight();
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

    private int getExponentialEnemyWeight() {
        if (waveNumber <= 5) {
            return 0;
        }
        if (waveNumber <= 8) {
            return 8;
        }
        return 14;
    }

    private int getGuaranteedSinCosKills() {
        return waveNumber >= SIN_COS_UNLOCK_WAVE ? 2 : 0;
    }

    private int getGuaranteedSinCosXp() {
        return waveNumber >= SIN_COS_UNLOCK_WAVE ? SINUS_XP_DROP + COSINUS_XP_DROP : 0;
    }

    public static final class WaveTickResult {
        private final List<Enemy> spawnedEnemies;
        private final boolean waveStarted;
        private final boolean waveCleared;

        public WaveTickResult(List<Enemy> spawnedEnemies, boolean waveStarted, boolean waveCleared) {
            this.spawnedEnemies = spawnedEnemies;
            this.waveStarted = waveStarted;
            this.waveCleared = waveCleared;
        }

        public List<Enemy> getSpawnedEnemies() {
            return spawnedEnemies;
        }

        public boolean isWaveStarted() {
            return waveStarted;
        }

        public boolean isWaveCleared() {
            return waveCleared;
        }
    }
}
