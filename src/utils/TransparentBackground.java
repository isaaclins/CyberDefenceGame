package src.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;

import javax.swing.JComponent;
import javax.swing.JFrame;

public final class TransparentBackground {
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private TransparentBackground() {
    }

    public static void configureWindow(JFrame frame, Component renderSurface) {
        if (frame == null || !isPerPixelTranslucencySupported()) {
            return;
        }

        try {
            frame.setBackground(TRANSPARENT);
            makeTransparent(frame.getRootPane());
            makeTransparent(frame.getLayeredPane());
            makeTransparent(frame.getContentPane());
            if (renderSurface != null) {
                makeTransparent(renderSurface);
            }
        } catch (UnsupportedOperationException | IllegalComponentStateException | SecurityException exception) {
            // Some platforms expose translucency APIs but reject transparent top-level windows.
        }
    }

    public static void clear(Graphics2D g2d, int width, int height) {
        Composite previousComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(previousComposite);
    }

    private static boolean isPerPixelTranslucencySupported() {
        if (GraphicsEnvironment.isHeadless()) {
            return false;
        }

        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        return graphicsDevice.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
    }

    private static void makeTransparent(Component component) {
        if (component == null) {
            return;
        }

        component.setBackground(TRANSPARENT);
        if (component instanceof JComponent) {
            ((JComponent) component).setOpaque(false);
        }
        if (!(component instanceof Container)) {
            return;
        }

        for (Component child : ((Container) component).getComponents()) {
            makeTransparent(child);
        }
    }
}
