package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class TerestialEnemy extends Enemy {

    public TerestialEnemy(Path path, int health, double speed) {
        super(path, health, speed * 1.2, 30, "TERESTIAL_ENEMY");
        this.color = new Color(102, 0, 102); // Deep Pink
        this.teleportTimer = 1250;
    }
}
