package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import java.awt.*;

public class DetectorTower extends Tower {
    public static final int COST = 300;
    public static final int RANGE = 200;

    public DetectorTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 0;
        this.fireRate = 1000;
        this.range = RANGE;
        this.color = new Color(255, 255, 0); // Yellow
        this.cost = COST;
    }

    @Override
    public void attack() {
        // This tower does not attack, it only detects
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval(x - 10, y - 10, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("D", x - 4, y + 5);
    }
}
