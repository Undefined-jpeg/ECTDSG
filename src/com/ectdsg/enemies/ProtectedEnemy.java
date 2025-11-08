package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class ProtectedEnemy extends Enemy {

    public ProtectedEnemy(Path path, int health, double speed) {
        super(path, health, speed, 20, "PROTECTED_ENEMY");
        this.color = new Color(128, 128, 128); // Gray
        this.damageReduction = 20;
    }
}
