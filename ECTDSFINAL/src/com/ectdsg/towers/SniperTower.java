package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.Projectile;
import java.awt.*;

public class SniperTower extends Tower {
    public static final int COST = 700;
    public static final int RANGE = 4000;

    public SniperTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 200;
        this.fireRate = 1;
        this.range = RANGE;
        this.color = Color.CYAN.darker();
        this.attackColor = Color.CYAN;
        this.cost = COST;
        this.upgradeCost = 500;
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
            game.projectiles.add(new Projectile(x, y, damage, currentTarget, attackColor));
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRect(x - 10, y, 20, 10);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x - 2, y - 25, 4, 25);
        g2d.setColor(this.color);
        g2d.fillOval(x - 7, y - 30, 14, 14);

        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.translate(x, y - 23);
        g2dCopy.rotate(currentAngle);

        g2dCopy.setColor(Color.BLACK);
        g2dCopy.fillRect(-1, -22, 2, 22);

        g2dCopy.dispose();

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.damage += 100;
        this.fireRate -= 500;
        this.upgradeCost *= 2;
    }
}
