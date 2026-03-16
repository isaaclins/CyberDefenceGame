package src.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import src.entity.Enemy;
import src.entity.EnemyBullet;
import src.entity.FloatingText;
import src.entity.Gun;
import src.entity.LaserLink;
import src.entity.LaserTwinEnemy;
import src.entity.MutantEnemy;
import src.entity.Particle;
import src.entity.Pellet;
import src.entity.Player;
import src.entity.SMG;
import src.entity.Shotgun;
import src.entity.Sniper;
import src.entity.WarperEnemy;
import src.entity.XP;
import src.screens.GameOverScreen;
import src.screens.MenuScreen;
import src.screens.UpgradeScreen;
import src.utils.AudioManager;
import src.utils.EffectManager;
import src.utils.GameLoop;
import src.utils.GameWindow;
import src.utils.InputHandler;
import src.utils.Renderer;
import src.utils.RoomRenderBucket;
import src.utils.RoomWindow;
import src.utils.SessionStats;
import src.utils.UpgradeManager;
import src.utils.WaveDirector;

public class Game extends Canvas {
    private static final Font PAUSE_FONT = new Font("Arial", Font.BOLD, 50);
    private static final Font INTER_WAVE_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final int PLAYER_COLLISION_RADIUS = 10;
    private static final int HIT_COOLDOWN_TICKS = 60;
    private static final int GAME_START_GRACE_TICKS = 120;
    private static final int ROOM_TRANSITION_GRACE_TICKS = 36;
    private static final int HEART_RECOVERY_WAVE_INTERVAL = 5;
    private static final int HEAL_TEXT_LIFETIME_TICKS = 48;
    private static final int LASER_LINK_DAMAGE = 1;
    private static final double LASER_PAIR_HALF_SPAN = 60.0;
    private static final double LASER_PAIR_ADVANCE_DISTANCE = 24.0;
    private static final double LASER_LINK_ROOM_PADDING = 8.0;
    private static final double LASER_LINK_COLLISION_RADIUS = PLAYER_COLLISION_RADIUS + 4.0;
    private static final int MUTANT_EXPOSURE_RECOVERY_PER_TICK = 2;
    private static final double ZERO_LENGTH_EPSILON = 0.0001;

    private final GameWindow window;
    private final GameLoop gameLoop;
    private final Renderer renderer;
    private final InputHandler inputHandler;
    private final UpgradeManager upgradeManager;
    private final AudioManager audioManager;
    private final EffectManager effectManager;
    private final WaveDirector waveDirector;
    private final SessionStats sessionStats;
    private final MenuScreen menuScreen;
    private final GameOverScreen gameOverScreen;
    private final UpgradeScreen upgradeScreen;
    private final Random random = new Random();
    private final List<Pellet> pellets = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final List<LaserLink> laserLinks = new ArrayList<>();
    private final List<XP> xps = new ArrayList<>();
    private final Map<String, RoomWindow> roomWindows = new HashMap<>();
    private final Map<String, RoomRenderBucket> roomBuckets = new HashMap<>();
    private final int WINDOW_WIDTH;
    private final int WINDOW_HEIGHT;
    private final int roomWidth;
    private final int roomHeight;
    private final Point initialWindowLocation;

    private GameState gameState;
    private Player player;
    private double cameraX;
    private double cameraY;
    private int roomCol;
    private int roomRow;
    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean dashPressed;
    private boolean dashRequested;
    private boolean shooting;
    private int hitCooldownTicksRemaining;
    private int playerGraceTicksRemaining;
    private int mutantExposureTicks;

    public Game() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        WINDOW_WIDTH = screenSize.width / 3;
        WINDOW_HEIGHT = screenSize.height / 3;
        roomWidth = WINDOW_WIDTH;
        roomHeight = WINDOW_HEIGHT;

        Dimension size = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setPreferredSize(size);
        setIgnoreRepaint(true);

        window = new GameWindow("Dungeon Crawler", WINDOW_WIDTH, WINDOW_HEIGHT, this, javax.swing.JFrame.EXIT_ON_CLOSE);
        window.setAlwaysOnTop(true);
        initialWindowLocation = window.getLocation();

        menuScreen = new MenuScreen();
        gameOverScreen = new GameOverScreen();
        upgradeScreen = new UpgradeScreen();
        upgradeManager = new UpgradeManager();
        audioManager = new AudioManager();
        effectManager = new EffectManager();
        waveDirector = new WaveDirector();
        sessionStats = new SessionStats();
        renderer = new Renderer(roomWidth, roomHeight);
        inputHandler = new InputHandler(this);
        gameLoop = new GameLoop(this);

        addKeyListener(inputHandler);
        addMouseMotionListener(inputHandler);
        addMouseListener(inputHandler);
        setFocusable(true);

        resetRunState();
        gameState = GameState.MENU;
    }

    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
        audioManager.close();
        closeAllRoomWindows();
    }

    public void startGame(Gun selectedGun) {
        resetRunState();
        player.setGun(selectedGun);
        sessionStats.updateLevel(player.getLevelingSystem().getLevel());
        waveDirector.startRun();
        gameState = GameState.PLAYING;
        effectManager.emitWaveStart(player.getX(), player.getY(), random);
        audioManager.playWaveStart();
        requestMainFocus();
    }

    public void tick() {
        if (gameState == GameState.PLAYING) {
            tickPlaying();
        }
    }

    public void tickPlaying() {
        sessionStats.tick();
        effectManager.tick();
        tickFloatingTexts();
        tickLaserLinks();

        if (hitCooldownTicksRemaining > 0) {
            hitCooldownTicksRemaining--;
        }
        if (playerGraceTicksRemaining > 0) {
            playerGraceTicksRemaining--;
        }
        if (player.tickDashCooldown()) {
            audioManager.playDashReady();
        }

        Gun.TickResult gunTickResult = player.tickGun();
        playPendingReloadStartAudio();
        handleReloadAudio(gunTickResult);

        if (player.getLevelingSystem().hasLeveledUp()) {
            enterLevelUp();
            return;
        }

        handleDashInput();
        player.move(upPressed, downPressed, leftPressed, rightPressed);
        if (updateRoomFromPlayerPosition()) {
            playerGraceTicksRemaining = ROOM_TRANSITION_GRACE_TICKS;
        }

        updatePlayerAim();

        if (shooting) {
            shoot();
        }
        playPendingReloadStartAudio();

        updatePellets();
        updateXps();
        updateEnemies();
        updateEnemyBullets();
        resolveProjectileCollisions();
        resolvePelletEnemyCollisions();
        handleEnemyBulletPlayerCollisions();
        handleLaserLinkPlayerCollisions();
        handleMutantAuraPlayerCollisions();
        handlePlayerEnemyCollisions();
        sessionStats.updateLevel(player.getLevelingSystem().getLevel());

        if (player.getHealth() <= 0) {
            gameState = GameState.GAME_OVER;
            refreshRoomWindows();
            updateMainWindowLocation();
            return;
        }

        advanceWave();
        refreshRoomWindows();
        updateMainWindowLocation();

        if (gameState == GameState.PLAYING && player.getLevelingSystem().hasLeveledUp()) {
            enterLevelUp();
        }
    }

    public void render() {
        BufferStrategy bufferStrategy = getBufferStrategy();
        if (bufferStrategy == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics2D g2d = (Graphics2D) bufferStrategy.getDrawGraphics();
        try {
            switch (gameState) {
                case MENU:
                    menuScreen.render(g2d, getWidth(), getHeight());
                    break;
                case PLAYING:
                    renderPlaying(g2d);
                    break;
                case PAUSED:
                    renderPlaying(g2d);
                    renderPaused(g2d);
                    break;
                case GAME_OVER:
                    renderPlaying(g2d);
                    gameOverScreen.render(g2d, getWidth(), getHeight(), sessionStats);
                    break;
                case LEVEL_UP:
                    renderPlaying(g2d);
                    upgradeScreen.render(g2d, getWidth(), getHeight());
                    break;
                default:
                    break;
            }
        } finally {
            g2d.dispose();
            bufferStrategy.show();
        }
    }

    public void renderPlaying(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        prepareRoomBuckets();
        g2d.translate(-cameraX, -cameraY);
        renderer.render(g2d, player, roomBuckets, roomCol, roomRow);
        renderFloatingTexts(g2d);
        g2d.translate(cameraX, cameraY);

        renderer.drawWaveNumber(g2d, getWidth(), waveDirector.getWaveNumber(), getKillsToNextLevelEstimate());
        drawInterWaveStatus(g2d);
        effectManager.renderOverlay(g2d, getWidth(), getHeight());
        renderRoomWindows();
    }

    private void enterLevelUp() {
        gameState = GameState.LEVEL_UP;
        audioManager.playLevelUp();
        effectManager.emitLevelUp(player.getX(), player.getY(), random);
        upgradeScreen.presentUpgrades(this);
    }

    private void updatePlayerAim() {
        Enemy nearestEnemy = getNearestEnemy();
        if (nearestEnemy != null) {
            player.updateGunAngle(nearestEnemy.getX(), nearestEnemy.getY());
        } else {
            player.spinGun();
        }
        player.smoothGunTransition();
    }

    private void handleDashInput() {
        if (!dashRequested) {
            return;
        }

        dashRequested = false;
        if (!player.tryDash(upPressed, downPressed, leftPressed, rightPressed)) {
            return;
        }

        effectManager.emitDash(player.getX(), player.getY(), player.getLastMoveDirectionX(),
                player.getLastMoveDirectionY(), random);
        audioManager.playDash();
    }

    private void shoot() {
        if (player.getGun() == null) {
            return;
        }

        Enemy nearestEnemy = getNearestEnemy();
        double shotAngle = player.getGunAngle();
        if (nearestEnemy != null) {
            shotAngle = player.aimGunDirectlyAt(nearestEnemy.getX(), nearestEnemy.getY());
        }

        List<Pellet> newPellets = player.shootFromCenter(shotAngle);
        if (newPellets.isEmpty()) {
            return;
        }

        pellets.addAll(newPellets);
        double shotOriginX = player.getX();
        double shotOriginY = player.getY();
        if (player.getGun() instanceof SMG) {
            effectManager.emitMuzzleFlash(shotOriginX, shotOriginY, shotAngle,
                    getShotEffectColor(player.getGun()), random, 2, 3, 1.8, 3);
        } else if (player.getGun() instanceof Shotgun) {
            effectManager.emitMuzzleFlash(shotOriginX, shotOriginY, shotAngle,
                    getShotEffectColor(player.getGun()), random, 7, 9, 4.8, 6);
        } else if (player.getGun() instanceof Sniper) {
            effectManager.emitMuzzleFlash(shotOriginX, shotOriginY, shotAngle,
                    getShotEffectColor(player.getGun()), random, 4, 5, 3.2, 4);
        } else {
            effectManager.emitMuzzleFlash(shotOriginX, shotOriginY, shotAngle,
                    getShotEffectColor(player.getGun()), random, 4, 6, 3.0, 4);
        }
        audioManager.playShot(player.getGun());
    }

    private void handleReloadAudio(Gun.TickResult gunTickResult) {
        if (player.getGun() == null) {
            return;
        }
        if (gunTickResult.isReloadCompleted()) {
            audioManager.playReloadComplete(player.getGun());
            return;
        }
        if (gunTickResult.isAmmoInserted()) {
            audioManager.playReloadProgress(player.getGun());
        }
    }

    private void playPendingReloadStartAudio() {
        if (player.getGun() == null) {
            return;
        }
        if (player.consumeReloadStarted()) {
            audioManager.playReloadProgress(player.getGun());
        }
    }

    private void updatePellets() {
        Iterator<Pellet> pelletIterator = pellets.iterator();
        while (pelletIterator.hasNext()) {
            Pellet pellet = pelletIterator.next();
            pellet.move();
            if (isPelletOutOfRange(pellet)) {
                pelletIterator.remove();
            }
        }
    }

    private void updateXps() {
        Iterator<XP> xpIterator = xps.iterator();
        double pickupRadiusSquared = player.getPickupRadius() * player.getPickupRadius();
        double attractionRadiusSquared = player.getAttractionRadius() * player.getAttractionRadius();

        while (xpIterator.hasNext()) {
            XP xp = xpIterator.next();
            double dx = player.getX() - xp.getX();
            double dy = player.getY() - xp.getY();
            double distanceSquared = (dx * dx) + (dy * dy);

            if (distanceSquared <= pickupRadiusSquared) {
                player.getLevelingSystem().addXp(xp.getAmount());
                effectManager.emitXpPickup(xp.getX(), xp.getY(), random);
                audioManager.playXpPickup();
                xpIterator.remove();
                continue;
            }

            if (distanceSquared <= attractionRadiusSquared) {
                xp.moveTo(player.getX(), player.getY());
            }
            xp.move();
        }
    }

    private void updateEnemies() {
        double playerX = player.getX();
        double playerY = player.getY();
        WarperEnemy pendingWarper = null;

        for (Enemy enemy : enemies) {
            enemy.move();
        }

        reconcileLaserTwinLinks(playerX, playerY);

        for (Enemy enemy : enemies) {
            if (enemy instanceof LaserTwinEnemy && ((LaserTwinEnemy) enemy).isLinked()) {
                continue;
            }

            if (enemy instanceof WarperEnemy) {
                enemyBullets.addAll(((WarperEnemy) enemy).updateBehavior(playerX, playerY, enemies.size()));
            } else {
                enemyBullets.addAll(enemy.updateBehavior(playerX, playerY));
            }
            if (pendingWarper == null && enemy instanceof WarperEnemy && ((WarperEnemy) enemy).hasPendingWarp()) {
                pendingWarper = (WarperEnemy) enemy;
            }
        }

        if (pendingWarper != null && pendingWarper.consumePendingWarp()) {
            swapPlayerWithWarper(pendingWarper);
            playerX = player.getX();
            playerY = player.getY();
        }

        updateLinkedLaserTwinBehavior(playerX, playerY);
    }

    private void updateEnemyBullets() {
        Iterator<EnemyBullet> bulletIterator = enemyBullets.iterator();
        while (bulletIterator.hasNext()) {
            EnemyBullet enemyBullet = bulletIterator.next();
            enemyBullet.move();
            if (isEnemyBulletOutOfRange(enemyBullet)) {
                bulletIterator.remove();
            }
        }
    }

    private void resolveProjectileCollisions() {
        if (pellets.isEmpty() || enemyBullets.isEmpty()) {
            return;
        }

        boolean[] cancelledEnemyBullets = new boolean[enemyBullets.size()];
        boolean cancelledAny = false;
        Iterator<Pellet> pelletIterator = pellets.iterator();
        while (pelletIterator.hasNext()) {
            Pellet pellet = pelletIterator.next();
            for (int i = 0; i < enemyBullets.size(); i++) {
                if (cancelledEnemyBullets[i]) {
                    continue;
                }

                EnemyBullet enemyBullet = enemyBullets.get(i);
                if (!checkCollision(pellet, enemyBullet)) {
                    continue;
                }

                double clashX = (pellet.getX() + enemyBullet.getX()) / 2.0;
                double clashY = (pellet.getY() + enemyBullet.getY()) / 2.0;
                effectManager.emitProjectileClash(clashX, clashY, random);
                pelletIterator.remove();
                cancelledEnemyBullets[i] = true;
                cancelledAny = true;
                break;
            }
        }

        if (!cancelledAny) {
            return;
        }

        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            if (cancelledEnemyBullets[i]) {
                enemyBullets.remove(i);
            }
        }
    }

    private void resolvePelletEnemyCollisions() {
        Iterator<Pellet> pelletIterator = pellets.iterator();
        while (pelletIterator.hasNext()) {
            Pellet pellet = pelletIterator.next();
            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                if (!checkCollision(pellet, enemy)) {
                    continue;
                }

                enemy.setHealth(enemy.getHealth() - (int) pellet.getDamage());
                applyKnockback(enemy, pellet);
                effectManager.emitHitSparks(pellet.getX(), pellet.getY(), enemy.getColor(), random);
                pelletIterator.remove();

                if (enemy.getHealth() <= 0) {
                    if (enemy instanceof LaserTwinEnemy) {
                        breakLaserTwinLink((LaserTwinEnemy) enemy);
                    }
                    enemies.remove(i);
                    xps.add(new XP(enemy.getX(), enemy.getY(), enemy.getXpDropAmount()));
                    effectManager.emitEnemyDeath(enemy.getX(), enemy.getY(), enemy.getColor(), random);
                    audioManager.playEnemyDefeated();
                    sessionStats.recordEnemyDefeated();
                } else {
                    audioManager.playEnemyHit();
                }
                break;
            }
        }
    }

    private void handleEnemyBulletPlayerCollisions() {
        if (hitCooldownTicksRemaining > 0 || playerGraceTicksRemaining > 0) {
            return;
        }

        Iterator<EnemyBullet> bulletIterator = enemyBullets.iterator();
        while (bulletIterator.hasNext()) {
            EnemyBullet enemyBullet = bulletIterator.next();
            if (!checkCollision(player, enemyBullet)) {
                continue;
            }

            player.takeDamage(enemyBullet.getDamage());
            hitCooldownTicksRemaining = HIT_COOLDOWN_TICKS;
            effectManager.triggerDamageFlash();
            effectManager.emitHitSparks(enemyBullet.getX(), enemyBullet.getY(), Color.ORANGE, random);
            audioManager.playPlayerHit();
            bulletIterator.remove();
            break;
        }
    }

    private void handleLaserLinkPlayerCollisions() {
        if (hitCooldownTicksRemaining > 0 || playerGraceTicksRemaining > 0) {
            return;
        }

        for (LaserLink laserLink : laserLinks) {
            if (!laserLink.isActive() || !checkCollision(player, laserLink)) {
                continue;
            }

            player.takeDamage(LASER_LINK_DAMAGE);
            hitCooldownTicksRemaining = HIT_COOLDOWN_TICKS;
            effectManager.triggerDamageFlash();
            effectManager.emitHitSparks(player.getX(), player.getY(), Color.RED, random);
            audioManager.playPlayerHit();
            break;
        }
    }

    private void handleMutantAuraPlayerCollisions() {
        if (playerGraceTicksRemaining > 0) {
            mutantExposureTicks = 0;
            return;
        }

        MutantEnemy radiatingMutant = getRadiatingMutant();
        if (radiatingMutant == null) {
            mutantExposureTicks = Math.max(0, mutantExposureTicks - MUTANT_EXPOSURE_RECOVERY_PER_TICK);
            return;
        }

        audioManager.playMutantRadiationAura(getMutantAuraIntensity(radiatingMutant));
        int maxExposureTicks = radiatingMutant.getExposureThresholdTicks() + HIT_COOLDOWN_TICKS;
        mutantExposureTicks = Math.min(maxExposureTicks, mutantExposureTicks + 1);
        if (hitCooldownTicksRemaining > 0 || mutantExposureTicks < radiatingMutant.getExposureThresholdTicks()) {
            return;
        }

        player.takeDamage(radiatingMutant.getRadiationDamage());
        mutantExposureTicks = radiatingMutant.getExposureResetTicks();
        hitCooldownTicksRemaining = HIT_COOLDOWN_TICKS;
        effectManager.triggerDamageFlash();
        effectManager.emitHitSparks(player.getX(), player.getY(), radiatingMutant.getAuraColor(), random);
        audioManager.playMutantRadiation();
        audioManager.playPlayerHit();
    }

    private void handlePlayerEnemyCollisions() {
        if (hitCooldownTicksRemaining > 0 || playerGraceTicksRemaining > 0) {
            return;
        }

        for (Enemy enemy : enemies) {
            if (!checkCollision(player, enemy)) {
                continue;
            }

            player.takeDamage(enemy.getDamage());
            hitCooldownTicksRemaining = HIT_COOLDOWN_TICKS;
            applyKnockbackToPlayer(enemy);
            effectManager.triggerDamageFlash();
            effectManager.emitHitSparks(player.getX(), player.getY(), Color.RED, random);
            audioManager.playPlayerHit();
            break;
        }
    }

    private MutantEnemy getRadiatingMutant() {
        MutantEnemy radiatingMutant = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Enemy enemy : enemies) {
            if (!(enemy instanceof MutantEnemy)) {
                continue;
            }

            MutantEnemy mutant = (MutantEnemy) enemy;
            if (!checkCollision(player, mutant)) {
                continue;
            }

            double dx = player.getX() - mutant.getX();
            double dy = player.getY() - mutant.getY();
            double distanceSquared = (dx * dx) + (dy * dy);
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                radiatingMutant = mutant;
            }
        }

        return radiatingMutant;
    }

    private double getMutantAuraIntensity(MutantEnemy mutant) {
        double dx = player.getX() - mutant.getX();
        double dy = player.getY() - mutant.getY();
        double distance = Math.sqrt((dx * dx) + (dy * dy));
        double safeDistance = Math.max(1.0, mutant.getAuraRadius());
        double normalized = 1.0 - Math.min(1.0, distance / safeDistance);
        return Math.max(0.0, normalized);
    }

    private void advanceWave() {
        WaveDirector.WaveTickResult tickResult = waveDirector.tick(enemies.size(), player.getX(), player.getY(), random);
        if (tickResult.isWaveCleared()) {
            grantWaveRecovery();
        }
        if (tickResult.isWaveStarted()) {
            effectManager.emitWaveStart(player.getX(), player.getY(), random);
            audioManager.playWaveStart();
        }
        if (!tickResult.getSpawnedEnemies().isEmpty()) {
            enemies.addAll(tickResult.getSpawnedEnemies());
            reconcileLaserTwinLinks(player.getX(), player.getY());
        }
    }

    private void refreshRoomWindows() {
        Set<String> activeRooms = new HashSet<>();
        boolean createdWindow = false;
        collectActiveEnemyRooms(activeRooms);
        collectActiveLaserLinkRooms(activeRooms);
        collectActiveRooms(activeRooms, xps);
        collectActiveRooms(activeRooms, effectManager.getParticles());

        for (String key : activeRooms) {
            if (!roomWindows.containsKey(key)) {
                int separator = key.indexOf(',');
                int col = Integer.parseInt(key.substring(0, separator));
                int row = Integer.parseInt(key.substring(separator + 1));
                roomWindows.put(key, new RoomWindow(col, row, WINDOW_WIDTH, WINDOW_HEIGHT));
                createdWindow = true;
            }
        }

        Iterator<Map.Entry<String, RoomWindow>> iterator = roomWindows.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, RoomWindow> entry = iterator.next();
            if (!activeRooms.contains(entry.getKey())) {
                entry.getValue().close();
                iterator.remove();
                continue;
            }

            RoomWindow roomWindow = entry.getValue();
            int roomWindowX = initialWindowLocation.x + (roomWindow.getRoomCol() * WINDOW_WIDTH);
            int roomWindowY = initialWindowLocation.y + (roomWindow.getRoomRow() * WINDOW_HEIGHT);
            roomWindow.setLocation(roomWindowX, roomWindowY);
        }

        if (createdWindow) {
            requestMainFocus();
        }
    }

    private void collectActiveRooms(Set<String> activeRooms, List<?> entities) {
        for (Object entity : entities) {
            double entityX;
            double entityY;

            if (entity instanceof Pellet) {
                Pellet pellet = (Pellet) entity;
                entityX = pellet.getX();
                entityY = pellet.getY();
            } else if (entity instanceof XP) {
                XP xp = (XP) entity;
                entityX = xp.getX();
                entityY = xp.getY();
            } else if (entity instanceof Particle) {
                Particle particle = (Particle) entity;
                if (!particle.activatesRoomWindow()) {
                    continue;
                }
                entityX = particle.getX();
                entityY = particle.getY();
            } else {
                continue;
            }

            int entityCol = toRoomCol(entityX);
            int entityRow = toRoomRow(entityY);
            if (entityCol != roomCol || entityRow != roomRow) {
                activeRooms.add(RoomRenderBucket.key(entityCol, entityRow));
            }
        }
    }

    private void collectActiveEnemyRooms(Set<String> activeRooms) {
        for (Enemy enemy : enemies) {
            collectCoveredRooms(activeRooms, enemy.getX(), enemy.getY(), enemy.getRenderRadius());
        }
    }

    private void collectCoveredRooms(Set<String> activeRooms, double centerX, double centerY, double radius) {
        int minCol = toRoomCol(centerX - radius);
        int maxCol = toRoomCol(centerX + radius);
        int minRow = toRoomRow(centerY - radius);
        int maxRow = toRoomRow(centerY + radius);

        for (int col = minCol; col <= maxCol; col++) {
            for (int row = minRow; row <= maxRow; row++) {
                if (col == roomCol && row == roomRow) {
                    continue;
                }
                activeRooms.add(RoomRenderBucket.key(col, row));
            }
        }
    }

    private void updateMainWindowLocation() {
        Point shakeOffset = effectManager.getShakeOffset(random);
        int newWindowX = initialWindowLocation.x + (roomCol * WINDOW_WIDTH) + shakeOffset.x;
        int newWindowY = initialWindowLocation.y + (roomRow * WINDOW_HEIGHT) + shakeOffset.y;
        window.setLocation(newWindowX, newWindowY);
    }

    private void prepareRoomBuckets() {
        roomBuckets.clear();

        for (LaserLink laserLink : laserLinks) {
            addLaserLinkToBuckets(laserLink);
        }
        for (Enemy enemy : enemies) {
            addEnemyToBuckets(enemy);
        }
        for (EnemyBullet enemyBullet : enemyBullets) {
            getOrCreateBucket(enemyBullet.getX(), enemyBullet.getY()).getEnemyBullets().add(enemyBullet);
        }
        for (Pellet pellet : pellets) {
            getOrCreateBucket(pellet.getX(), pellet.getY()).getPellets().add(pellet);
        }
        for (XP xp : xps) {
            getOrCreateBucket(xp.getX(), xp.getY()).getXps().add(xp);
        }
        for (Particle particle : effectManager.getParticles()) {
            getOrCreateBucket(particle.getX(), particle.getY()).getParticles().add(particle);
        }
    }

    private RoomRenderBucket getOrCreateBucket(double x, double y) {
        return getOrCreateBucket(toRoomCol(x), toRoomRow(y));
    }

    private RoomRenderBucket getOrCreateBucket(int col, int row) {
        String key = RoomRenderBucket.key(col, row);
        RoomRenderBucket bucket = roomBuckets.get(key);
        if (bucket == null) {
            bucket = new RoomRenderBucket(col, row);
            roomBuckets.put(key, bucket);
        }
        return bucket;
    }

    private void addEnemyToBuckets(Enemy enemy) {
        double renderRadius = enemy.getRenderRadius();
        int minCol = toRoomCol(enemy.getX() - renderRadius);
        int maxCol = toRoomCol(enemy.getX() + renderRadius);
        int minRow = toRoomRow(enemy.getY() - renderRadius);
        int maxRow = toRoomRow(enemy.getY() + renderRadius);

        for (int col = minCol; col <= maxCol; col++) {
            for (int row = minRow; row <= maxRow; row++) {
                getOrCreateBucket(col, row).getEnemies().add(enemy);
            }
        }
    }

    private void renderRoomWindows() {
        for (RoomWindow roomWindow : roomWindows.values()) {
            String key = RoomRenderBucket.key(roomWindow.getRoomCol(), roomWindow.getRoomRow());
            roomWindow.render(roomBuckets.get(key));
        }
    }

    private void drawInterWaveStatus(Graphics2D g2d) {
        if (waveDirector.getInterWaveTicksRemaining() <= 0 || gameState == GameState.GAME_OVER) {
            return;
        }

        int seconds = Math.max(1, (int) Math.ceil(waveDirector.getInterWaveTicksRemaining() / 60.0));
        String text = "Next Wave In: " + seconds;
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.setFont(INTER_WAVE_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, 72);
    }

    private void tickFloatingTexts() {
        Iterator<FloatingText> iterator = floatingTexts.iterator();
        while (iterator.hasNext()) {
            FloatingText floatingText = iterator.next();
            floatingText.tick();
            if (!floatingText.isAlive()) {
                iterator.remove();
            }
        }
    }

    private void renderFloatingTexts(Graphics2D g2d) {
        for (FloatingText floatingText : floatingTexts) {
            floatingText.render(g2d);
        }
    }

    private void renderPaused(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
        g2d.setFont(PAUSE_FONT);
        String text = "PAUSED";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
    }

    private boolean updateRoomFromPlayerPosition() {
        double playerX = player.getX();
        double playerY = player.getY();
        double roomLeft = roomCol * roomWidth;
        double roomRight = (roomCol + 1) * roomWidth;
        double roomTop = roomRow * roomHeight;
        double roomBottom = (roomRow + 1) * roomHeight;

        boolean changed = false;
        if (playerX < roomLeft) {
            roomCol--;
            player.setX(((roomCol + 1) * roomWidth) - 1);
            changed = true;
        } else if (playerX >= roomRight) {
            roomCol++;
            player.setX((roomCol * roomWidth) + 1);
            changed = true;
        }

        if (playerY < roomTop) {
            roomRow--;
            player.setY(((roomRow + 1) * roomHeight) - 1);
            changed = true;
        } else if (playerY >= roomBottom) {
            roomRow++;
            player.setY((roomRow * roomHeight) + 1);
            changed = true;
        }

        if (changed) {
            cameraX = roomCol * roomWidth;
            cameraY = roomRow * roomHeight;
        }

        return changed;
    }

    private void swapPlayerWithWarper(WarperEnemy warper) {
        double playerX = player.getX();
        double playerY = player.getY();
        double warperX = warper.getX();
        double warperY = warper.getY();

        player.teleportTo(warperX, warperY);
        warper.teleportTo(playerX, playerY);
        syncRoomToPlayerPosition();
        effectManager.emitTeleportWave(player.getX(), player.getY(), random);
    }

    private void syncRoomToPlayerPosition() {
        roomCol = toRoomCol(player.getX());
        roomRow = toRoomRow(player.getY());
        cameraX = roomCol * roomWidth;
        cameraY = roomRow * roomHeight;
    }

    private Enemy getNearestEnemy() {
        Enemy nearestEnemy = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Enemy enemy : enemies) {
            double dx = player.getX() - enemy.getX();
            double dy = player.getY() - enemy.getY();
            double distanceSquared = (dx * dx) + (dy * dy);
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestEnemy = enemy;
            }
        }

        return nearestEnemy;
    }

    private void tickLaserLinks() {
        Iterator<LaserLink> iterator = laserLinks.iterator();
        while (iterator.hasNext()) {
            LaserLink laserLink = iterator.next();
            laserLink.tick();
            if (laserLink.isExpired()) {
                iterator.remove();
            }
        }
    }

    private void reconcileLaserTwinLinks(double playerX, double playerY) {
        List<LaserTwinEnemy> liveLaserTwins = collectLiveLaserTwins();
        Set<LaserTwinEnemy> liveTwinSet = new HashSet<>(liveLaserTwins);
        Set<LaserTwinEnemy> pairedLaserTwins = new HashSet<>();

        for (LaserLink laserLink : laserLinks) {
            if (!laserLink.isActive()) {
                continue;
            }

            LaserTwinEnemy firstTwin = laserLink.getFirstTwin();
            LaserTwinEnemy secondTwin = laserLink.getSecondTwin();
            if (firstTwin == null || secondTwin == null || !liveTwinSet.contains(firstTwin)
                    || !liveTwinSet.contains(secondTwin)) {
                breakLaserLink(laserLink);
                continue;
            }

            pairedLaserTwins.add(firstTwin);
            pairedLaserTwins.add(secondTwin);
        }

        List<LaserTwinEnemy> availableLaserTwins = new ArrayList<>();
        for (LaserTwinEnemy laserTwin : liveLaserTwins) {
            if (!pairedLaserTwins.contains(laserTwin)) {
                laserTwin.clearLinkedState();
                availableLaserTwins.add(laserTwin);
            }
        }

        while (availableLaserTwins.size() >= 2) {
            int firstIndex = 0;
            int secondIndex = 1;
            double nearestDistanceSquared = Double.MAX_VALUE;

            for (int i = 0; i < availableLaserTwins.size() - 1; i++) {
                LaserTwinEnemy firstTwin = availableLaserTwins.get(i);
                for (int j = i + 1; j < availableLaserTwins.size(); j++) {
                    LaserTwinEnemy secondTwin = availableLaserTwins.get(j);
                    double dx = firstTwin.getX() - secondTwin.getX();
                    double dy = firstTwin.getY() - secondTwin.getY();
                    double distanceSquared = (dx * dx) + (dy * dy);
                    if (distanceSquared < nearestDistanceSquared) {
                        nearestDistanceSquared = distanceSquared;
                        firstIndex = i;
                        secondIndex = j;
                    }
                }
            }

            LaserTwinEnemy firstTwin = availableLaserTwins.get(firstIndex);
            LaserTwinEnemy secondTwin = availableLaserTwins.get(secondIndex);
            laserLinks.add(createLaserLink(firstTwin, secondTwin, playerX, playerY));

            availableLaserTwins.remove(secondIndex);
            availableLaserTwins.remove(firstIndex);
        }
    }

    private List<LaserTwinEnemy> collectLiveLaserTwins() {
        List<LaserTwinEnemy> liveLaserTwins = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy instanceof LaserTwinEnemy) {
                liveLaserTwins.add((LaserTwinEnemy) enemy);
            }
        }
        return liveLaserTwins;
    }

    private LaserLink createLaserLink(LaserTwinEnemy firstTwin, LaserTwinEnemy secondTwin, double playerX,
            double playerY) {
        int firstFormationSide = determineFormationSide(firstTwin, secondTwin, playerX, playerY);
        firstTwin.setLinkedState(firstFormationSide);
        secondTwin.setLinkedState(-firstFormationSide);
        audioManager.playLaserLink();
        return new LaserLink(firstTwin, secondTwin);
    }

    private int determineFormationSide(LaserTwinEnemy firstTwin, LaserTwinEnemy secondTwin, double playerX,
            double playerY) {
        double midpointX = (firstTwin.getX() + secondTwin.getX()) / 2.0;
        double midpointY = (firstTwin.getY() + secondTwin.getY()) / 2.0;
        double directionX = playerX - midpointX;
        double directionY = playerY - midpointY;
        double directionLengthSquared = (directionX * directionX) + (directionY * directionY);

        if (directionLengthSquared <= ZERO_LENGTH_EPSILON) {
            directionX = secondTwin.getX() - firstTwin.getX();
            directionY = secondTwin.getY() - firstTwin.getY();
            directionLengthSquared = (directionX * directionX) + (directionY * directionY);
        }

        if (directionLengthSquared <= ZERO_LENGTH_EPSILON) {
            return firstTwin.getX() <= secondTwin.getX() ? -1 : 1;
        }

        double directionLength = Math.sqrt(directionLengthSquared);
        double perpendicularX = -(directionY / directionLength);
        double perpendicularY = directionX / directionLength;
        double offsetX = firstTwin.getX() - midpointX;
        double offsetY = firstTwin.getY() - midpointY;
        double sideProjection = (offsetX * perpendicularX) + (offsetY * perpendicularY);

        if (Math.abs(sideProjection) <= ZERO_LENGTH_EPSILON) {
            return firstTwin.getX() <= secondTwin.getX() ? -1 : 1;
        }

        return sideProjection < 0 ? -1 : 1;
    }

    private void updateLinkedLaserTwinBehavior(double playerX, double playerY) {
        for (LaserLink laserLink : laserLinks) {
            if (!laserLink.isActive()) {
                continue;
            }

            LaserTwinEnemy firstTwin = laserLink.getFirstTwin();
            LaserTwinEnemy secondTwin = laserLink.getSecondTwin();
            if (firstTwin == null || secondTwin == null) {
                continue;
            }

            double midpointX = (firstTwin.getX() + secondTwin.getX()) / 2.0;
            double midpointY = (firstTwin.getY() + secondTwin.getY()) / 2.0;
            double directionX = playerX - midpointX;
            double directionY = playerY - midpointY;
            double directionLengthSquared = (directionX * directionX) + (directionY * directionY);

            if (directionLengthSquared <= ZERO_LENGTH_EPSILON) {
                directionX = 1.0;
                directionY = 0.0;
                directionLengthSquared = 1.0;
            }

            double directionLength = Math.sqrt(directionLengthSquared);
            directionX /= directionLength;
            directionY /= directionLength;

            double perpendicularX = -directionY;
            double perpendicularY = directionX;
            double targetMidpointX = midpointX + (directionX * LASER_PAIR_ADVANCE_DISTANCE);
            double targetMidpointY = midpointY + (directionY * LASER_PAIR_ADVANCE_DISTANCE);

            firstTwin.updateLinkedBehavior(
                    targetMidpointX + (perpendicularX * firstTwin.getFormationSide() * LASER_PAIR_HALF_SPAN),
                    targetMidpointY + (perpendicularY * firstTwin.getFormationSide() * LASER_PAIR_HALF_SPAN));
            secondTwin.updateLinkedBehavior(
                    targetMidpointX + (perpendicularX * secondTwin.getFormationSide() * LASER_PAIR_HALF_SPAN),
                    targetMidpointY + (perpendicularY * secondTwin.getFormationSide() * LASER_PAIR_HALF_SPAN));
        }
    }

    private void breakLaserTwinLink(LaserTwinEnemy laserTwin) {
        for (LaserLink laserLink : laserLinks) {
            if (laserLink.isActive() && laserLink.references(laserTwin)) {
                breakLaserLink(laserLink);
                return;
            }
        }
    }

    private void breakLaserLink(LaserLink laserLink) {
        LaserTwinEnemy firstTwin = laserLink.getFirstTwin();
        LaserTwinEnemy secondTwin = laserLink.getSecondTwin();
        laserLink.breakLink();
        if (firstTwin != null) {
            firstTwin.clearLinkedState();
        }
        if (secondTwin != null) {
            secondTwin.clearLinkedState();
        }
    }

    private void collectActiveLaserLinkRooms(Set<String> activeRooms) {
        for (LaserLink laserLink : laserLinks) {
            int minCol = toRoomCol(Math.min(laserLink.getStartX(), laserLink.getEndX()) - LASER_LINK_ROOM_PADDING);
            int maxCol = toRoomCol(Math.max(laserLink.getStartX(), laserLink.getEndX()) + LASER_LINK_ROOM_PADDING);
            int minRow = toRoomRow(Math.min(laserLink.getStartY(), laserLink.getEndY()) - LASER_LINK_ROOM_PADDING);
            int maxRow = toRoomRow(Math.max(laserLink.getStartY(), laserLink.getEndY()) + LASER_LINK_ROOM_PADDING);

            for (int col = minCol; col <= maxCol; col++) {
                for (int row = minRow; row <= maxRow; row++) {
                    if (col == roomCol && row == roomRow) {
                        continue;
                    }
                    activeRooms.add(RoomRenderBucket.key(col, row));
                }
            }
        }
    }

    private void addLaserLinkToBuckets(LaserLink laserLink) {
        int minCol = toRoomCol(Math.min(laserLink.getStartX(), laserLink.getEndX()) - LASER_LINK_ROOM_PADDING);
        int maxCol = toRoomCol(Math.max(laserLink.getStartX(), laserLink.getEndX()) + LASER_LINK_ROOM_PADDING);
        int minRow = toRoomRow(Math.min(laserLink.getStartY(), laserLink.getEndY()) - LASER_LINK_ROOM_PADDING);
        int maxRow = toRoomRow(Math.max(laserLink.getStartY(), laserLink.getEndY()) + LASER_LINK_ROOM_PADDING);

        for (int col = minCol; col <= maxCol; col++) {
            for (int row = minRow; row <= maxRow; row++) {
                getOrCreateBucket(col, row).getLaserLinks().add(laserLink);
            }
        }
    }

    private boolean checkCollision(Pellet pellet, Enemy enemy) {
        double radius = (enemy.getSize() / 2.0) + (pellet.getSize() / 2.0);
        double startX = pellet.getPreviousX();
        double startY = pellet.getPreviousY();
        double endX = pellet.getX();
        double endY = pellet.getY();
        double segmentX = endX - startX;
        double segmentY = endY - startY;
        double segmentLengthSquared = (segmentX * segmentX) + (segmentY * segmentY);

        if (segmentLengthSquared <= ZERO_LENGTH_EPSILON) {
            double dx = endX - enemy.getX();
            double dy = endY - enemy.getY();
            return (dx * dx) + (dy * dy) <= radius * radius;
        }

        double projection = ((enemy.getX() - startX) * segmentX) + ((enemy.getY() - startY) * segmentY);
        double t = Math.max(0.0, Math.min(1.0, projection / segmentLengthSquared));
        double closestX = startX + (segmentX * t);
        double closestY = startY + (segmentY * t);
        double dx = closestX - enemy.getX();
        double dy = closestY - enemy.getY();
        return (dx * dx) + (dy * dy) <= radius * radius;
    }

    private boolean checkCollision(Player currentPlayer, Enemy enemy) {
        double dx = currentPlayer.getX() - enemy.getX();
        double dy = currentPlayer.getY() - enemy.getY();
        double radius = PLAYER_COLLISION_RADIUS + (enemy.getSize() / 2.0);
        return (dx * dx) + (dy * dy) <= radius * radius;
    }

    private boolean checkCollision(Player currentPlayer, MutantEnemy mutant) {
        return checkCollision(currentPlayer, mutant.getX(), mutant.getY(), mutant.getAuraRadius());
    }

    private boolean checkCollision(Player currentPlayer, EnemyBullet enemyBullet) {
        double radius = PLAYER_COLLISION_RADIUS + (enemyBullet.getSize() / 2.0);
        double startX = enemyBullet.getPreviousX();
        double startY = enemyBullet.getPreviousY();
        double endX = enemyBullet.getX();
        double endY = enemyBullet.getY();
        double segmentX = endX - startX;
        double segmentY = endY - startY;
        double segmentLengthSquared = (segmentX * segmentX) + (segmentY * segmentY);

        if (segmentLengthSquared <= ZERO_LENGTH_EPSILON) {
            double dx = endX - currentPlayer.getX();
            double dy = endY - currentPlayer.getY();
            return (dx * dx) + (dy * dy) <= radius * radius;
        }

        double projection = ((currentPlayer.getX() - startX) * segmentX) + ((currentPlayer.getY() - startY) * segmentY);
        double t = Math.max(0.0, Math.min(1.0, projection / segmentLengthSquared));
        double closestX = startX + (segmentX * t);
        double closestY = startY + (segmentY * t);
        double dx = closestX - currentPlayer.getX();
        double dy = closestY - currentPlayer.getY();
        return (dx * dx) + (dy * dy) <= radius * radius;
    }

    private boolean checkCollision(Player currentPlayer, double centerX, double centerY, double collisionRadius) {
        double combinedRadius = PLAYER_COLLISION_RADIUS + collisionRadius;
        double combinedRadiusSquared = combinedRadius * combinedRadius;
        return pointToSegmentDistanceSquared(centerX, centerY, currentPlayer.getPreviousX(), currentPlayer.getPreviousY(),
                currentPlayer.getX(), currentPlayer.getY()) <= combinedRadiusSquared;
    }

    private boolean checkCollision(Player currentPlayer, LaserLink laserLink) {
        double playerStartX = currentPlayer.getPreviousX();
        double playerStartY = currentPlayer.getPreviousY();
        double playerEndX = currentPlayer.getX();
        double playerEndY = currentPlayer.getY();
        double collisionRadiusSquared = LASER_LINK_COLLISION_RADIUS * LASER_LINK_COLLISION_RADIUS;

        if (getSegmentDistanceSquared(playerStartX, playerStartY, playerEndX, playerEndY, laserLink.getStartX(),
                laserLink.getStartY(), laserLink.getEndX(), laserLink.getEndY()) <= collisionRadiusSquared) {
            return true;
        }

        if (getSegmentDistanceSquared(playerStartX, playerStartY, playerEndX, playerEndY, laserLink.getPreviousStartX(),
                laserLink.getPreviousStartY(), laserLink.getPreviousEndX(), laserLink.getPreviousEndY()) <= collisionRadiusSquared) {
            return true;
        }

        double midpointStartX = (laserLink.getPreviousStartX() + laserLink.getStartX()) / 2.0;
        double midpointStartY = (laserLink.getPreviousStartY() + laserLink.getStartY()) / 2.0;
        double midpointEndX = (laserLink.getPreviousEndX() + laserLink.getEndX()) / 2.0;
        double midpointEndY = (laserLink.getPreviousEndY() + laserLink.getEndY()) / 2.0;
        if (getSegmentDistanceSquared(playerStartX, playerStartY, playerEndX, playerEndY, midpointStartX,
                midpointStartY, midpointEndX, midpointEndY) <= collisionRadiusSquared) {
            return true;
        }

        if (getSegmentDistanceSquared(playerStartX, playerStartY, playerEndX, playerEndY, laserLink.getPreviousStartX(),
                laserLink.getPreviousStartY(), laserLink.getStartX(), laserLink.getStartY()) <= collisionRadiusSquared) {
            return true;
        }

        return getSegmentDistanceSquared(playerStartX, playerStartY, playerEndX, playerEndY, laserLink.getPreviousEndX(),
                laserLink.getPreviousEndY(), laserLink.getEndX(), laserLink.getEndY()) <= collisionRadiusSquared;
    }

    private boolean checkCollision(Pellet pellet, EnemyBullet enemyBullet) {
        double radius = (pellet.getSize() / 2.0) + (enemyBullet.getSize() / 2.0);
        double relativeStartX = pellet.getPreviousX() - enemyBullet.getPreviousX();
        double relativeStartY = pellet.getPreviousY() - enemyBullet.getPreviousY();
        double relativeVelocityX = (pellet.getX() - pellet.getPreviousX())
                - (enemyBullet.getX() - enemyBullet.getPreviousX());
        double relativeVelocityY = (pellet.getY() - pellet.getPreviousY())
                - (enemyBullet.getY() - enemyBullet.getPreviousY());
        double relativeSpeedSquared = (relativeVelocityX * relativeVelocityX)
                + (relativeVelocityY * relativeVelocityY);

        double t = 0.0;
        if (relativeSpeedSquared > ZERO_LENGTH_EPSILON) {
            double projection = -((relativeStartX * relativeVelocityX) + (relativeStartY * relativeVelocityY));
            t = Math.max(0.0, Math.min(1.0, projection / relativeSpeedSquared));
        }

        double closestX = relativeStartX + (relativeVelocityX * t);
        double closestY = relativeStartY + (relativeVelocityY * t);
        return (closestX * closestX) + (closestY * closestY) <= radius * radius;
    }

    private double getSegmentDistanceSquared(double firstStartX, double firstStartY, double firstEndX, double firstEndY,
            double secondStartX, double secondStartY, double secondEndX, double secondEndY) {
        if (segmentsIntersect(firstStartX, firstStartY, firstEndX, firstEndY, secondStartX, secondStartY, secondEndX,
                secondEndY)) {
            return 0.0;
        }

        double firstPointDistance = pointToSegmentDistanceSquared(firstStartX, firstStartY, secondStartX, secondStartY,
                secondEndX, secondEndY);
        double secondPointDistance = pointToSegmentDistanceSquared(firstEndX, firstEndY, secondStartX, secondStartY,
                secondEndX, secondEndY);
        double thirdPointDistance = pointToSegmentDistanceSquared(secondStartX, secondStartY, firstStartX, firstStartY,
                firstEndX, firstEndY);
        double fourthPointDistance = pointToSegmentDistanceSquared(secondEndX, secondEndY, firstStartX, firstStartY,
                firstEndX, firstEndY);

        return Math.min(Math.min(firstPointDistance, secondPointDistance),
                Math.min(thirdPointDistance, fourthPointDistance));
    }

    private double pointToSegmentDistanceSquared(double pointX, double pointY, double segmentStartX, double segmentStartY,
            double segmentEndX, double segmentEndY) {
        double segmentX = segmentEndX - segmentStartX;
        double segmentY = segmentEndY - segmentStartY;
        double segmentLengthSquared = (segmentX * segmentX) + (segmentY * segmentY);

        if (segmentLengthSquared <= ZERO_LENGTH_EPSILON) {
            double dx = pointX - segmentStartX;
            double dy = pointY - segmentStartY;
            return (dx * dx) + (dy * dy);
        }

        double projection = ((pointX - segmentStartX) * segmentX) + ((pointY - segmentStartY) * segmentY);
        double t = Math.max(0.0, Math.min(1.0, projection / segmentLengthSquared));
        double closestX = segmentStartX + (segmentX * t);
        double closestY = segmentStartY + (segmentY * t);
        double dx = pointX - closestX;
        double dy = pointY - closestY;
        return (dx * dx) + (dy * dy);
    }

    private boolean segmentsIntersect(double firstStartX, double firstStartY, double firstEndX, double firstEndY,
            double secondStartX, double secondStartY, double secondEndX, double secondEndY) {
        double firstOrientation = crossProduct(firstStartX, firstStartY, firstEndX, firstEndY, secondStartX,
                secondStartY);
        double secondOrientation = crossProduct(firstStartX, firstStartY, firstEndX, firstEndY, secondEndX,
                secondEndY);
        double thirdOrientation = crossProduct(secondStartX, secondStartY, secondEndX, secondEndY, firstStartX,
                firstStartY);
        double fourthOrientation = crossProduct(secondStartX, secondStartY, secondEndX, secondEndY, firstEndX,
                firstEndY);

        boolean firstCrosses = (firstOrientation > ZERO_LENGTH_EPSILON && secondOrientation < -ZERO_LENGTH_EPSILON)
                || (firstOrientation < -ZERO_LENGTH_EPSILON && secondOrientation > ZERO_LENGTH_EPSILON);
        boolean secondCrosses = (thirdOrientation > ZERO_LENGTH_EPSILON && fourthOrientation < -ZERO_LENGTH_EPSILON)
                || (thirdOrientation < -ZERO_LENGTH_EPSILON && fourthOrientation > ZERO_LENGTH_EPSILON);
        if (firstCrosses && secondCrosses) {
            return true;
        }

        return isPointOnSegment(secondStartX, secondStartY, firstStartX, firstStartY, firstEndX, firstEndY,
                firstOrientation)
                || isPointOnSegment(secondEndX, secondEndY, firstStartX, firstStartY, firstEndX, firstEndY,
                        secondOrientation)
                || isPointOnSegment(firstStartX, firstStartY, secondStartX, secondStartY, secondEndX, secondEndY,
                        thirdOrientation)
                || isPointOnSegment(firstEndX, firstEndY, secondStartX, secondStartY, secondEndX, secondEndY,
                        fourthOrientation);
    }

    private double crossProduct(double startX, double startY, double endX, double endY, double pointX, double pointY) {
        return ((endX - startX) * (pointY - startY)) - ((endY - startY) * (pointX - startX));
    }

    private boolean isPointOnSegment(double pointX, double pointY, double startX, double startY, double endX,
            double endY, double orientation) {
        if (Math.abs(orientation) > ZERO_LENGTH_EPSILON) {
            return false;
        }

        return pointX >= Math.min(startX, endX) - ZERO_LENGTH_EPSILON
                && pointX <= Math.max(startX, endX) + ZERO_LENGTH_EPSILON
                && pointY >= Math.min(startY, endY) - ZERO_LENGTH_EPSILON
                && pointY <= Math.max(startY, endY) + ZERO_LENGTH_EPSILON;
    }

    private boolean isPelletOutOfRange(Pellet pellet) {
        double minX = (roomCol - 2) * roomWidth;
        double maxX = (roomCol + 3) * roomWidth;
        double minY = (roomRow - 2) * roomHeight;
        double maxY = (roomRow + 3) * roomHeight;
        return pellet.getX() < minX || pellet.getX() > maxX || pellet.getY() < minY || pellet.getY() > maxY;
    }

    private boolean isEnemyBulletOutOfRange(EnemyBullet enemyBullet) {
        double minX = (roomCol - 2) * roomWidth;
        double maxX = (roomCol + 3) * roomWidth;
        double minY = (roomRow - 2) * roomHeight;
        double maxY = (roomRow + 3) * roomHeight;
        return enemyBullet.getX() < minX || enemyBullet.getX() > maxX || enemyBullet.getY() < minY
                || enemyBullet.getY() > maxY;
    }

    private void applyKnockback(Enemy enemy, Pellet pellet) {
        double dx = enemy.getX() - pellet.getX();
        double dy = enemy.getY() - pellet.getY();
        double distanceSquared = (dx * dx) + (dy * dy);
        if (distanceSquared <= ZERO_LENGTH_EPSILON) {
            return;
        }

        double distance = Math.sqrt(distanceSquared);
        enemy.applyKnockback((dx / distance) * pellet.getKnockback(), (dy / distance) * pellet.getKnockback());
    }

    private void applyKnockbackToPlayer(Enemy enemy) {
        double knockbackStrength = 30.0;
        double dx = player.getX() - enemy.getX();
        double dy = player.getY() - enemy.getY();
        double distanceSquared = (dx * dx) + (dy * dy);
        if (distanceSquared <= ZERO_LENGTH_EPSILON) {
            return;
        }

        double distance = Math.sqrt(distanceSquared);
        player.applyKnockback((dx / distance) * knockbackStrength, (dy / distance) * knockbackStrength);
    }

    private Color getShotEffectColor(Gun gun) {
        if (gun instanceof Shotgun) {
            return new Color(255, 150, 70);
        }
        if (gun instanceof Sniper) {
            return new Color(160, 240, 255);
        }
        if (gun instanceof SMG) {
            return new Color(255, 220, 96);
        }
        return Color.ORANGE;
    }

    private int getKillsToNextLevelEstimate() {
        int remainingXp = player.getLevelingSystem().getXpRemainingToNextLevel();
        if (remainingXp <= 0) {
            return 0;
        }

        int xpPerKillEstimate = getXpPerKillEstimate();
        return Math.max(1, (int) Math.ceil(remainingXp / (double) xpPerKillEstimate));
    }

    private int getXpPerKillEstimate() {
        if (!enemies.isEmpty()) {
            int totalXp = 0;
            for (Enemy enemy : enemies) {
                totalXp += enemy.getXpDropAmount();
            }
            return Math.max(1, Math.round(totalXp / (float) enemies.size()));
        }

        return waveDirector.getEstimatedXpPerKill();
    }

    private void grantWaveRecovery() {
        int clearedWave = waveDirector.getWaveNumber();
        if (clearedWave <= 0 || clearedWave % HEART_RECOVERY_WAVE_INTERVAL != 0) {
            return;
        }

        int healedAmount = player.heal(1);
        if (healedAmount <= 0) {
            return;
        }

        floatingTexts.add(new FloatingText(player.getX(), player.getY() - 26, "+" + healedAmount, Color.RED,
                HEAL_TEXT_LIFETIME_TICKS));
        audioManager.playHeal();
    }

    private int toRoomCol(double x) {
        return (int) Math.floor(x / roomWidth);
    }

    private int toRoomRow(double y) {
        return (int) Math.floor(y / roomHeight);
    }

    private void resetRunState() {
        pellets.clear();
        enemyBullets.clear();
        enemies.clear();
        floatingTexts.clear();
        laserLinks.clear();
        xps.clear();
        effectManager.reset();
        waveDirector.reset();
        sessionStats.reset();
        upgradeScreen.clear();
        closeAllRoomWindows();
        roomBuckets.clear();
        roomCol = 0;
        roomRow = 0;
        cameraX = 0;
        cameraY = 0;
        player = new Player(100, 100);
        hitCooldownTicksRemaining = 0;
        playerGraceTicksRemaining = GAME_START_GRACE_TICKS;
        mutantExposureTicks = 0;
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        dashPressed = false;
        dashRequested = false;
        shooting = false;
        window.setLocation(initialWindowLocation.x, initialWindowLocation.y);
    }

    private void closeAllRoomWindows() {
        for (RoomWindow roomWindow : roomWindows.values()) {
            roomWindow.close();
        }
        roomWindows.clear();
    }

    public int getRoomCol() {
        return roomCol;
    }

    public int getRoomRow() {
        return roomRow;
    }

    public double getCameraX() {
        return cameraX;
    }

    public double getCameraY() {
        return cameraY;
    }

    public void setUpPressed(boolean upPressed) {
        this.upPressed = upPressed;
    }

    public void setDownPressed(boolean downPressed) {
        this.downPressed = downPressed;
    }

    public void setLeftPressed(boolean leftPressed) {
        this.leftPressed = leftPressed;
    }

    public void setRightPressed(boolean rightPressed) {
        this.rightPressed = rightPressed;
    }

    public void setDashPressed(boolean dashPressed) {
        if (dashPressed && !this.dashPressed && gameState == GameState.PLAYING) {
            dashRequested = true;
        }
        this.dashPressed = dashPressed;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public void reloadGun() {
        if (gameState != GameState.PLAYING) {
            return;
        }
        if (player.reloadGun()) {
            player.consumeReloadStarted();
            audioManager.playReloadProgress(player.getGun());
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public MenuScreen getMenuScreen() {
        return menuScreen;
    }

    public GameOverScreen getGameOverScreen() {
        return gameOverScreen;
    }

    public UpgradeScreen getUpgradeScreen() {
        return upgradeScreen;
    }

    public Player getPlayer() {
        return player;
    }

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            requestMainFocus();
        } else if (gameState == GameState.GAME_OVER) {
            reset();
        }
    }

    public void resumeWave() {
        player.getLevelingSystem().resetLevelUpFlag();
        gameState = GameState.PLAYING;
        requestMainFocus();
    }

    public void reset() {
        resetRunState();
        gameState = GameState.MENU;
        requestMainFocus();
    }

    public int getWaveNumber() {
        return waveDirector.getWaveNumber();
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public void playUiClick() {
        audioManager.playUiClick();
    }

    public void requestMainFocus() {
        window.requestCanvasFocus(this);
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
