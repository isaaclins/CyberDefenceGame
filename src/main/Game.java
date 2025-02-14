package src.main;

import src.utils.GameWindow;
import src.entity.Player;
import src.entity.Enemy;
import src.entity.Pellet;
import src.utils.Renderer;
import src.utils.InputHandler;
import src.utils.GameLoop;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
    private final long shotCooldown = 25; // 500 milliseconds cooldown
    private final double knockbackstrength = 0.2;
    private double mouseX, mouseY;

    private List<Pellet> pellets;
    private List<Enemy> enemies;
    private Timer enemySpawnTimer;
    private Random random;

    public Game() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        WINDOW_WIDTH = screenSize.width / 3;
        WINDOW_HEIGHT = screenSize.height / 3;
        roomWidth = WINDOW_WIDTH;
        roomHeight = WINDOW_HEIGHT;

        Dimension size = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setPreferredSize(size);

        window = new GameWindow("Dungeon Crawler", WINDOW_WIDTH, WINDOW_HEIGHT, this);
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
        }, 0, (2 + random.nextInt(4)) * 1000); // 2-5 seconds
    }

    private void spawnEnemyNearPlayer() {
        double playerX = player.getX();
        double playerY = player.getY();
        double spawnX = playerX + (random.nextDouble() * 200 - 100); // Random position within 100 units of the player
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

        // Update gun angle based on the nearest enemy or spin if no enemies are present
        Enemy nearestEnemy = getNearestEnemy();
        if (nearestEnemy != null) {
            player.updateGunAngle(nearestEnemy.getX(), nearestEnemy.getY());
        } else {
            player.spinGun();
        }

        // Smoothly transition the gun angle
        player.smoothGunTransition();

        // Handle shooting
        if (shooting && System.currentTimeMillis() - lastShotTime >= shotCooldown) {
            shoot();
            lastShotTime = System.currentTimeMillis();
        }

        // Move pellets and handle collision with enemies
        Iterator<Pellet> pelletIterator = pellets.iterator();
        while (pelletIterator.hasNext()) {
            Pellet pellet = pelletIterator.next();
            pellet.move();

            // Check collision with enemies
            boolean pelletRemoved = false;
            for (Enemy enemy : enemies) {
                if (checkCollision(pellet, enemy)) {
                    applyKnockback(enemy, pellet);
                    pelletIterator.remove();
                    pelletRemoved = true;
                    break;
                }
            }

            // Remove pellets that are out of bounds
            if (!pelletRemoved && (pellet.getX() < 0 || pellet.getX() > roomWidth * 3 || pellet.getY() < 0
                    || pellet.getY() > roomHeight * 3)) {
                pelletIterator.remove();
            }
        }

        // Move enemies towards the player and update their facing angle
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
        return distance < 15; // Assuming 15 is the collision radius
    }

    private boolean checkCollision(Player player, Enemy enemy) {
        double distance = Math.hypot(player.getX() - enemy.getX(), player.getY() - enemy.getY());
        return distance < 20; // Assuming 20 is the collision radius for player and enemy
    }

    private void applyKnockback(Enemy enemy, Pellet pellet) {
        double knockbackX = (enemy.getX() - pellet.getX()) * knockbackstrength;
        double knockbackY = (enemy.getY() - pellet.getY()) * knockbackstrength;
        enemy.applyKnockback(knockbackX, knockbackY);
    }

    private void applyKnockbackToPlayer(Enemy enemy) {
        double knockbackX = (player.getX() - enemy.getX()) * 2.0; // Strong knockback
        double knockbackY = (player.getY() - enemy.getY()) * 2.0; // Strong knockback
        player.applyKnockback(knockbackX, knockbackY);
    }

    public void render() {
        renderer.render(player, enemies, pellets);
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

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
