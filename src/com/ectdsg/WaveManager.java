package com.ectdsg;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.ectdsg.enemies.Enemy;
import com.ectdsg.enemies.BossEnemy;

public class WaveManager {

    private TowerDefence game;

    public WaveManager(TowerDefence game) {
        this.game = game;
    }

    public void spawnWave() {
        game.controlPanel.hideTowerDetails();
        game.selectedTower = null;

        game.waveNumber++;
        game.controlPanel.waveInProgress = true;
        game.controlPanel.startWaveButton.setEnabled(false);
        game.controlPanel.updateLabels();

        if (game.waveNumber % TowerDefence.BOSS_WAVE_FREQUENCY == 0) {
            int bossType = (game.waveNumber / TowerDefence.BOSS_WAVE_FREQUENCY) % 5;
            switch (bossType) {
                case 0:
                    game.enemies.add(new com.ectdsg.enemies.TerryBoss(game, game.path, game.waveNumber / TowerDefence.BOSS_WAVE_FREQUENCY));
                    break;
                case 1:
                    game.enemies.add(new com.ectdsg.enemies.ExtraterrestrialBoss(game, game.path, game.waveNumber / TowerDefence.BOSS_WAVE_FREQUENCY));
                    break;
                case 2:
                    game.enemies.add(new com.ectdsg.enemies.BruteBoss(game, game.path, game.waveNumber / TowerDefence.BOSS_WAVE_FREQUENCY));
                    break;
                case 3:
                    game.enemies.add(new com.ectdsg.enemies.GuardianBoss(game, game.path, game.waveNumber / TowerDefence.BOSS_WAVE_FREQUENCY));
                    break;
                case 4:
                    game.enemies.add(new com.ectdsg.enemies.SlowingBoss(game, game.path, game.waveNumber / TowerDefence.BOSS_WAVE_FREQUENCY));
                    break;
            }

            game.controlPanel.waveTimer = new Timer(1, null);
            game.controlPanel.waveTimer.setRepeats(false);
            game.controlPanel.waveTimer.start();
            return;
        }

        final int enemiesToSpawn = 5 + game.waveNumber * 2;
        final int baseHealth = 100 + game.waveNumber * 15;
        final double baseSpeed = 1.0 + (game.waveNumber * 0.05);

        game.controlPanel.waveTimer = new Timer((int) (500 / game.GAME_SPEED_MULTIPLIER), null);
        game.controlPanel.waveTimer.addActionListener(new ActionListener() {
            int spawned = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (spawned < enemiesToSpawn) {
                    String type = TowerDefence.BASIC_ENEMY;
                    int health = baseHealth;
                    double speed = baseSpeed;
                    int bounty = 10;

                    double rand = Math.random();
                    if (game.waveNumber >= 2 && rand < 0.25) {
                        type = TowerDefence.ARMORED_ENEMY;
                        health = (int)(baseHealth * 1.5);
                        bounty = 15;
                    } else if (game.waveNumber >= 4 && rand > 0.75) {
                         type = TowerDefence.SHIELDED_ENEMY;
                         health = (int)(baseHealth * 1.2);
                         bounty = 12;
                    } else if (game.waveNumber >= 6 && rand > 0.9) {
                         type = TowerDefence.TELEPORTER_ENEMY;
                         health = baseHealth;
                         speed = baseSpeed * 1.5;
                         bounty = 20;
                    }
                    game.enemies.add(new Enemy(game, game.path, health, speed, bounty, type));
                    spawned++;
                } else {
                    game.controlPanel.waveTimer.stop();
                }
            }
        });
        game.controlPanel.waveTimer.start();
    }
}
