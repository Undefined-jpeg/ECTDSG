package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class RiotEnemy extends Enemy {

    public RiotEnemy(Path path, int health, double speed) {
        super(path, health, speed, 12, "RIOT_ENEMY");
        this.color = new Color(64, 64, 64); // Dark Blue
        this.damageReduction = 20;
    }
}
