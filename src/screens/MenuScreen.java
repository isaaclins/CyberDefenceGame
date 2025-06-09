package src.screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import src.main.Game;

public class MenuScreen {
    private Rectangle startButton;

    public MenuScreen() {
        // We don't know the width and height yet, so we'll create the button in the
        // render method.
    }

    public void render(Graphics g, int width, int height) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Cyber Defence Game";
        int strWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - strWidth) / 2, height / 2 - 50);

        // Draw start button
        g.setFont(new Font("Arial", Font.BOLD, 30));
        String startText = "Start Game";
        strWidth = g.getFontMetrics().stringWidth(startText);
        int buttonX = (width - 200) / 2;
        int buttonY = height / 2 + 20;
        int buttonWidth = 200;
        int buttonHeight = 50;
        startButton = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);

        g.drawRect(buttonX, buttonY, buttonWidth, buttonHeight);
        g.drawString(startText, buttonX + (buttonWidth - strWidth) / 2, buttonY + 35);
    }

    public void handleClick(int x, int y, Game game) {
        if (startButton != null && startButton.contains(x, y)) {
            game.startGame();
        }
    }
}
