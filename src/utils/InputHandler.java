package src.utils;

import src.main.Game;
import src.main.GameState;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class InputHandler implements KeyListener, MouseMotionListener, MouseListener {
    private Game game;

    public InputHandler(Game game) {
        this.game = game;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W)
            game.setUpPressed(true);
        if (code == KeyEvent.VK_S)
            game.setDownPressed(true);
        if (code == KeyEvent.VK_A)
            game.setLeftPressed(true);
        if (code == KeyEvent.VK_D)
            game.setRightPressed(true);
        if (code == KeyEvent.VK_SPACE)
            game.setShooting(true);
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) {
            game.togglePause();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W)
            game.setUpPressed(false);
        if (code == KeyEvent.VK_S)
            game.setDownPressed(false);
        if (code == KeyEvent.VK_A)
            game.setLeftPressed(false);
        if (code == KeyEvent.VK_D)
            game.setRightPressed(false);
        if (code == KeyEvent.VK_SPACE)
            game.setShooting(false);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (game.getGameState() == GameState.MENU) {
            game.getMenuScreen().handleClick(e.getX(), e.getY(), game);
        } else if (game.getGameState() == GameState.GAME_OVER) {
            game.getGameOverScreen().handleClick(e.getX(), e.getY(), game);
        } else if (game.getGameState() == GameState.LEVEL_UP) {
            game.getUpgradeScreen().handleClick(e.getX(), e.getY(), game, game.getPlayer());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
