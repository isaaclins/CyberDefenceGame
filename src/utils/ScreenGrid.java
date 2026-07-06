package src.utils;

import java.awt.Point;
import java.awt.Rectangle;

public class ScreenGrid {
    private final int windowWidth;
    private final int windowHeight;
    private final int columns;
    private final int rows;
    private final int originX;
    private final int originY;
    private final int baseColumnSlot;
    private final int baseRowSlot;

    public ScreenGrid(Rectangle screenBounds, int windowWidth, int windowHeight, Point initialWindowLocation) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.columns = Math.max(1, screenBounds.width / windowWidth);
        this.rows = Math.max(1, screenBounds.height / windowHeight);
        this.originX = screenBounds.x + ((screenBounds.width - (columns * windowWidth)) / 2);
        this.originY = screenBounds.y + ((screenBounds.height - (rows * windowHeight)) / 2);
        this.baseColumnSlot = clampSlot(Math.round((initialWindowLocation.x - originX) / (float) windowWidth),
                columns);
        this.baseRowSlot = clampSlot(Math.round((initialWindowLocation.y - originY) / (float) windowHeight), rows);
    }

    public Point locationForRoom(int roomCol, int roomRow) {
        int slotCol = Math.floorMod(baseColumnSlot + roomCol, columns);
        int slotRow = Math.floorMod(baseRowSlot + roomRow, rows);
        return new Point(originX + (slotCol * windowWidth), originY + (slotRow * windowHeight));
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    private static int clampSlot(int slot, int slotCount) {
        return Math.max(0, Math.min(slotCount - 1, slot));
    }
}
