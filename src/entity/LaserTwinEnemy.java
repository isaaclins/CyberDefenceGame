package src.entity;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

public class LaserTwinEnemy extends Enemy {
    private static final int HEALTH = 70;
    private static final double SPEED = 0.18;
    private static final double SIZE = 18;
    private static final int DAMAGE = 1;
    private static final int XP_DROP_AMOUNT = 24;
    private static final Color COLOR = new Color(255, 92, 92);

    private boolean linked;
    private int formationSide;

    public LaserTwinEnemy(double x, double y) {
        super(x, y, HEALTH, COLOR, SPEED, SIZE, DAMAGE, XP_DROP_AMOUNT);
        linked = false;
        formationSide = 0;
    }

    @Override
    public List<EnemyBullet> updateBehavior(double playerX, double playerY) {
        if (linked) {
            return Collections.emptyList();
        }

        moveToPlayer(playerX, playerY);
        return Collections.emptyList();
    }

    public void clearLinkedState() {
        linked = false;
        formationSide = 0;
    }

    public void setLinkedState(int formationSide) {
        linked = true;
        this.formationSide = formationSide < 0 ? -1 : 1;
    }

    public boolean isLinked() {
        return linked;
    }

    public int getFormationSide() {
        return formationSide;
    }

    public void updateLinkedBehavior(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distanceSquared = (dx * dx) + (dy * dy);
        if (distanceSquared <= 0.0001) {
            return;
        }

        double distance = Math.sqrt(distanceSquared);
        velocityX += speed * (dx / distance);
        velocityY += speed * (dy / distance);
        facingAngle = Math.atan2(dy, dx);
    }
}
