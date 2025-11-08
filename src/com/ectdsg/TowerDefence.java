package com.ectdsg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ectdsg.enemies.*;
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
    public int waveNumber = 0;

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
    public static final String GAMBLER = "GAMBLER";
    public static final String DETECTOR = "DETECTOR";
    public static final String CHAIN_LIGHTNING = "CHAIN_LIGHTNING";
    public static final String CONVERTER = "CONVERTER";

    public static final String BASIC_ENEMY = "BASIC_ENEMY";
    public static final String ARMORED_ENEMY = "ARMORED_ENEMY";
    public static final String SHIELDED_ENEMY = "SHIELDED_ENEMY";
    public static final String TELEPORTER_ENEMY = "TELEPORTER_ENEMY";
    public static final String TANKED_ENEMY = "TANKED_ENEMY";
    public static final String RIOT_ENEMY = "RIOT_ENEMY";
    public static final String PROTECTED_ENEMY = "PROTECTED_ENEMY";
    public static final String RITUAL_ENEMY = "RITUAL_ENEMY";
    public static final String WARPER_ENEMY = "WARPER_ENEMY";
    public static final String HEALER_ENEMY = "HEALER_ENEMY";
    public static final String SPLITTER_ENEMY = "SPLITTER_ENEMY";
    public static final String GHOST_ENEMY = "GHOST_ENEMY";
    public static final String FRIENDLY_ENEMY = "FRIENDLY_ENEMY";
    public static final String BOSS_ENEMY_TYPE = "BOSS";

    public static final int BOSS_WAVE_FREQUENCY = 20;

    public final List<Enemy> enemies = new ArrayList<>();
    public final List<Tower> towers = new ArrayList<>();
    public final List<Projectile> projectiles = new ArrayList<>();

    public Tower selectedTower = null;

    public Path path;
    public WaveManager waveManager;

    private long airstrikeCooldown = 30000; // 30 seconds
    private long freezeCooldown = 20000; // 20 seconds
    private long lastAirstrikeTime = 0;
    private long lastFreezeTime = 0;

    private int gamePanelWidth;
    private int gamePanelHeight;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TowerDefence());
    }

    public TowerDefence() {
        setTitle("ECTDSG - Egors Crappy Tower Defense Simulator Gaem");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int controlPanelWidth = 200;
        gamePanelHeight = screenSize.height;
        gamePanelWidth = screenSize.width - controlPanelWidth;

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
                if (gamePanel != null) {
                    gamePanel.repaint();
                }
            }
        });
    }

    public void setMap(String map) {
        if (map.equals("Map 2")) {
            path = new Path2(gamePanelWidth, gamePanelHeight);
        } else if (map.equals("Map 3")) {
            path = new Path3(gamePanelWidth, gamePanelHeight);
        } else {
            path = new Path(gamePanelWidth, gamePanelHeight);
        }
    }

    public void startGame() {
        if (path == null) {
            setMap("Map 1");
        }
        gamePanel = new GamePanel(this, gamePanelWidth, gamePanelHeight);
        controlPanel = new ControlPanel(this, gamePanelHeight);
        waveManager = new WaveManager(this);

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
            if (enemy instanceof HealerEnemy) {
                ((HealerEnemy) enemy).healNearby(enemies);
            }
            if (enemy instanceof FriendlyEnemy) {
                ((FriendlyEnemy) enemy).attackNearby(enemies);
            }
            if (enemy.hasReachedEnd()) {
                if (!(enemy instanceof FriendlyEnemy)) {
                    playerLives--;
                }
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
                 if (p.target instanceof SplitterEnemy) {
                     ((SplitterEnemy) p.target).split(enemies);
                 }
                 enemies.remove(p.target);
                 controlPanel.updateLabels();
            }
        }

        enemies.removeIf(enemy -> {
            if (enemy.isDead()) {
                if (!(enemy instanceof FriendlyEnemy)) {
                    playerMoney += enemy.bounty;
                }
                if (enemy instanceof SplitterEnemy) {
                    ((SplitterEnemy) enemy).split(enemies);
                }
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

    public void airstrike() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAirstrikeTime > airstrikeCooldown) {
            if (playerMoney >= 1000) {
                playerMoney -= 1000;
                for (Enemy enemy : enemies) {
                    enemy.takeDamage(500);
                }
                lastAirstrikeTime = currentTime;
                controlPanel.updateLabels();
            }
        }
    }

    public void freeze() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFreezeTime > freezeCooldown) {
            if (playerMoney >= 500) {
                playerMoney -= 500;
                for (Enemy enemy : enemies) {
                    enemy.applySlow();
                }
                lastFreezeTime = currentTime;
                controlPanel.updateLabels();
            }
        }
    }
}
