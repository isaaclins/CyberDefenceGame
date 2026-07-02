package src.utils;

public class ComboTracker {
    private static final int COMBO_WINDOW_TICKS = 150;
    private static final double XP_BONUS_PER_KILL = 0.1;
    private static final double MAX_XP_MULTIPLIER = 3.0;
    private static final int[] MILESTONES = { 5, 10, 20, 30 };

    private int streak;
    private int windowTicksRemaining;

    public ComboTracker() {
        reset();
    }

    public void reset() {
        streak = 0;
        windowTicksRemaining = 0;
    }

    public void tick() {
        if (windowTicksRemaining <= 0) {
            return;
        }

        windowTicksRemaining--;
        if (windowTicksRemaining == 0) {
            streak = 0;
        }
    }

    public boolean recordKill() {
        streak++;
        windowTicksRemaining = COMBO_WINDOW_TICKS;
        return isMilestone(streak);
    }

    public boolean breakCombo() {
        boolean hadCombo = streak >= MILESTONES[0];
        reset();
        return hadCombo;
    }

    public int getStreak() {
        return streak;
    }

    public double getXpMultiplier() {
        return Math.min(MAX_XP_MULTIPLIER, 1.0 + (streak * XP_BONUS_PER_KILL));
    }

    public double getWindowRatio() {
        return windowTicksRemaining / (double) COMBO_WINDOW_TICKS;
    }

    public boolean isActive() {
        return streak >= 2 && windowTicksRemaining > 0;
    }

    private boolean isMilestone(int killStreak) {
        for (int milestone : MILESTONES) {
            if (killStreak == milestone) {
                return true;
            }
        }
        return false;
    }
}
