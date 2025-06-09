package src.screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import src.entity.Gun;
import src.main.Game;
import src.utils.ClassFinder;

public class MenuScreen {
    private List<Rectangle> gunButtons;
    private List<Class<?>> gunClasses;

    public MenuScreen() {
        gunButtons = new ArrayList<>();
        gunClasses = ClassFinder.findSubclasses("src.entity", Gun.class);
    }

    public void render(Graphics g, int width, int height) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Cyber Defence Game";
        int strWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - strWidth) / 2, height / 2 - 150);

        g.setFont(new Font("Arial", Font.BOLD, 30));
        String selectText = "Select a Gun";
        strWidth = g.getFontMetrics().stringWidth(selectText);
        g.drawString(selectText, (width - strWidth) / 2, height / 2 - 50);

        gunButtons.clear();
        int buttonY = height / 2;
        for (Class<?> gunClass : gunClasses) {
            String gunName = gunClass.getSimpleName();
            strWidth = g.getFontMetrics().stringWidth(gunName);
            int buttonX = (width - 200) / 2;
            int buttonWidth = 200;
            int buttonHeight = 50;
            Rectangle button = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
            gunButtons.add(button);

            g.drawRect(buttonX, buttonY, buttonWidth, buttonHeight);
            g.drawString(gunName, buttonX + (buttonWidth - strWidth) / 2, buttonY + 35);
            buttonY += 60;
        }
    }

    public void handleClick(int x, int y, Game game) {
        for (int i = 0; i < gunButtons.size(); i++) {
            if (gunButtons.get(i).contains(x, y)) {
                try {
                    Gun selectedGun = (Gun) gunClasses.get(i).getDeclaredConstructor().newInstance();
                    game.startGame(selectedGun);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
