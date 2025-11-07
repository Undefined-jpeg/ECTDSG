package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.Projectile;
import java.awt.*;

public class SlowTower extends Tower {
    public static final int COST = 200;
    public static final int RANGE = 180;

    public SlowTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 0;
        this.fireRate = 800;
        this.range = RANGE;
        this.color = Color.blue.darker();
        this.attackColor = Color.CYAN;
        this.cost = COST;
    }

    @Override
    public void attack() {
        long currentTime = System.currentTimeMillis();
        long effectiveCooldown = (long) (getActualFireRate() / game.GAME_SPEED_MULTIPLIER);

        updateTarget();

        if (currentTime - lastShotTime < effectiveCooldown) {
            return;
        }

        if (currentTarget != null) {
            currentTarget.applySlow();
            game.projectiles.add(new Projectile(x, y, 0, currentTarget, attackColor));
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(new Color(224, 255, 255));
        g2d.fillOval(x - 15, y - 15, 30, 30);

        g2d.setColor(new Color(173, 216, 230));
        int[] xPoints = {x, x - 10, x, x + 10};
        int[] yPoints = {y - 10, y, y + 10, y};
        g2d.fillPolygon(xPoints, yPoints, 4);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(x - 2, y - 10, 4, 10);
        g2d.fillRect(x - 1, y - 15, 2, 5);

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }
}
