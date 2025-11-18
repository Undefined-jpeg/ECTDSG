package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import java.awt.*;

public class BeaconTower extends Tower {
    public static final int COST = 7500;
    public static final int RANGE = 250;
    private double buffMultiplier = 1;

    public BeaconTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 0;
        this.fireRate = 100000;
        this.range = RANGE;
        this.color = Color.MAGENTA;
        this.cost = COST;
    }

    @Override
    public void attack() { /* Passive buffing, no attack required */ }

    public double getBuff() { return buffMultiplier; }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.MAGENTA.darker());
        g2d.fillOval(x - 30, y - 30, 60, 60);

        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - 20, y - 20, 40, 40);

        g2d.setColor(Color.MAGENTA.darker());
        g2d.drawString("⬆", x - 5, y + 5);

        g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 30));
        g2d.fillOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
    }
}
