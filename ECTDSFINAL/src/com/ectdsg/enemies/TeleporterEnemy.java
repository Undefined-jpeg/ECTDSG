package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class TeleporterEnemy extends Enemy {

    public TeleporterEnemy(Path path, int health, double speed) {
        super(path, health, speed * 1.2, 30, "TELEPORTER_ENEMY");
        this.color = new Color(255, 51, 255); // Deep Pink
        this.teleportTimer = 10000;
    }
}
