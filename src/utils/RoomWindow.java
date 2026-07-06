package src.utils;

import java.awt.*;

public class RoomWindow {
    private final GameWindow window;
    private final TransparentRenderSurface renderSurface;
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
        renderSurface = new TransparentRenderSurface(windowWidth, windowHeight, false);
        window = new GameWindow("Room (" + roomCol + ", " + roomRow + ")", windowWidth, windowHeight, renderSurface,
                javax.swing.JFrame.DISPOSE_ON_CLOSE, false);
        renderer = new WindowRenderer(windowWidth, windowHeight, roomCol * windowWidth, roomRow * windowHeight);
    }

    public void setLocation(int x, int y) {
        window.setLocation(x, y);
    }

    public void render(RoomRenderBucket bucket) {
        renderSurface.renderFrame(g2d -> {
            renderer.render(g2d, bucket);
        });
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
