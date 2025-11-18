package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class ShieldedEnemy extends Enemy {

    public ShieldedEnemy(Path path, int health, double speed) {
        super(path, health, speed, 20, "SHIELDED_ENEMY");
        this.color = new Color(102, 102, 255); // Light Blue
        this.shieldHitsRemaining = 10;
    }
}
