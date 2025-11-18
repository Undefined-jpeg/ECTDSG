package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class ArmoredEnemy extends Enemy {

    public ArmoredEnemy(Path path, int health, double speed) {
        super(path, health * 2, speed * 0.8, 15, "ARMORED_ENEMY");
        this.color = new Color(179, 179, 179); // Light Gray
        this.damageReduction = 7;
    }
}
