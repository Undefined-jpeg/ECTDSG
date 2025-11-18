package com.ectdsg.projectiles;

import com.ectdsg.enemies.Enemy;
import java.awt.*;

public class FireProjectile extends Projectile {
    private final int fireDamage;
    private final int fireDuration;

    public FireProjectile(int x, int y, int damage, Enemy target, Color color, int fireDamage, int fireDuration) {
        super(x, y, damage, target, color);
        this.fireDamage = fireDamage;
        this.fireDuration = fireDuration;
    }

    @Override
    public boolean hasHitTarget() {
        if (super.hasHitTarget()) {
            target.applyDamageOverTime(fireDamage, fireDuration);
            return true;
        }
        return false;
    }
}
