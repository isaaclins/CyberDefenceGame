package src.utils;

import src.entity.Enemy;
import src.entity.Pellet;
import src.entity.Player;
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

    public void render(Player player, List<Enemy> enemies, List<Pellet> pellets) {
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

        // Render the current room and its neighbors
        renderRoom(g2d, game.getRoomCol(), game.getRoomRow());
        renderRoom(g2d, game.getRoomCol() - 1, game.getRoomRow());
        renderRoom(g2d, game.getRoomCol() + 1, game.getRoomRow());
        renderRoom(g2d, game.getRoomCol(), game.getRoomRow() - 1);
        renderRoom(g2d, game.getRoomCol(), game.getRoomRow() + 1);

        // Render player
        g.setColor(Color.RED);
        g.fillRect((int) player.getX() - 10, (int) player.getY() - 10, 20, 20);
        g.setColor(Color.BLUE);
        g.fillRect((int) player.getGunX() - 5, (int) player.getGunY() - 5, 10, 10);

        // Render enemies and pellets
        for (Enemy enemy : enemies) {
            enemy.render(g);
        }
        for (Pellet pellet : pellets) {
            pellet.render(g);
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
