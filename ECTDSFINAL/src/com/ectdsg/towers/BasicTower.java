package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.Projectile;
import java.awt.*;

public class BasicTower extends Tower {
    public static final int COST = 150;
    public static final int RANGE = 150;

    public BasicTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 25;
        this.fireRate = 1000;
        this.range = RANGE;
        this.color = Color.CYAN;
        this.attackColor = Color.YELLOW;
        this.cost = COST;
        this.upgradeCost = 100;
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

        g2d.setColor(new Color(50, 100, 50).darker());
        g2d.fillRect(x - 12, y - 5, 24, 15);
        g2d.setColor(Color.BLUE.darker());
        g2d.fillOval(x - 8, y - 12, 16, 16);

        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.translate(x, y);
        g2dCopy.rotate(currentAngle);

        g2dCopy.setColor(Color.LIGHT_GRAY);
        g2dCopy.fillRect(-3, -20, 6, 18);
        g2dCopy.setColor(Color.BLACK);
        g2dCopy.fillRect(-3, -22, 6, 2);

        g2dCopy.dispose();

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.damage += 10;
        this.range += 20;
        this.upgradeCost *= 2;
    }
}
