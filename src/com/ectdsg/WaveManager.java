package com.ectdsg;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.ectdsg.enemies.*;

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
            game.enemies.add(new BossEnemy(game.path, game.waveNumber / TowerDefence.BOSS_WAVE_FREQUENCY));

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
                    double rand = Math.random();
                    if (rand < 0.1) {
                        game.enemies.add(new TankedEnemy(game.path, baseHealth, baseSpeed));
                    } else if (rand < 0.2) {
                        game.enemies.add(new RiotEnemy(game.path, baseHealth, baseSpeed));
                    } else if (rand < 0.3) {
                        game.enemies.add(new ProtectedEnemy(game.path, baseHealth, baseSpeed));
                    } else if (rand < 0.4) {
                        game.enemies.add(new RitualEnemy(game.path, baseHealth, baseSpeed));
                    } else if (rand < 0.5) {
                        game.enemies.add(new WarperEnemy(game.path, baseHealth, baseSpeed));
                    } else {
                        game.enemies.add(new Enemy(game.path, baseHealth, baseSpeed, 10, "BASIC_ENEMY"));
                    }
                    spawned++;
                } else {
                    game.controlPanel.waveTimer.stop();
                }
            }
        });
        game.controlPanel.waveTimer.start();
    }
}
