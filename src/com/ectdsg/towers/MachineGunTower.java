package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.Projectile;
import java.awt.*;

public class MachineGunTower extends Tower {
    public static final int COST = 300;
    public static final int RANGE = 200;

    public MachineGunTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 20;
        this.fireRate = 150;
        this.range = RANGE;
        this.color = Color.DARK_GRAY;
        this.attackColor = Color.LIGHT_GRAY;
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
            game.projectiles.add(new Projectile(x, y, damage, currentTarget, attackColor));
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(this.color.darker());
        g2d.fillRect(x - 12, y - 12, 24, 24);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillOval(x - 8, y - 8, 16, 16);

        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.translate(x, y);
        g2dCopy.rotate(currentAngle);

        g2dCopy.setColor(Color.BLACK);
        g2dCopy.fillRect(-7, -15, 3, 10);
        g2dCopy.fillRect(-1, -18, 3, 13);
        g2dCopy.fillRect(5, -15, 3, 10);

        g2dCopy.dispose();

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }
}
