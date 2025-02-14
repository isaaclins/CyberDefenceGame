package src.utils;

import src.main.Game;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener, MouseMotionListener {
    private final Game game;

    public InputHandler(Game game) {
        this.game = game;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W)
            game.setUpPressed(true);
        if (key == KeyEvent.VK_S)
            game.setDownPressed(true);
        if (key == KeyEvent.VK_A)
            game.setLeftPressed(true);
        if (key == KeyEvent.VK_D)
            game.setRightPressed(true);
        if (key == KeyEvent.VK_SPACE) {
            game.setShooting(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W)
            game.setUpPressed(false);
        if (key == KeyEvent.VK_S)
            game.setDownPressed(false);
        if (key == KeyEvent.VK_A)
            game.setLeftPressed(false);
        if (key == KeyEvent.VK_D)
            game.setRightPressed(false);
        if (key == KeyEvent.VK_SPACE) {
            game.setShooting(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Not used.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Not used.
    }
}