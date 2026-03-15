package src.screens;

import src.entity.Player;
import src.main.Game;
import src.upgrades.Upgrade;

import java.awt.*;
import java.util.List;

public class UpgradeScreen {
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 34);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font UPGRADE_TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font UPGRADE_BODY_FONT = new Font("Arial", Font.PLAIN, 13);
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 180);
    private static final Color BOX_COLOR = new Color(70, 70, 70, 240);
    private static final Color BOX_BORDER_COLOR = Color.WHITE;

    private List<Upgrade> currentUpgrades;
    private Rectangle[] upgradeBounds;
    private int titleY;
    private int subtitleY;

    public void presentUpgrades(Game game) {
        this.currentUpgrades = game.getUpgradeManager().getRandomUpgrades(3);
        int screenWidth = game.getWidth();
        int screenHeight = game.getHeight();
        int count = Math.max(1, currentUpgrades.size());
        int boxWidth = Math.min(420, screenWidth - 64);
        int spacing = Math.max(10, Math.min(16, screenHeight / 28));
        int topArea = 78;
        int bottomPadding = 18;
        int availableHeight = Math.max(120, screenHeight - topArea - bottomPadding - ((count - 1) * spacing));
        int boxHeight = Math.max(50, Math.min(94, availableHeight / count));
        int startY = topArea;

        titleY = 36;
        subtitleY = 54;

        upgradeBounds = new Rectangle[currentUpgrades.size()];
        for (int i = 0; i < currentUpgrades.size(); i++) {
            int x = (screenWidth - boxWidth) / 2;
            int y = startY + i * (boxHeight + spacing);
            upgradeBounds[i] = new Rectangle(x, y, boxWidth, boxHeight);
        }
    }

    public void render(Graphics g, int width, int height) {
        if (currentUpgrades == null)
            return;

        g.setColor(OVERLAY_COLOR);
        g.fillRect(0, 0, width, height);

        g.setFont(TITLE_FONT);
        g.setColor(Color.WHITE);
        String title = "LEVEL UP!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (width - fm.stringWidth(title)) / 2, titleY);

        g.setFont(SUBTITLE_FONT);
        String subtitle = "Choose one upgrade";
        fm = g.getFontMetrics();
        g.drawString(subtitle, (width - fm.stringWidth(subtitle)) / 2, subtitleY);

        for (int i = 0; i < currentUpgrades.size(); i++) {
            Rectangle box = upgradeBounds[i];
            Upgrade upgrade = currentUpgrades.get(i);

            g.setColor(BOX_COLOR);
            g.fillRoundRect(box.x, box.y, box.width, box.height, 18, 18);
            g.setColor(BOX_BORDER_COLOR);
            g.drawRoundRect(box.x, box.y, box.width, box.height, 18, 18);

            g.setFont(UPGRADE_TITLE_FONT);
            fm = g.getFontMetrics();
            int titleY = box.y + 12 + fm.getAscent();
            g.drawString(upgrade.getName(), box.x + (box.width - fm.stringWidth(upgrade.getName())) / 2, titleY);

            g.setFont(UPGRADE_BODY_FONT);
            drawCenteredWrappedText(g, upgrade.getDescription(), box.x + 20, box.y + 32, box.width - 40, box.height - 18);
        }
    }

    public void handleClick(int mouseX, int mouseY, Game game, Player player) {
        if (upgradeBounds == null)
            return;

        for (int i = 0; i < upgradeBounds.length; i++) {
            if (upgradeBounds[i].contains(mouseX, mouseY)) {
                game.playUiClick();
                currentUpgrades.get(i).apply(player);
                game.setGameState(src.main.GameState.PLAYING);
                game.resumeWave();
                currentUpgrades = null;
                upgradeBounds = null;
                break;
            }
        }
    }

    public void clear() {
        currentUpgrades = null;
        upgradeBounds = null;
    }

    private void drawCenteredWrappedText(Graphics g, String text, int x, int y, int maxWidth, int maxHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int drawY = y + fm.getAscent();

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
                continue;
            }

            if (line.length() == 0) {
                drawCenteredLine(g, word, x, maxWidth, drawY, fm);
                drawY += fm.getHeight();
                if (drawY > y + maxHeight) {
                    return;
                }
                continue;
            }

            drawCenteredLine(g, line.toString(), x, maxWidth, drawY, fm);
            drawY += fm.getHeight();
            if (drawY > y + maxHeight) {
                return;
            }
            line.setLength(0);
            line.append(word);
        }

        if (line.length() > 0 && drawY <= y + maxHeight) {
            drawCenteredLine(g, line.toString(), x, maxWidth, drawY, fm);
        }
    }

    private void drawCenteredLine(Graphics g, String text, int x, int maxWidth, int y, FontMetrics fm) {
        g.drawString(text, x + ((maxWidth - fm.stringWidth(text)) / 2), y);
    }
}
