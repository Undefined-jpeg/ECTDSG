package com.ectdsg.enemies;

import com.ectdsg.Path;
import com.ectdsg.TowerDefence;
import java.awt.*;

public class BruteBoss extends BossEnemy {
    public BruteBoss(TowerDefence game, Path path, int bossWaveLevel) {
        super(game, path, bossWaveLevel);
        this.health = 10000 + bossWaveLevel * 2000;
        this.baseSpeed = 0.2 + bossWaveLevel * 0.05;
        this.damageReduction = 50;
        this.maxHealth = this.health;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)getX() - 25, (int)getY() - 25, 50, 50);
        g2d.setColor(Color.GRAY);
        g2d.fillRect((int)getX() - 20, (int)getY() - 20, 40, 40);
        g2d.setColor(Color.RED);
        g2d.fillOval((int)getX() - 5, (int)getY() - 5, 10, 10);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)getX() - 25, (int)getY() - 35, 50, 8);
        g2d.setColor(Color.GREEN);
        double healthPercent = (double)health / maxHealth;
        g2d.fillRect((int)getX() - 25, (int)getY() - 35, (int)(50 * healthPercent), 8);
    }
}
