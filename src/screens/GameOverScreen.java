package src.screens;

import src.main.Game;
import src.main.GameState;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

public class GameOverScreen {

    private Rectangle menuButton;

    public GameOverScreen() {
        menuButton = new Rectangle(0, 0, 0, 0); // Will be initialized in render
    }

    public void render(Graphics g, int width, int height) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 70));
        String gameOverText = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(gameOverText)) / 2;
        int y = height / 4 + fm.getAscent();
        g.drawString(gameOverText, x, y);

        // Placeholder for stats
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String statsText = "Time Survived: 0s | Enemies Defeated: 0 | Level Reached: 1";
        x = (width - fm.stringWidth(statsText)) / 2 + 125;
        y += fm.getHeight() * 2;
        g.drawString(statsText, x, y);

        // Menu Button
        g.setFont(new Font("Arial", Font.BOLD, 30));
        String menuText = "Return to Menu";
        int buttonWidth = fm.stringWidth(menuText) + 40;
        int buttonHeight = 50;
        int buttonX = (width - buttonWidth) / 2 + 50;
        int buttonY = y + 100;
        menuButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);
        g.setColor(Color.WHITE);
        g.drawRect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);

        x = menuButton.x + (buttonWidth - fm.stringWidth(menuText) + 50) / 2;
        y = menuButton.y + (buttonHeight - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(menuText, x, y);
    }

    public void handleClick(int mouseX, int mouseY, Game game) {
        if (menuButton.contains(mouseX, mouseY)) {
            game.reset(); // A new method to reset the game state
        }
    }
}
