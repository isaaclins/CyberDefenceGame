package src.utils;

import javax.swing.*;
import java.awt.*;

public class GameWindow {
    private JFrame frame;
    private final double width;
    private final double height;

    public GameWindow(String title, int width, int height, Canvas canvas, int defaultCloseOperation) {
        this(title, width, height, canvas, defaultCloseOperation, true);
    }

    public GameWindow(String title, int width, int height, Canvas canvas, int defaultCloseOperation,
            boolean focusableWindow) {
        this.width = width;
        this.height = height;
        frame = new JFrame(title);
        frame.setUndecorated(true);
        frame.setFocusableWindowState(focusableWindow);
        frame.setAutoRequestFocus(focusableWindow);
        canvas.setFocusable(focusableWindow);
        frame.add(canvas);
        frame.pack();
        frame.setDefaultCloseOperation(defaultCloseOperation);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void setAlwaysOnTop(boolean flag) {
        if (frame != null) {
            frame.setAlwaysOnTop(flag);
        }
    }

    public void setLocation(int x, int y) {
        frame.setLocation(x, y);
    }

    public Point getLocation() {
        return frame.getLocation();
    }

    public void requestCanvasFocus(Canvas canvas) {
        if (frame != null && canvas != null) {
            frame.toFront();
            canvas.requestFocus();
            canvas.requestFocusInWindow();
        }
    }

    public void dispose() {
        if (frame != null) {
            frame.dispose();
        }
    }
}
