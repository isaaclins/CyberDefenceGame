package src.utils;

import javax.swing.*;
import java.awt.*;

public class GameWindow {
    private JFrame frame;
    private final double width;
    private final double height;

    public GameWindow(String title, int width, int height, Component renderSurface, int defaultCloseOperation) {
        this(title, width, height, renderSurface, defaultCloseOperation, true);
    }

    public GameWindow(String title, int width, int height, Component renderSurface, int defaultCloseOperation,
            boolean focusableWindow) {
        this.width = width;
        this.height = height;
        frame = new JFrame(title);
        frame.setUndecorated(true);
        TransparentBackground.configureWindow(frame, renderSurface);
        frame.setFocusableWindowState(focusableWindow);
        frame.setAutoRequestFocus(focusableWindow);
        renderSurface.setFocusable(focusableWindow);
        frame.add(renderSurface);
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

    public void requestRenderSurfaceFocus(Component renderSurface) {
        if (frame != null && renderSurface != null) {
            frame.toFront();
            renderSurface.requestFocus();
            renderSurface.requestFocusInWindow();
        }
    }

    public void dispose() {
        if (frame != null) {
            frame.dispose();
        }
    }
}
