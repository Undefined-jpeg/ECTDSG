package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;

public class TankedEnemy extends Enemy {

    public TankedEnemy(Path path, int health, double speed) {
        super(path, health * 2, speed * 0.8, 15, "TANKED_ENEMY");
        this.color = new Color(139, 69, 19); // Brown
    }
}
