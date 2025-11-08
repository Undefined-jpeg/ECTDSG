package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;
import java.util.List;

public class SplitterEnemy extends Enemy {

    public SplitterEnemy(Path path, int health, double speed) {
        super(path, health * 3, speed * 0.7, 40, "SPLITTER_ENEMY");
        this.color = new Color(255, 165, 0); // Orange
    }

    public void split(List<Enemy> enemies) {
        if (isDead()) {
            enemies.add(new Enemy(path, maxHealth / 2, baseSpeed * 1.5, 10, "BASIC_ENEMY"));
            enemies.add(new Enemy(path, maxHealth / 2, baseSpeed * 1.5, 10, "BASIC_ENEMY"));
        }
    }
}
