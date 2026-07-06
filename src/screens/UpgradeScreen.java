package src.screens;

import src.entity.Player;
import src.items.Item;
import src.main.Game;
import src.upgrades.Upgrade;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

public class UpgradeScreen {
    private static final int LEVEL_UP_CHOICE_COUNT = 3;
    private static final int SUPPLY_CHEST_CHOICE_COUNT = 5;

    private volatile List<Upgrade> currentUpgrades;
    private volatile Rectangle[] upgradeBounds;
    private volatile int selectedIndex;
    private volatile long shownAtNanos;
    private volatile Player currentPlayer;
    private volatile String title = "LEVEL UP";
    private volatile String subtitle = "Choose an upgrade";
    private volatile boolean resetLevelUpOnConfirm = true;

    public void presentUpgrades(Game game) {
        presentChoices(
            game,
            game.getPlayer(),
            game.getUpgradeManager().getRandomUpgrades(LEVEL_UP_CHOICE_COUNT),
            "LEVEL UP",
            "Choose an upgrade",
            true
        );
    }

    public void presentItemChoices(Game game, Player player) {
        presentChoices(
            game,
            player,
            game.getUpgradeManager().getRandomItems(SUPPLY_CHEST_CHOICE_COUNT),
            "SUPPLY CHEST",
            "Choose an item",
            false
        );
    }

    private void presentChoices(Game game, Player player, List<Upgrade> choices, String title, String subtitle,
            boolean resetLevelUpOnConfirm) {
        this.currentUpgrades = choices;
        this.currentPlayer = player;
        this.title = title;
        this.subtitle = subtitle;
        this.resetLevelUpOnConfirm = resetLevelUpOnConfirm;
        this.selectedIndex = 0;
        this.shownAtNanos = System.nanoTime();

        int screenWidth = game.getWidth();
        int screenHeight = game.getHeight();
        int count = Math.max(1, currentUpgrades.size());
        int boxWidth = Math.min(400, screenWidth - 48);
        int spacing = 12;
        int topArea = (int) (screenHeight * 0.24);
        int bottomPadding = 40;
        int available = Math.max(120, screenHeight - topArea - bottomPadding - ((count - 1) * spacing));
        int boxHeight = Math.max(56, Math.min(92, available / count));
        int startY = topArea;

        upgradeBounds = new Rectangle[currentUpgrades.size()];
        for (int i = 0; i < currentUpgrades.size(); i++) {
            int x = (screenWidth - boxWidth) / 2;
            int y = startY + i * (boxHeight + spacing);
            upgradeBounds[i] = new Rectangle(x, y, boxWidth, boxHeight);
        }
    }

    public void render(Graphics g, int width, int height) {
        List<Upgrade> upgrades = currentUpgrades;
        Rectangle[] bounds = upgradeBounds;
        int focusedIndex = selectedIndex;
        if (upgrades == null || bounds == null) {
            return;
        }
        if (upgrades.isEmpty() || bounds.length < upgrades.size()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        UiTheme.enableAntialias(g2d);

        double elapsed = elapsedSeconds();
        UiTheme.drawScrim(g2d, width, height, elapsed / 0.25);

        int centerX = width / 2;
        g2d.setFont(UiTheme.HEADING);
        g2d.setColor(UiTheme.ACCENT);
        UiTheme.drawCenteredString(g2d, title, centerX, (int) (height * 0.13));

        g2d.setFont(UiTheme.BODY);
        g2d.setColor(UiTheme.MUTED);
        UiTheme.drawCenteredString(g2d, subtitle, centerX, (int) (height * 0.13) + 22);

        for (int i = 0; i < upgrades.size(); i++) {
            renderCard(g2d, upgrades, bounds, focusedIndex, i, elapsed);
        }

        g2d.setFont(UiTheme.HINT);
        g2d.setColor(UiTheme.MUTED);
        UiTheme.drawCenteredString(g2d, "1-" + upgrades.size() + " / arrows  select      ENTER  confirm", centerX,
                height - 16);
    }

    private void renderCard(Graphics2D g2d, List<Upgrade> upgrades, Rectangle[] bounds, int focusedIndex, int index,
            double elapsed) {
        Rectangle box = bounds[index];
        Upgrade upgrade = upgrades.get(index);

        // Staggered slide-and-fade entrance per card.
        double delay = 0.06 * index;
        double appear = UiTheme.easeOut((elapsed - delay) / 0.32);
        int offsetY = (int) ((1.0 - appear) * 18);
        int drawY = box.y + offsetY;

        double focus = index == focusedIndex ? 0.85 + 0.15 * Math.sin(elapsed * 4.0) : 0.0;
        UiTheme.drawOptionBox(g2d, box.x, drawY, box.width, box.height, focus);

        // Number badge.
        g2d.setFont(UiTheme.SMALL);
        g2d.setColor(UiTheme.mix(UiTheme.MUTED, UiTheme.ACCENT, focus));
        g2d.drawString("[" + (index + 1) + "]", box.x + 16, drawY + 20);

        if (upgrade instanceof Item) {
            drawTypeBadge(g2d, box, drawY, focus);
        }

        g2d.setFont(UiTheme.LABEL);
        g2d.setColor(UiTheme.mix(UiTheme.TEXT, UiTheme.ACCENT, focus));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(getDisplayName(upgrade), box.x + 42, drawY + 14 + fm.getAscent() - 4);

        g2d.setFont(UiTheme.BODY);
        g2d.setColor(UiTheme.MUTED);
        drawWrappedText(g2d, getDisplayDescription(upgrade), box.x + 42, drawY + 34, box.width - 58,
                drawY + box.height - 8);
    }

    private String getDisplayName(Upgrade upgrade) {
        if (upgrade instanceof Item && currentPlayer != null) {
            return ((Item) upgrade).getName(currentPlayer);
        }
        return upgrade.getName();
    }

    private String getDisplayDescription(Upgrade upgrade) {
        if (upgrade instanceof Item && currentPlayer != null) {
            return ((Item) upgrade).getDescription(currentPlayer);
        }
        return upgrade.getDescription();
    }

    private void drawTypeBadge(Graphics2D g2d, Rectangle box, int drawY, double focus) {
        String label = "ITEM";
        g2d.setFont(UiTheme.SMALL);
        FontMetrics fm = g2d.getFontMetrics();
        int paddingX = 8;
        int badgeWidth = fm.stringWidth(label) + (paddingX * 2);
        int badgeHeight = 17;
        int badgeX = box.x + box.width - badgeWidth - 14;
        int badgeY = drawY + 10;

        g2d.setColor(UiTheme.mix(UiTheme.PANEL, UiTheme.ACCENT, 0.18 + (focus * 0.18)));
        g2d.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 8, 8);
        g2d.setColor(UiTheme.mix(UiTheme.MUTED, UiTheme.ACCENT, 0.65 + (focus * 0.25)));
        g2d.drawRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 8, 8);
        g2d.drawString(label, badgeX + paddingX, badgeY + fm.getAscent() + 1);
    }

    public void moveSelection(int delta) {
        List<Upgrade> upgrades = currentUpgrades;
        if (upgrades == null || upgrades.isEmpty()) {
            return;
        }
        int count = upgrades.size();
        selectedIndex = ((selectedIndex + delta) % count + count) % count;
    }

    public void select(int index) {
        List<Upgrade> upgrades = currentUpgrades;
        if (upgrades != null && index >= 0 && index < upgrades.size()) {
            selectedIndex = index;
        }
    }

    public void setPointer(int x, int y) {
        Rectangle[] bounds = upgradeBounds;
        if (bounds == null) {
            return;
        }
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i].contains(x, y)) {
                selectedIndex = i;
                return;
            }
        }
    }

    public void confirm(Game game, Player player) {
        List<Upgrade> upgrades = currentUpgrades;
        int index = selectedIndex;
        if (upgrades == null || index < 0 || index >= upgrades.size()) {
            return;
        }
        game.playUiClick();
        upgrades.get(index).apply(player);
        if (resetLevelUpOnConfirm) {
            game.resumeWave();
        } else {
            game.resumeAfterItemChoice();
        }
        clear();
    }

    public void handleClick(int mouseX, int mouseY, Game game, Player player) {
        Rectangle[] bounds = upgradeBounds;
        if (bounds == null) {
            return;
        }
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i].contains(mouseX, mouseY)) {
                selectedIndex = i;
                confirm(game, player);
                return;
            }
        }
    }

    public void clear() {
        currentUpgrades = null;
        upgradeBounds = null;
        currentPlayer = null;
    }

    private double elapsedSeconds() {
        return (System.nanoTime() - shownAtNanos) / 1_000_000_000.0;
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int maxY) {
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
            if (line.length() > 0) {
                g.drawString(line.toString(), x, drawY);
                drawY += fm.getHeight();
                if (drawY > maxY) {
                    return;
                }
                line.setLength(0);
            }
            line.append(word);
        }
        if (line.length() > 0 && drawY <= maxY) {
            g.drawString(line.toString(), x, drawY);
        }
    }
}
