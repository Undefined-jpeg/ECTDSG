package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.enemies.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class Tower {
    public int x, y;
    public int damage;
    protected long fireRate;
    protected long lastShotTime = 0;
    public int range;
    protected Color color;
    protected Color attackColor;
    protected int cost;

    private String targetingMode = TowerDefence.NEAREST;

    protected Enemy currentTarget = null;
    protected double currentAngle = 0.0;

    private double slowFactor = 0.0;
    private long slowEndTime = 0;

    protected TowerDefence game;

    public Tower(int x, int y, TowerDefence game) {
        this.x = x;
        this.y = y;
        this.game = game;
    }

    public void setTargetingMode(String mode) {
        this.targetingMode = mode;
    }

    public String getTargetingMode() {
        return targetingMode;
    }

    public int getCost() {
        return cost;
    }

    public long getActualFireRate() {
        double multiplier = 1.0;
        for (Tower t : game.towers) {
            if (t instanceof BeaconTower) {
                BeaconTower beacon = (BeaconTower) t;
                double distance = Point2D.distance(x, y, beacon.x, beacon.y);
                if (distance <= beacon.range) {
                    multiplier += beacon.getBuff();
                }
            }
        }
        if (System.currentTimeMillis() < slowEndTime) {
            multiplier *= (1.0 - slowFactor);
        }
        return (long)(fireRate / multiplier);
    }

    public void applySlow(double slowFactor) {
        this.slowFactor = slowFactor;
        this.slowEndTime = System.currentTimeMillis() + 1000; // Slow for 1 second
    }

    public void attack() {
        long currentTime = System.currentTimeMillis();
        long actualFireRate = getActualFireRate();

        long effectiveCooldown = (long) (actualFireRate / game.GAME_SPEED_MULTIPLIER);

        if (currentTime - lastShotTime < effectiveCooldown) {
            updateTarget();
            return;
        }

        updateTarget();

        if (currentTarget != null) {
            // This will need to be changed to create a Projectile, not a generic one
            // game.projectiles.add(new Projectile(x, y, damage, currentTarget, attackColor));
            lastShotTime = currentTime;
        }
    }

    protected void updateTargetAngle() {
        if (currentTarget != null && !currentTarget.isDead()) {
            double angle = Math.atan2(currentTarget.getY() - y, currentTarget.getX() - x);
            this.currentAngle = angle - -(Math.PI / 2);
        } else {
            currentTarget = null;
        }
    }

    protected void updateTarget() {
        if (currentTarget == null || currentTarget.isDead() ||
            Point2D.distance(currentTarget.getX(), currentTarget.getY(), x, y) > this.range) {

            this.currentTarget = findTarget();
        }
    }

    protected Enemy findTarget() {
        List<Enemy> inRangeEnemies = game.enemies.stream()
            .filter(enemy -> !enemy.isDead() && Point2D.distance(enemy.getX(), enemy.getY(), x, y) <= this.range)
            .toList();

        if (inRangeEnemies.isEmpty()) {
            return null;
        }

        Optional<Enemy> target;
        switch (targetingMode) {
            case TowerDefence.FARTHEST:
                target = inRangeEnemies.stream()
                    .max(Comparator.comparingInt((Enemy e) -> e.currentWaypoint)
                                 .thenComparingDouble(e -> e.getDistanceOnWaypoint()));
                break;
            case TowerDefence.STRONGEST:
                target = inRangeEnemies.stream()
                    .max(Comparator.comparingInt(e -> e.health));
                break;
            case TowerDefence.NEAREST:
            default:
                target = inRangeEnemies.stream()
                    .min(Comparator.comparingDouble(e -> Point2D.distance(e.getX(), e.getY(), x, y)));
                break;
        }
        return target.orElse(null);
    }

    public abstract void draw(Graphics2D g2d);
}
