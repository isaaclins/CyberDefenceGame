package src.main;

import src.entity.Player;
import src.entity.Enemy;
import src.entity.Pellet;
import src.utils.GameWindow;
import src.utils.Renderer;
import src.utils.InputHandler;
import src.utils.GameLoop;
import src.utils.RoomWindow;

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
import java.util.stream.Collectors;

public class Game extends Canvas {
    private GameWindow window;
    private GameLoop gameLoop;
    private Renderer renderer;
    private InputHandler inputHandler;

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
    private long lastShotTime = 0;
    private final long shotCooldown = 25; // milliseconds
    private final double knockbackstrength = 0.2;
    private double mouseX, mouseY;

    private List<Pellet> pellets;
    private List<Enemy> enemies;
    private Timer enemySpawnTimer;
    private Random random;

    // Map for additional room windows (rooms other than player's)
    private Map<String, RoomWindow> roomWindows;
    private int tickCounter = 0; // Used for cleaning up windows

    public Game() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        WINDOW_WIDTH = screenSize.width / 3;
        WINDOW_HEIGHT = screenSize.height / 3;
        roomWidth = WINDOW_WIDTH;
        roomHeight = WINDOW_HEIGHT;

        Dimension size = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setPreferredSize(size);

        window = new GameWindow("Dungeon Crawler", WINDOW_WIDTH, WINDOW_HEIGHT, this);
        // Main window always on top (if desired):
        window.setAlwaysOnTop(true);
        initialWindowLocation = window.getLocation();

        cameraX = roomCol * roomWidth;
        cameraY = roomRow * roomHeight;

        player = new Player(cameraX + 100, cameraY + 100);

        pellets = new ArrayList<>();
        enemies = new ArrayList<>();
        random = new Random();

        renderer = new Renderer(this, roomWidth, roomHeight);
        inputHandler = new InputHandler(this);
        gameLoop = new GameLoop(this);

        addKeyListener(inputHandler);
        addMouseMotionListener(inputHandler);
        setFocusable(true);

        roomWindows = new HashMap<>();

        startEnemySpawnTimer();
    }

    private void startEnemySpawnTimer() {
        enemySpawnTimer = new Timer();
        enemySpawnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (enemies.size() < 10) {
                    spawnEnemyNearPlayer();
                }
            }
        }, 0, (2 + random.nextInt(4)) * 1000); // every 2-5 seconds
    }

    private void spawnEnemyNearPlayer() {
        double playerX = player.getX();
        double playerY = player.getY();
        double spawnX = playerX + (random.nextDouble() * 200 - 100);
        double spawnY = playerY + (random.nextDouble() * 200 - 100);
        enemies.add(new Enemy(spawnX, spawnY, 100, Color.GREEN));
    }

    public void start() {
        gameLoop.start();
    }

    public void stop() {
        gameLoop.stop();
    }

    public void tick() {
        tickCounter++;

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
            int newWindowX = initialWindowLocation.x + roomCol * WINDOW_WIDTH;
            int newWindowY = initialWindowLocation.y + roomRow * WINDOW_HEIGHT;
            window.setLocation(newWindowX, newWindowY);
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
        if (shooting && System.currentTimeMillis() - lastShotTime >= shotCooldown) {
            shoot();
            lastShotTime = System.currentTimeMillis();
        }

        // Move pellets and handle collisions with enemies
        Iterator<Pellet> pelletIterator = pellets.iterator();
        while (pelletIterator.hasNext()) {
            Pellet pellet = pelletIterator.next();
            pellet.move();

            boolean pelletRemoved = false;
            for (Enemy enemy : enemies) {
                if (checkCollision(pellet, enemy)) {
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

        // Move enemies toward the player and update facing angle
        for (Enemy enemy : enemies) {
            enemy.moveToPlayer(playerX, playerY);
            enemy.move();
        }

        // Check collision between player and enemies
        for (Enemy enemy : enemies) {
            if (checkCollision(player, enemy)) {
                applyKnockbackToPlayer(enemy);
            }
        }

        // For each enemy in a room different from the player's, ensure a RoomWindow
        // exists.
        for (Enemy enemy : enemies) {
            int eCol = (int) Math.floor(enemy.getX() / roomWidth);
            int eRow = (int) Math.floor(enemy.getY() / roomHeight);
            if (eCol != roomCol || eRow != roomRow) {
                String key = eCol + "," + eRow;
                if (!roomWindows.containsKey(key)) {
                    RoomWindow rw = new RoomWindow(eCol, eRow, WINDOW_WIDTH, WINDOW_HEIGHT, initialWindowLocation);
                    roomWindows.put(key, rw);
                }
            }
        }
        // Update locations of room windows (in case room indices shift)
        for (RoomWindow rw : roomWindows.values()) {
            rw.updateLocation(initialWindowLocation);
        }

        // Every 60 ticks, clean up room windows with no enemies or pellets.
        if (tickCounter % 60 == 0) {
            List<String> keysToRemove = new ArrayList<>();
            for (String key : roomWindows.keySet()) {
                String[] parts = key.split(",");
                int windowCol = Integer.parseInt(parts[0]);
                int windowRow = Integer.parseInt(parts[1]);

                boolean hasEnemy = enemies.stream().anyMatch(e -> ((int) Math.floor(e.getX() / roomWidth)) == windowCol
                        && ((int) Math.floor(e.getY() / roomHeight)) == windowRow);
                boolean hasPellet = pellets.stream().anyMatch(p -> ((int) Math.floor(p.getX() / roomWidth)) == windowCol
                        && ((int) Math.floor(p.getY() / roomHeight)) == windowRow);

                if (!hasEnemy && !hasPellet) {
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
    }

    private void shoot() {
        Enemy nearestEnemy = getNearestEnemy();
        if (nearestEnemy != null) {
            pellets.add(new Pellet(player.getGunX(), player.getGunY(), nearestEnemy.getX(), nearestEnemy.getY()));
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
        double knockbackX = (enemy.getX() - pellet.getX()) * knockbackstrength;
        double knockbackY = (enemy.getY() - pellet.getY()) * knockbackstrength;
        enemy.applyKnockback(knockbackX, knockbackY);
    }

    private void applyKnockbackToPlayer(Enemy enemy) {
        double knockbackX = (player.getX() - enemy.getX()) * 2.0;
        double knockbackY = (player.getY() - enemy.getY()) * 2.0;
        player.applyKnockback(knockbackX, knockbackY);
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        // Clear and render main (player) window.
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-cameraX, -cameraY);
        renderer.render(player, enemies, pellets);
        g2d.translate(cameraX, cameraY);
        g.dispose();
        bs.show();

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
            rw.render(roomEnemies, roomPellets, null);
        });

        // Request focus to keep input on the main window.
        this.requestFocus();
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

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
