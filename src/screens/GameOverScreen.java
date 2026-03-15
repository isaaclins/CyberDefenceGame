package src.screens;

import src.main.Game;
import src.utils.SessionStats;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

public class GameOverScreen {
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 70);
    private static final Font STATS_FONT = new Font("Arial", Font.PLAIN, 20);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 30);

    private Rectangle menuButton;

    public GameOverScreen() {
        menuButton = new Rectangle(0, 0, 0, 0); // Will be initialized in render
    }

    public void render(Graphics g, int width, int height, SessionStats stats) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.RED);
        g.setFont(TITLE_FONT);
        String gameOverText = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(gameOverText)) / 2;
        int y = height / 4 + fm.getAscent();
        g.drawString(gameOverText, x, y);

        g.setColor(Color.WHITE);
        g.setFont(STATS_FONT);
        fm = g.getFontMetrics();

        String timeText = "Time Survived: " + stats.getFormattedSurvivalTime();
        String enemiesText = "Enemies Defeated: " + stats.getEnemiesDefeated();
        String levelText = "Level Reached: " + stats.getHighestLevel();

        x = (width - fm.stringWidth(timeText)) / 2;
        y += fm.getHeight() * 2;
        g.drawString(timeText, x, y);

        x = (width - fm.stringWidth(enemiesText)) / 2;
        y += fm.getHeight() + 10;
        g.drawString(enemiesText, x, y);

        x = (width - fm.stringWidth(levelText)) / 2;
        y += fm.getHeight() + 10;
        g.drawString(levelText, x, y);

        g.setFont(BUTTON_FONT);
        fm = g.getFontMetrics();
        String menuText = "Return to Menu";
        int buttonWidth = fm.stringWidth(menuText) + 60;
        int buttonHeight = 50;
        int buttonX = (width - buttonWidth) / 2;
        int buttonY = y + 100;
        menuButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);
        g.setColor(Color.WHITE);
        g.drawRect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);

        x = menuButton.x + (buttonWidth - fm.stringWidth(menuText)) / 2;
        y = menuButton.y + (buttonHeight - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(menuText, x, y);
    }

    public void handleClick(int mouseX, int mouseY, Game game) {
        if (menuButton.contains(mouseX, mouseY)) {
            game.playUiClick();
            game.reset(); // A new method to reset the game state
        }
    }
}
