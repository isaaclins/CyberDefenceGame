package src.entity;

import java.awt.Color;

public class SinusEnemy extends OscillatingEnemy {
    private static final int HEALTH = 90;
    private static final double SPEED = 0.5;
    private static final double SIZE = 18;
    private static final int DAMAGE = 1;
    private static final int XP_DROP_AMOUNT = 24;
    private static final double STRAFE_STRENGTH = 0.5;
    private static final double WAVE_STEP = 0.1;
    private static final Color COLOR = new Color(96, 255, 184);

    public SinusEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT, 0.0, STRAFE_STRENGTH, WAVE_STEP);
    }
}
