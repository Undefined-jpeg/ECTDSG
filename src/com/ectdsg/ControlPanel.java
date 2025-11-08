package com.ectdsg;

import javax.swing.*;
import java.awt.*;
import com.ectdsg.towers.*;

public class ControlPanel extends JPanel {

    private TowerDefence game;

    private JLabel livesLabel;
    private JLabel moneyLabel;
    private JLabel waveLabel;
    public JButton startWaveButton;
    public JButton fastForwardButton;

    public Timer waveTimer;

    private final JButton buildBasicTowerButton;
    private final JButton buildSniperTowerButton;
    private final JButton buildMgTowerButton;
    private final JButton buildInfernoTowerButton;
    private final JButton buildLaserBeamerButton;
    private final JButton buildMortarTowerButton;
    private final JButton buildBombTowerButton;
    private final JButton buildSlowTowerButton;
    private final JButton buildFarmTowerButton;
    private final JButton buildBeaconTowerButton;
    private JButton cancelBuildButton;

    private JPanel towerDetailPanel;
    private JLabel detailNameLabel;
    private JLabel detailSellLabel;
    private JComboBox<String> targetingDropdown;
    private JButton sellButton;

    public boolean waveInProgress = false;

    public ControlPanel(TowerDefence game, int height) {
        this.game = game;
        setPreferredSize(new Dimension(200, height));
        setLayout(new BorderLayout());
        setBackground(new Color(200, 200, 200));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(7, 1, 5, 5));
        topPanel.setBackground(new Color(220, 220, 220));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        livesLabel = new JLabel("Lives: " + game.playerLives);
        livesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        moneyLabel = new JLabel("Money: $" + game.playerMoney);
        moneyLabel.setFont(new Font("Arial", Font.BOLD, 14));
        waveLabel = new JLabel("Wave: " + game.waveNumber);
        waveLabel.setFont(new Font("Arial", Font.BOLD, 14));

        startWaveButton = new JButton("Start Wave");
        startWaveButton.addActionListener(e -> game.spawnWave());

        fastForwardButton = new JButton("Speed: 1x (OFF)");
        fastForwardButton.addActionListener(e -> game.toggleGameSpeed());

        topPanel.add(new JLabel("--- STATUS ---"));
        topPanel.add(livesLabel);
        topPanel.add(moneyLabel);
        topPanel.add(waveLabel);
        topPanel.add(startWaveButton);
        topPanel.add(fastForwardButton);

        JPanel buildPanel = new JPanel();
        buildPanel.setLayout(new GridLayout(14, 1, 5, 5));
        buildPanel.setBackground(new Color(220, 220, 220));

        buildBasicTowerButton = new JButton("TURRET ($" + BasicTower.COST + ")");
        buildBasicTowerButton.setForeground(Color.GREEN.darker());
        buildBasicTowerButton.addActionListener(e -> { if (game.playerMoney >= BasicTower.COST) { game.placingTowerType = TowerDefence.BASIC; toggleBuildButtons(false); } });

        buildSniperTowerButton = new JButton("SNIPER ($" + SniperTower.COST + ")");
        buildSniperTowerButton.setForeground(Color.GREEN.darker());
        buildSniperTowerButton.addActionListener(e -> { if (game.playerMoney >= SniperTower.COST) { game.placingTowerType = TowerDefence.SNIPER; toggleBuildButtons(false); } });

        buildMgTowerButton = new JButton("MINIGUN ($" + MachineGunTower.COST + ")");
        buildMgTowerButton.setForeground(Color.GREEN.darker());
        buildMgTowerButton.addActionListener(e -> { if (game.playerMoney >= MachineGunTower.COST) { game.placingTowerType = TowerDefence.MG; toggleBuildButtons(false); } });

        buildMortarTowerButton = new JButton("MORTAR ($" + MortarTower.COST + ")");
        buildMortarTowerButton.setForeground(Color.GREEN.darker());
        buildMortarTowerButton.addActionListener(e -> { if (game.playerMoney >= MortarTower.COST) { game.placingTowerType = TowerDefence.MORTAR; toggleBuildButtons(false); } });

        buildBombTowerButton = new JButton("BOMB ($" + BombTower.COST + ")");
        buildBombTowerButton.setForeground(Color.GREEN.darker());
        buildBombTowerButton.addActionListener(e -> { if (game.playerMoney >= BombTower.COST) { game.placingTowerType = TowerDefence.BOMB; toggleBuildButtons(false); } });

        buildInfernoTowerButton = new JButton("INFERNO ($" + InfernoTower.COST + ")");
        buildInfernoTowerButton.setForeground(Color.ORANGE.darker());
        buildInfernoTowerButton.addActionListener(e -> { if (game.playerMoney >= InfernoTower.COST) { game.placingTowerType = TowerDefence.INFERNO; toggleBuildButtons(false); } });

        buildLaserBeamerButton = new JButton("LASER BEAM ($" + LaserBeamer.COST + ")");
        buildLaserBeamerButton.setForeground(Color.ORANGE.darker());
        buildLaserBeamerButton.addActionListener(e -> { if (game.playerMoney >= LaserBeamer.COST) { game.placingTowerType = TowerDefence.LASER; toggleBuildButtons(false); } });

        buildSlowTowerButton = new JButton("SLOWER ($" + SlowTower.COST + ")");
        buildSlowTowerButton.setForeground(Color.BLUE);
        buildSlowTowerButton.addActionListener(e -> { if (game.playerMoney >= SlowTower.COST) { game.placingTowerType = TowerDefence.SLOW; toggleBuildButtons(false); } });

        buildFarmTowerButton = new JButton("GOLD MINE ($" + MoneyFarm.COST + ")");
        buildFarmTowerButton.setForeground(Color.BLUE);
        buildFarmTowerButton.addActionListener(e -> { if (game.playerMoney >= MoneyFarm.COST) { game.placingTowerType = TowerDefence.FARM; toggleBuildButtons(false); } });

        buildBeaconTowerButton = new JButton("BEACON ($" + BeaconTower.COST + ")");
        buildBeaconTowerButton.setForeground(Color.BLUE);
        buildBeaconTowerButton.addActionListener(e -> { if (game.playerMoney >= BeaconTower.COST) { game.placingTowerType = TowerDefence.BEACON; toggleBuildButtons(false); } });

        cancelBuildButton = new JButton("Cancel Build");
        cancelBuildButton.addActionListener(e -> resetButtons());
        cancelBuildButton.setEnabled(false);

        buildPanel.add(new JLabel("--- BUILD TOWERS ---"));
        buildPanel.add(buildBasicTowerButton);
        buildPanel.add(buildSniperTowerButton);
        buildPanel.add(buildMgTowerButton);
        buildPanel.add(buildMortarTowerButton);
        buildPanel.add(buildBombTowerButton);
        buildPanel.add(buildInfernoTowerButton);
        buildPanel.add(buildLaserBeamerButton);
        buildPanel.add(buildSlowTowerButton);
        buildPanel.add(buildFarmTowerButton);
        buildPanel.add(buildBeaconTowerButton);
        buildPanel.add(cancelBuildButton);

        towerDetailPanel = new JPanel();
        towerDetailPanel.setLayout(new BoxLayout(towerDetailPanel, BoxLayout.Y_AXIS));
        towerDetailPanel.setBorder(BorderFactory.createTitledBorder("Tower Details"));
        towerDetailPanel.setBackground(new Color(180, 180, 180));
        towerDetailPanel.setVisible(false);

        detailNameLabel = new JLabel("Tower: ");
        detailNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailSellLabel = new JLabel("Sell for: ");
        detailSellLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sellButton = new JButton("SELL");
        sellButton.addActionListener(e -> sellSelectedTower());
        sellButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        targetingDropdown = new JComboBox<>(new String[]{TowerDefence.NEAREST, TowerDefence.FARTHEST, TowerDefence.STRONGEST});
        targetingDropdown.addActionListener(e -> changeTargetingMode());
        targetingDropdown.setMaximumSize(new Dimension(150, 30));

        towerDetailPanel.add(Box.createVerticalStrut(10));
        towerDetailPanel.add(detailNameLabel);
        towerDetailPanel.add(Box.createVerticalStrut(10));
        towerDetailPanel.add(new JLabel("Targeting Priority:"));
        towerDetailPanel.add(targetingDropdown);
        towerDetailPanel.add(Box.createVerticalStrut(10));
        towerDetailPanel.add(detailSellLabel);
        towerDetailPanel.add(sellButton);
        towerDetailPanel.add(Box.createVerticalGlue());

        add(topPanel, BorderLayout.NORTH);
        add(buildPanel, BorderLayout.CENTER);
        add(towerDetailPanel, BorderLayout.SOUTH);
    }

    public void showTowerDetails(Tower tower) {
        if (tower == null) return;

        int sellValue = (int) (tower.getCost() * 0.75);

        detailNameLabel.setText("Tower: " + tower.getClass().getSimpleName());
        detailSellLabel.setText("Sell for: $" + sellValue);
        sellButton.setText("SELL ($" + sellValue + ")");

        targetingDropdown.setSelectedItem(tower.getTargetingMode());

        towerDetailPanel.setVisible(true);

        targetingDropdown.setEnabled(tower.damage > 0 || tower instanceof SlowTower);
    }

    public void hideTowerDetails() {
        towerDetailPanel.setVisible(false);
    }

    private void sellSelectedTower() {
        if (game.selectedTower != null) {
            int sellValue = (int) (game.selectedTower.getCost() * 0.75);
            game.playerMoney += sellValue;
            game.towers.remove(game.selectedTower);
            game.selectedTower = null;
            hideTowerDetails();
            updateLabels();
            game.gamePanel.repaint();
        }
    }

    private void changeTargetingMode() {
        if (game.selectedTower != null && targetingDropdown.isEnabled()) {
            String mode = (String) targetingDropdown.getSelectedItem();
            game.selectedTower.setTargetingMode(mode);
        }
    }

    private void toggleBuildButtons(boolean enableBuild) {
        buildBasicTowerButton.setEnabled(enableBuild);
        buildSniperTowerButton.setEnabled(enableBuild);
        buildMgTowerButton.setEnabled(enableBuild);
        buildMortarTowerButton.setEnabled(enableBuild);
        buildBombTowerButton.setEnabled(enableBuild);
        buildInfernoTowerButton.setEnabled(enableBuild);
        buildLaserBeamerButton.setEnabled(enableBuild);
        buildSlowTowerButton.setEnabled(enableBuild);
        buildFarmTowerButton.setEnabled(enableBuild);
        buildBeaconTowerButton.setEnabled(enableBuild);
        cancelBuildButton.setEnabled(!enableBuild);
    }

    public void resetButtons() {
        game.placingTowerType = "NONE";
        toggleBuildButtons(true);
    }

    public void updateLabels() {
        livesLabel.setText("Lives: " + game.playerLives);

        if (game.waveNumber > 0 && game.waveNumber % TowerDefence.BOSS_WAVE_FREQUENCY == 0 && !game.enemies.isEmpty()) {
             waveLabel.setText("Wave: BOSS (" + game.waveNumber + ")");
        } else {
             waveLabel.setText("Wave: " + game.waveNumber);
        }
        moneyLabel.setText("Money: $" + game.playerMoney);
    }
}
