package com.ectdsg.towers;

import com.ectdsg.TowerDefence;
import com.ectdsg.projectiles.Projectile;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class GamblerTower extends Tower {
    public static final int COST = 500;
    public static final int RANGE = 200;
    private Random rand = new Random();

    public GamblerTower(int x, int y, TowerDefence game) {
        super(x, y, game);
        this.damage = 20;
        this.fireRate = 1000;
        this.range = RANGE;
        this.color = new Color(255, 0, 255); // Magenta
        this.attackColor = new Color(255, 255, 0); // Yellow
        this.cost = COST;
    }

    @Override
    public void attack() {
        long currentTime = System.currentTimeMillis();
        long actualFireRate = getActualFireRate();

        long effectiveCooldown = (long) (actualFireRate / game.GAME_SPEED_MULTIPLIER);

        if (currentTime - lastShotTime < effectiveCooldown) {
            updateTarget();
            return;
        }

        updateTarget();

        if (currentTarget != null) {
            int outcome = rand.nextInt(100);
            if (outcome < 10) { // 10% chance of massive damage
                game.projectiles.add(new Projectile(x, y, damage * 100, currentTarget, Color.RED));
            } else if (outcome < 20) { // 10% chance of healing
                game.projectiles.add(new Projectile(x, y, -damage, currentTarget, Color.GREEN));
            } else { // 80% chance of normal damage
                game.projectiles.add(new Projectile(x, y, damage, currentTarget, attackColor));
            }
            lastShotTime = currentTime;
        }
    }

    @Override
public void draw(Graphics2D g2d) {
    // 1. Store the original transformation state (CRUCIAL)
    AffineTransform oldTransform = g2d.getTransform();

    // 2. Move the drawing origin to the tower's center
    g2d.translate(x, y);

    // 3. Apply rotation (45 degrees makes the square look like a diamond)
    g2d.rotate(Math.toRadians(45));

    // --- DRAW THE ROTATED BODY (CASINO CHIP / DIAMOND) ---

    // A. Inner Body: Deep Red (A core gambling color)
    g2d.setColor(new Color(175, 0, 0)); 
    g2d.fillRect(-10, -10, 20, 20);

    // B. Outer Border: Gold/Brass
    g2d.setColor(new Color(255, 215, 0)); // Gold
    // Draw a border slightly outside the inner body
    g2d.drawRect(-12, -12, 24, 24);

    // 4. Restore the original transform state 
    // This resets the rotation and translation before drawing the text
    g2d.setTransform(oldTransform);

    // --- DRAW THE QUESTION MARK SYMBOL (UPRIGHT) ---

    // Set a large, bold font for the question mark
    g2d.setFont(new Font("Arial", Font.BOLD, 16));
    g2d.setColor(Color.WHITE);

    // Calculate position to center the text
    String symbol = "🀪";
    FontMetrics fm = g2d.getFontMetrics();
    int textX = x - fm.stringWidth(symbol) / 2;
    int textY = y + fm.getAscent() / 2 - 2; 

    g2d.drawString(symbol, textX, textY);
    
    // Remember to call updateTargetAngle() if it handles turret head rotation!
    updateTargetAngle();
}
}
