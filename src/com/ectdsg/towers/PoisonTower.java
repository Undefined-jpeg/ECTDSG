package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.PoisonProjectile;
import java.awt.*;

public class PoisonTower extends Tower {
    public static final int COST = 500;
    public static final int RANGE = 250;
    private static final int POISON_DAMAGE = 5;
    private static final int POISON_DURATION = 3;

    public PoisonTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 10;
        this.fireRate = 1000;
        this.range = RANGE;
        this.color = Color.GREEN;
        this.attackColor = Color.GREEN.darker();
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
            game.projectiles.add(new PoisonProjectile(x, y, damage, currentTarget, attackColor, POISON_DAMAGE, POISON_DURATION));
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(new Color(0, 100, 0));
        g2d.fillRect(x - 12, y - 5, 24, 15);
        g2d.setColor(Color.GREEN.brighter());
        g2d.fillOval(x - 8, y - 12, 16, 16);

        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.translate(x, y);
        g2dCopy.rotate(currentAngle);

        g2dCopy.setColor(Color.GREEN.darker());
        g2dCopy.fillRect(-4, -25, 8, 15);

        g2dCopy.dispose();

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }
}
