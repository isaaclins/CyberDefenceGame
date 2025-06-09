package src.utils;

import src.entity.Enemy;
import src.entity.Pellet;
import src.entity.Player;
import src.entity.XP;
import src.main.Game;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.List;

public class Renderer {
    private final Game game;
    private final int roomWidth;
    private final int roomHeight;

    public Renderer(Game game, int roomWidth, int roomHeight) {
        this.game = game;
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
    }

    public void render(Player player, List<Enemy> enemies, List<Pellet> pellets, List<XP> xps) {
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

        // Render player.
        g.setColor(Color.RED);
        g.fillRect((int) player.getX() - 10, (int) player.getY() - 10, 20, 20);

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

        g.setColor(Color.BLUE);
        g.fillRect((int) player.getGunX() - 5, (int) player.getGunY() - 5, 10, 10);

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

        g2d.translate(game.getCameraX(), game.getCameraY());

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
}
