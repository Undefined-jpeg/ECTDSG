package com.ectdsg.projectiles;

import com.ectdsg.enemies.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class MortarProjectile extends Projectile {
    private final int aoeRange;

    public MortarProjectile(int x, int y, int damage, Enemy target, Color color, int aoeRange) {
        super(x, y, damage, target, color);
        this.speed = 10.0;
        this.aoeRange = aoeRange;
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
