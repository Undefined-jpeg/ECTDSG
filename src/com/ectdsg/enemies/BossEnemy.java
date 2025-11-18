package com.ectdsg.enemies;

import com.ectdsg.Path;
import com.ectdsg.TowerDefence;
import java.awt.*;

public abstract class BossEnemy extends Enemy {
    public BossEnemy(TowerDefence game, Path path, int bossWaveLevel) {
        super(game, path,
              5000 + bossWaveLevel * 1000,
              0.3 + bossWaveLevel * 0.1,
              300 + bossWaveLevel * 50,
              "BOSS");
        this.maxHealth = this.health;
    }

    @Override
    public void takeDamage(int damage) {
        if (isDead()) return;
        double resistanceMultiplier = 0.60;
        int finalDamage = (int) (damage * resistanceMultiplier);
        this.health -= Math.max(1, finalDamage);
    }

    @Override
    public abstract void draw(Graphics2D g2d);
}
