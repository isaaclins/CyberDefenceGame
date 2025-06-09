package src.entity;

import java.awt.Color;
import java.awt.Graphics;

public class XP {
    private double x, y;
    private int amount;

    public XP(double x, double y, int amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;
    }

    public void render(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect((int) x - 2, (int) y - 2, 4, 4);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getAmount() {
        return amount;
    }
}
