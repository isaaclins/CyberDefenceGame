package src.utils;

import src.entity.Enemy;
import src.entity.EnemyBullet;
import src.entity.Pellet;
import src.entity.Player;
import src.entity.XP;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import src.entity.Gun;
import src.entity.Particle;

public class Renderer {
    private static final Font LEVEL_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font WAVE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Font LEVEL_PROGRESS_FONT = new Font("Arial", Font.PLAIN, 15);
    private static final Color ROOM_COLOR = new Color(30, 30, 30);
    private static final Color XP_BAR_BACKGROUND = new Color(70, 70, 70, 220);
    private static final Color XP_BAR_COLOR = new Color(64, 220, 120);
    private static final Color AMMO_COLOR = new Color(255, 255, 255, 150);
    private static final Color AMMO_RELOAD_OUTLINE_COLOR = new Color(255, 255, 255, 55);

    private final int roomWidth;
    private final int roomHeight;

    public Renderer(int roomWidth, int roomHeight) {
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
    }

    public void render(Graphics2D g2d, Player player, Map<String, RoomRenderBucket> roomBuckets, int roomCol,
            int roomRow) {
        renderRoomAndContents(g2d, roomBuckets, roomCol, roomRow);
        renderRoomAndContents(g2d, roomBuckets, roomCol - 1, roomRow);
        renderRoomAndContents(g2d, roomBuckets, roomCol + 1, roomRow);
        renderRoomAndContents(g2d, roomBuckets, roomCol, roomRow - 1);
        renderRoomAndContents(g2d, roomBuckets, roomCol, roomRow + 1);

        Rectangle2D playerShape = new Rectangle2D.Double(player.getX() - 10, player.getY() - 10, 20, 20);
        GlowRenderer.drawGlow(g2d, playerShape, Color.RED, 5);
        g2d.setColor(Color.RED);
        g2d.fill(playerShape);

        drawPlayerHud(g2d, player);

        Rectangle2D gunShape = new Rectangle2D.Double(player.getGunX() - 5, player.getGunY() - 5, 10, 10);
        GlowRenderer.drawGlow(g2d, gunShape, Color.BLUE, 4);
        g2d.setColor(Color.BLUE);
        g2d.fill(gunShape);
    }

    public void drawWaveNumber(Graphics2D g2d, int screenWidth, int waveNumber, int killsToNextLevel) {
        String waveText = "Wave: " + waveNumber;
        g2d.setColor(Color.WHITE);
        g2d.setFont(WAVE_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (screenWidth - fm.stringWidth(waveText)) / 2;
        g2d.drawString(waveText, x, 30);

        String levelText = "Next Level: ~" + killsToNextLevel + " kills";
        g2d.setFont(LEVEL_PROGRESS_FONT);
        fm = g2d.getFontMetrics();
        x = (screenWidth - fm.stringWidth(levelText)) / 2;
        g2d.drawString(levelText, x, 52);
    }

    private void drawPlayerHud(Graphics2D g2d, Player player) {
        String levelStr = "Lvl " + player.getLevelingSystem().getLevel();
        g2d.setColor(Color.WHITE);
        g2d.setFont(LEVEL_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int strWidth = fm.stringWidth(levelStr);
        g2d.drawString(levelStr, (int) player.getX() - strWidth / 2, (int) player.getY() + fm.getAscent() / 2);

        double xpPercentage = (double) player.getLevelingSystem().getXp()
                / player.getLevelingSystem().getXpToNextLevel();
        g2d.setColor(XP_BAR_BACKGROUND);
        g2d.fillRect((int) player.getX() - 15, (int) player.getY() + 15, 30, 5);
        g2d.setColor(XP_BAR_COLOR);
        g2d.fillRect((int) player.getX() - 15, (int) player.getY() + 15, (int) (30 * xpPercentage), 5);

        drawHealth(g2d, player);
        drawAmmo(g2d, player);
    }

    private void drawHealth(Graphics2D g2d, Player player) {
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
            g2d.setColor(Color.RED);
            if (i < health) {
                g2d.fillOval(startX + i * (circleSize + spacing), startY, circleSize, circleSize);
            } else {
                g2d.drawOval(startX + i * (circleSize + spacing), startY, circleSize, circleSize);
            }
        }
    }

    private void drawAmmo(Graphics2D g2d, Player player) {
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

        for (int i = 0; i < magazineSize; i++) {
            double angle = i * angleStep - Math.PI / 2; // Start from the top
            int x = (int) (playerScreenX + orbitRadius * Math.cos(angle)) - ammoCircleRadius;
            int y = (int) (playerScreenY + orbitRadius * Math.sin(angle)) - ammoCircleRadius;

            if (i < currentAmmo) {
                g2d.setColor(AMMO_COLOR);
                g2d.fillOval(x, y, ammoCircleRadius * 2, ammoCircleRadius * 2);
                continue;
            }

            if (gun.isReloading()) {
                g2d.setColor(AMMO_RELOAD_OUTLINE_COLOR);
                g2d.drawOval(x, y, ammoCircleRadius * 2, ammoCircleRadius * 2);
            }
        }
    }

    private void renderRoomAndContents(Graphics2D g2d, Map<String, RoomRenderBucket> roomBuckets, int col, int row) {
        renderRoom(g2d, col, row);
        RoomRenderBucket bucket = roomBuckets.get(RoomRenderBucket.key(col, row));
        if (bucket == null) {
            return;
        }

        renderBucket(g2d, bucket);
    }

    private void renderBucket(Graphics2D g2d, RoomRenderBucket bucket) {
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

    private void renderRoom(Graphics2D g2d, int col, int row) {
        int roomX = col * roomWidth;
        int roomY = row * roomHeight;
        g2d.setColor(ROOM_COLOR);
        g2d.fillRect(roomX, roomY, roomWidth, roomHeight);
        g2d.setColor(Color.GRAY);
        g2d.drawRect(roomX, roomY, roomWidth, roomHeight);
    }
}
