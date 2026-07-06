package src.entity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SplitterEnemy extends Enemy {
    private static final int MAX_GENERATION = 2;
    private static final int[] HEALTH_BY_GENERATION = { 90, 40, 18 };
    private static final double[] SPEED_BY_GENERATION = { 0.22, 0.32, 0.42 };
    private static final double[] SIZE_BY_GENERATION = { 26, 17, 11 };
    private static final int[] XP_BY_GENERATION = { 20, 8, 4 };
    private static final int[] CHILDREN_BY_GENERATION = { 300, 2, 0 };
    private static final Color[] COLOR_BY_GENERATION = {
            new Color(255, 96, 200),
            new Color(255, 138, 216),
            new Color(255, 180, 232)
    };
    private static final int DAMAGE = 1;
    private static final double CHILD_SCATTER_SPEED = 4.5;

    private final int generation;

    public SplitterEnemy(double x, double y) {
        this(x, y, 0);
    }

    private SplitterEnemy(double x, double y, int generation) {
        super(x, y, HEALTH_BY_GENERATION[generation], COLOR_BY_GENERATION[generation], SPEED_BY_GENERATION[generation],
                SIZE_BY_GENERATION[generation], DAMAGE, XP_BY_GENERATION[generation]);
        this.generation = generation;
    }

    public List<SplitterEnemy> spawnChildren(Random random) {
        if (generation >= MAX_GENERATION) {
            return Collections.emptyList();
        }

        int childGeneration = generation + 1;
        int childCount = CHILDREN_BY_GENERATION[generation];
        List<SplitterEnemy> children = new ArrayList<>(childCount);
        double baseAngle = random.nextDouble() * Math.PI * 2;

        for (int i = 0; i < childCount; i++) {
            double angle = baseAngle + ((Math.PI * 2.0 * i) / childCount);
            double offset = SIZE_BY_GENERATION[childGeneration] / 2.0;
            SplitterEnemy child = new SplitterEnemy(x + (Math.cos(angle) * offset), y + (Math.sin(angle) * offset),
                    childGeneration);
            child.applyKnockback(Math.cos(angle) * CHILD_SCATTER_SPEED, Math.sin(angle) * CHILD_SCATTER_SPEED);
            children.add(child);
        }

        return children;
    }
}
