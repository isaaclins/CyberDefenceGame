package src.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.List;
import src.entity.Enemy;
import src.entity.Pellet;
import src.entity.Player;
import src.entity.XP;

public class RoomWindow {
    private GameWindow window;
    private Canvas canvas;
    private WindowRenderer renderer;
    private int roomCol;
    private int roomRow;
    private int windowWidth;
    private int windowHeight;

    public RoomWindow(int roomCol, int roomRow, int windowWidth, int windowHeight) {
        this.roomCol = roomCol;
        this.roomRow = roomRow;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(windowWidth, windowHeight));
        window = new GameWindow("Room (" + roomCol + ", " + roomRow + ")", windowWidth, windowHeight, canvas);
        canvas.createBufferStrategy(3);
        renderer = new WindowRenderer(canvas, windowWidth, windowHeight, roomCol * windowWidth, roomRow * windowHeight);
    }

    public void setLocation(int x, int y) {
        window.setLocation(x, y);
    }

    public void render(List<Enemy> enemies, List<Pellet> pellets, List<XP> xps) {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        Graphics g = null;
        try {
            g = bs.getDrawGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, windowWidth, windowHeight);
            renderer.render(g, enemies, pellets, null, xps);
        } finally {
            if (g != null) {
                g.dispose();
            }
            bs.show();
        }
    }

    // Dispose of this window.
    public void close() {
        if (window != null) {
            window.dispose();
        }
    }

    public int getRoomCol() {
        return roomCol;
    }

    public int getRoomRow() {
        return roomRow;
    }
}
