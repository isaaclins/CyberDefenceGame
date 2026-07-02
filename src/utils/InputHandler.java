package src.utils;

import src.main.Game;
import src.main.GameState;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class InputHandler implements KeyListener, MouseMotionListener, MouseListener {
    private final Game game;

    public InputHandler(Game game) {
        this.game = game;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (game.getGameState()) {
            case MENU:
                handleMenuKey(code);
                return;
            case LEVEL_UP:
                handleUpgradeKey(code);
                return;
            case GAME_OVER:
                handleGameOverKey(code);
                return;
            case PAUSED:
                handlePausedKey(code);
                return;
            default:
                handlePlayingKey(code);
        }
    }

    private void handlePlayingKey(int code) {
        if (code == KeyEvent.VK_W)
            game.setUpPressed(true);
        if (code == KeyEvent.VK_S)
            game.setDownPressed(true);
        if (code == KeyEvent.VK_A)
            game.setLeftPressed(true);
        if (code == KeyEvent.VK_D)
            game.setRightPressed(true);
        if (code == KeyEvent.VK_SHIFT)
            game.setDashPressed(true);
        if (code == KeyEvent.VK_SPACE)
            game.setShooting(true);
        if (code == KeyEvent.VK_R)
            game.reloadGun();
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P)
            game.togglePause();
    }

    private void handleMenuKey(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            game.getMenuScreen().moveSelection(-1);
            game.playUiClick();
        } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            game.getMenuScreen().moveSelection(1);
            game.playUiClick();
        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            game.getMenuScreen().confirm(game);
        }
    }

    private void handleUpgradeKey(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP || code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            game.getUpgradeScreen().moveSelection(-1);
            game.playUiClick();
        } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_D
                || code == KeyEvent.VK_RIGHT) {
            game.getUpgradeScreen().moveSelection(1);
            game.playUiClick();
        } else if (code == KeyEvent.VK_1) {
            game.getUpgradeScreen().select(0);
            game.getUpgradeScreen().confirm(game, game.getPlayer());
        } else if (code == KeyEvent.VK_2) {
            game.getUpgradeScreen().select(1);
            game.getUpgradeScreen().confirm(game, game.getPlayer());
        } else if (code == KeyEvent.VK_3) {
            game.getUpgradeScreen().select(2);
            game.getUpgradeScreen().confirm(game, game.getPlayer());
        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            game.getUpgradeScreen().confirm(game, game.getPlayer());
        }
    }

    private void handleGameOverKey(int code) {
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            game.getGameOverScreen().moveSelection(-1);
            game.playUiClick();
        } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            game.getGameOverScreen().moveSelection(1);
            game.playUiClick();
        } else if (code == KeyEvent.VK_R) {
            game.restartRun();
        } else if (code == KeyEvent.VK_M || code == KeyEvent.VK_ESCAPE) {
            game.reset();
        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            game.getGameOverScreen().confirm(game);
        }
    }

    private void handlePausedKey(int code) {
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) {
            game.togglePause();
        } else if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            game.getPauseScreen().moveSelection(-1);
            game.playUiClick();
        } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            game.getPauseScreen().moveSelection(1);
            game.playUiClick();
        } else if (code == KeyEvent.VK_R) {
            game.restartRun();
        } else if (code == KeyEvent.VK_M || code == KeyEvent.VK_Q) {
            game.reset();
        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            game.getPauseScreen().confirm(game);
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
        if (code == KeyEvent.VK_SHIFT)
            game.setDashPressed(false);
        if (code == KeyEvent.VK_SPACE)
            game.setShooting(false);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        switch (game.getGameState()) {
            case MENU:
                game.getMenuScreen().setPointer(e.getX(), e.getY());
                break;
            case LEVEL_UP:
                game.getUpgradeScreen().setPointer(e.getX(), e.getY());
                break;
            case GAME_OVER:
                game.getGameOverScreen().setPointer(e.getX(), e.getY());
                break;
            case PAUSED:
                game.getPauseScreen().setPointer(e.getX(), e.getY());
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        switch (game.getGameState()) {
            case MENU:
                game.getMenuScreen().handleClick(e.getX(), e.getY(), game);
                break;
            case GAME_OVER:
                game.getGameOverScreen().handleClick(e.getX(), e.getY(), game);
                break;
            case LEVEL_UP:
                game.getUpgradeScreen().handleClick(e.getX(), e.getY(), game, game.getPlayer());
                break;
            case PAUSED:
                game.getPauseScreen().handleClick(e.getX(), e.getY(), game);
                break;
            default:
                break;
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
