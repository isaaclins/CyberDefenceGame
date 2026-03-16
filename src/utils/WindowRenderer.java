package src.utils;

import java.awt.Color;
import java.awt.Graphics2D;

import src.entity.Enemy;
import src.entity.EnemyBullet;
import src.entity.LaserLink;
import src.entity.Particle;
import src.entity.Pellet;
import src.entity.XP;

public class WindowRenderer {
    private static final Color ROOM_COLOR = new Color(30, 30, 30);

    private final int roomWidth;
    private final int roomHeight;
    private final double cameraX;
    private final double cameraY;

    public WindowRenderer(int roomWidth, int roomHeight, double cameraX, double cameraY) {
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public void render(Graphics2D g2d, RoomRenderBucket bucket) {
        g2d.translate(-cameraX, -cameraY);
        renderRoom(g2d, (int) (cameraX / roomWidth), (int) (cameraY / roomHeight));

        if (bucket != null) {
            for (LaserLink laserLink : bucket.getLaserLinks()) {
                laserLink.render(g2d);
            }
            for (Enemy enemy : bucket.getEnemies()) {
                enemy.render(g2d);
            }
            for (EnemyBullet enemyBullet : bucket.getEnemyBullets()) {
                enemyBullet.render(g2d);
            }
            for (Pellet pellet : bucket.getPellets()) {
                pellet.render(g2d);
            }
            for (XP xp : bucket.getXps()) {
                xp.render(g2d);
            }
            for (Particle particle : bucket.getParticles()) {
                particle.render(g2d);
            }
        }

        g2d.translate(cameraX, cameraY);
    }

    private void renderRoom(Graphics2D g2d, int col, int row) {
        int roomX = col * roomWidth;
        int roomY = row * roomHeight;
        g2d.setColor(ROOM_COLOR);
        g2d.fillRect(roomX, roomY, roomWidth, roomHeight);
        g2d.setColor(Color.GRAY);
        g2d.drawRect(roomX, roomY, roomWidth, roomHeight);
    }
}
