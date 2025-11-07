package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import java.awt.*;

public class MoneyFarm extends Tower {
    public static final int COST = 150;
    public static final int RANGE = 0;
    private int moneyPerCycle = 25;

    public MoneyFarm(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 0;
        this.fireRate = 3000;
        this.range = RANGE;
        this.color = Color.YELLOW;
        this.cost = COST;
    }

    @Override
    public void attack() {
        long currentTime = System.currentTimeMillis();
        long effectiveCooldown = (long) (fireRate / game.GAME_SPEED_MULTIPLIER);

        if (currentTime - lastShotTime < effectiveCooldown) {
            return;
        }
        game.playerMoney += moneyPerCycle;
        lastShotTime = currentTime;
        game.controlPanel.updateLabels();
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(x - 10, y - 10, 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawString("$", x - 4, y + 5);
    }
}
