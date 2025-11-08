package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;
import java.util.List;

public class HealerEnemy extends Enemy {

    private long lastHealTime = 0;
    private final long HEAL_COOLDOWN = 2000; // 2 seconds
    private final int HEAL_RADIUS = 50;
    private final int HEAL_AMOUNT = 10;

    public HealerEnemy(Path path, int health, double speed) {
        super(path, health, speed, 35, "HEALER_ENEMY");
        this.color = new Color(0, 255, 0); // Green
    }

    public void healNearby(List<Enemy> enemies) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHealTime < HEAL_COOLDOWN) {
            return;
        }

        for (Enemy enemy : enemies) {
            if (enemy != this && !enemy.isDead()) {
                double distance = Math.sqrt(Math.pow(getX() - enemy.getX(), 2) + Math.pow(getY() - enemy.getY(), 2));
                if (distance <= HEAL_RADIUS) {
                    enemy.health += HEAL_AMOUNT;
                    if (enemy.health > enemy.maxHealth) {
                        enemy.health = enemy.maxHealth;
                    }
                }
            }
        }
        lastHealTime = currentTime;
    }
}
