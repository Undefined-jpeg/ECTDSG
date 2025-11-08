package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class WarperEnemy extends Enemy {

    public WarperEnemy(Path path, int health, double speed) {
        super(path, health, speed * 1.2, 30, "WARPER_ENEMY");
        this.color = new Color(255, 20, 147); // Deep Pink
        this.teleportTimer = 5000;
    }
}
