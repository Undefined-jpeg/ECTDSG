package com.ectdsg.enemies;

import com.ectdsg.Path;
import java.awt.*;
import java.awt.geom.Point2D;

public class Enemy {
    private Path path;
    public int health;
    protected double baseSpeed;
    public int bounty;
    protected Color color;
    protected String type;

    private double x, y;

    public int currentWaypoint = 0;
    private boolean reachedEnd = false;
    protected int maxHealth;

    public long slowEndTime = 0;
    private final long SLOW_DURATION = 1500;

    protected int damageReduction = 0;
    protected int shieldHitsRemaining = 0;
    protected long teleportTimer = 0;
    private final long TELEPORT_COOLDOWN = 1000;

    public Enemy(Path path, int health, double speed, int bounty, String type) {
        this.path = path;
        this.type = type;
        this.maxHealth = health;
        this.health = health;
        this.baseSpeed = speed;
        this.bounty = bounty;

        if (type.equals("ARMORED_ENEMY")) {
            this.color = new Color(80, 80, 80);
            this.damageReduction = 12;
        }
        else if (type.equals("TANKED_ENEMY")) {
            this.color = new Color(120,120,120);
            this.damageReduction = 25;
        }
        else if (type.equals("RIOT_ENEMY")) {
            this.color = new Color(180, 180, 180);
            this.damageReduction = 50;
        }
        else if (type.equals("SHIELDED_ENEMY")) {
            this.color = new Color(0, 150, 255);
            this.shieldHitsRemaining = 3;
        }
        else if (type.equals("PROTECTED_ENEMY")) {
            this.color = new Color(0, 102, 204);
            this.shieldHitsRemaining = 10;
        }
        else if (type.equals("RITUAL_ENEMY")) {
            this.color = new Color(0, 0, 204);
            this.shieldHitsRemaining = 50;
        }
         else if (type.equals("TELEPORTER_ENEMY")) {
            this.color = new Color(204, 51, 255);
            this.teleportTimer = TELEPORT_COOLDOWN;
        }
        else if (type.equals("WARPER_ENEMY")) {
            this.color = new Color(102, 0, 102);
            this.teleportTimer = TELEPORT_COOLDOWN;
        }
        else if (type.equals("BOSS")) {
            this.color = new Color(75, 0, 130);
        }
        else {
            this.color = Color.RED;
        }

        Point startPoint = path.getPoint(0);
        this.x = startPoint.x;
        this.y = startPoint.y;
        this.currentWaypoint = 1;
    }

    public void move(double speedMultiplier) {
        if (reachedEnd) return;

        double actualSpeed = baseSpeed;
        if (System.currentTimeMillis() < slowEndTime) {
            actualSpeed *= 0.5;
        }
        actualSpeed *= speedMultiplier;

        if (this.type.equals("TELEPORTER_ENEMY")) {
            this.teleportTimer -= (long)(16 * speedMultiplier);
            if (this.teleportTimer <= 0) {
                currentWaypoint = Math.min(currentWaypoint + 3, path.getLength() - 1);
                this.teleportTimer = TELEPORT_COOLDOWN;
            }
        }
        else if (this.type.equals("WARPER_ENEMY")) {
            this.teleportTimer -= (long)(32 * speedMultiplier);
            if (this.teleportTimer <= 0) {
                currentWaypoint = Math.min(currentWaypoint + 5, path.getLength() - 5);
                this.teleportTimer = TELEPORT_COOLDOWN;
            }
        }

        if (currentWaypoint >= path.getLength()) {
             reachedEnd = true;
             return;
        }

        Point targetPoint = path.getPoint(currentWaypoint);

        double dx = targetPoint.x - x;
        double dy = targetPoint.y - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= actualSpeed) {
            x = targetPoint.x;
            y = targetPoint.y;
            currentWaypoint++;
            if (currentWaypoint >= path.getLength()) {
                reachedEnd = true;
            }
        } else {
            x += (dx / distance) * actualSpeed;
            y += (dy / distance) * actualSpeed;
        }
    }

    public void takeDamage(int damage) {
        if (isDead()) return;

        if (this.shieldHitsRemaining > 0) {
            this.shieldHitsRemaining--;
            return;
        }

        int finalDamage = damage;
        if (this.damageReduction > 0) {
             finalDamage = Math.max(0, damage - this.damageReduction);
        }

        health -= finalDamage;
    }

    public void applySlow() {
        this.slowEndTime = System.currentTimeMillis() + SLOW_DURATION;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean hasReachedEnd() {
        return reachedEnd;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public double getDistanceOnWaypoint() {
        if (currentWaypoint == 0) return 0;
        Point p1 = path.getPoint(currentWaypoint - 1);
        return Point2D.distance(p1.x, p1.y, x, y);
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(this.color);
        g2d.fillOval((int)x - 10, (int)y - 10, 20, 20);

        if (this.shieldHitsRemaining > 0) {
            g2d.setColor(new Color(0, 200, 255, 100));
            g2d.fillOval((int)x - 12, (int)y - 12, 24, 24);
        }
        if (this.type.equals("ARMORED_ENEMY")) {
            g2d.setColor(Color.BLACK);
            g2d.drawOval((int)x - 10, (int)y - 10, 20, 20);
        }
        else if (this.type.equals("TANKED_ENEMY")) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawOval((int)x - 10, (int)y - 10, 20, 20);
        }
        else if (this.type.equals("RIOT_ENEMY")) {
            g2d.setColor(Color.GRAY);
            g2d.drawOval((int)x - 10, (int)y - 10, 20, 20);
        }
        if (this.type.equals("TELEPORTER_ENEMY")) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("㊃", (int)x - 4, (int)y + 4);
        }
        else if (this.type.equals("WARPER_ENEMY")) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("Ѱ", (int)x - 4, (int)y + 4);
        }

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)x - 10, (int)y - 18, 20, 4);
        g2d.setColor(Color.GREEN);
        double healthPercent = (double)health / maxHealth;
        g2d.fillRect((int)x - 10, (int)y - 18, (int)(20 * healthPercent), 4);

        if (System.currentTimeMillis() < slowEndTime) {
            g2d.setColor(new Color(0, 150, 255));
            g2d.fillOval((int)x - 12, (int)y - 12, 5, 5);
        }
    }
}
