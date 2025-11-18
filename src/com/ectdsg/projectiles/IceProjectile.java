package com.ectdsg.projectiles;

import com.ectdsg.enemies.Enemy;
import java.awt.*;
import java.util.Random;

public class IceProjectile extends Projectile {
    private final int freezeChance;
    private static final Random rand = new Random();

    public IceProjectile(int x, int y, int damage, Enemy target, Color color, int freezeChance) {
        super(x, y, damage, target, color);
        this.freezeChance = freezeChance;
    }

    @Override
    public boolean hasHitTarget() {
        if (super.hasHitTarget()) {
            if (rand.nextInt(100) < freezeChance) {
                target.applyFreeze(2000); // Freeze for 2 seconds
            }
            return true;
        }
        return false;
    }
}
