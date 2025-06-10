package src.utils;

import src.entity.Enemy;
import src.entity.Pellet;
import src.entity.Player;
import src.entity.XP;
import src.main.Game;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.List;
import src.entity.Gun;
import src.entity.Particle;

public class Renderer {
    private final Game game;
    private final int roomWidth;
    private final int roomHeight;

    public Renderer(Game game, int roomWidth, int roomHeight) {
        this.game = game;
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
    }

    public void render(Player player, List<Enemy> enemies, List<Pellet> pellets, List<XP> xps,
            List<Particle> particles) {
        BufferStrategy bs = game.getBufferStrategy();
        if (bs == null) {
            game.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, game.getWidth(), game.getHeight());

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-game.getCameraX(), -game.getCameraY());

        // Render the current room and neighbors.
        renderRoom(g2d, game.getRoomCol(), game.getRoomRow());
        renderRoom(g2d, game.getRoomCol() - 1, game.getRoomRow());
        renderRoom(g2d, game.getRoomCol() + 1, game.getRoomRow());
        renderRoom(g2d, game.getRoomCol(), game.getRoomRow() - 1);
        renderRoom(g2d, game.getRoomCol(), game.getRoomRow() + 1);

        // Render player with glow
        Rectangle2D playerShape = new Rectangle.Double(player.getX() - 10, player.getY() - 10, 20, 20);
        GlowRenderer.drawGlow(g2d, playerShape, Color.RED, 10);
        g2d.setColor(Color.RED);
        g2d.fill(playerShape);

        // Draw player level
        String levelStr = "Lvl " + player.getLevelingSystem().getLevel();
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int strWidth = fm.stringWidth(levelStr);
        g.drawString(levelStr, (int) player.getX() - strWidth / 2, (int) player.getY() + fm.getAscent() / 2);

        // Draw XP bar
        double xpPercentage = (double) player.getLevelingSystem().getXp()
                / player.getLevelingSystem().getXpToNextLevel();
        g.setColor(Color.GRAY);
        g.fillRect((int) player.getX() - 15, (int) player.getY() + 15, 30, 5);
        g.setColor(Color.GREEN);
        g.fillRect((int) player.getX() - 15, (int) player.getY() + 15, (int) (30 * xpPercentage), 5);

        // Render gun with glow
        Rectangle2D gunShape = new Rectangle.Double(player.getGunX() - 5, player.getGunY() - 5, 10, 10);
        GlowRenderer.drawGlow(g2d, gunShape, Color.BLUE, 8);
        g2d.setColor(Color.BLUE);
        g2d.fill(gunShape);

        // Render enemies and pellets.
        for (Enemy enemy : enemies) {
            enemy.render(g);
        }
        for (Pellet pellet : pellets) {
            pellet.render(g);
        }
        for (XP xp : xps) {
            xp.render(g);
        }

        for (Particle particle : particles) {
            particle.render(g);
        }

        drawHealth(g, player);
        drawAmmo(g, player);

        g2d.translate(game.getCameraX(), game.getCameraY());

        g.dispose();
        bs.show();
    }

    private void drawHealth(Graphics g, Player player) {
        if (player == null)
            return;
        int health = player.getHealth();
        int maxHealth = player.getMaxHealth();
        int circleSize = 10;
        int spacing = 5;
        int totalWidth = (circleSize + spacing) * maxHealth - spacing;

        double playerScreenX = player.getX();
        double playerScreenY = player.getY();

        int startX = (int) (playerScreenX - totalWidth / 2);
        int startY = (int) (playerScreenY - 30 - circleSize);

        for (int i = 0; i < maxHealth; i++) {
            g.setColor(Color.RED);
            if (i < health) {
                g.fillOval(startX + i * (circleSize + spacing), startY, circleSize, circleSize);
            } else {
                g.drawOval(startX + i * (circleSize + spacing), startY, circleSize, circleSize);
            }
        }
    }

    private void drawAmmo(Graphics g, Player player) {
        if (player == null || player.getGun() == null)
            return;
        Gun gun = player.getGun();
        int currentAmmo = gun.getCurrentAmmo();
        int magazineSize = gun.getMagazineSize();
        if (magazineSize <= 0)
            return;

        double angleStep = 2 * Math.PI / magazineSize;
        int ammoCircleRadius = 4;
        int orbitRadius = 40; // The radius of the circle on which the ammo dots are placed

        double playerScreenX = player.getX();
        double playerScreenY = player.getY();

        for (int i = 0; i < currentAmmo; i++) {
            double angle = i * angleStep - Math.PI / 2; // Start from the top
            int x = (int) (playerScreenX + orbitRadius * Math.cos(angle)) - ammoCircleRadius;
            int y = (int) (playerScreenY + orbitRadius * Math.sin(angle)) - ammoCircleRadius;

            g.setColor(new Color(255, 255, 255, 150)); // Slightly opaque white
            g.fillOval(x, y, ammoCircleRadius * 2, ammoCircleRadius * 2);
        }
    }

    private void renderRoom(Graphics g, int col, int row) {
        int roomX = col * roomWidth;
        int roomY = row * roomHeight;
        g.setColor(new Color(30, 30, 30));
        g.fillRect(roomX, roomY, roomWidth, roomHeight);
        g.setColor(Color.GRAY);
        g.drawRect(roomX, roomY, roomWidth, roomHeight);
    }
}
