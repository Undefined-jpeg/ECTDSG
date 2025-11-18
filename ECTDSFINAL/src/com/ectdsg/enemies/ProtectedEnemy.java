package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class ProtectedEnemy extends Enemy {

    public ProtectedEnemy(Path path, int health, double speed) {
        super(path, health, speed, 20, "PROTECTED_ENEMY");
        this.color = new Color(0, 0, 230); // Bright Blue
        this.shieldHitsRemaining = 25;
    }
}
