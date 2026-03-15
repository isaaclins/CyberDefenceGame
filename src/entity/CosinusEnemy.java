package src.entity;

import java.awt.Color;

public class CosinusEnemy extends OscillatingEnemy {
    private static final int HEALTH = 90;
    private static final double SPEED = 0.16;
    private static final double SIZE = 18;
    private static final int DAMAGE = 1;
    private static final int XP_DROP_AMOUNT = 24;
    private static final double STRAFE_STRENGTH = 0.42;
    private static final double WAVE_STEP = 0.005;
    private static final Color COLOR = new Color(132, 164, 255);

    public CosinusEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT, Math.PI / 2.0, STRAFE_STRENGTH, WAVE_STEP);
    }
}
