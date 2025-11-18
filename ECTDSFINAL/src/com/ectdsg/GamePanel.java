package com.ectdsg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import com.ectdsg.towers.*;

public class GamePanel extends JPanel {

    private TowerDefence game;

    public GamePanel(TowerDefence game, int width, int height) {
        this.game = game;
        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(50, 150, 50));

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if (!game.placingTowerType.equals("NONE")) {
                    handleTowerPlacement(x, y);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    handleTowerRightClick(x, y);
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                     game.controlPanel.hideTowerDetails();
                     game.selectedTower = null;
                     repaint();
                }
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void handleTowerPlacement(int x, int y) {
        if (!isLocationValid(x, y)) {
             JOptionPane.showMessageDialog(this, "Cannot place tower on path!", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        Tower newTower = null;
        int cost = 0;

        switch (game.placingTowerType) {
            case TowerDefence.BASIC -> { newTower = new BasicTower(x, y, game); cost = BasicTower.COST; }
            case TowerDefence.SNIPER -> { newTower = new SniperTower(x, y, game); cost = SniperTower.COST; }
            case TowerDefence.MG -> { newTower = new MachineGunTower(x, y, game); cost = MachineGunTower.COST; }
            case TowerDefence.INFERNO -> { newTower = new InfernoTower(x, y, game); cost = InfernoTower.COST; }
            case TowerDefence.LASER -> { newTower = new LaserBeamer(x, y, game); cost = LaserBeamer.COST; }
            case TowerDefence.MORTAR -> { newTower = new MortarTower(x, y, game); cost = MortarTower.COST; }
            case TowerDefence.BOMB -> { newTower = new BombTower(x, y, game); cost = BombTower.COST; }
            case TowerDefence.SLOW -> { newTower = new SlowTower(x, y, game); cost = SlowTower.COST; }
            case TowerDefence.FARM -> { newTower = new MoneyFarm(x, y, game); cost = MoneyFarm.COST; }
            case TowerDefence.BEACON -> { newTower = new BeaconTower(x, y, game); cost = BeaconTower.COST; }
            case TowerDefence.REACTOR -> { newTower = new ReactorCoreTower(x, y, game); cost = ReactorCoreTower.COST; }
            case TowerDefence.GAMBLER -> { newTower = new GamblerTower(x, y, game); cost = GamblerTower.COST; }
            case TowerDefence.DETECTOR -> { newTower = new DetectorTower(x, y, game); cost = DetectorTower.COST; }
            case TowerDefence.CHAIN_LIGHTNING -> { newTower = new ChainLightningTower(x, y, game); cost = ChainLightningTower.COST; }
            case TowerDefence.CONVERTER -> { newTower = new ConverterTower(x, y, game); cost = ConverterTower.COST; }
        }

        if (newTower != null) {
            if (game.playerMoney >= cost) {
                game.towers.add(newTower);
                game.playerMoney -= cost;
            } else {
                JOptionPane.showMessageDialog(this, "Not enough money! Cost: $" + cost, "Error", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        game.placingTowerType = "NONE";
        game.controlPanel.resetButtons();
        game.controlPanel.updateLabels();
        repaint();
    }

    private void handleTowerRightClick(int x, int y) {
        game.selectedTower = null;
        for (Tower tower : game.towers) {
            double distance = Point2D.distance(x, y, tower.x, tower.y);
            if (distance < 15) {
                game.selectedTower = tower;
                break;
            }
        }

        if (game.selectedTower != null) {
             game.controlPanel.showTowerDetails(game.selectedTower);
        } else {
             game.controlPanel.hideTowerDetails();
        }
        repaint();
    }

    private boolean isLocationValid(int x, int y) {
        int pathProximity = 25;

        for (int i = 0; i < game.path.points.size() - 1; i++) {
            Point p1 = game.path.points.get(i);
            Point p2 = game.path.points.get(i+1);

            Rectangle pathRect = new Rectangle(
                Math.min(p1.x, p2.x) - pathProximity,
                Math.min(p1.y, p2.y) - pathProximity,
                Math.abs(p1.x - p2.x) + pathProximity * 2,
                Math.abs(p1.y - p2.y) + pathProximity * 2
            );

            if (pathRect.contains(x, y)) {
                double l2 = Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2);
                if (l2 == 0.0) {
                    if (Point2D.distance(x, y, p1.x, p1.y) < pathProximity) return false;
                } else {
                    double t = ((x - p1.x) * (p2.x - p1.x) + (y - p1.y) * (p2.y - p1.y)) / l2;
                    t = Math.max(0, Math.min(1, t));
                    double closestX = p1.x + t * (p2.x - p1.x);
                    double closestY = p1.y + t * (p2.y - p1.y);
                    if (Point2D.distance(x, y, closestX, closestY) < pathProximity) return false;
                }
            }
        }

        for (Tower tower : game.towers) {
            if (Point2D.distance(x, y, tower.x, tower.y) < 30) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        game.path.draw(g2d);

        for (Tower tower : game.towers) {
            if (tower == game.selectedTower) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(tower.x - 15, tower.y - 15, 30, 30);
                g2d.setStroke(new BasicStroke(1));
            }
            tower.draw(g2d);
        }

        for (com.ectdsg.enemies.Enemy enemy : new java.util.ArrayList<>(game.enemies)) {
            enemy.draw(g2d);
        }

        for (com.ectdsg.projectiles.Projectile p : new java.util.ArrayList<>(game.projectiles)) {
            p.draw(g2d);
        }

        if (!game.placingTowerType.equals("NONE")) {
            Point mousePos = getMousePosition();
            if (mousePos != null) {

                int previewRange = 0;
                Color previewColor = Color.WHITE;
                int towerCost = 0;

                switch (game.placingTowerType) {
                    case TowerDefence.BASIC -> { previewRange = BasicTower.RANGE; previewColor = Color.CYAN; towerCost = BasicTower.COST; }
                    case TowerDefence.SNIPER -> { previewRange = SniperTower.RANGE; previewColor = Color.CYAN.darker(); towerCost = SniperTower.COST; }
                    case TowerDefence.MG -> { previewRange = MachineGunTower.RANGE; previewColor = Color.DARK_GRAY; towerCost = MachineGunTower.COST; }
                    case TowerDefence.INFERNO -> { previewRange = InfernoTower.RANGE; previewColor = Color.ORANGE; towerCost = InfernoTower.COST; }
                    case TowerDefence.LASER -> { previewRange = LaserBeamer.RANGE; previewColor = Color.BLUE; towerCost = LaserBeamer.COST; }
                    case TowerDefence.MORTAR -> { previewRange = MortarTower.RANGE; previewColor = Color.GRAY; towerCost = MortarTower.COST; }
                    case TowerDefence.BOMB -> { previewRange = BombTower.RANGE; previewColor = Color.GRAY; towerCost = BombTower.COST; }
                    case TowerDefence.SLOW -> { previewRange = SlowTower.RANGE; previewColor = Color.MAGENTA; towerCost = SlowTower.COST; }
                    case TowerDefence.FARM -> { previewRange = MoneyFarm.RANGE; previewColor = Color.YELLOW; towerCost = MoneyFarm.COST; }
                    case TowerDefence.BEACON -> { previewRange = BeaconTower.RANGE; previewColor = Color.MAGENTA; towerCost = BeaconTower.COST; }
                    case TowerDefence.REACTOR -> { previewRange = ReactorCoreTower.RANGE; previewColor = Color.GREEN; towerCost = ReactorCoreTower.COST; }
                    case TowerDefence.GAMBLER -> { previewRange = GamblerTower.RANGE; previewColor = Color.MAGENTA; towerCost = GamblerTower.COST; }
                    case TowerDefence.DETECTOR -> { previewRange = DetectorTower.RANGE; previewColor = Color.YELLOW; towerCost = DetectorTower.COST; }
                    case TowerDefence.CHAIN_LIGHTNING -> { previewRange = ChainLightningTower.RANGE; previewColor = Color.CYAN; towerCost = ChainLightningTower.COST; }
                    case TowerDefence.CONVERTER -> { previewRange = ConverterTower.RANGE; previewColor = Color.CYAN; towerCost = ConverterTower.COST; }
                }

                boolean isValid = isLocationValid(mousePos.x, mousePos.y) && game.playerMoney >= towerCost;
                Color fill = isValid ? new Color(0, 255, 0, 80) : new Color(255, 0, 0, 80);
                Color border = isValid ? Color.GREEN : Color.RED;

                g2d.setColor(fill);
                g2d.fillOval(mousePos.x - previewRange, mousePos.y - previewRange, previewRange * 2, previewRange * 2);
                g2d.setColor(border);
                g2d.drawOval(mousePos.x - previewRange, mousePos.y - previewRange, previewRange * 2, previewRange * 2);

                g2d.setColor(previewColor);
                g2d.fillRect(mousePos.x - 10, mousePos.y - 10, 20, 20);
            }
        }
    }
}
