package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.ChainLightningProjectile;
import java.awt.*;

public class ChainLightningTower extends Tower {
    public static final int COST = 600;
    public static final int RANGE = 200;

    public ChainLightningTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 30;
        this.fireRate = 1500;
        this.range = RANGE;
        this.color = new Color(0, 191, 255); // Deep Sky Blue
        this.attackColor = new Color(135, 206, 250); // Light Sky Blue
        this.cost = COST;
    }

    @Override
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
            game.projectiles.add(new ChainLightningProjectile(x, y, damage, currentTarget, attackColor, game.enemies));
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(color);
        g2d.fillRect(x - 10, y - 10, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("~", x - 4, y + 5);
    }
}
