package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;
import java.util.List;

public class FriendlyEnemy extends Enemy {

    private long lastAttackTime = 0;
    private final long ATTACK_COOLDOWN = 1000; // 1 second
    private final int ATTACK_RADIUS = 50;
    private final int ATTACK_DAMAGE = 10;

    public FriendlyEnemy(Path path, int health, double speed, int currentWaypoint) {
        super(path, health, speed, 0, "FRIENDLY_ENEMY");
        this.color = new Color(0, 0, 255); // Blue
        this.currentWaypoint = currentWaypoint;
    }

    @Override
    public void move(double speedMultiplier) {
        if (currentWaypoint <= 0) {
            return;
        }

        double actualSpeed = baseSpeed * speedMultiplier;

        Point targetPoint = path.getPoint(currentWaypoint - 1);

        double dx = targetPoint.x - getX();
        double dy = targetPoint.y - getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= actualSpeed) {
            setX(targetPoint.x);
            setY(targetPoint.y);
            currentWaypoint--;
        } else {
            setX(getX() + (dx / distance) * actualSpeed);
            setY(getY() + (dy / distance) * actualSpeed);
        }
    }

    public void attackNearby(List<Enemy> enemies) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < ATTACK_COOLDOWN) {
            return;
        }

        for (Enemy enemy : enemies) {
            if (enemy != this && !(enemy instanceof FriendlyEnemy) && !enemy.isDead()) {
                double distance = Math.sqrt(Math.pow(getX() - enemy.getX(), 2) + Math.pow(getY() - enemy.getY(), 2));
                if (distance <= ATTACK_RADIUS) {
                    enemy.takeDamage(ATTACK_DAMAGE);
                }
            }
        }
        lastAttackTime = currentTime;
    }
}
