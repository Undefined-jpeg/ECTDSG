package com.ectdsg.projectiles;

import com.ectdsg.enemies.Enemy;
import java.awt.*;

public class PoisonProjectile extends Projectile {
    private final int poisonDamage;
    private final int poisonDuration;

    public PoisonProjectile(int x, int y, int damage, Enemy target, Color color, int poisonDamage, int poisonDuration) {
        super(x, y, damage, target, color);
        this.poisonDamage = poisonDamage;
        this.poisonDuration = poisonDuration;
    }

    @Override
    public boolean hasHitTarget() {
        if (super.hasHitTarget()) {
            target.applyDamageOverTime(poisonDamage, poisonDuration);
            return true;
        }
        return false;
    }
}
