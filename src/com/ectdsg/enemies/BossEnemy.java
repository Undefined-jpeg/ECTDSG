package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class BossEnemy extends Enemy {
    private static final int BOSS_DRAW_SIZE = 45;

    public BossEnemy(Path path, int bossWaveLevel) {
        super(path,
              5000 + bossWaveLevel * 1000,
              0.3 + bossWaveLevel * 0.1,
              300 + bossWaveLevel * 50,
              "BOSS");

        this.maxHealth = this.health;
        this.color = new Color(75, 0, 130);
    }

    @Override
    public void takeDamage(int damage) {
        if (isDead()) return;

        double resistanceMultiplier = 0.60;

        int finalDamage = (int) (damage * resistanceMultiplier);

        this.health -= Math.max(1, finalDamage);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(this.color);
        g2d.fillOval((int)getX() - BOSS_DRAW_SIZE / 2, (int)getY() - BOSS_DRAW_SIZE / 2, BOSS_DRAW_SIZE, BOSS_DRAW_SIZE);

        g2d.setColor(Color.RED.darker());
        g2d.drawRect((int)getX() - BOSS_DRAW_SIZE / 2, (int)getY() - BOSS_DRAW_SIZE / 2, BOSS_DRAW_SIZE, BOSS_DRAW_SIZE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("TERRY", (int)getX() - 16, (int)getY() + 5);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)getX() - BOSS_DRAW_SIZE / 2, (int)getY() - BOSS_DRAW_SIZE / 2 - 15, BOSS_DRAW_SIZE, 8);
        g2d.setColor(Color.RED);
        double healthPercent = (double)health / maxHealth;
        g2d.fillRect((int)getX() - BOSS_DRAW_SIZE / 2, (int)getY() - BOSS_DRAW_SIZE / 2 - 15, (int)(BOSS_DRAW_SIZE * healthPercent), 8);

        if (System.currentTimeMillis() < slowEndTime) {
            g2d.setColor(new Color(0, 150, 255));
            g2d.fillOval((int)getX() - BOSS_DRAW_SIZE / 2 - 5, (int)getY() - BOSS_DRAW_SIZE / 2 - 5, 10, 10);
        }
    }
}
