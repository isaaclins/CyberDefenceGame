package src.utils;

import java.awt.*;
import java.util.List;
import src.entity.Enemy;
import src.entity.Pellet;
import src.entity.Player;

public class WindowRenderer {
    private int roomWidth;
    private int roomHeight;
    private double cameraX;
    private double cameraY;

    public WindowRenderer(Canvas canvas, int roomWidth, int roomHeight, double cameraX, double cameraY) {
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public void render(Graphics g, List<Enemy> enemies, List<Pellet> pellets, Player player) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-cameraX, -cameraY);

        renderRoom(g2d, (int)(cameraX/roomWidth), (int)(cameraY/roomHeight));

        if(player != null) {
            g.setColor(Color.RED);
            g.fillRect((int) player.getX() - 10, (int) player.getY() - 10, 20, 20);
            g.setColor(Color.BLUE);
            g.fillRect((int) player.getGunX() - 5, (int) player.getGunY() - 5, 10, 10);
        }

        for (Enemy enemy : enemies) {
            enemy.render(g);
        }
        for (Pellet pellet : pellets) {
            pellet.render(g);
        }

        g2d.translate(cameraX, cameraY);
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
