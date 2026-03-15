package src.utils;

import java.util.ArrayList;
import java.util.List;

import src.entity.Enemy;
import src.entity.Particle;
import src.entity.Pellet;
import src.entity.XP;

public class RoomRenderBucket {
    private final int roomCol;
    private final int roomRow;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Pellet> pellets = new ArrayList<>();
    private final List<XP> xps = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();

    public RoomRenderBucket(int roomCol, int roomRow) {
        this.roomCol = roomCol;
        this.roomRow = roomRow;
    }

    public static String key(int roomCol, int roomRow) {
        return roomCol + "," + roomRow;
    }

    public int getRoomCol() {
        return roomCol;
    }

    public int getRoomRow() {
        return roomRow;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Pellet> getPellets() {
        return pellets;
    }

    public List<XP> getXps() {
        return xps;
    }

    public List<Particle> getParticles() {
        return particles;
    }
}
