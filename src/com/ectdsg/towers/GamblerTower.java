package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.Projectile;
import java.awt.*;
import java.util.Random;

public class GamblerTower extends Tower {
    public static final int COST = 500;
    public static final int RANGE = 200;
    private Random rand = new Random();

    public GamblerTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 20;
        this.fireRate = 1000;
        this.range = RANGE;
        this.color = new Color(255, 0, 255); // Magenta
        this.attackColor = new Color(255, 255, 0); // Yellow
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
            int outcome = rand.nextInt(100);
            if (outcome < 10) { // 10% chance of massive damage
                game.projectiles.add(new Projectile(x, y, damage * 5, currentTarget, Color.RED));
            } else if (outcome < 20) { // 10% chance of healing
                game.projectiles.add(new Projectile(x, y, -damage, currentTarget, Color.GREEN));
            } else { // 80% chance of normal damage
                game.projectiles.add(new Projectile(x, y, damage, currentTarget, attackColor));
            }
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(color);
        g2d.fillRect(x - 10, y - 10, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("?", x - 4, y + 5);
    }
}
