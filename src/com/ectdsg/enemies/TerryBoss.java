package com.ectdsg.enemies;

import com.ectdsg.Path;
import com.ectdsg.TowerDefence;
import java.awt.*;

public class TerryBoss extends BossEnemy {
    public TerryBoss(TowerDefence game, Path path, int bossWaveLevel) {
        super(game, path, bossWaveLevel);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillOval((int)getX() - 22, (int)getY() - 22, 44, 44);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Rockwell Extra Bold", Font.BOLD, 20));
        g2d.drawString("TERRY", (int)getX() - 35, (int)getY() + 5);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)getX() - 22, (int)getY() - 32, 44, 8);
        g2d.setColor(Color.GREEN);
        double healthPercent = (double)health / maxHealth;
        g2d.fillRect((int)getX() - 22, (int)getY() - 32, (int)(44 * healthPercent), 8);
    }
}
