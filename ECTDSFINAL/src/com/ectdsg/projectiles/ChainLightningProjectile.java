package com.ectdsg.projectiles;

import com.ectdsg.enemies.Enemy;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChainLightningProjectile extends Projectile {
    private List<Enemy> enemies;
    private int jumps = 10;
    private double jumpRadius = 100;

    public ChainLightningProjectile(int x, int y, int damage, Enemy target, Color color, List<Enemy> enemies) {
        super(x, y, damage, target, color);
        this.enemies = enemies;
    }

    @Override
    public void move(double speedMultiplier) {
        super.move(speedMultiplier);
        if (hasHitTarget()) {
            jump();
        }
    }

    private void jump() {
        if (jumps <= 0) {
            return;
        }

        List<Enemy> nearbyEnemies = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy != target && !enemy.isDead()) {
                double distance = Math.sqrt(Math.pow(target.getX() - enemy.getX(), 2) + Math.pow(target.getY() - enemy.getY(), 2));
                if (distance <= jumpRadius) {
                    nearbyEnemies.add(enemy);
                }
            }
        }

        if (!nearbyEnemies.isEmpty()) {
            Enemy nextTarget = nearbyEnemies.get(0);
            target.takeDamage(damage);
            damage /= 1.2;
            target = nextTarget;
            jumps--;
        } else {
            target.takeDamage(damage);
            jumps = 0;
        }
    }
}
