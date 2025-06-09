package src.entity;

import java.awt.Color;

public class BigEnemy extends Enemy {
    private static final int HEALTH = 250;
    private static final double SPEED = 0.1;
    private static final double SIZE = 35;
    private static final int DAMAGE = 20;
    private static final int XP_DROP_AMOUNT = 50;
    private static final Color COLOR = Color.MAGENTA;

    public BigEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
    }
}
