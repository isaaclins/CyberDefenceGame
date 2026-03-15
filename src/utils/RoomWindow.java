package src.utils;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class RoomWindow {
    private final GameWindow window;
    private final Canvas canvas;
    private final WindowRenderer renderer;
    private final int roomCol;
    private final int roomRow;
    private final int windowWidth;
    private final int windowHeight;

    public RoomWindow(int roomCol, int roomRow, int windowWidth, int windowHeight) {
        this.roomCol = roomCol;
        this.roomRow = roomRow;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        canvas = new Canvas();
        canvas.setFocusable(false);
        canvas.setPreferredSize(new Dimension(windowWidth, windowHeight));
        window = new GameWindow("Room (" + roomCol + ", " + roomRow + ")", windowWidth, windowHeight, canvas,
                javax.swing.JFrame.DISPOSE_ON_CLOSE, false);
        canvas.createBufferStrategy(3);
        renderer = new WindowRenderer(windowWidth, windowHeight, roomCol * windowWidth, roomRow * windowHeight);
    }

    public void setLocation(int x, int y) {
        window.setLocation(x, y);
    }

    public void render(RoomRenderBucket bucket) {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        Graphics2D g2d = null;
        try {
            g2d = (Graphics2D) bs.getDrawGraphics();
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, windowWidth, windowHeight);
            renderer.render(g2d, bucket);
        } finally {
            if (g2d != null) {
                g2d.dispose();
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
