package src.entity;

import java.awt.Color;

public class BossEnemy extends Enemy {
    private static final int HEALTH = 1000;
    private static final double SPEED = 0.05;
    private static final double SIZE = 60;
    private static final int DAMAGE = 40;
    private static final int XP_DROP_AMOUNT = 200;
    private static final Color COLOR = new Color(128, 0, 128); // Purple

    public BossEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
    }
}
