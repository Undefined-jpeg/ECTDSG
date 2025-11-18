package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class DetectorTower extends Tower {
    public static final int COST = 300;
    public static final int RANGE = 200;

    public DetectorTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 0;
        this.fireRate = 1000;
        this.range = RANGE;
        this.color = new Color(0, 255, 0); // geeen, ur elemental power is.... green
        this.cost = COST;
    }

    @Override
    public void attack() {
        // This tower does not attack, it only detects
    }

    @Override
    public void draw(Graphics2D g2d) {

        
        g2d.setColor(color);
        g2d.fillOval(x - 5, y - 5, 30, 30);
        g2d.setColor(Color.BLACK);
        g2d.drawString("ø", x - 4, y + 5);
    }
}
