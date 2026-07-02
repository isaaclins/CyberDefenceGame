package src.screens;

import src.main.Game;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class PauseScreen {
    private static final String[] OPTIONS = { "RESUME", "RESTART RUN", "QUIT TO MENU" };
    private static final String[][] CONTROLS = {
            { "WASD", "Move" },
            { "SHIFT", "Dash" },
            { "SPACE", "Shoot" },
            { "R", "Reload" },
            { "ESC / P", "Pause" }
    };

    private final Rectangle[] optionButtons = new Rectangle[OPTIONS.length];
    private int selectedIndex;
    private long shownAtNanos;

    public PauseScreen() {
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = new Rectangle();
        }
    }

    public void onShow() {
        shownAtNanos = System.nanoTime();
        selectedIndex = 0;
    }

    public void render(Graphics2D g2d, int width, int height) {
        UiTheme.enableAntialias(g2d);

        double elapsed = elapsedSeconds();
        UiTheme.drawScrim(g2d, width, height, elapsed / 0.2);

        int centerX = width / 2;
        g2d.setFont(UiTheme.TITLE);
        g2d.setColor(UiTheme.TEXT);
        int titleY = (int) (height * 0.20);
        UiTheme.drawCenteredString(g2d, "PAUSED", centerX, titleY);

        int boxWidth = Math.min(280, width - 60);
        int boxHeight = 40;
        int spacing = 10;
        int startY = titleY + 26;
        int x = (width - boxWidth) / 2;

        for (int i = 0; i < OPTIONS.length; i++) {
            int y = startY + i * (boxHeight + spacing);
            optionButtons[i].setBounds(x, y, boxWidth, boxHeight);
            double focus = i == selectedIndex ? 0.85 + 0.15 * Math.sin(elapsed * 4.0) : 0.0;
            UiTheme.drawOptionBox(g2d, x, y, boxWidth, boxHeight, focus);
            g2d.setFont(UiTheme.LABEL);
            g2d.setColor(UiTheme.mix(UiTheme.TEXT, UiTheme.ACCENT, focus));
            FontMetrics fm = g2d.getFontMetrics();
            int textY = y + (boxHeight + fm.getAscent() - fm.getDescent()) / 2;
            UiTheme.drawCenteredString(g2d, OPTIONS[i], centerX, textY);
        }

        // Controls reference below the options.
        int controlsY = startY + OPTIONS.length * (boxHeight + spacing) + 18;
        g2d.setFont(UiTheme.SMALL);
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight() + 2;
        int available = height - 16 - controlsY;
        if (available > CONTROLS.length * lineHeight) {
            for (int i = 0; i < CONTROLS.length; i++) {
                int y = controlsY + i * lineHeight;
                g2d.setColor(UiTheme.ACCENT);
                g2d.drawString(CONTROLS[i][0], centerX - 70, y);
                g2d.setColor(UiTheme.MUTED);
                g2d.drawString(CONTROLS[i][1], centerX + 10, y);
            }
        }
    }

    public void moveSelection(int delta) {
        int count = OPTIONS.length;
        selectedIndex = ((selectedIndex + delta) % count + count) % count;
    }

    public void setPointer(int x, int y) {
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].contains(x, y)) {
                selectedIndex = i;
                return;
            }
        }
    }

    public void confirm(Game game) {
        game.playUiClick();
        if (selectedIndex == 0) {
            game.togglePause();
        } else if (selectedIndex == 1) {
            game.restartRun();
        } else {
            game.reset();
        }
    }

    public void handleClick(int mouseX, int mouseY, Game game) {
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].contains(mouseX, mouseY)) {
                selectedIndex = i;
                confirm(game);
                return;
            }
        }
    }

    private double elapsedSeconds() {
        return (System.nanoTime() - shownAtNanos) / 1_000_000_000.0;
    }
}
