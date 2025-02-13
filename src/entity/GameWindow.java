package src.entity;

import javax.swing.*;
import java.awt.*;

public class GameWindow {
    private JFrame frame;
    private final int width;
    private final int height;

    public GameWindow(String title, int width, int height, Canvas canvas) {
        this.width = width;
        this.height = height;

        frame = new JFrame(title);
        frame.add(canvas);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void setLocation(int x, int y) {
        frame.setLocation(x, y);
    }

    public Point getLocation() {
        return frame.getLocation();
    }
}
