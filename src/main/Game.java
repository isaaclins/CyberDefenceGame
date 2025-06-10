package src.main;

import src.entity.Player;
import src.entity.Enemy;
import src.entity.Pellet;
import src.entity.BigEnemy;
import src.entity.NormalEnemy;
import src.entity.SmallEnemy;
import src.entity.XP;
import src.entity.Gun;
import src.entity.Particle;
import src.screens.MenuScreen;
import src.screens.GameOverScreen;
import src.screens.UpgradeScreen;
import src.utils.GameWindow;
import src.utils.Renderer;
import src.utils.InputHandler;
import src.utils.GameLoop;
import src.utils.RoomWindow;
import src.utils.UpgradeManager;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Game extends Canvas {
    private GameWindow window;
    private GameLoop gameLoop;
    private Renderer renderer;
    private InputHandler inputHandler;
    private UpgradeManager upgradeManager;

    private GameState gameState;
    private MenuScreen menuScreen;
    private GameOverScreen gameOverScreen;
    private UpgradeScreen upgradeScreen;

    private final int WINDOW_WIDTH;
    private final int WINDOW_HEIGHT;

    private Point initialWindowLocation;

    private Player player;

    private final int roomWidth, roomHeight;
    private int roomCol = 0, roomRow = 0;

    private double cameraX, cameraY;

    private boolean transitioning = false;
    private final double transitionSpeed = 20.0;
    private double targetCameraX, targetCameraY;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean shooting = false;
    private double mouseX, mouseY;

    private List<Pellet> pellets;
    private List<Enemy> enemies;
    private List<XP> xps;
    private List<Particle> particles;
    private Random random;

    // Map for additional room windows (rooms other than player's)
    private Map<String, RoomWindow> roomWindows;
    private int tickCounter = 0; // Used for cleaning up windows

    private long lastHitTime = 0;
    private final long hitCooldown = 1000; // 1 second in milliseconds
    private double screenShakeMagnitude = 10;
    private int screenShakeDuration = 1;

    private int waveNumber = 0;
    private int enemiesPerWave = 5;
    private int enemiesToSpawnThisWave;
    private long timeBetweenWaves = 5000; // 5 seconds
    private Timer waveTimer;
    private boolean waveSpawningActive = false;
    private boolean isWaitingForNextWave = false;

    public Game() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        WINDOW_WIDTH = screenSize.width / 3;
        WINDOW_HEIGHT = screenSize.height / 3;
        roomWidth = WINDOW_WIDTH;
        roomHeight = WINDOW_HEIGHT;

        Dimension size = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setPreferredSize(size);

        window = new GameWindow("Dungeon Crawler", WINDOW_WIDTH, WINDOW_HEIGHT, this, javax.swing.JFrame.EXIT_ON_CLOSE);
        // Main window always on top (if desired):
        window.setAlwaysOnTop(true);
        initialWindowLocation = window.getLocation();

        gameState = GameState.MENU;
        menuScreen = new MenuScreen();
        gameOverScreen = new GameOverScreen();
        upgradeScreen = new UpgradeScreen();

        cameraX = roomCol * roomWidth;
        cameraY = roomRow * roomHeight;

        player = new Player(cameraX + 100, cameraY + 100);

        pellets = new ArrayList<>();
        enemies = new CopyOnWriteArrayList<>();
        xps = new ArrayList<>();
        particles = new ArrayList<>();
        random = new Random();

        renderer = new Renderer(this, roomWidth, roomHeight);
        inputHandler = new InputHandler(this);
        gameLoop = new GameLoop(this);
        upgradeManager = new UpgradeManager();

        addKeyListener(inputHandler);
        addMouseMotionListener(inputHandler);
        addMouseListener(inputHandler);
        setFocusable(true);

        roomWindows = new HashMap<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook ran.");
            new Exception("Stack trace").printStackTrace();
        }));
    }

    private void startNextWave() {
        isWaitingForNextWave = false;
        waveNumber++;
        enemiesToSpawnThisWave = enemiesPerWave * waveNumber;
        System.out.println("Starting Wave " + waveNumber);
        waveSpawningActive = true;

        if (waveTimer != null) {
            waveTimer.cancel();
        }
        waveTimer = new Timer();
        waveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (gameState != GameState.PLAYING)
                        return;

                    if (enemiesToSpawnThisWave > 0 && enemies.size() < 20) { // Max 20 enemies at a time
                        spawnEnemyNearPlayer();
                        enemiesToSpawnThisWave--;
                    } else if (enemiesToSpawnThisWave <= 0) {
                        waveSpawningActive = false;
                        this.cancel(); // Stop this timer task
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000); // Spawn one enemy per second
    }

    private void spawnEnemyNearPlayer() {
        double playerX = player.getX();
        double playerY = player.getY();

        // Spawn enemies in a ring around the player
        double spawnRadius = 200 + random.nextDouble() * 200; // 200 to 400 pixels away
        double spawnAngle = random.nextDouble() * 2 * Math.PI;

        double spawnX = playerX + spawnRadius * Math.cos(spawnAngle);
        double spawnY = playerY + spawnRadius * Math.sin(spawnAngle);

        int enemyType = random.nextInt(3);
        switch (enemyType) {
            case 0:
                enemies.add(new SmallEnemy(spawnX, spawnY));
                break;
            case 1:
                enemies.add(new NormalEnemy(spawnX, spawnY));
                break;
            case 2:
                enemies.add(new BigEnemy(spawnX, spawnY));
                break;
        }
    }

    public void start() {
        gameLoop.start();
    }

    public void startGame(Gun selectedGun) {
        player.setGun(selectedGun);
        gameState = GameState.PLAYING;
        startNextWave();
    }

    public void stop() {
        gameLoop.stop();
    }

    public void tick() {
        switch (gameState) {
            case MENU:
                // No tick logic for menu yet
                break;
            case PLAYING:
                tickPlaying();
                break;
            case PAUSED:
                // No updates while paused
                break;
            case GAME_OVER:
                // No updates on game over
                break;
            case LEVEL_UP:
                // No updates while waiting for upgrade choice
                break;
        }
    }

    public void tickPlaying() {
        tickCounter++;

        if (player.getLevelingSystem().hasLeveledUp()) {
            gameState = GameState.LEVEL_UP;
            upgradeScreen.presentUpgrades(this);
            if (waveTimer != null) {
                waveTimer.cancel();
            }
            return;
        }

        if (!transitioning) {
            player.move(upPressed, downPressed, leftPressed, rightPressed);
        }

        double playerX = player.getX();
        double playerY = player.getY();

        double roomLeft = roomCol * roomWidth;
        double roomRight = (roomCol + 1) * roomWidth;
        double roomTop = roomRow * roomHeight;
        double roomBottom = (roomRow + 1) * roomHeight;

        boolean changed = false;
        if (playerX < roomLeft) {
            roomCol -= 1;
            player.setX((roomCol + 1) * roomWidth - 1);
            changed = true;
        } else if (playerX >= roomRight) {
            roomCol += 1;
            player.setX(roomCol * roomWidth + 1);
            changed = true;
        }
        if (playerY < roomTop) {
            roomRow -= 1;
            player.setY((roomRow + 1) * roomHeight - 1);
            changed = true;
        } else if (playerY >= roomBottom) {
            roomRow += 1;
            player.setY(roomRow * roomHeight + 1);
            changed = true;
        }

        if (changed) {
            cameraX = roomCol * roomWidth;
            cameraY = roomRow * roomHeight;
        }

        // Update the player's gun angle based on the nearest enemy
        Enemy nearestEnemy = getNearestEnemy();
        if (nearestEnemy != null) {
            player.updateGunAngle(nearestEnemy.getX(), nearestEnemy.getY());
        } else {
            player.spinGun();
        }
        player.smoothGunTransition();

        // Handle shooting
        if (shooting) {
            shoot();
        }

        tickParticles();
        tickScreenShake();

        // Update window position with shake
        double shakeOffsetX = 0;
        double shakeOffsetY = 0;
        if (screenShakeDuration > 0) {
            shakeOffsetX = (random.nextDouble() - 0.5) * screenShakeMagnitude;
            shakeOffsetY = (random.nextDouble() - 0.5) * screenShakeMagnitude;
        }
        int newWindowX = initialWindowLocation.x + roomCol * WINDOW_WIDTH + (int) shakeOffsetX;
        int newWindowY = initialWindowLocation.y + roomRow * WINDOW_HEIGHT + (int) shakeOffsetY;
        window.setLocation(newWindowX, newWindowY);

        // Check for collisions between player and enemies
        Iterator<Enemy> enemyCollisionIterator = enemies.iterator();
        while (enemyCollisionIterator.hasNext()) {
            Enemy enemy = enemyCollisionIterator.next();
            if (checkCollision(player, enemy)) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastHitTime > hitCooldown) {
                    player.takeDamage(enemy.getDamage());
                    lastHitTime = currentTime;
                    applyKnockbackToPlayer(enemy);
                }
            }
        }

        // Move pellets and handle collisions with enemies
        Iterator<Pellet> pelletIterator = pellets.iterator();
        List<Enemy> enemiesToRemove = new ArrayList<>();
        while (pelletIterator.hasNext()) {
            Pellet pellet = pelletIterator.next();
            pellet.move();

            boolean pelletRemoved = false;
            for (Enemy enemy : enemies) {
                if (checkCollision(pellet, enemy)) {
                    enemy.setHealth(enemy.getHealth() - (int) pellet.getDamage());
                    if (enemy.getHealth() <= 0) {
                        xps.add(new XP(enemy.getX(), enemy.getY(), enemy.getXpDropAmount()));
                        // Create particle explosion on death
                        for (int i = 0; i < 30; i++) {
                            double angle = random.nextDouble() * 2 * Math.PI;
                            double speed = 1 + random.nextDouble() * 2;
                            double dx = Math.cos(angle) * speed;
                            double dy = Math.sin(angle) * speed;
                            particles.add(new Particle(enemy.getX(), enemy.getY(), dx, dy, 30 + random.nextInt(30),
                                    enemy.getColor()));
                        }
                        enemiesToRemove.add(enemy);
                    }
                    applyKnockback(enemy, pellet);
                    pelletIterator.remove();
                    pelletRemoved = true;
                    break;
                }
            }
            if (!pelletRemoved) {
                // A liberal bounding box for pellets around the current player's room region.
                // This prevents pellets from being immediately removed in negative-coordinate
                // rooms.
                double minX = (roomCol - 2) * roomWidth;
                double maxX = (roomCol + 3) * roomWidth;
                double minY = (roomRow - 2) * roomHeight;
                double maxY = (roomRow + 3) * roomHeight;

                if (pellet.getX() < minX || pellet.getX() > maxX || pellet.getY() < minY || pellet.getY() > maxY) {
                    pelletIterator.remove();
                }
            }
        }
        enemies.removeAll(enemiesToRemove);

        // Handle XP pickup
        Iterator<XP> xpIterator = xps.iterator();
        while (xpIterator.hasNext()) {
            XP xp = xpIterator.next();
            double distance = Math.hypot(player.getX() - xp.getX(), player.getY() - xp.getY());
            if (distance < player.getPickupRadius()) {
                player.getLevelingSystem().addXp(xp.getAmount());
                xpIterator.remove();
            } else if (distance < player.getAttractionRadius()) {
                xp.moveTo(player.getX(), player.getY());
            }
            xp.move();
        }

        // Move enemies toward the player and update facing angle
        for (Enemy enemy : enemies) {
            enemy.move();
            enemy.moveToPlayer(playerX, playerY);
        }

        // Create and manage windows for other rooms
        List<String> activeRooms = new ArrayList<>();
        for (Enemy e : enemies) {
            int eCol = (int) Math.floor(e.getX() / roomWidth);
            int eRow = (int) Math.floor(e.getY() / roomHeight);
            if (eCol != roomCol || eRow != roomRow) {
                String key = eCol + "," + eRow;
                if (!roomWindows.containsKey(key)) {
                    RoomWindow rw = new RoomWindow(eCol, eRow, WINDOW_WIDTH, WINDOW_HEIGHT);
                    roomWindows.put(key, rw);
                }
                activeRooms.add(key);
            }
        }

        // Update locations of room windows (in case room indices shift)
        for (RoomWindow rw : roomWindows.values()) {
            int roomWindowX = initialWindowLocation.x + (rw.getRoomCol() * WINDOW_WIDTH);
            int roomWindowY = initialWindowLocation.y + (rw.getRoomRow() * WINDOW_HEIGHT);
            rw.setLocation(roomWindowX, roomWindowY);
        }

        // Periodically clean up windows for rooms with no enemies
        if (tickCounter % 60 == 0) { // every second
            List<String> keysToRemove = new ArrayList<>();
            for (String key : roomWindows.keySet()) {
                if (!activeRooms.contains(key)) {
                    keysToRemove.add(key);
                }
            }
            for (String key : keysToRemove) {
                RoomWindow rw = roomWindows.get(key);
                if (rw != null) {
                    rw.close(); // Dispose the JFrame
                }
                roomWindows.remove(key);
            }
        }

        if (player.getHealth() <= 0) {
            gameState = GameState.GAME_OVER;
            if (waveTimer != null) {
                waveTimer.cancel();
            }
        }

        if (!waveSpawningActive && enemies.isEmpty() && enemiesToSpawnThisWave <= 0 && !isWaitingForNextWave) {
            isWaitingForNextWave = true;
            System.out.println("Wave " + waveNumber + " complete!");
            if (waveTimer != null) {
                waveTimer.cancel();
                waveTimer = new Timer();
                waveTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            if (gameState == GameState.PLAYING) {
                                startNextWave();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, timeBetweenWaves);
            }
        }

        this.requestFocus();
    }

    private void tickScreenShake() {
        if (screenShakeDuration > 0) {
            screenShakeDuration--;
            screenShakeMagnitude *= 0.9; // Decay the magnitude
        } else {
            screenShakeMagnitude = 0;
        }
    }

    private void tickParticles() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.tick();
            if (!p.isAlive()) {
                iterator.remove();
            }
        }
    }

    private void shoot() {
        if (player.getGun() != null) {
            List<Pellet> newPellets = player.shoot();
            if (newPellets != null && !newPellets.isEmpty()) {
                this.pellets.addAll(newPellets);
                // Add particle effects
                double gunX = player.getGunX();
                double gunY = player.getGunY();
                double angle = player.getGunAngle();
                double spread = player.getGun().getSpread();
                for (int i = 0; i < 20; i++) {
                    double particleAngle = angle + (random.nextDouble() - 0.5) * spread;
                    double particleSpeed = 2 + random.nextDouble() * 2;
                    double dx = Math.cos(particleAngle) * particleSpeed;
                    double dy = Math.sin(particleAngle) * particleSpeed;
                    particles.add(new Particle(gunX, gunY, dx, dy, 20 + random.nextInt(20), Color.ORANGE));
                }
                // Trigger screen shake
                screenShakeMagnitude = 5;
                screenShakeDuration = 10;
            }
        }
    }

    private Enemy getNearestEnemy() {
        Enemy nearestEnemy = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Enemy enemy : enemies) {
            double distance = Math.hypot(player.getX() - enemy.getX(), player.getY() - enemy.getY());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestEnemy = enemy;
            }
        }
        return nearestEnemy;
    }

    private boolean checkCollision(Pellet pellet, Enemy enemy) {
        double distance = Math.hypot(pellet.getX() - enemy.getX(), pellet.getY() - enemy.getY());
        return distance < 15;
    }

    private boolean checkCollision(Player player, Enemy enemy) {
        double distance = Math.hypot(player.getX() - enemy.getX(), player.getY() - enemy.getY());
        return distance < 20;
    }

    private void applyKnockback(Enemy enemy, Pellet pellet) {
        double angle = Math.atan2(enemy.getY() - pellet.getY(), enemy.getX() - pellet.getX());
        double knockbackX = pellet.getKnockback() * Math.cos(angle);
        double knockbackY = pellet.getKnockback() * Math.sin(angle);
        enemy.applyKnockback(knockbackX, knockbackY);
    }

    private void applyKnockbackToPlayer(Enemy enemy) {
        double knockbackStrength = 30.0; // Adjust strength as needed
        double dx = player.getX() - enemy.getX();
        double dy = player.getY() - enemy.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance != 0) {
            double knockbackX = (dx / distance) * knockbackStrength;
            double knockbackY = (dy / distance) * knockbackStrength;
            player.applyKnockback(knockbackX, knockbackY);
        }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        switch (gameState) {
            case MENU:
                menuScreen.render(g, getWidth(), getHeight());
                break;
            case PLAYING:
                renderPlaying(g);
                break;
            case PAUSED:
                renderPlaying(g); // Render the game state but don't update it
                renderPaused(g);
                break;
            case GAME_OVER:
                renderPlaying(g); // Render the game state but don't update it
                gameOverScreen.render(g, getWidth(), getHeight());
                break;
            case LEVEL_UP:
                renderPlaying(g);
                upgradeScreen.render(g);
                break;
        }

        g.dispose();
        bs.show();

        // Request focus to keep input on the main window.
        this.requestFocus();
    }

    public void renderPlaying(Graphics g) {
        // Clear and render main (player) window.
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-cameraX, -cameraY);
        renderer.render(player, enemies, pellets, xps, particles);
        g2d.translate(cameraX, cameraY);

        // Render each additional room window.
        roomWindows.values().forEach(rw -> {
            List<Enemy> roomEnemies = enemies.stream().filter(e -> {
                int col = (int) Math.floor(e.getX() / roomWidth);
                int row = (int) Math.floor(e.getY() / roomHeight);
                return col == rw.getRoomCol() && row == rw.getRoomRow();
            }).collect(Collectors.toList());
            List<Pellet> roomPellets = pellets.stream().filter(p -> {
                int col = (int) Math.floor(p.getX() / roomWidth);
                int row = (int) Math.floor(p.getY() / roomHeight);
                return col == rw.getRoomCol() && row == rw.getRoomRow();
            }).collect(Collectors.toList());
            List<XP> roomXPs = xps.stream().filter(x -> {
                int col = (int) Math.floor(x.getX() / roomWidth);
                int row = (int) Math.floor(x.getY() / roomHeight);
                return col == rw.getRoomCol() && row == rw.getRoomRow();
            }).collect(Collectors.toList());
            List<Particle> roomParticles = particles.stream().filter(p -> {
                int col = (int) Math.floor(p.getX() / roomWidth);
                int row = (int) Math.floor(p.getY() / roomHeight);
                return col == rw.getRoomCol() && row == rw.getRoomRow();
            }).collect(Collectors.toList());
            rw.render(roomEnemies, roomPellets, roomXPs, roomParticles);
        });

        // Request focus to keep input on the main window.
        this.requestFocus();
    }

    private void renderPaused(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String text = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }

    private void drawHealth(Graphics g) {
        if (player == null)
            return;
        int health = player.getHealth();
        int maxHealth = player.getMaxHealth();
        int circleSize = 10;
        int spacing = 5;
        int totalWidth = (circleSize + spacing) * maxHealth - spacing;
        // Position above the player
        double playerScreenX = player.getX() - cameraX;
        double playerScreenY = player.getY() - cameraY;

        int startX = (int) (playerScreenX - totalWidth / 2);
        int startY = (int) (playerScreenY - 30 - circleSize); // 30 pixels above player

        for (int i = 0; i < maxHealth; i++) {
            g.setColor(Color.RED);
            if (i < health) {
                g.fillOval(startX + i * (circleSize + spacing), startY, circleSize, circleSize);
            } else {
                g.drawOval(startX + i * (circleSize + spacing), startY, circleSize, circleSize);
            }
        }
    }

    private void drawAmmo(Graphics g) {
        if (player == null || player.getGun() == null)
            return;
        Gun gun = player.getGun();
        int currentAmmo = gun.getCurrentAmmo();
        int magazineSize = gun.getMagazineSize();
        if (magazineSize <= 0)
            return;

        double angleStep = 2 * Math.PI / magazineSize;
        int ammoCircleRadius = 4;
        int orbitRadius = 25; // The radius of the circle on which the ammo dots are placed

        double playerScreenX = player.getX() - cameraX;
        double playerScreenY = player.getY() - cameraY;

        for (int i = 0; i < currentAmmo; i++) {
            double angle = i * angleStep - Math.PI / 2; // Start from the top
            int x = (int) (playerScreenX + orbitRadius * Math.cos(angle)) - ammoCircleRadius;
            int y = (int) (playerScreenY + orbitRadius * Math.sin(angle)) - ammoCircleRadius;

            g.setColor(new Color(255, 255, 255, 150)); // Slightly opaque white
            g.fillOval(x, y, ammoCircleRadius * 2, ammoCircleRadius * 2);
        }
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

    public BufferStrategy getBufferStrategy() {
        return super.getBufferStrategy();
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
        } else if (gameState == GameState.GAME_OVER) {
            reset();
        }
    }

    public void resumeWave() {
        player.getLevelingSystem().resetLevelUpFlag();
        if (waveTimer != null) {
            waveTimer.cancel();
        }
        waveTimer = new Timer();
        waveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (gameState != GameState.PLAYING)
                        return;

                    if (enemiesToSpawnThisWave > 0 && enemies.size() < 20) {
                        spawnEnemyNearPlayer();
                        enemiesToSpawnThisWave--;
                    } else if (enemiesToSpawnThisWave <= 0) {
                        waveSpawningActive = false;
                        this.cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    public void reset() {
        // Reset player
        player = new Player(cameraX + 100, cameraY + 100);
        // Clear all entities
        pellets.clear();
        enemies.clear();
        xps.clear();
        particles.clear();
        // Reset game state
        gameState = GameState.MENU;
        waveNumber = 0;
        if (waveTimer != null) {
            waveTimer.cancel();
        }
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
