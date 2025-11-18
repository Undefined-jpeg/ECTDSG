package com.ectdsg.enemies;

import com.ectdsg.Path;
import com.ectdsg.TowerDefence;
import java.awt.*;

public class ExtraterrestrialBoss extends BossEnemy {
    public ExtraterrestrialBoss(TowerDefence game, Path path, int bossWaveLevel) {
        super(game, path, bossWaveLevel);
        this.health = 6000 + bossWaveLevel * 1200;
        this.baseSpeed = 0.4 + bossWaveLevel * 0.15;
        this.maxHealth = this.health;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int)getX() - 20, (int)getY() - 20, 40, 40);
        g2d.setColor(Color.CYAN);
        g2d.drawOval((int)getX() - 20, (int)getY() - 20, 40, 40);
        g2d.setColor(Color.MAGENTA);
        g2d.fillOval((int)getX() - 5, (int)getY() - 5, 10, 10);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)getX() - 20, (int)getY() - 30, 40, 8);
        g2d.setColor(Color.GREEN);
        double healthPercent = (double)health / maxHealth;
        g2d.fillRect((int)getX() - 20, (int)getY() - 30, (int)(40 * healthPercent), 8);
    }
}
