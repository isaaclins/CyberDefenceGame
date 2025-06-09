package src.entity;

import java.awt.Color;

public class SmallEnemy extends Enemy {
    private static final int HEALTH = 50;
    private static final double SPEED = 0.3;
    private static final double SIZE = 15;
    private static final int DAMAGE = 5;
    private static final int XP_DROP_AMOUNT = 10;
    private static final Color COLOR = Color.CYAN;

    public SmallEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
    }
}
