package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class RitualEnemy extends Enemy {

    public RitualEnemy(Path path, int health, double speed) {
        super(path, health / 2, speed, 25, "RITUAL_ENEMY");
        this.color = new Color(0, 0, 153); // Deep Blue
        this.shieldHitsRemaining = 75;
    }
}
