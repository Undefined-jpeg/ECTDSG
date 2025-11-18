package com.ectdsg.enemies;

import com.ectdsg.Path;
import com.ectdsg.TowerDefence;
import java.awt.*;

public class GuardianBoss extends BossEnemy {
    public GuardianBoss(TowerDefence game, Path path, int bossWaveLevel) {
        super(game, path, bossWaveLevel);
        this.health = 7000 + bossWaveLevel * 1400;
        this.baseSpeed = 0.3 + bossWaveLevel * 0.1;
        this.shieldHitsRemaining = 50;
        this.maxHealth = this.health;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval((int)getX() - 22, (int)getY() - 22, 44, 44);
        g2d.setColor(Color.WHITE);
        g2d.drawOval((int)getX() - 22, (int)getY() - 22, 44, 44);
        g2d.setColor(Color.ORANGE);
        g2d.fillOval((int)getX() - 10, (int)getY() - 10, 20, 20);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)getX() - 22, (int)getY() - 32, 44, 8);
        g2d.setColor(Color.GREEN);
        double healthPercent = (double)health / maxHealth;
        g2d.fillRect((int)getX() - 22, (int)getY() - 32, (int)(44 * healthPercent), 8);
    }
}
