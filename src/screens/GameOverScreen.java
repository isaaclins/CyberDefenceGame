package src.screens;

import src.main.Game;
import src.utils.SessionStats;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GameOverScreen {
    private static final String[] OPTIONS = { "RESTART RUN", "RETURN TO MENU" };

    private final Rectangle[] optionButtons = new Rectangle[OPTIONS.length];
    private int selectedIndex;
    private long shownAtNanos;

    public GameOverScreen() {
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = new Rectangle();
        }
        onShow();
    }

    public void onShow() {
        shownAtNanos = System.nanoTime();
        selectedIndex = 0;
    }

    public void render(Graphics g, int width, int height, SessionStats stats) {
        Graphics2D g2d = (Graphics2D) g;
        UiTheme.enableAntialias(g2d);

        double elapsed = elapsedSeconds();
        UiTheme.drawScrim(g2d, width, height, elapsed / 0.3);

        int centerX = width / 2;
        g2d.setFont(UiTheme.TITLE);
        g2d.setColor(UiTheme.DANGER);
        int titleY = (int) (height * 0.20);
        UiTheme.drawCenteredString(g2d, "GAME OVER", centerX, titleY);

        String[] labels = {
                "Time Survived", "Enemies Defeated", "Level Reached", "Highest Combo", "Power-ups Collected"
        };
        String[] values = {
                stats.getFormattedSurvivalTime(),
                Integer.toString(stats.getEnemiesDefeated()),
                Integer.toString(stats.getHighestLevel()),
                "x" + stats.getHighestCombo(),
                Integer.toString(stats.getPowerUpsCollected())
        };

        int rowY = titleY + 40;
        int rowHeight = 24;
        FontMetrics fm;
        for (int i = 0; i < labels.length; i++) {
            double appear = UiTheme.easeOut((elapsed - 0.2 - i * 0.07) / 0.3);
            if (appear <= 0.0) {
                continue;
            }
            int alpha = (int) Math.round(255 * appear);
            int y = rowY + i * rowHeight + (int) ((1.0 - appear) * 8);

            g2d.setFont(UiTheme.BODY);
            g2d.setColor(UiTheme.withAlpha(UiTheme.MUTED, alpha));
            fm = g2d.getFontMetrics();
            g2d.drawString(labels[i], centerX - 130, y);

            g2d.setFont(UiTheme.LABEL);
            g2d.setColor(UiTheme.withAlpha(UiTheme.TEXT, alpha));
            fm = g2d.getFontMetrics();
            g2d.drawString(values[i], centerX + 130 - fm.stringWidth(values[i]), y);
        }

        int buttonsY = rowY + labels.length * rowHeight + 20;
        int buttonWidth = Math.min(180, (width - 60) / 2);
        int gap = 16;
        int totalWidth = buttonWidth * OPTIONS.length + gap * (OPTIONS.length - 1);
        int startX = centerX - totalWidth / 2;
        double buttonsAppear = UiTheme.easeOut((elapsed - 0.7) / 0.3);

        for (int i = 0; i < OPTIONS.length; i++) {
            int x = startX + i * (buttonWidth + gap);
            optionButtons[i].setBounds(x, buttonsY, buttonWidth, 42);
            if (buttonsAppear <= 0.0) {
                continue;
            }
            double focus = i == selectedIndex ? 0.85 + 0.15 * Math.sin(elapsed * 4.0) : 0.0;
            UiTheme.drawOptionBox(g2d, x, buttonsY, buttonWidth, 42, focus);
            g2d.setFont(UiTheme.LABEL);
            g2d.setColor(UiTheme.mix(UiTheme.TEXT, UiTheme.ACCENT, focus));
            fm = g2d.getFontMetrics();
            int textY = buttonsY + (42 + fm.getAscent() - fm.getDescent()) / 2;
            UiTheme.drawCenteredString(g2d, OPTIONS[i], x + buttonWidth / 2, textY);
        }

        g2d.setFont(UiTheme.HINT);
        g2d.setColor(UiTheme.MUTED);
        UiTheme.drawCenteredString(g2d, "A / D  select      ENTER  confirm", centerX, height - 16);
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
