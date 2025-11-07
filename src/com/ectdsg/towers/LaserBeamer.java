package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import java.awt.*;

public class LaserBeamer extends Tower {
    public static final int COST = 500;
    public static final int RANGE = 300;

    private long visualEndTime = 0;

    public LaserBeamer(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 40;
        this.fireRate = 800;
        this.range = RANGE;
        this.color = Color.BLUE.darker();
        this.attackColor = Color.MAGENTA;
        this.cost = COST;
    }

    @Override
    public void attack() {
        long currentTime = System.currentTimeMillis();
        long effectiveCooldown = (long) (getActualFireRate() / game.GAME_SPEED_MULTIPLIER);

        updateTarget();

        if (currentTime - lastShotTime < effectiveCooldown) {
            if (currentTarget != null) {
                visualEndTime = currentTime + 150;
            }
            return;
        }

        if (currentTarget != null) {
            currentTarget.takeDamage(damage);
            visualEndTime = currentTime + 150;
            lastShotTime = currentTime;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        updateTargetAngle();

        g2d.setColor(new Color(150, 50, 0));
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

        if (currentTarget != null && System.currentTimeMillis() < visualEndTime && !currentTarget.isDead()) {
            g2dCopy.setColor(attackColor);
            g2dCopy.setStroke(new BasicStroke(5));
            g2dCopy.drawLine(0, -25, 0, -35 - (int)(Math.random() * 10));
        }
        g2dCopy.dispose();

        if (game.selectedTower == this) {
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }
}
