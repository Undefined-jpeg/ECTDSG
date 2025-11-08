package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.MortarProjectile;
import java.awt.*;

public class BombTower extends Tower {
    public static final int COST = 400;
    public static final int RANGE = 300;
    private static final int AOE_RANGE = 50;

    public BombTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 150;
        this.fireRate = 1000;
        setTargetingMode(TowerDefence.FARTHEST);
        this.color = Color.GRAY;
        this.attackColor = Color.BLACK;
        this.cost = COST;
        this.upgradeCost = 250;
        setTargetingMode(TowerDefence.FARTHEST);
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
            game.projectiles.add(new MortarProjectile(x, y, damage, currentTarget, attackColor, AOE_RANGE));
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(x - 12, y - 5, 24, 15);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillOval(x - 8, y - 12, 16, 16);

        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.translate(x, y);
        g2dCopy.rotate(currentAngle);

        g2dCopy.setColor(Color.DARK_GRAY);
        g2dCopy.fillRect(-4, -25, 8, 15);

        g2dCopy.dispose();

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.damage += 50;
        this.range += 20;
        this.upgradeCost *= 2;
    }
}
