package src.screens;

import src.entity.Player;
import src.main.Game;
import src.upgrades.Upgrade;

import java.awt.*;
import java.util.List;

public class UpgradeScreen {
    private List<Upgrade> currentUpgrades;
    private Rectangle[] upgradeBounds;

    public void presentUpgrades(Game game) {
        this.currentUpgrades = game.getUpgradeManager().getRandomUpgrades(3);
        int screenWidth = game.getWidth();
        int screenHeight = game.getHeight();
        int boxWidth = 300;
        int boxHeight = 100;
        int spacing = 50;
        int startY = (screenHeight - (boxHeight * 3 + spacing * 2)) / 2;

        upgradeBounds = new Rectangle[currentUpgrades.size()];
        for (int i = 0; i < currentUpgrades.size(); i++) {
            int x = (screenWidth - boxWidth) / 2;
            int y = startY + i * (boxHeight + spacing);
            upgradeBounds[i] = new Rectangle(x, y, boxWidth, boxHeight);
        }
    }

    public void render(Graphics g) {
        if (currentUpgrades == null)
            return;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, 10000, 10000); // A big rectangle to cover the screen

        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.setColor(Color.WHITE);
        String title = "LEVEL UP!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (upgradeBounds[0].x + upgradeBounds[0].width / 2) - fm.stringWidth(title) / 2,
                upgradeBounds[0].y - 50);

        for (int i = 0; i < currentUpgrades.size(); i++) {
            Rectangle box = upgradeBounds[i];
            Upgrade upgrade = currentUpgrades.get(i);

            g.setColor(Color.DARK_GRAY);
            g.fillRect(box.x, box.y, box.width, box.height);
            g.setColor(Color.WHITE);
            g.drawRect(box.x, box.y, box.width, box.height);

            g.setFont(new Font("Arial", Font.BOLD, 24));
            fm = g.getFontMetrics();
            g.drawString(upgrade.getName(), box.x + (box.width - fm.stringWidth(upgrade.getName())) / 2, box.y + 40);

            g.setFont(new Font("Arial", Font.PLAIN, 16));
            fm = g.getFontMetrics();
            g.drawString(upgrade.getDescription(), box.x + (box.width - fm.stringWidth(upgrade.getDescription())) / 2,
                    box.y + 70);
        }
    }

    public void handleClick(int mouseX, int mouseY, Game game, Player player) {
        if (upgradeBounds == null)
            return;

        for (int i = 0; i < upgradeBounds.length; i++) {
            if (upgradeBounds[i].contains(mouseX, mouseY)) {
                currentUpgrades.get(i).apply(player);
                game.setGameState(src.main.GameState.PLAYING);
                game.resumeWave();
                currentUpgrades = null;
                upgradeBounds = null;
                break;
            }
        }
    }
}
