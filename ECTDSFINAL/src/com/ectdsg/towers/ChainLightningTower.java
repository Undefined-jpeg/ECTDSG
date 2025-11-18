package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.ChainLightningProjectile;
import java.awt.*;

public class ChainLightningTower extends Tower {
    public static final int COST = 600;
    public static final int RANGE = 200;

    public ChainLightningTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 50;
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

        g2d.setColor(new Color(64, 64, 64));
        g2d.fillOval(x - 15, y - 15, 30, 30);
        g2d.setColor(Color.BLUE);
        g2d.fillOval(x - 8, y - 8, 16, 16);

        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.translate(x, y);
        g2dCopy.rotate(currentAngle);

        g2dCopy.setColor(Color.GRAY);
        g2dCopy.fillOval(-10, -10, 20, 20);

        g2dCopy.setColor(Color.DARK_GRAY);
        g2dCopy.fillRect(-15, -5, 10, 10);
        g2dCopy.fillRect(5, -5, 10, 10);

        g2dCopy.setColor(Color.cyan);
        g2dCopy.fillOval(-5, -5, 10, 10);

        g2dCopy.dispose();

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }
}
