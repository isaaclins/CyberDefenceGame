package src.entity;

import java.awt.Color;

public class NormalEnemy extends Enemy {
    private static final int HEALTH = 100;
    private static final double SPEED = 0.2;
    private static final double SIZE = 20;
    private static final int DAMAGE = 10;
    private static final int XP_DROP_AMOUNT = 20;
    private static final Color COLOR = Color.ORANGE;

    public NormalEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
    }
}
