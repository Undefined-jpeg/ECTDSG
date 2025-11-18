package com.ectdsg.projectiles;

import com.ectdsg.enemies.Enemy;
import java.awt.*;
import java.awt.geom.Point2D;

public class Projectile {
    protected double x, y;
    public int damage;
    public Enemy target;
    protected Color color;
    protected double speed = 30.0;

    public Projectile(int x, int y, int damage, Enemy target, Color color) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.target = target;
        this.color = color;
    }

    public void move(double speedMultiplier) {
        if (target == null || target.isDead()) return;

        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        double moveDistance = speed * speedMultiplier;

        if (distance > moveDistance) {
            x += (dx / distance) * moveDistance;
            y += (dy / distance) * moveDistance;
        } else {
            x = target.getX();
            y = target.getY();
        }
    }

    public boolean hasHitTarget() {
        if (target == null || target.isDead()) return false;

        double distance = Point2D.distance(x, y, target.getX(), target.getY());
        return distance < 5;
    }

    public boolean isOutOfBounds(int gamePanelWidth, int gamePanelHeight) {
        return x < 0 || x > gamePanelWidth || y < 0 || y > gamePanelHeight;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillRect((int)x - 3, (int)y - 3, 6, 6);
    }
}
