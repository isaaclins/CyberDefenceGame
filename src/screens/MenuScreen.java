package src.screens;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import src.entity.Gun;
import src.main.Game;
import src.utils.ClassFinder;

public class MenuScreen {
    private final List<Class<?>> gunClasses;
    private final List<Gun> gunPreviews;
    private final List<Rectangle> gunButtons = new ArrayList<>();

    private int selectedIndex;
    private long shownAtNanos;
    private double indicatorY;
    private boolean indicatorReady;

    public MenuScreen() {
        gunClasses = ClassFinder.findSubclasses("src.entity", Gun.class);
        gunPreviews = new ArrayList<>();
        for (Class<?> gunClass : gunClasses) {
            gunPreviews.add(instantiate(gunClass));
        }
        onShow();
    }

    public void onShow() {
        shownAtNanos = System.nanoTime();
        selectedIndex = 0;
        indicatorReady = false;
    }

    public void render(Graphics g, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        UiTheme.enableAntialias(g2d);
        UiTheme.drawBackdrop(g2d, width, height);

        double elapsed = elapsedSeconds();
        int centerX = width / 2;

        // Title with a short accent underline sweep on entry.
        g2d.setFont(UiTheme.TITLE);
        g2d.setColor(UiTheme.TEXT);
        int titleY = (int) (height * 0.20);
        UiTheme.drawCenteredString(g2d, "CYBER DEFENCE", centerX, titleY);

        double underline = UiTheme.easeOut(elapsed / 0.5);
        int underlineWidth = (int) (140 * underline);
        g2d.setColor(UiTheme.ACCENT);
        g2d.fillRect(centerX - underlineWidth / 2, titleY + 10, underlineWidth, 2);

        g2d.setFont(UiTheme.BODY);
        g2d.setColor(UiTheme.MUTED);
        UiTheme.drawCenteredString(g2d, "SELECT YOUR WEAPON", centerX, titleY + 34);

        layoutButtons(width, height);

        // Smoothly slide the focus indicator toward the selected row.
        if (!gunButtons.isEmpty()) {
            Rectangle selected = gunButtons.get(selectedIndex);
            double targetY = selected.y;
            if (!indicatorReady) {
                indicatorY = targetY;
                indicatorReady = true;
            } else {
                indicatorY = UiTheme.lerp(indicatorY, targetY, 0.3);
            }
        }

        for (int i = 0; i < gunButtons.size(); i++) {
            Rectangle box = gunButtons.get(i);
            double focus = focusFor(i, box);
            UiTheme.drawOptionBox(g2d, box.x, box.y, box.width, box.height, focus);
            drawGunEntry(g2d, box, i, focus);
        }

        // Footer controls hint.
        g2d.setFont(UiTheme.HINT);
        g2d.setColor(UiTheme.MUTED);
        UiTheme.drawCenteredString(g2d, "W / S  move      ENTER  start      mouse also works", centerX, height - 16);
    }

    private void drawGunEntry(Graphics2D g2d, Rectangle box, int index, double focus) {
        String name = gunClasses.get(index).getSimpleName().toUpperCase();
        g2d.setFont(UiTheme.LABEL);
        g2d.setColor(UiTheme.mix(UiTheme.TEXT, UiTheme.ACCENT, focus));
        FontMetrics fm = g2d.getFontMetrics();
        int textY = box.y + (box.height / 2) + (fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(name, box.x + 18, textY);

        Gun preview = gunPreviews.get(index);
        if (preview == null) {
            return;
        }

        // Compact stat bars on the right side of the row.
        int barsRight = box.x + box.width - 16;
        int barWidth = 74;
        int barX = barsRight - barWidth;
        int barY = box.y + 14;
        drawStat(g2d, "DMG", barX, barY, barWidth, statRatio(preview.getDamage(), maxDamage()));
        drawStat(g2d, "RATE", barX, barY + 15, barWidth, rateRatio(preview.getShotCooldownTicks()));
        drawStat(g2d, "CLIP", barX, barY + 30, barWidth, statRatio(preview.getMagazineSize(), maxMagazine()));
    }

    private void drawStat(Graphics2D g2d, String label, int x, int y, int width, double ratio) {
        g2d.setFont(UiTheme.SMALL);
        g2d.setColor(UiTheme.MUTED);
        g2d.drawString(label, x - 30, y + 7);
        g2d.setColor(UiTheme.withAlpha(UiTheme.BORDER, 160));
        g2d.fillRoundRect(x, y, width, 5, 4, 4);
        int fill = (int) Math.round(width * UiTheme.clamp01(ratio));
        g2d.setColor(UiTheme.ACCENT);
        g2d.fillRoundRect(x, y, fill, 5, 4, 4);
    }

    private void layoutButtons(int width, int height) {
        gunButtons.clear();
        int count = Math.max(1, gunClasses.size());
        int boxWidth = Math.min(360, width - 48);
        int boxHeight = 58;
        int spacing = 12;
        int totalHeight = count * boxHeight + (count - 1) * spacing;
        int startY = (int) (height * 0.34);
        int maxStartY = height - 44 - totalHeight;
        if (startY > maxStartY) {
            startY = Math.max((int) (height * 0.30), maxStartY);
        }
        int x = (width - boxWidth) / 2;
        for (int i = 0; i < count; i++) {
            int y = startY + i * (boxHeight + spacing);
            gunButtons.add(new Rectangle(x, y, boxWidth, boxHeight));
        }
    }

    private double focusFor(int index, Rectangle box) {
        if (index != selectedIndex) {
            return 0.0;
        }
        // Gentle pulse on the focused row so it reads as "live".
        double pulse = 0.85 + 0.15 * Math.sin(elapsedSeconds() * 4.0);
        return pulse;
    }

    public void moveSelection(int delta) {
        if (gunButtons.isEmpty() && gunClasses.isEmpty()) {
            return;
        }
        int count = Math.max(1, gunClasses.size());
        selectedIndex = ((selectedIndex + delta) % count + count) % count;
    }

    public void setPointer(int x, int y) {
        for (int i = 0; i < gunButtons.size(); i++) {
            if (gunButtons.get(i).contains(x, y)) {
                selectedIndex = i;
                return;
            }
        }
    }

    public void confirm(Game game) {
        if (gunClasses.isEmpty()) {
            return;
        }
        Gun gun = instantiate(gunClasses.get(selectedIndex));
        if (gun == null) {
            return;
        }
        game.playUiClick();
        game.startGame(gun);
    }

    public void handleClick(int x, int y, Game game) {
        for (int i = 0; i < gunButtons.size(); i++) {
            if (gunButtons.get(i).contains(x, y)) {
                selectedIndex = i;
                confirm(game);
                return;
            }
        }
    }

    private double elapsedSeconds() {
        return (System.nanoTime() - shownAtNanos) / 1_000_000_000.0;
    }

    private double statRatio(double value, double max) {
        if (max <= 0.0) {
            return 0.0;
        }
        return value / max;
    }

    // Fewer cooldown ticks means faster fire, so a low cooldown fills the bar.
    private double rateRatio(int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return 1.0;
        }
        double best = Double.MAX_VALUE;
        for (Gun gun : gunPreviews) {
            if (gun != null) {
                best = Math.min(best, gun.getShotCooldownTicks());
            }
        }
        if (best == Double.MAX_VALUE || best <= 0) {
            return 1.0;
        }
        return best / cooldownTicks;
    }

    private double maxDamage() {
        double max = 0.0;
        for (Gun gun : gunPreviews) {
            if (gun != null) {
                max = Math.max(max, gun.getDamage());
            }
        }
        return max;
    }

    private double maxMagazine() {
        double max = 0.0;
        for (Gun gun : gunPreviews) {
            if (gun != null) {
                max = Math.max(max, gun.getMagazineSize());
            }
        }
        return max;
    }

    private Gun instantiate(Class<?> gunClass) {
        try {
            return (Gun) gunClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
