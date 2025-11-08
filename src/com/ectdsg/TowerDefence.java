package com.ectdsg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ectdsg.enemies.Enemy;
import com.ectdsg.projectiles.Projectile;
import com.ectdsg.towers.Tower;

public class TowerDefence extends JFrame {

    public static final String NEAREST = "Nearest";
    public static final String FARTHEST = "Farthest";
    public static final String STRONGEST = "Strongest";

    public GamePanel gamePanel;
    public ControlPanel controlPanel;
    private final StartScreenPanel startScreenPanel;
    private final Timer gameLoop;

    public double GAME_SPEED_MULTIPLIER = 1.0;

    public int playerLives = 100;
    public int playerMoney = 750;
    public int waveNumber = 21;

    public String placingTowerType = "NONE";

    public static final String BASIC = "BASIC";
    public static final String SNIPER = "SNIPER";
    public static final String MG = "MG";
    public static final String INFERNO = "INFERNO";
    public static final String LASER = "LASER";
    public static final String MORTAR = "MORTAR";
    public static final String BOMB = "BOMB";
    public static final String SLOW = "SLOW";
    public static final String FARM = "FARM";
    public static final String BEACON = "BEACON";

    public static final String BASIC_ENEMY = "BASIC_ENEMY";
    public static final String ARMORED_ENEMY = "ARMORED_ENEMY";
    public static final String SHIELDED_ENEMY = "SHIELDED_ENEMY";
    public static final String TELEPORTER_ENEMY = "TELEPORTER_ENEMY";
    public static final String BOSS_ENEMY_TYPE = "BOSS";

    public static final int BOSS_WAVE_FREQUENCY = 20;

    public final List<Enemy> enemies = new ArrayList<>();
    public final List<Tower> towers = new ArrayList<>();
    public final List<Projectile> projectiles = new ArrayList<>();

    public Tower selectedTower = null;

    public Path path;
    public WaveManager waveManager;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TowerDefence());
    }

    public TowerDefence() {
        setTitle("ECTDSG - Egors Crappy Tower Defense Simulator Gaem");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int controlPanelWidth = 200;
        int gamePanelHeight = screenSize.height;
        int gamePanelWidth = screenSize.width - controlPanelWidth;

        path = new Path(gamePanelWidth, gamePanelHeight);
        gamePanel = new GamePanel(this, gamePanelWidth, gamePanelHeight);
        controlPanel = new ControlPanel(this, gamePanelHeight);
        waveManager = new WaveManager(this);

        startScreenPanel = new StartScreenPanel(this, gamePanelWidth + controlPanelWidth, gamePanelHeight);

        setLayout(new BorderLayout());
        add(startScreenPanel, BorderLayout.CENTER);

        setUndecorated(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        gameLoop = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                gamePanel.repaint();
            }
        });
    }

    public void startGame() {
        remove(startScreenPanel);

        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        revalidate();
        repaint();

        gameLoop.start();

        controlPanel.updateLabels();
    }

    public double getGameSpeedMultiplier() {
        return GAME_SPEED_MULTIPLIER;
    }

    public void toggleGameSpeed() {
        if (GAME_SPEED_MULTIPLIER == 1.0) {
            GAME_SPEED_MULTIPLIER = 2.0;
            controlPanel.fastForwardButton.setText(">>");
        } else {
            GAME_SPEED_MULTIPLIER = 1.0;
            controlPanel.fastForwardButton.setText(">");
        }
        if (controlPanel.waveTimer != null && controlPanel.waveTimer.isRunning()) {
             controlPanel.waveTimer.setDelay((int) (500 / GAME_SPEED_MULTIPLIER));
        }
    }

    private void updateGame() {
        if (playerLives <= 0) {
            gameLoop.stop();
            JOptionPane.showMessageDialog(this, "GGs! You got to " + waveNumber, "Game over.", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.move(GAME_SPEED_MULTIPLIER);
            if (enemy.hasReachedEnd()) {
                playerLives--;
                enemyIterator.remove();
                controlPanel.updateLabels();
            }
        }

        for (Tower tower : towers) {
            tower.attack();
        }

        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile p = projectileIterator.next();
            p.move(GAME_SPEED_MULTIPLIER);

            if (p.isOutOfBounds(gamePanel.getWidth(), gamePanel.getHeight())) {
                projectileIterator.remove();
                continue;
            }

            if (p.target != null && p.target.isDead()) {
                 projectileIterator.remove();
                 continue;
            }

            if (p.hasHitTarget()) {
                if (p instanceof com.ectdsg.projectiles.MortarProjectile) {
                    ((com.ectdsg.projectiles.MortarProjectile) p).applyExplosionDamage(enemies);
                } else {
                    p.target.takeDamage(p.damage);
                }
                projectileIterator.remove();
            }

            if (p.target != null && p.target.isDead() && enemies.contains(p.target)) {
                 playerMoney += p.target.bounty;
                 enemies.remove(p.target);
                 controlPanel.updateLabels();
            }
        }

        enemies.removeIf(enemy -> {
            if (enemy.isDead()) {
                playerMoney += enemy.bounty;
                controlPanel.updateLabels();
                return true;
            }
            return false;
        });


        if (enemies.isEmpty() && waveNumber > 0 && controlPanel.waveInProgress) {
            controlPanel.waveInProgress = false;
            controlPanel.startWaveButton.setEnabled(true);
            playerMoney += 50 + waveNumber * 10;
            controlPanel.updateLabels();
        }
    }

    public void spawnWave() {
        waveManager.spawnWave();
    }
}
