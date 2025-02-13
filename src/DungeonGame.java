package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

public class DungeonGame extends Canvas implements Runnable, KeyListener {
    private JFrame frame;
    private boolean running = false;
    private Thread thread;

    // Window size as 1/3 of the screen (change as needed)
    private final int WINDOW_WIDTH;
    private final int WINDOW_HEIGHT;

    // Save the initial window location so we can compute the new location when moving
    private Point initialWindowLocation;

    // Player properties (world coordinates now)
    private double playerX, playerY;
    private double velocityX = 0, velocityY = 0;
    private final double acceleration = 0.5;
    private final double friction = 0.90;

    // Room size (set to the window dimensions)
    private final int roomWidth, roomHeight;

    // Current room coordinates in a grid (for example, (0,0), (1,0), etc.)
    private int roomCol = 0, roomRow = 0;

    // Camera offset for drawing the world (top-left of current view in world coordinates)
    // For consistency, cameraX and cameraY will always be roomCol * roomWidth and roomRow * roomHeight.
    private double cameraX, cameraY;

    // Transition variables
    private boolean transitioning = false;
    private final double transitionSpeed = 20.0; // Pixels per tick
    private double targetCameraX, targetCameraY;

    // Direction flags
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    public DungeonGame() {
        // Get screen dimensions and set window size (e.g., 1/3 of the screen)
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        WINDOW_WIDTH = screenSize.width / 3;
        WINDOW_HEIGHT = screenSize.height / 3;
        roomWidth = WINDOW_WIDTH;
        roomHeight = WINDOW_HEIGHT;

        Dimension size = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setPreferredSize(size);

        frame = new JFrame("Dungeon Crawler");
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the window initially on the screen.
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Save the initial window location
        initialWindowLocation = frame.getLocation();

        addKeyListener(this);
        setFocusable(true);

        // Initialize camera offset to the top-left of the current room
        cameraX = roomCol * roomWidth;
        cameraY = roomRow * roomHeight;
        // Place the player somewhere in the first room (world coordinates)
        playerX = cameraX + 100;
        playerY = cameraY + 100;
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
        // If not in a transition, update player movement based on key presses
        if (!transitioning) {
            if (upPressed) velocityY -= acceleration;
            if (downPressed) velocityY += acceleration;
            if (leftPressed) velocityX -= acceleration;
            if (rightPressed) velocityX += acceleration;
        }

        // Update player's world position
        playerX += velocityX;
        playerY += velocityY;

        // Apply friction to slow down the player over time
        velocityX *= friction;
        velocityY *= friction;

        // Compute current room boundaries in world coordinates
        double roomLeft = roomCol * roomWidth;
        double roomRight = (roomCol + 1) * roomWidth;
        double roomTop = roomRow * roomHeight;
        double roomBottom = (roomRow + 1) * roomHeight;

        boolean changed = false;
        if (playerX < roomLeft) {
            // Transition left
            roomCol -= 1;
            // Wrap player to right side of new room
            playerX = (roomCol + 1) * roomWidth - 1;
            changed = true;
        } else if (playerX >= roomRight) {
            // Transition right
            roomCol += 1;
            // Wrap player to left side of new room
            playerX = roomCol * roomWidth + 1;
            changed = true;
        }
        if (playerY < roomTop) {
            // Transition upward
            roomRow -= 1;
            // Wrap player to bottom side of new room
            playerY = (roomRow + 1) * roomHeight - 1;
            changed = true;
        } else if (playerY >= roomBottom) {
            // Transition downward
            roomRow += 1;
            // Wrap player to top side of new room
            playerY = roomRow * roomHeight + 1;
            changed = true;
        }

        // If the room changed, update the camera and window position instantly.
        if (changed) {
            // Instant transition: immediately set camera to the new room's top-left
            cameraX = roomCol * roomWidth;
            cameraY = roomRow * roomHeight;
            // Move the current window instantly to reflect the new room's location on the screen.
            int newWindowX = initialWindowLocation.x + roomCol * WINDOW_WIDTH;
            int newWindowY = initialWindowLocation.y + roomRow * WINDOW_HEIGHT;
            frame.setLocation(newWindowX, newWindowY);
        }
    }


    // Initiates the camera transition by setting the target camera position.
    private void startTransition() {
        transitioning = true;
        targetCameraX = roomCol * roomWidth;
        targetCameraY = roomRow * roomHeight;
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        // Clear the screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Translate graphics context by negative camera offset to use world coordinates
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-cameraX, -cameraY);

        // Render the current room (and adjacent rooms for smoother visuals during transition)
        renderRoom(g2d, roomCol, roomRow);
        renderRoom(g2d, roomCol - 1, roomRow);
        renderRoom(g2d, roomCol + 1, roomRow);
        renderRoom(g2d, roomCol, roomRow - 1);
        renderRoom(g2d, roomCol, roomRow + 1);

        // Render the player at its world coordinate
        g.setColor(Color.RED);
        // Draw the player (adjust size and offset as needed)
        g.fillRect((int) playerX - 10, (int) playerY - 10, 20, 20);

        // Reset the translation
        g2d.translate(cameraX, cameraY);

        g.dispose();
        bs.show();
    }

    // Draws a room's background and border based on its grid position.
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
        if (key == KeyEvent.VK_UP) upPressed = true;
        if (key == KeyEvent.VK_DOWN) downPressed = true;
        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP) upPressed = false;
        if (key == KeyEvent.VK_DOWN) downPressed = false;
        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    public static void main(String[] args) {
        DungeonGame game = new DungeonGame();
        game.start();
    }
}
