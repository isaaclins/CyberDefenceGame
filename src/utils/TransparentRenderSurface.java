package src.utils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.JComponent;

public class TransparentRenderSurface extends JComponent {
    private volatile BufferedImage currentFrame;

    public TransparentRenderSurface() {
        setOpaque(false);
        setDoubleBuffered(false);
        setBackground(TransparentBackground.TRANSPARENT);
    }

    public TransparentRenderSurface(int width, int height, boolean focusable) {
        this();
        setFocusable(focusable);
        setPreferredSize(new Dimension(width, height));
    }

    public void renderFrame(Consumer<Graphics2D> painter) {
        int frameWidth = getRenderWidth();
        int frameHeight = getRenderHeight();
        BufferedImage nextFrame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = nextFrame.createGraphics();
        try {
            TransparentBackground.clear(g2d, frameWidth, frameHeight);
            painter.accept(g2d);
        } finally {
            g2d.dispose();
        }

        currentFrame = nextFrame;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics.create();
        try {
            TransparentBackground.clear(g2d, getWidth(), getHeight());
            BufferedImage frame = currentFrame;
            if (frame != null) {
                g2d.drawImage(frame, 0, 0, null);
            }
        } finally {
            g2d.dispose();
        }
    }

    private int getRenderWidth() {
        int width = getWidth();
        if (width > 0) {
            return width;
        }
        return Math.max(1, getPreferredSize().width);
    }

    private int getRenderHeight() {
        int height = getHeight();
        if (height > 0) {
            return height;
        }
        return Math.max(1, getPreferredSize().height);
    }
}
