package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.enemies.Enemy;
import com.ectdsg.enemies.FriendlyEnemy;
import java.awt.*;
import java.util.Random;

public class ConverterTower extends Tower {
    public static final int COST = 1000;
    public static final int RANGE = 150;
    private Random rand = new Random();

    public ConverterTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 200;
        this.fireRate = 5000;
        this.range = RANGE;
        this.color = new Color(0, 120, 0); // Cyan
        this.attackColor = new Color(0, 255, 255);
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
            if (currentTarget.health <= damage) {
                if (rand.nextInt(100) < 25) { // 25% chance to convert
                    game.enemies.remove(currentTarget);
                    game.enemies.add(new FriendlyEnemy(game.path, currentTarget.maxHealth, currentTarget.baseSpeed, currentTarget.currentWaypoint));
                } else {
                    currentTarget.takeDamage(damage);
                }
            } else {
                currentTarget.takeDamage(damage);
            }
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.setColor(Color.BLACK);
        int[] xPoints = {-5, 5, 0};
        int[] yPoints = {-15, -15, -25};
        g2dCopy.fillPolygon(xPoints, yPoints, 3);

        g2d.setColor(color);
        g2d.fillRect(x - 10, y - 10, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("㈢", x - 7, y + 5);
    }
}
