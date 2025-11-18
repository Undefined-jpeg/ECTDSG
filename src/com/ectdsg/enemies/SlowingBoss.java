package com.ectdsg.enemies;

import com.ectdsg.Path;
import com.ectdsg.TowerDefence;
import com.ectdsg.towers.Tower;
import java.awt.*;
import java.awt.geom.Point2D;

public class SlowingBoss extends BossEnemy {
    private static final int SLOWING_RADIUS = 200;
    private static final double SLOWING_FACTOR = 0.45; // 45% slow

    public SlowingBoss(TowerDefence game, Path path, int bossWaveLevel) {
        super(game, path, bossWaveLevel);
        this.health = 8000 + bossWaveLevel * 1500;
        this.baseSpeed = 0.2 + bossWaveLevel * 0.05;
        this.bounty = 500 + bossWaveLevel * 100;
        this.maxHealth = this.health;
        this.color = new Color(0, 0, 139); // Dark Blue
    }

    @Override
    public void move(double speedMultiplier) {
        super.move(speedMultiplier);
        applySlowingAura();
    }

    private void applySlowingAura() {
        for (Tower tower : game.towers) {
            if (Point2D.distance(getX(), getY(), tower.x, tower.y) <= SLOWING_RADIUS) {
                tower.applySlow(SLOWING_FACTOR);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(this.color);
        g2d.fillOval((int)getX() - 22, (int)getY() - 22, 44, 44);
        g2d.setColor(new Color(0, 0, 255, 50));
        g2d.fillOval((int)getX() - SLOWING_RADIUS, (int)getY() - SLOWING_RADIUS, SLOWING_RADIUS * 2, SLOWING_RADIUS * 2);

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)getX() - 22, (int)getY() - 32, 44, 8);
        g2d.setColor(Color.GREEN);
        double healthPercent = (double)health / maxHealth;
        g2d.fillRect((int)getX() - 22, (int)getY() - 32, (int)(44 * healthPercent), 8);
    }
}
