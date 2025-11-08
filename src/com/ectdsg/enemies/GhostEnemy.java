package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class GhostEnemy extends Enemy {

    public boolean isVisible = true;
    private long lastVisibilityChange = 0;
    private final long VISIBILITY_DURATION = 2000; // 2 seconds

    public GhostEnemy(Path path, int health, double speed) {
        super(path, health, speed, 50, "GHOST_ENEMY");
        this.color = new Color(255, 255, 255, 100); // Semi-transparent white
    }

    @Override
    public void move(double speedMultiplier) {
        super.move(speedMultiplier);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastVisibilityChange > VISIBILITY_DURATION) {
            isVisible = !isVisible;
            lastVisibilityChange = currentTime;
        }
    }
}
