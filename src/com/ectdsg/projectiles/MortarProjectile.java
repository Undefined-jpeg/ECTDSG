package com.ectdsg.projectiles;

import com.ectdsg.enemies.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class MortarProjectile extends Projectile {
    private final int aoeRange;
    private final double startX, startY;
    private final double targetX, targetY;
    private final double duration;
    private double elapsedTime = 0;
    private final double arcHeight;

    public MortarProjectile(int x, int y, int damage, Enemy target, Color color, int aoeRange) {
        super(x, y, damage, target, color);
        this.speed = 10.0; // This is now more of a "travel time" factor than a "speed"
        this.aoeRange = aoeRange;
        this.startX = x;
        this.startY = y;

        // Mortar projectiles should target the ground, not a moving enemy
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.target = null; // Set target to null so it doesn't follow the enemy

        double distance = Point2D.distance(startX, startY, targetX, targetY);
        this.duration = distance / (speed * 10); // Adjust duration calculation
        this.arcHeight = Math.max(50, distance / 3); // Make the arc more pronounced
    }

    @Override
    public void move(double speedMultiplier) {
        elapsedTime += 0.016 * speedMultiplier; // Assuming 60 FPS (1/60 ~= 0.016)

        if (hasHitTarget()) {
            this.x = targetX;
            this.y = targetY;
            return;
        }

        double t = elapsedTime / duration;
        this.x = startX + (targetX - startX) * t;
        double y_linear = startY + (targetY - startY) * t;

        // Parabolic arc
        double arc = -4 * arcHeight * t * (t - 1);
        this.y = y_linear + arc;
    }

    @Override
    public boolean hasHitTarget() {
        return elapsedTime >= duration;
    }


    public void applyExplosionDamage(List<Enemy> allEnemies) {
        double impactX = this.x;
        double impactY = this.y;

        for (Enemy enemy : allEnemies) {
            if (enemy.isDead()) continue;
            double distance = Point2D.distance(impactX, impactY, enemy.getX(), enemy.getY());

            if (distance <= aoeRange) {
                enemy.takeDamage(damage);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval((int)x - 5, (int)y - 5, 10, 10);

        if (this.hasHitTarget()) {
             g2d.setColor(new Color(255, 165, 0, 150));
             g2d.drawOval((int)x - aoeRange, (int)y - aoeRange, aoeRange * 2, aoeRange * 2);
        }
    }
}
