package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.IceProjectile;
import java.awt.*;

public class IceTower extends Tower {
    public static final int COST = 600;
    public static final int RANGE = 200;
    private static final int ICE_DAMAGE = 10;
    private static final int FREEZE_CHANCE = 10; // in percent

    public IceTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = ICE_DAMAGE;
        this.fireRate = 1200;
        this.range = RANGE;
        this.color = Color.CYAN;
        this.attackColor = Color.BLUE;
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
            game.projectiles.add(new IceProjectile(x, y, damage, currentTarget, attackColor, FREEZE_CHANCE));
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(new Color(0, 139, 139));
        g2d.fillRect(x - 12, y - 5, 24, 15);
        g2d.setColor(Color.CYAN.brighter());
        g2d.fillOval(x - 8, y - 12, 16, 16);

        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.translate(x, y);
        g2dCopy.rotate(currentAngle);

        g2dCopy.setColor(Color.BLUE);
        g2dCopy.fillRect(-4, -25, 8, 15);

        g2dCopy.dispose();

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }
}
