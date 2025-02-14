package src.main;

import src.entity.GameWindow;
import src.entity.Player;
import src.entity.Enemy;
import src.entity.Pellet;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends Canvas implements Runnable, KeyListener, MouseMotionListener {
    private GameWindow window;
    private boolean running = false;
    private Thread thread;

    private final int WINDOW_WIDTH;
    private final int WINDOW_HEIGHT;

    private Point initialWindowLocation;

    private Player player;

    private Enemy enemy1, enemy2;

    private final int roomWidth, roomHeight;
    private int roomCol = 0, roomRow = 0;

    private double cameraX, cameraY;

    private boolean transitioning = false;
    private final double transitionSpeed = 20.0;
    private double targetCameraX, targetCameraY;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean shooting = false;
    private long lastShotTime = 0;
    private final long shotCooldown = 50; // 500 milliseconds cooldown

    private double mouseX, mouseY;

    private List<Pellet> pellets;

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

        addKeyListener(this);
        addMouseMotionListener(this);
        setFocusable(true);

        cameraX = roomCol * roomWidth;
        cameraY = roomRow * roomHeight;

        player = new Player(cameraX + 100, cameraY + 100);
        enemy1 = new Enemy(cameraX + 200, cameraY + 200, 100, Color.GREEN);
        enemy2 = new Enemy(cameraX + 300, cameraY + 300, 100, Color.ORANGE);

        pellets = new ArrayList<>();
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this, "Game Thread");
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerTick = 1_000_000_000.0 / 60.0;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1) {
                tick();
                delta--;
            }

            render();
        }
    }

    private void tick() {
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
            if (checkCollision(pellet, enemy1)) {
                applyKnockback(enemy1, pellet);
                pelletIterator.remove();
            } else if (checkCollision(pellet, enemy2)) {
                applyKnockback(enemy2, pellet);
                pelletIterator.remove();
            }

            // Remove pellets that are out of bounds
            if (pellet.getX() < 0 || pellet.getX() > roomWidth * 3 || pellet.getY() < 0
                    || pellet.getY() > roomHeight * 3) {
                pelletIterator.remove();
            }
        }

        // Move enemies
        enemy1.move();
        enemy2.move();
    }

    private void shoot() {
        Enemy nearestEnemy = getNearestEnemy();
        if (nearestEnemy != null) {
            pellets.add(new Pellet(player.getGunX(), player.getGunY(), nearestEnemy.getX(), nearestEnemy.getY()));
        }
    }

    private Enemy getNearestEnemy() {
        Enemy[] enemies = { enemy1, enemy2 };
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

    private void applyKnockback(Enemy enemy, Pellet pellet) {
        double knockbackX = (enemy.getX() - pellet.getX()) * 0.1;
        double knockbackY = (enemy.getY() - pellet.getY()) * 0.1;
        enemy.applyKnockback(knockbackX, knockbackY);
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-cameraX, -cameraY);

        renderRoom(g2d, roomCol, roomRow);
        renderRoom(g2d, roomCol - 1, roomRow);
        renderRoom(g2d, roomCol + 1, roomRow);
        renderRoom(g2d, roomCol, roomRow - 1);
        renderRoom(g2d, roomCol, roomRow + 1);

        g.setColor(Color.RED);
        g.fillRect((int) player.getX() - 10, (int) player.getY() - 10, 20, 20);

        g.setColor(Color.BLUE);
        g.fillRect((int) player.getGunX() - 5, (int) player.getGunY() - 5, 10, 10);
        enemy1.render(g);
        enemy2.render(g);

        // Render pellets
        for (Pellet pellet : pellets) {
            pellet.render(g);
        }

        g2d.translate(cameraX, cameraY);

        g.dispose();
        bs.show();
    }

    private void renderRoom(Graphics g, int col, int row) {
        int roomX = col * roomWidth;
        int roomY = row * roomHeight;
        g.setColor(new Color(30, 30, 30));
        g.fillRect(roomX, roomY, roomWidth, roomHeight);
        g.setColor(Color.GRAY);
        g.drawRect(roomX, roomY, roomWidth, roomHeight);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W)
            upPressed = true;
        if (key == KeyEvent.VK_S)
            downPressed = true;
        if (key == KeyEvent.VK_A)
            leftPressed = true;
        if (key == KeyEvent.VK_D)
            rightPressed = true;
        if (key == KeyEvent.VK_SPACE) {
            shooting = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W)
            upPressed = false;
        if (key == KeyEvent.VK_S)
            downPressed = false;
        if (key == KeyEvent.VK_A)
            leftPressed = false;
        if (key == KeyEvent.VK_D)
            rightPressed = false;
        if (key == KeyEvent.VK_SPACE) {
            shooting = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Not used.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Not used.
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
