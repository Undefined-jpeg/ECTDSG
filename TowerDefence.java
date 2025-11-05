package towerdefense;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple single-file Tower Defense Simulator.
 * This class contains all components: the main window, game panel,
 * controls, and game logic classes (Enemy, Tower, Projectile).
 */
@SuppressWarnings("serial")
public class TowerDefence extends JFrame {

    private GamePanel gamePanel;
    private ControlPanel controlPanel;
    private StartScreenPanel startScreenPanel; // NEW: Start Screen
    private Timer gameLoop;
    
    // --- NEW GAME STATE ---
    private double GAME_SPEED_MULTIPLIER = 1.0; 
    // ----------------------

    // Game state variables
    private int playerLives = 100;
    private int playerMoney = 750;
    private int waveNumber = 0;
    
    // Updated tower placing logic: Added Mortar, Slow, Farm, Beacon
    private String placingTowerType = "NONE"; 
    
    // Tower Types
    private static final String BASIC = "BASIC";
    private static final String SNIPER = "SNIPER";
    private static final String MG = "MG";
    private static final String INFERNO = "INFERNO";
    private static final String MORTAR = "MORTAR";
    private static final String SLOW = "SLOW";
    private static final String FARM = "FARM";
    private static final String BEACON = "BEACON"; // NEW
    
    // Enemy Types (for spawning logic)
    private static final String BASIC_ENEMY = "BASIC_ENEMY";
    private static final String ARMORED_ENEMY = "ARMORED_ENEMY";
    private static final String SHIELDED_ENEMY = "SHIELDED_ENEMY";
    private static final String TELEPORTER_ENEMY = "TELEPORTER_ENEMY";
    
    // Game element lists
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Tower> towers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private Tower selectedTower = null; // For sell/upgrade actions

    // Path for enemies (a simple list of waypoints)
    private Path path;

    /**
     * Main method to run the game.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TowerDefence());
    }

    /**
     * Main constructor: Sets up the entire game window.
     * Starts by showing the StartScreenPanel.
     */
    public TowerDefence() {
        setTitle("ECTDSG - Egors Crappy Tower Defense Simulator Gaem");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int controlPanelWidth = 180; // Increased width for more buttons
        int gamePanelHeight = screenSize.height;
        int gamePanelWidth = screenSize.width - controlPanelWidth;

        // Initialize components (but don't add them yet)
        path = new Path(gamePanelWidth, gamePanelHeight);
        gamePanel = new GamePanel(gamePanelWidth, gamePanelHeight);
        controlPanel = new ControlPanel(gamePanelHeight);
        
        // Initialize and show Start Screen first
        startScreenPanel = new StartScreenPanel(gamePanelWidth + controlPanelWidth, gamePanelHeight);

        setLayout(new BorderLayout());
        add(startScreenPanel, BorderLayout.CENTER); // Start with the Start Screen

        setUndecorated(false); 
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Initialize Timer but DO NOT start it yet
        gameLoop = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                gamePanel.repaint();
            }
        });
    }
    
    /** * NEW METHOD: Transitions from the start screen to the main game.
     */
    public void startGame() {
        // 1. Remove the start screen
        remove(startScreenPanel);
        
        // 2. Add the game components
        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        
        // 3. Re-validate and repaint the JFrame to show the new content
        revalidate();
        repaint();
        
        // 4. Start the game loop
        gameLoop.start();
        
        // Ensure labels are up to date
        controlPanel.updateLabels();
    }
    
    public double getGameSpeedMultiplier() {
        return GAME_SPEED_MULTIPLIER;
    }
    
    private void toggleGameSpeed() {
        if (GAME_SPEED_MULTIPLIER == 1.0) {
            GAME_SPEED_MULTIPLIER = 2.0;
            controlPanel.fastForwardButton.setText("Speed: 2x (ON)");
        } else {
            GAME_SPEED_MULTIPLIER = 1.0;
            controlPanel.fastForwardButton.setText("Speed: 1x (OFF)");
        }
        // When speed changes, restart the wave timer immediately to apply speed change
        if (controlPanel.waveTimer != null && controlPanel.waveTimer.isRunning()) {
             controlPanel.waveTimer.setDelay((int) (500 / GAME_SPEED_MULTIPLIER));
        }
    }

    /**
     * The main game logic update method, called by the Timer.
     */
    private void updateGame() {
        if (playerLives <= 0) {
            gameLoop.stop();
            JOptionPane.showMessageDialog(this, "Game Over! You reached wave " + waveNumber, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }

        // 1. Move enemies and check for path end
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.move(GAME_SPEED_MULTIPLIER); // Pass multiplier
            if (enemy.hasReachedEnd()) {
                playerLives--;
                enemyIterator.remove(); 
                controlPanel.updateLabels();
            }
        }

        // 2. Towers find targets and shoot / update special effects
        for (Tower tower : towers) {
            tower.attack();
        }

        // 3. Move projectiles and check for hits
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile p = projectileIterator.next();
            p.move(GAME_SPEED_MULTIPLIER); // Pass multiplier

            if (p.isOutOfBounds()) {
                projectileIterator.remove();
                continue;
            }
            
            // Check if the projectile has hit its target
            if (p.hasHitTarget()) {
                if (p instanceof MortarProjectile) {
                    ((MortarProjectile)p).applyExplosionDamage(enemies);
                } else {
                    p.target.takeDamage(p.damage);
                }
                projectileIterator.remove(); 
            }
            
            // Check if target died due to damage
            if (p.target != null && p.target.isDead() && enemies.contains(p.target)) {
                 playerMoney += p.target.bounty;
                 enemies.remove(p.target);
                 controlPanel.updateLabels();
            }
        }
        
        // Remove dead enemies outside of projectile loop (for constant damage like Inferno)
        enemies.removeIf(enemy -> {
            if (enemy.isDead()) {
                playerMoney += enemy.bounty;
                controlPanel.updateLabels();
                return true;
            }
            return false;
        });


        // 4. Check for wave end
        if (enemies.isEmpty() && waveNumber > 0 && controlPanel.waveInProgress) {
            controlPanel.waveInProgress = false;
            controlPanel.startWaveButton.setEnabled(true);
            playerMoney += 50 + waveNumber * 10; 
            controlPanel.updateLabels();
        }
    }

    /**
     * Spawns a new wave of enemies.
     */
    private void spawnWave() {
        waveNumber++;
        controlPanel.waveInProgress = true;
        controlPanel.startWaveButton.setEnabled(false);
        controlPanel.updateLabels();

        final int enemiesToSpawn = 5 + waveNumber * 2;
        final int baseHealth = 100 + waveNumber * 15;
        final double baseSpeed = 1.0 + (waveNumber * 0.05);

        // Spawn enemies with a slight delay
        controlPanel.waveTimer = new Timer((int) (500 / GAME_SPEED_MULTIPLIER), null); // Speed affected
        controlPanel.waveTimer.addActionListener(new ActionListener() {
            int spawned = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (spawned < enemiesToSpawn) {
                    String type = BASIC_ENEMY;
                    int health = baseHealth;
                    double speed = baseSpeed;
                    int bounty = 10;
                    
                    double rand = Math.random();
                    if (waveNumber >= 2 && rand < 0.25) { 
                        type = ARMORED_ENEMY;
                        health = (int)(baseHealth * 1.5); 
                        bounty = 15;
                    } else if (waveNumber >= 4 && rand > 0.75) {
                         type = SHIELDED_ENEMY;
                         health = (int)(baseHealth * 1.2);
                         bounty = 12;
                    } else if (waveNumber >= 6 && rand > 0.9) {
                         type = TELEPORTER_ENEMY;
                         health = baseHealth; 
                         speed = baseSpeed * 1.5;
                         bounty = 20;
                    }

                    enemies.add(new Enemy(path, health, speed, bounty, type));
                    spawned++;
                } else {
                    controlPanel.waveTimer.stop();
                }
            }
        });
        controlPanel.waveTimer.start();
    }

    /**
     * NEW CLASS: Represents the starting screen panel.
     */
    class StartScreenPanel extends JPanel {
        public StartScreenPanel(int width, int height) {
            setPreferredSize(new Dimension(width, height));
            setBackground(new Color(40, 40, 40)); 
            setLayout(new GridBagLayout()); 
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 15, 15, 15);
            gbc.gridy = 0;

            // Title Label
            JLabel title = new JLabel("Egors Crappy TDS gaem");
            title.setFont(new Font("Monospaced", Font.BOLD, 48));
            title.setForeground(new Color(50, 200, 255)); 
            add(title, gbc);

            // Instructions/Info Label
            gbc.gridy++;
            JLabel info = new JLabel("ts game sucks y r u playing");
            info.setFont(new Font("Arial", Font.ITALIC, 16));
            info.setForeground(Color.LIGHT_GRAY);
            add(info, gbc);
            
            // Start Button
            gbc.gridy++;
            JButton startButton = new JButton("Start");
            startButton.setFont(new Font("Arial", Font.BOLD, 30));
            startButton.setBackground(new Color(0, 150, 0));
            startButton.setForeground(Color.WHITE);
            startButton.setFocusPainted(false);
            startButton.setBorder(BorderFactory.createRaisedBevelBorder());
            // Action listener calls the main startGame method in the outer class
            startButton.addActionListener(e -> {
                TowerDefence.this.startGame();
            });
            add(startButton, gbc);
        }
    }


    /**
     * Represents the game area where everything is drawn.
     */
    class GamePanel extends JPanel {
        public GamePanel(int width, int height) {
            setPreferredSize(new Dimension(width, height));
            setBackground(new Color(50, 150, 50)); 

            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();

                    if (!placingTowerType.equals("NONE")) {
                        handleTowerPlacement(x, y);
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        // Right-click to sell tower
                        handleTowerRightClick(x, y, e);
                    }
                }
            };
            addMouseListener(mouseHandler);
        }
        
        /** Handles tower placement logic. */
        private void handleTowerPlacement(int x, int y) {
            if (!isLocationValid(x, y)) {
                 JOptionPane.showMessageDialog(this, "Cannot place tower on path!", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            Tower newTower = null;
            int cost = 0;
            
            switch (placingTowerType) {
                case BASIC -> { newTower = new BasicTower(x, y); cost = BasicTower.COST; }
                case SNIPER -> { newTower = new SniperTower(x, y); cost = SniperTower.COST; }
                case MG -> { newTower = new MachineGunTower(x, y); cost = MachineGunTower.COST; }
                case INFERNO -> { newTower = new InfernoTower(x, y); cost = InfernoTower.COST; }
                case MORTAR -> { newTower = new MortarTower(x, y); cost = MortarTower.COST; }
                case SLOW -> { newTower = new SlowTower(x, y); cost = SlowTower.COST; }
                case FARM -> { newTower = new MoneyFarm(x, y); cost = MoneyFarm.COST; }
                case BEACON -> { newTower = new BeaconTower(x, y); cost = BeaconTower.COST; }
            }

            if (newTower != null) {
                if (playerMoney >= cost) {
                    towers.add(newTower);
                    playerMoney -= cost;
                } else {
                    JOptionPane.showMessageDialog(this, "Not enough money! Cost: $" + cost, "Error", JOptionPane.ERROR_MESSAGE);
                    return; 
                }
            }

            // Reset placement mode
            placingTowerType = "NONE";
            controlPanel.resetButtons();
            controlPanel.updateLabels();
        }
        
        /** Handles right-click to select and sell tower. */
        private void handleTowerRightClick(int x, int y, MouseEvent e) {
            selectedTower = null;
            for (Tower tower : towers) {
                double distance = Point2D.distance(x, y, tower.x, tower.y);
                if (distance < 15) { // Tower size is around 10-15
                    selectedTower = tower;
                    break;
                }
            }

            if (selectedTower != null) {
                JPopupMenu popup = new JPopupMenu();
                
                // Sell Option
                int sellValue = (int) (selectedTower.getCost() * 0.75);
                JMenuItem sellItem = new JMenuItem("Sell ($" + sellValue + ")");
                sellItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        playerMoney += sellValue;
                        towers.remove(selectedTower);
                        selectedTower = null;
                        controlPanel.updateLabels();
                        repaint();
                    }
                });
                popup.add(sellItem);
                
                // Show info (simplified)
                JMenuItem infoItem = new JMenuItem(selectedTower.getClass().getSimpleName() + " - Dmg: " + selectedTower.damage);
                infoItem.setEnabled(false);
                popup.add(infoItem);
                
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        private boolean isLocationValid(int x, int y) {
            int pathProximity = 25; 
            
            for (int i = 0; i < path.points.size() - 1; i++) {
                Point p1 = path.points.get(i);
                Point p2 = path.points.get(i+1);
                
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
            return true; 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // 1. Draw the path
            path.draw(g2d);

            // 2. Draw Towers and their range
            for (Tower tower : towers) {
                tower.draw(g2d);
            }
            
            // Highlight selected tower
            if (selectedTower != null) {
                g2d.setColor(Color.YELLOW);
                g2d.drawRect(selectedTower.x - 15, selectedTower.y - 15, 30, 30);
            }

            // 3. Draw Enemies
            for (Enemy enemy : new ArrayList<>(enemies)) {
                enemy.draw(g2d);
            }

            // 4. Draw Projectiles
            for (Projectile p : new ArrayList<>(projectiles)) {
                p.draw(g2d);
            }

            // 5. Draw tower placement preview
            if (!placingTowerType.equals("NONE")) {
                Point mousePos = getMousePosition();
                if (mousePos != null) {
                    
                    int previewRange = 0;
                    Color previewColor = Color.WHITE;
                    int towerCost = 0;
                    
                    switch (placingTowerType) {
                        case BASIC -> { previewRange = BasicTower.RANGE; previewColor = Color.CYAN; towerCost = BasicTower.COST; }
                        case SNIPER -> { previewRange = SniperTower.RANGE; previewColor = Color.CYAN.darker(); towerCost = SniperTower.COST; }
                        case MG -> { previewRange = MachineGunTower.RANGE; previewColor = Color.DARK_GRAY; towerCost = MachineGunTower.COST; }
                        case INFERNO -> { previewRange = InfernoTower.RANGE; previewColor = Color.ORANGE; towerCost = InfernoTower.COST; }
                        case MORTAR -> { previewRange = MortarTower.RANGE; previewColor = Color.GRAY; towerCost = MortarTower.COST; }
                        case SLOW -> { previewRange = SlowTower.RANGE; previewColor = Color.MAGENTA; towerCost = SlowTower.COST; }
                        case FARM -> { previewRange = MoneyFarm.RANGE; previewColor = Color.YELLOW; towerCost = MoneyFarm.COST; }
                        case BEACON -> { previewRange = BeaconTower.RANGE; previewColor = Color.MAGENTA; towerCost = BeaconTower.COST; }
                    }
                    
                    boolean isValid = isLocationValid(mousePos.x, mousePos.y) && playerMoney >= towerCost;
                    Color fill = isValid ? new Color(0, 255, 0, 80) : new Color(255, 0, 0, 80);
                    Color border = isValid ? Color.GREEN : Color.RED;

                    // Draw range preview
                    g2d.setColor(fill);
                    g2d.fillOval(mousePos.x - previewRange, mousePos.y - previewRange, previewRange * 2, previewRange * 2);
                    g2d.setColor(border);
                    g2d.drawOval(mousePos.x - previewRange, mousePos.y - previewRange, previewRange * 2, previewRange * 2);
                    
                    // Draw tower preview (simple fill)
                    g2d.setColor(previewColor);
                    g2d.fillRect(mousePos.x - 10, mousePos.y - 10, 20, 20);
                }
            }
        }
    }

    /** Represents the control panel with buttons and labels. */
    class ControlPanel extends JPanel {
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
        private final JButton buildMortarTowerButton; 
        private final JButton buildSlowTowerButton;   
        private final JButton buildFarmTowerButton;   
        private final JButton buildBeaconTowerButton; 
        
        private JButton cancelBuildButton;
        
        public boolean waveInProgress = false;

        public ControlPanel(int height) {
            setPreferredSize(new Dimension(180, height));
            setLayout(new GridLayout(20, 1, 5, 5)); 
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setBackground(new Color(200, 200, 200));

            // --- Labels ---
            livesLabel = new JLabel("Lives: " + playerLives);
            livesLabel.setFont(new Font("Arial", Font.BOLD, 14));
            moneyLabel = new JLabel("Money: $" + playerMoney);
            moneyLabel.setFont(new Font("Arial", Font.BOLD, 14));
            waveLabel = new JLabel("Wave: " + waveNumber);
            waveLabel.setFont(new Font("Arial", Font.BOLD, 14));

            // --- Utility Buttons ---
            startWaveButton = new JButton("Start Wave");
            startWaveButton.addActionListener(e -> spawnWave());
            
            fastForwardButton = new JButton("Speed: 1x (OFF)");
            fastForwardButton.addActionListener(e -> toggleGameSpeed());

            // --- Build Tower Buttons ---
            buildBasicTowerButton = new JButton("TURRET ($" + BasicTower.COST + ")");
            buildBasicTowerButton.addActionListener(e -> { if (playerMoney >= BasicTower.COST) { placingTowerType = BASIC; toggleBuildButtons(false); } });
            
            buildSniperTowerButton = new JButton("SNIPER ($" + SniperTower.COST + ")");
            buildSniperTowerButton.addActionListener(e -> { if (playerMoney >= SniperTower.COST) { placingTowerType = SNIPER; toggleBuildButtons(false); } });
            
            buildMgTowerButton = new JButton("MINIGUN ($" + MachineGunTower.COST + ")");
            buildMgTowerButton.addActionListener(e -> { if (playerMoney >= MachineGunTower.COST) { placingTowerType = MG; toggleBuildButtons(false); } });

            buildInfernoTowerButton = new JButton("INFERNO ($" + InfernoTower.COST + ")");
            buildInfernoTowerButton.addActionListener(e -> { if (playerMoney >= InfernoTower.COST) { placingTowerType = INFERNO; toggleBuildButtons(false); } });

            buildMortarTowerButton = new JButton("MORTAR ($" + MortarTower.COST + ")");
            buildMortarTowerButton.addActionListener(e -> { if (playerMoney >= MortarTower.COST) { placingTowerType = MORTAR; toggleBuildButtons(false); } });
            
            buildSlowTowerButton = new JButton("SLOWER ($" + SlowTower.COST + ")");
            buildSlowTowerButton.addActionListener(e -> { if (playerMoney >= SlowTower.COST) { placingTowerType = SLOW; toggleBuildButtons(false); } });
            
            buildFarmTowerButton = new JButton("GOLD MINE ($" + MoneyFarm.COST + ")");
            buildFarmTowerButton.addActionListener(e -> { if (playerMoney >= MoneyFarm.COST) { placingTowerType = FARM; toggleBuildButtons(false); } });
            
            buildBeaconTowerButton = new JButton("BEACON ($" + BeaconTower.COST + ")");
            buildBeaconTowerButton.addActionListener(e -> { if (playerMoney >= BeaconTower.COST) { placingTowerType = BEACON; toggleBuildButtons(false); } });

            cancelBuildButton = new JButton("Cancel Build");
            cancelBuildButton.addActionListener(e -> resetButtons());
            cancelBuildButton.setEnabled(false); 
            
            // Add components to panel
            add(new JLabel("--- STATUS ---"));
            add(livesLabel);
            add(moneyLabel);
            add(waveLabel);
            add(new JSeparator());
            add(startWaveButton);
            add(fastForwardButton);
            add(new JSeparator());
            add(new JLabel("--- TOWERS ---"));
            add(buildBasicTowerButton);
            add(buildSniperTowerButton);
            add(buildMgTowerButton);
            add(buildInfernoTowerButton);
            add(buildMortarTowerButton);
            add(buildSlowTowerButton);
            add(buildFarmTowerButton);
            add(buildBeaconTowerButton);
            add(cancelBuildButton);
        }

        /** Disables/Enables build buttons and cancel button */
        private void toggleBuildButtons(boolean enableBuild) {
            buildBasicTowerButton.setEnabled(enableBuild);
            buildSniperTowerButton.setEnabled(enableBuild);
            buildMgTowerButton.setEnabled(enableBuild);
            buildInfernoTowerButton.setEnabled(enableBuild);
            buildMortarTowerButton.setEnabled(enableBuild);
            buildSlowTowerButton.setEnabled(enableBuild);
            buildFarmTowerButton.setEnabled(enableBuild);
            buildBeaconTowerButton.setEnabled(enableBuild);
            cancelBuildButton.setEnabled(!enableBuild);
        }
        
        /** Resets buttons to default state */
        public void resetButtons() {
            placingTowerType = "NONE";
            toggleBuildButtons(true);
        }

        /** Updates the text on the status labels. */
        public void updateLabels() {
            livesLabel.setText("Lives: " + playerLives);
            moneyLabel.setText("Money: $" + playerMoney);
            waveLabel.setText("Wave: " + waveNumber);
        }
    }

    /** Defines the path enemies follow. */
    class Path {
        List<Point> points = new ArrayList<>();
        
        public Path(int width, int height) {
            // Simplified Path (similar to user's relative path logic)
            addPoint(0, (int)(height * 0.15)); 
            addPoint((int)(width * 0.58), (int)(height * 0.15));
            addPoint((int)(width * 0.58), (int)(height * 0.45));
            addPoint((int)(width * 0.15), (int)(height * 0.45));
            addPoint((int)(width * 0.15), (int)(height * 0.75));
            addPoint(width, (int)(height * 0.75));
        }

        public void addPoint(int x, int y) {
            points.add(new Point(x, y));
        }

        public Point getPoint(int index) {
            return points.get(index);
        }

        public int getLength() {
            return points.size();
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(139, 69, 19, 150)); 
            g2d.setStroke(new BasicStroke(30, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); 
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            g2d.setStroke(new BasicStroke(1));
        }
    }

    /** Represents an Enemy unit, enhanced to handle new types. */
    class Enemy {
        private Path path;
        protected int health;
        protected double baseSpeed; 
        protected int bounty;
        protected Color color;
        protected String type;
        
        private double x, y;
        protected int currentWaypoint = 0;
        private boolean reachedEnd = false;
        protected int maxHealth;
        
        // --- NEW ENEMY STATE ---
        public long slowEndTime = 0; 
        private final long SLOW_DURATION = 1000;
        
        protected int damageReduction = 0; // For Armored
        protected int shieldHitsRemaining = 0; // For Shielded
        protected long teleportTimer = 0; // For Teleporter
        private final long TELEPORT_COOLDOWN = 10000;
        // -----------------------

        public Enemy(Path path, int health, double speed, int bounty, String type) {
            this.path = path;
            this.type = type;
            this.maxHealth = health;
            this.health = health;
            this.baseSpeed = speed; 
            this.bounty = bounty;
            
            // Set properties based on type
            if (type.equals(ARMORED_ENEMY)) {
                this.color = new Color(80, 80, 80); 
                this.damageReduction = 10;
            } else if (type.equals(SHIELDED_ENEMY)) {
                this.color = new Color(0, 150, 255); 
                this.shieldHitsRemaining = 3;
            } else if (type.equals(TELEPORTER_ENEMY)) {
                this.color = new Color(255, 140, 0); 
                this.teleportTimer = TELEPORT_COOLDOWN;
            } else {
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

            // --- Teleporter Logic ---
            if (this.type.equals(TELEPORTER_ENEMY)) {
                this.teleportTimer -= (long)(16 * speedMultiplier);
                if (this.teleportTimer <= 0) {
                    currentWaypoint = Math.min(currentWaypoint + 3, path.getLength());
                    this.teleportTimer = TELEPORT_COOLDOWN; 
                }
            }
            // --------------------------

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
            
            // Shield logic
            if (this.shieldHitsRemaining > 0) {
                this.shieldHitsRemaining--;
                return; 
            }
            
            int finalDamage = damage;
            // Armored logic
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

        public void draw(Graphics2D g2d) {
            // Body
            g2d.setColor(this.color);
            g2d.fillOval((int)x - 10, (int)y - 10, 20, 20);
            
            // Shield indicator
            if (this.shieldHitsRemaining > 0) {
                g2d.setColor(new Color(0, 200, 255, 100));
                g2d.fillOval((int)x - 12, (int)y - 12, 24, 24);
            }
            // Armor indicator
            if (this.type.equals(ARMORED_ENEMY)) {
                g2d.setColor(Color.BLACK);
                g2d.drawOval((int)x - 10, (int)y - 10, 20, 20);
            }
            // Teleporter indicator
            if (this.type.equals(TELEPORTER_ENEMY)) {
                g2d.setColor(Color.WHITE);
                g2d.drawString("T", (int)x - 4, (int)y + 4);
            }
            
            // Health bar
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect((int)x - 10, (int)y - 18, 20, 4);
            g2d.setColor(Color.GREEN);
            double healthPercent = (double)health / maxHealth;
            g2d.fillRect((int)x - 10, (int)y - 18, (int)(20 * healthPercent), 4);
            
            // Draw slow indicator
            if (System.currentTimeMillis() < slowEndTime) {
                g2d.setColor(new Color(0, 150, 255)); // Cyan
                g2d.fillOval((int)x - 12, (int)y - 12, 5, 5);
            }
        }
    }

    /** Represents a base Tower class. */
    abstract class Tower {
        protected int x, y;
        protected int damage;
        protected long fireRate; 
        protected long lastShotTime = 0;
        protected int range;
        protected Color color;
        protected Color attackColor;
        protected int cost; // Added cost for selling logic

        public Tower(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public int getCost() {
            return cost;
        }

        /** Gets the effective fire rate (time between shots) factoring in Beacons. */
        public long getActualFireRate() {
            double multiplier = 1.0;
            for (Tower t : towers) {
                if (t instanceof BeaconTower) {
                    BeaconTower beacon = (BeaconTower) t;
                    double distance = Point2D.distance(x, y, beacon.x, beacon.y);
                    if (distance <= beacon.range) {
                        multiplier += beacon.getBuff();
                    }
                }
            }
            // fireRate is in milliseconds, so increasing multiplier decreases time between shots.
            return (long)(fireRate / multiplier);
        }

        public void attack() {
            long currentTime = System.currentTimeMillis();
            long actualFireRate = getActualFireRate();
            
            // Apply game speed multiplier to the effective fire rate
            long effectiveCooldown = (long) (actualFireRate / GAME_SPEED_MULTIPLIER);
            
            if (currentTime - lastShotTime < effectiveCooldown) {
                return; 
            }

            Enemy target = findTarget();
            if (target != null) {
                projectiles.add(new Projectile(x, y, damage, target, attackColor));
                lastShotTime = currentTime;
            }
        }

        protected Enemy findTarget() {
            Enemy closestEnemy = null;
            double minDistance = Double.MAX_VALUE;

            for (Enemy enemy : enemies) {
                if (enemy.isDead()) continue;
                
                double distance = Point2D.distance(enemy.getX(), enemy.getY(), x, y);

                if (distance <= this.range && distance < minDistance) {
                    minDistance = distance;
                    closestEnemy = enemy;
                }
            }
            return closestEnemy;
        }
        
        protected Enemy findFarthestOnPath() {
            Enemy farthestEnemy = null;
            int maxWaypointIndex = -1;

            for (Enemy enemy : enemies) {
                if (enemy.isDead()) continue;
                
                double distance = Point2D.distance(enemy.getX(), enemy.getY(), x, y);

                if (distance <= this.range) {
                    if (enemy.currentWaypoint > maxWaypointIndex) {
                        maxWaypointIndex = enemy.currentWaypoint;
                        farthestEnemy = enemy;
                    }
                }
            }
            return farthestEnemy;
        }

        public abstract void draw(Graphics2D g2d);
    }

    /** Basic Cannon Tower. */
    class BasicTower extends Tower {
        public static final int COST = 25;
        public static final int RANGE = 150; 

        public BasicTower(int x, int y) {
            super(x, y);
            this.damage = 25;
            this.fireRate = 1250; 
            this.range = RANGE;
            this.color = Color.CYAN;
            this.attackColor = Color.YELLOW;
            this.cost = COST;
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(50, 100, 50).darker());
            g2d.fillRect(x - 12, y - 5, 24, 15);
            g2d.setColor(Color.BLUE.darker());
            g2d.fillOval(x - 8, y - 12, 16, 16);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(x - 3, y - 20, 6, 18);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(x - 3, y - 22, 6, 2);
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }

    /** Long-range, high-damage Sniper Tower. */
    class SniperTower extends Tower {
        public static final int COST = 100;
        public static final int RANGE = 1200;

        public SniperTower(int x, int y) {
            super(x, y);
            this.damage = 100;
            this.fireRate = 2000; 
            this.range = RANGE;
            this.color = Color.CYAN.darker();
            this.attackColor = Color.CYAN;
            this.cost = COST;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(200, 200, 200)); 
            g2d.fillRect(x - 10, y, 20, 10);
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(x - 2, y - 25, 4, 25); 
            g2d.setColor(this.color);
            g2d.fillOval(x - 7, y - 30, 14, 14);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(x - 1, y - 45, 2, 15);
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50)); 
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }
    
    /** Fast-firing Machine Gun Tower. */
    class MachineGunTower extends Tower {
        public static final int COST = 75;
        public static final int RANGE = 100; 

        public MachineGunTower(int x, int y) {
            super(x, y);
            this.damage = 20;
            this.fireRate = 150; 
            this.range = RANGE;
            this.color = Color.DARK_GRAY;
            this.attackColor = Color.LIGHT_GRAY;
            this.cost = COST;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(this.color.darker());
            g2d.fillRect(x - 12, y - 12, 24, 24);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(x - 8, y - 8, 16, 16);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(x - 7, y - 15, 3, 10);
            g2d.fillRect(x, y - 18, 3, 13);
            g2d.fillRect(x + 4, y - 15, 3, 10);
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50)); 
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }

    /** Continuous-damage Inferno Tower. */
    class InfernoTower extends Tower {
        public static final int COST = 150;
        public static final int RANGE = 120; 

        private Enemy currentAttackTarget = null;
        private long visualEndTime = 0; 

        public InfernoTower(int x, int y) {
            super(x, y);
            this.damage = 5; 
            this.fireRate = 100;
            this.range = RANGE;
            this.color = Color.ORANGE;
            this.attackColor = Color.YELLOW; 
            this.cost = COST;
        }
        
        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (getActualFireRate() / GAME_SPEED_MULTIPLIER);

            if (currentTime - lastShotTime < effectiveCooldown) {
                return; 
            }
            
            Enemy target = findTarget();
            if (target != null) {
                target.takeDamage(damage);
                currentAttackTarget = target;
                visualEndTime = currentTime + 150;
                lastShotTime = currentTime;
            } else {
                currentAttackTarget = null;
            }
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(150, 50, 0)); 
            g2d.fillOval(x - 15, y - 15, 30, 30); 
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(x - 8, y - 8, 16, 16);
            g2d.setColor(Color.BLACK);
            int[] xPoints = {x - 5, x + 5, x};
            int[] yPoints = {y - 15, y - 15, y - 25};
            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50)); 
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);

            // Draw flame visual if attacking
            if (currentAttackTarget != null && System.currentTimeMillis() < visualEndTime && !currentAttackTarget.isDead()) {
                Graphics2D g2dCopy = (Graphics2D) g2d.create();
                g2dCopy.setColor(attackColor);
                g2dCopy.setStroke(new BasicStroke(5));
                g2dCopy.drawLine(x, y, (int)currentAttackTarget.getX(), (int)currentAttackTarget.getY());
                g2dCopy.dispose();
            }
        }
    }

    /** Mortar Tower: Area of Effect. */
    class MortarTower extends Tower {
        public static final int COST = 120;
        public static final int RANGE = 200; 
        private static final int AOE_RANGE = 50; 

        public MortarTower(int x, int y) {
            super(x, y);
            this.damage = 40;
            this.fireRate = 2500; 
            this.range = RANGE;
            this.color = Color.GRAY;
            this.attackColor = Color.BLACK;
            this.cost = COST;
        }

        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (getActualFireRate() / GAME_SPEED_MULTIPLIER);
            if (currentTime - lastShotTime < effectiveCooldown) {
                return; 
            }

            Enemy target = findFarthestOnPath();
            if (target != null) {
                projectiles.add(new MortarProjectile(x, y, damage, target, attackColor, AOE_RANGE)); 
                lastShotTime = currentTime;
            }
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(50, 50, 50)); 
            g2d.fillRect(x - 12, y - 5, 24, 15);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(x - 8, y - 12, 16, 16);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawLine(x, y, x - 10, y - 25);
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }
    
    /** Slow Tower: Applies a slow debuff. */
    class SlowTower extends Tower {
        public static final int COST = 80;
        public static final int RANGE = 180; 

        public SlowTower(int x, int y) {
            super(x, y);
            this.damage = 0; 
            this.fireRate = 1500; 
            this.range = RANGE;
            this.color = Color.MAGENTA;
            this.attackColor = Color.CYAN;
            this.cost = COST;
        }
        
        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (getActualFireRate() / GAME_SPEED_MULTIPLIER);

            if (currentTime - lastShotTime < effectiveCooldown) {
                return; 
            }
            
            Enemy target = findTarget();
            if (target != null) {
                target.applySlow();
                projectiles.add(new Projectile(x, y, 0, target, attackColor)); // Visual only
                lastShotTime = currentTime;
            }
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.PINK);
            g2d.fillOval(x - 10, y - 10, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawString("S", x - 4, y + 4);
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
            g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }

    /** Money Farm: Generates money passively. */
    class MoneyFarm extends Tower {
        public static final int COST = 150;
        public static final int RANGE = 0; 
        private int moneyPerCycle = 25;

        public MoneyFarm(int x, int y) {
            super(x, y);
            this.damage = 0;
            this.fireRate = 3000;
            this.range = RANGE;
            this.color = Color.YELLOW;
            this.cost = COST;
        }

        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (fireRate / GAME_SPEED_MULTIPLIER);

            if (currentTime - lastShotTime < effectiveCooldown) {
                return; 
            }
            playerMoney += moneyPerCycle;
            lastShotTime = currentTime;
            controlPanel.updateLabels();
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(x - 10, y - 10, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString("$", x - 4, y + 5);
        }
    }
    
    /** Beacon Tower: Buffs nearby towers' fire rate. */
    class BeaconTower extends Tower {
        public static final int COST = 200;
        public static final int RANGE = 150; 
        private double buffMultiplier = 0.25; 

        public BeaconTower(int x, int y) {
            super(x, y);
            this.damage = 0;
            this.fireRate = 100000; 
            this.range = RANGE;
            this.color = Color.MAGENTA;
            this.cost = COST;
        }

        @Override
        public void attack() { /* Passive buffing, no attack required */ }
        
        public double getBuff() { return buffMultiplier; }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.MAGENTA.darker());
            g2d.fillOval(x - 15, y - 15, 30, 30);
            g2d.setColor(Color.WHITE);
            g2d.drawString("B", x - 5, y + 5);
            
            // Draw persistent range circle to indicate buff area
            g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 30));
            g2d.fillOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
        }
    }


    /** Represents a Projectile fired by a Tower. */
    class Projectile {
        protected double x, y;
        protected int damage;
        protected Enemy target;
        protected Color color;
        protected double speed = 15.0;

        public Projectile(int x, int y, int damage, Enemy target, Color color) {
            this.x = x;
            this.y = y;
            this.damage = damage;
            this.target = target;
            this.color = color;
        }

        public void move(double speedMultiplier) {
            if (target.isDead()) return;
            
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            double moveDistance = speed * speedMultiplier;

            if (distance > moveDistance) {
                x += (dx / distance) * moveDistance;
                y += (dy / distance) * moveDistance;
            } else {
                x = target.getX();
                y = target.getY();
            }
        }

        public boolean hasHitTarget() {
            if (target.isDead()) return false;
            
            double distance = Point2D.distance(x, y, target.getX(), target.getY());
            return distance < 5; // A small radius for collision
        }

        public boolean isOutOfBounds() {
            return x < 0 || x > gamePanel.getWidth() || y < 0 || y > gamePanel.getHeight();
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int)x - 3, (int)y - 3, 6, 6);
        }
    }
    
    /** Projectile with Area of Effect damage. */
    class MortarProjectile extends Projectile {
        private final int aoeRange;

        public MortarProjectile(int x, int y, int damage, Enemy target, Color color, int aoeRange) {
            super(x, y, damage, target, color);
            this.speed = 10.0; // Slower speed
            this.aoeRange = aoeRange;
        }
        
        public void applyExplosionDamage(List<Enemy> allEnemies) {
            for (Enemy enemy : allEnemies) {
                if (enemy.isDead()) continue;
                double distance = Point2D.distance(x, y, enemy.getX(), enemy.getY());
                
                if (distance <= aoeRange) {
                    // Damage falls off based on distance, but keep it simple for now
                    enemy.takeDamage(damage); 
                }
            }
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int)x - 5, (int)y - 5, 10, 10);
            
            // Draw a temporary explosion ring on impact for visual
            if (Point2D.distance(x, y, target.getX(), target.getY()) < 10) {
                 g2d.setColor(new Color(255, 165, 0, 150));
                 g2d.drawOval((int)x - aoeRange, (int)y - aoeRange, aoeRange * 2, aoeRange * 2);
            }
        }
    }
}
