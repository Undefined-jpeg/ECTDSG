package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import java.awt.*;

public class ReactorCoreTower extends Tower {
    public static final int COST = 5000;
    public static final int RANGE = 40;
    private double buffMultiplier = 1.25;

    public ReactorCoreTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 0;
        this.fireRate = 100000;
        this.range = RANGE;
        this.color = Color.green;
        this.cost = COST;
    }

    @Override
    public void attack() { /* Passive buffing, no attack required */ }

    public double getBuff() { return buffMultiplier; }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.GREEN.darker());
        g2d.fillOval(x - 15, y - 15, 30, 30);

        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - 10, y - 10, 20, 20);

        g2d.setColor(Color.GREEN.darker());
        g2d.drawString("☢", x - 4, y + 3);

        g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 30));
        g2d.fillOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
    }
}
