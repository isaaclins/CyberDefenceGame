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
import src.entity.Gun;
import src.entity.Particle;
import src.entity.Pellet;
import src.entity.Player;
import src.entity.SMG;
import src.entity.Shotgun;
import src.entity.Sniper;
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
    private final List<Enemy> enemies = new ArrayList<>();
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
    private boolean shooting;
    private int hitCooldownTicksRemaining;
    private int playerGraceTicksRemaining;

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

        if (hitCooldownTicksRemaining > 0) {
            hitCooldownTicksRemaining--;
        }
        if (playerGraceTicksRemaining > 0) {
            playerGraceTicksRemaining--;
        }

        Gun.TickResult gunTickResult = player.tickGun();
        playPendingReloadStartAudio();
        handleReloadAudio(gunTickResult);

        if (player.getLevelingSystem().hasLeveledUp()) {
            enterLevelUp();
            return;
        }

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

    private void shoot() {
        if (player.getGun() == null) {
            return;
        }

        List<Pellet> newPellets = player.shoot();
        if (newPellets.isEmpty()) {
            return;
        }

        pellets.addAll(newPellets);
        if (player.getGun() instanceof SMG) {
            effectManager.emitMuzzleFlash(player.getGunX(), player.getGunY(), player.getGunAngle(),
                    getShotEffectColor(player.getGun()), random, 2, 3, 1.8, 3);
        } else if (player.getGun() instanceof Shotgun) {
            effectManager.emitMuzzleFlash(player.getGunX(), player.getGunY(), player.getGunAngle(),
                    getShotEffectColor(player.getGun()), random, 7, 9, 4.8, 6);
        } else if (player.getGun() instanceof Sniper) {
            effectManager.emitMuzzleFlash(player.getGunX(), player.getGunY(), player.getGunAngle(),
                    getShotEffectColor(player.getGun()), random, 4, 5, 3.2, 4);
        } else {
            effectManager.emitMuzzleFlash(player.getGunX(), player.getGunY(), player.getGunAngle(),
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

            boolean pelletRemoved = false;
            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                if (!checkCollision(pellet, enemy)) {
                    continue;
                }

                enemy.setHealth(enemy.getHealth() - (int) pellet.getDamage());
                applyKnockback(enemy, pellet);
                effectManager.emitHitSparks(pellet.getX(), pellet.getY(), enemy.getColor(), random);
                pelletIterator.remove();
                pelletRemoved = true;

                if (enemy.getHealth() <= 0) {
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

            if (!pelletRemoved && isPelletOutOfRange(pellet)) {
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
        for (Enemy enemy : enemies) {
            enemy.move();
            enemy.moveToPlayer(playerX, playerY);
        }
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

    private void advanceWave() {
        WaveDirector.WaveTickResult tickResult = waveDirector.tick(enemies.size(), player.getX(), player.getY(), random);
        if (tickResult.isWaveStarted()) {
            effectManager.emitWaveStart(player.getX(), player.getY(), random);
            audioManager.playWaveStart();
        }
        if (!tickResult.getSpawnedEnemies().isEmpty()) {
            enemies.addAll(tickResult.getSpawnedEnemies());
        }
    }

    private void refreshRoomWindows() {
        Set<String> activeRooms = new HashSet<>();
        boolean createdWindow = false;
        collectActiveRooms(activeRooms, enemies);
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

            if (entity instanceof Enemy) {
                Enemy enemy = (Enemy) entity;
                entityX = enemy.getX();
                entityY = enemy.getY();
            } else if (entity instanceof Pellet) {
                Pellet pellet = (Pellet) entity;
                entityX = pellet.getX();
                entityY = pellet.getY();
            } else if (entity instanceof XP) {
                XP xp = (XP) entity;
                entityX = xp.getX();
                entityY = xp.getY();
            } else if (entity instanceof Particle) {
                Particle particle = (Particle) entity;
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

    private void updateMainWindowLocation() {
        Point shakeOffset = effectManager.getShakeOffset(random);
        int newWindowX = initialWindowLocation.x + (roomCol * WINDOW_WIDTH) + shakeOffset.x;
        int newWindowY = initialWindowLocation.y + (roomRow * WINDOW_HEIGHT) + shakeOffset.y;
        window.setLocation(newWindowX, newWindowY);
    }

    private void prepareRoomBuckets() {
        roomBuckets.clear();

        for (Enemy enemy : enemies) {
            getOrCreateBucket(enemy.getX(), enemy.getY()).getEnemies().add(enemy);
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
        int col = toRoomCol(x);
        int row = toRoomRow(y);
        String key = RoomRenderBucket.key(col, row);
        RoomRenderBucket bucket = roomBuckets.get(key);
        if (bucket == null) {
            bucket = new RoomRenderBucket(col, row);
            roomBuckets.put(key, bucket);
        }
        return bucket;
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

    private boolean checkCollision(Pellet pellet, Enemy enemy) {
        double dx = pellet.getX() - enemy.getX();
        double dy = pellet.getY() - enemy.getY();
        double radius = (enemy.getSize() / 2.0) + (pellet.getSize() / 2.0);
        return (dx * dx) + (dy * dy) <= radius * radius;
    }

    private boolean checkCollision(Player currentPlayer, Enemy enemy) {
        double dx = currentPlayer.getX() - enemy.getX();
        double dy = currentPlayer.getY() - enemy.getY();
        double radius = PLAYER_COLLISION_RADIUS + (enemy.getSize() / 2.0);
        return (dx * dx) + (dy * dy) <= radius * radius;
    }

    private boolean isPelletOutOfRange(Pellet pellet) {
        double minX = (roomCol - 2) * roomWidth;
        double maxX = (roomCol + 3) * roomWidth;
        double minY = (roomRow - 2) * roomHeight;
        double maxY = (roomRow + 3) * roomHeight;
        return pellet.getX() < minX || pellet.getX() > maxX || pellet.getY() < minY || pellet.getY() > maxY;
    }

    private void applyKnockback(Enemy enemy, Pellet pellet) {
        double dx = enemy.getX() - pellet.getX();
        double dy = enemy.getY() - pellet.getY();
        double distanceSquared = (dx * dx) + (dy * dy);
        if (distanceSquared <= 0.0001) {
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
        if (distanceSquared <= 0.0001) {
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

    private int toRoomCol(double x) {
        return (int) Math.floor(x / roomWidth);
    }

    private int toRoomRow(double y) {
        return (int) Math.floor(y / roomHeight);
    }

    private void resetRunState() {
        pellets.clear();
        enemies.clear();
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
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
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
