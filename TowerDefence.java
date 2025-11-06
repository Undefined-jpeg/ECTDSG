package towerdefense;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator; 
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

/**
 Ts is a single file TDS gaem made by egor for """""educational purposes""""" (yk when you see 'educationl purposes' you ARE redeemed to find a sigma trollface edit ts tuff).
 this version features A boss (his name is terry)
 2x speed and a detailed tower info panel.
 and also uh, targeting modes for towers.
 yea enjoy coding and not touching grass for once.


    -------THIS IS VERSION 1.2 -------
    -----YES I SKIPPED A VERSION------
 --BUT GENUINELY WHO CARES ABOUT VERSIONING--
 

 ------------------------------------ made by egor/undefined/nftc/sortl or however u know me ---- but i lowk use egor
 ---------only yogur and kos can call me sortl :victory_pose: ----------
 */

public class TowerDefence extends JFrame {

    // --- NEW: TARGETING MODES ---
    public static final String NEAREST = "Nearest";
    public static final String FARTHEST = "Farthest";
    public static final String STRONGEST = "Strongest"; // Targets highest current HP
    // -----------------------

    private GamePanel gamePanel;
    private final ControlPanel controlPanel;
    private final StartScreenPanel startScreenPanel; 
    private final Timer gameLoop;
    
    // --- GAME STATE ---
    private double GAME_SPEED_MULTIPLIER = 1.0; 
    // ----------------------

    // Game state variables
    private int playerLives = 100;
    private int playerMoney = 750;
    private int waveNumber = 0;
    
    // Tower placing logic
    private String placingTowerType = "NONE"; 
    
    // Tower Types
    private static final String BASIC = "BASIC";
    private static final String SNIPER = "SNIPER";
    private static final String MG = "MG";
    private static final String INFERNO = "INFERNO";
    private static final String LASER = "LASER";
    private static final String MORTAR = "MORTAR";
    private static final String BOMB = "BOMB";
    private static final String SLOW = "SLOW";
    private static final String FARM = "FARM";
    private static final String BEACON = "BEACON"; 
    
    // Enemy Types (for spawning logic)
    private static final String BASIC_ENEMY = "BASIC_ENEMY";
    private static final String ARMORED_ENEMY = "ARMORED_ENEMY";
    private static final String SHIELDED_ENEMY = "SHIELDED_ENEMY";
    private static final String TELEPORTER_ENEMY = "TELEPORTER_ENEMY";
    private static final String BOSS_ENEMY_TYPE = "BOSS"; // NEW BOSS TYPE (his name is terry)

    // Boss configuration
    private static final int BOSS_WAVE_FREQUENCY = 20; // Boss Spawns every "X" waves (Like how much you set it to)
    
    // Game element lists
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Tower> towers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    
    // This now holds the tower selected by right-click for detail panel updates
    private Tower selectedTower = null; 

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
        int controlPanelWidth = 200; // Increased width for detail panel
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

        // Initialize Timer but DONT. DO NOT start it yet
        gameLoop = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                gamePanel.repaint();
            }
        });
    }
    
    /** * Transitions from start screen to the game
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

        //And yeh its done.
    }
    
    public double getGameSpeedMultiplier() {
        return GAME_SPEED_MULTIPLIER;
    }
    
    private void toggleGameSpeed() {
        if (GAME_SPEED_MULTIPLIER == 1.0) {
            GAME_SPEED_MULTIPLIER = 2.0;
            controlPanel.fastForwardButton.setText(">>");
        } else {
            GAME_SPEED_MULTIPLIER = 1.0;
            controlPanel.fastForwardButton.setText(">");
        }
        // When speed changes, restart the wave timer immediately to apply speed change
        if (controlPanel.waveTimer != null && controlPanel.waveTimer.isRunning()) {
             controlPanel.waveTimer.setDelay((int) (500 / GAME_SPEED_MULTIPLIER));
        }
    }

    /**
     * The main game logic update method, timer calls ts every tick.
     */
    private void updateGame() {
        if (playerLives <= 0) {
            gameLoop.stop();
            JOptionPane.showMessageDialog(this, "GGs! You got to " + waveNumber, "Game over.", JOptionPane.INFORMATION_MESSAGE);
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
            
            // Check if projectile target is dead 
            if (p.target != null && p.target.isDead()) {
                 projectileIterator.remove(); // remove projectile if dead
                 continue;
            }
            
            if (p.hasHitTarget()) {
                if (p instanceof MortarProjectile mortarProjectile) {
                    mortarProjectile.applyExplosionDamage(enemies);
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
        
        // Remove dead enemies outside of projectile loop (usually used for Inferno and Laser)
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
     * Spawns a new wave of enemies, including a boss every "X" wave.
     */
    private void spawnWave() {

        controlPanel.hideTowerDetails();
        selectedTower = null;
        
        waveNumber++;
        controlPanel.waveInProgress = true;
        controlPanel.startWaveButton.setEnabled(false);
        controlPanel.updateLabels();
        
        // --- BOSS WAVE CHECK ---
        if (waveNumber % BOSS_WAVE_FREQUENCY == 0) {
            // Only spawn the boss on boss waves
            enemies.add(new BossEnemy(path, waveNumber / BOSS_WAVE_FREQUENCY));
            
            // Use a dummy timer to satisfy existing logic until wave is cleared
            controlPanel.waveTimer = new Timer(1, null); 
            controlPanel.waveTimer.setRepeats(false); 
            controlPanel.waveTimer.start(); 
            return;
        }
        // --- END BOSS WAVE CHECK ---

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
                        // Right-click to select tower and show details
                        handleTowerRightClick(x, y);
                    } else if (e.getButton() == MouseEvent.BUTTON1) {
                        // Left-click to deselect if not placing
                         controlPanel.hideTowerDetails();
                         selectedTower = null;
                         repaint();
                    }
                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler); // For placement preview
        }
        
        /** Handles tower placement logic. */
        private void handleTowerPlacement(int x, int y) {
            if (!isLocationValid(x, y)) {
                 // Using JOptionPane as we are in a Swing application
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
                case LASER -> { newTower = new LaserBeamer(x, y); cost = LaserBeamer.COST; }
                case MORTAR -> { newTower = new MortarTower(x, y); cost = MortarTower.COST; }
                case BOMB -> { newTower = new BombTower(x, y); cost = BombTower.COST; }
                case SLOW -> { newTower = new SlowTower(x, y); cost = SlowTower.COST; }
                case FARM -> { newTower = new MoneyFarm(x, y); cost = MoneyFarm.COST; }
                case BEACON -> { newTower = new BeaconTower(x, y); cost = BeaconTower.COST; }
            }

            if (newTower != null) {
                if (playerMoney >= cost) {
                    towers.add(newTower);
                    playerMoney -= cost;
                } else {
                    JOptionPane.showMessageDialog(this, "Not enough money! Cost: $" + cost, "Error", JOptionPane.INFORMATION_MESSAGE);
                    return; 
                }
            }

            // Reset placement mode
            placingTowerType = "NONE";
            controlPanel.resetButtons();
            controlPanel.updateLabels();
            repaint();
        }
        
        /** Handles right-click to select a tower and show its details in the control panel. */
        private void handleTowerRightClick(int x, int y) {
            selectedTower = null;
            for (Tower tower : towers) {
                double distance = Point2D.distance(x, y, tower.x, tower.y);
                if (distance < 15) { // Tower size is around 10-15
                    selectedTower = tower;
                    break;
                }
            }

            if (selectedTower != null) {
                 controlPanel.showTowerDetails(selectedTower);
            } else {
                 controlPanel.hideTowerDetails();
            }
            repaint();
        }

        private boolean isLocationValid(int x, int y) {
            int pathProximity = 25; 
            
            // Check proximity to path
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
            
            // NEW: Check proximity to other towers
            for (Tower tower : towers) {
                if (Point2D.distance(x, y, tower.x, tower.y) < 30) { // 30px buffer
                    return false;
                }
            }
            
            return true; 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            // NEW: Enable antialiasing for smoother shapes
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Draw the path
            path.draw(g2d);

            // 2. Draw Towers and their range
            for (Tower tower : towers) {
                // NEW: Draw a colored ring around the selected tower
                if (tower == selectedTower) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawOval(tower.x - 15, tower.y - 15, 30, 30);
                    g2d.setStroke(new BasicStroke(1));
                }
                tower.draw(g2d);
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
                        case LASER -> { previewRange = LaserBeamer.RANGE; previewColor = Color.BLUE; towerCost = LaserBeamer.COST; }
                        case MORTAR -> { previewRange = MortarTower.RANGE; previewColor = Color.GRAY; towerCost = MortarTower.COST; }
                        case BOMB -> { previewRange = BombTower.RANGE; previewColor = Color.GRAY; towerCost = BombTower.COST; }
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
        private final JButton buildLaserBeamerButton; 
        private final JButton buildMortarTowerButton; 
        private final JButton buildBombTowerButton; 
        private final JButton buildSlowTowerButton;   
        private final JButton buildFarmTowerButton;   
        private final JButton buildBeaconTowerButton; 
        private JButton cancelBuildButton;
        
        // --- NEW: Tower Details Panel Components ---
        private JPanel towerDetailPanel;
        private JLabel detailNameLabel;
        private JLabel detailSellLabel;
        private JComboBox<String> targetingDropdown;
        private JButton sellButton;
        
        public boolean waveInProgress = false;

        public ControlPanel(int height) {
            setPreferredSize(new Dimension(200, height)); // Increased width
            setLayout(new BorderLayout()); 
            setBackground(new Color(200, 200, 200));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // --- Top Panel (Status & Utility) ---
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new GridLayout(7, 1, 5, 5));
            topPanel.setBackground(new Color(220, 220, 220));
            topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

            livesLabel = new JLabel("Lives: " + playerLives);
            livesLabel.setFont(new Font("Arial", Font.BOLD, 14));
            moneyLabel = new JLabel("Money: $" + playerMoney);
            moneyLabel.setFont(new Font("Arial", Font.BOLD, 14));
            waveLabel = new JLabel("Wave: " + waveNumber);
            waveLabel.setFont(new Font("Arial", Font.BOLD, 14));

            startWaveButton = new JButton("Start Wave");
            startWaveButton.addActionListener(e -> spawnWave());
            
            fastForwardButton = new JButton("Speed: 1x (OFF)");
            fastForwardButton.addActionListener(e -> toggleGameSpeed());

            topPanel.add(new JLabel("--- STATUS ---"));
            topPanel.add(livesLabel);
            topPanel.add(moneyLabel);
            topPanel.add(waveLabel);
            topPanel.add(startWaveButton);
            topPanel.add(fastForwardButton);
            
            // --- Center Panel (Build Buttons) ---
            JPanel buildPanel = new JPanel();
            buildPanel.setLayout(new GridLayout(14, 1, 5, 5));
            buildPanel.setBackground(new Color(220, 220, 220));
            
            buildBasicTowerButton = new JButton("TURRET ($" + BasicTower.COST + ")");
            buildBasicTowerButton.addActionListener(e -> { if (playerMoney >= BasicTower.COST) { placingTowerType = BASIC; toggleBuildButtons(false); } });
            
            buildSniperTowerButton = new JButton("SNIPER ($" + SniperTower.COST + ")");
            buildSniperTowerButton.addActionListener(e -> { if (playerMoney >= SniperTower.COST) { placingTowerType = SNIPER; toggleBuildButtons(false); } });
            
            buildMgTowerButton = new JButton("MINIGUN ($" + MachineGunTower.COST + ")");
            buildMgTowerButton.addActionListener(e -> { if (playerMoney >= MachineGunTower.COST) { placingTowerType = MG; toggleBuildButtons(false); } });

            buildInfernoTowerButton = new JButton("INFERNO ($" + InfernoTower.COST + ")");
            buildInfernoTowerButton.addActionListener(e -> { if (playerMoney >= InfernoTower.COST) { placingTowerType = INFERNO; toggleBuildButtons(false); } });

            buildLaserBeamerButton = new JButton("LASER BEAM ($" + LaserBeamer.COST + ")");
            buildLaserBeamerButton.addActionListener(e -> { if (playerMoney >= LaserBeamer.COST) { placingTowerType = LASER; toggleBuildButtons(false); } });

            buildMortarTowerButton = new JButton("MORTAR ($" + MortarTower.COST + ")");
            buildMortarTowerButton.addActionListener(e -> { if (playerMoney >= MortarTower.COST) { placingTowerType = MORTAR; toggleBuildButtons(false); } });
            
            buildBombTowerButton = new JButton("BOMB ($" + BombTower.COST + ")");
            buildBombTowerButton.addActionListener(e -> { if (playerMoney >= BombTower.COST) { placingTowerType = BOMB; toggleBuildButtons(false); } });
            
            buildSlowTowerButton = new JButton("SLOWER ($" + SlowTower.COST + ")");
            buildSlowTowerButton.addActionListener(e -> { if (playerMoney >= SlowTower.COST) { placingTowerType = SLOW; toggleBuildButtons(false); } });
            
            buildFarmTowerButton = new JButton("GOLD MINE ($" + MoneyFarm.COST + ")");
            buildFarmTowerButton.addActionListener(e -> { if (playerMoney >= MoneyFarm.COST) { placingTowerType = FARM; toggleBuildButtons(false); } });
            
            buildBeaconTowerButton = new JButton("BEACON ($" + BeaconTower.COST + ")");
            buildBeaconTowerButton.addActionListener(e -> { if (playerMoney >= BeaconTower.COST) { placingTowerType = BEACON; toggleBuildButtons(false); } });

            cancelBuildButton = new JButton("Cancel Build");
            cancelBuildButton.addActionListener(e -> resetButtons());
            cancelBuildButton.setEnabled(false); 
            
            buildPanel.add(new JLabel("--- BUILD TOWERS ---"));
            buildPanel.add(buildBasicTowerButton);
            buildPanel.add(buildSniperTowerButton);
            buildPanel.add(buildMgTowerButton);
            buildPanel.add(buildInfernoTowerButton);
            buildPanel.add(buildLaserBeamerButton);
            buildPanel.add(buildMortarTowerButton);
            buildPanel.add(buildBombTowerButton);
            buildPanel.add(buildSlowTowerButton);
            buildPanel.add(buildFarmTowerButton);
            buildPanel.add(buildBeaconTowerButton);
            buildPanel.add(cancelBuildButton);
            
            // --- NEW: Bottom Panel (Tower Details) ---
            towerDetailPanel = new JPanel();
            towerDetailPanel.setLayout(new BoxLayout(towerDetailPanel, BoxLayout.Y_AXIS));
            towerDetailPanel.setBorder(BorderFactory.createTitledBorder("Tower Details"));
            towerDetailPanel.setBackground(new Color(180, 180, 180));
            towerDetailPanel.setVisible(false); // Hidden by default
            
            detailNameLabel = new JLabel("Tower: ");
            detailNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            detailSellLabel = new JLabel("Sell for: ");
            detailSellLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            sellButton = new JButton("SELL");
            sellButton.addActionListener(e -> sellSelectedTower());
            sellButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Targeting Dropdown
            targetingDropdown = new JComboBox<>(new String[]{NEAREST, FARTHEST, STRONGEST});
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

            // Add all panels to the control panel layout
            add(topPanel, BorderLayout.NORTH);
            add(buildPanel, BorderLayout.CENTER);
            add(towerDetailPanel, BorderLayout.SOUTH);
        }
        
        /** NEW: Shows the detail panel for the selected tower. */
        public void showTowerDetails(Tower tower) {
            if (tower == null) return;
            
            int sellValue = (int) (tower.getCost() * 0.75);
            
            detailNameLabel.setText("Tower: " + tower.getClass().getSimpleName());
            detailSellLabel.setText("Sell for: $" + sellValue);
            sellButton.setText("SELL ($" + sellValue + ")");
            
            // Set the dropdown to the tower's current targeting mode
            targetingDropdown.setSelectedItem(tower.getTargetingMode());
            
            towerDetailPanel.setVisible(true);
            
            // Disable targeting for income/utility towers
            targetingDropdown.setEnabled(tower.damage > 0 || tower instanceof SlowTower);
        }
        
        /** NEW: Hides the detail panel. */
        public void hideTowerDetails() {
            towerDetailPanel.setVisible(false);
        }
        
        /** NEW: Sells the currently selected tower. */
        private void sellSelectedTower() {
            if (selectedTower != null) {
                int sellValue = (int) (selectedTower.getCost() * 0.75);
                playerMoney += sellValue;
                towers.remove(selectedTower);
                selectedTower = null;
                hideTowerDetails();
                updateLabels();
                gamePanel.repaint();
            }
        }
        
        /** NEW: Changes the targeting mode for the selected tower. */
        private void changeTargetingMode() {
            if (selectedTower != null && targetingDropdown.isEnabled()) {
                String mode = (String) targetingDropdown.getSelectedItem();
                selectedTower.setTargetingMode(mode);
            }
        }


        /** Disables/Enables build buttons and cancel button */
        private void toggleBuildButtons(boolean enableBuild) {
            buildBasicTowerButton.setEnabled(enableBuild);
            buildSniperTowerButton.setEnabled(enableBuild);
            buildMgTowerButton.setEnabled(enableBuild);
            buildInfernoTowerButton.setEnabled(enableBuild);
            buildLaserBeamerButton.setEnabled(enableBuild);
            buildMortarTowerButton.setEnabled(enableBuild);
            buildBombTowerButton.setEnabled(enableBuild);
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
            
            // NEW: Highlight boss wave for player awareness
            if (waveNumber > 0 && waveNumber % BOSS_WAVE_FREQUENCY == 0 && !enemies.isEmpty()) {
                 waveLabel.setText("Wave: BOSS (" + waveNumber + ")");
            } else {
                 waveLabel.setText("Wave: " + waveNumber);
            }
            moneyLabel.setText("Money: $" + playerMoney);
        }
    }

    /** Defines the path enemies follow. */
    final class Path {
        List<Point> points = new ArrayList<>();
        
        public Path(int width, int height) {
            // Start at the top-left corner
            addPoint(0, (int)(height * 0.10)); // Start at the top-left
        
            // Zigzag pattern across the screen
            addPoint((int)(width * 0.20), (int)(height * 0.10)); // Move right
            addPoint((int)(width * 0.20), (int)(height * 0.30)); // Move down
            addPoint((int)(width * 0.40), (int)(height * 0.30)); // Move right
            addPoint((int)(width * 0.40), (int)(height * 0.50)); // Move down
            addPoint((int)(width * 0.60), (int)(height * 0.50)); // Move right
            addPoint((int)(width * 0.60), (int)(height * 0.70)); // Move down
            addPoint((int)(width * 0.80), (int)(height * 0.70)); // Move right
            addPoint((int)(width * 0.80), (int)(height * 0.90)); // Move down
            addPoint(width, (int)(height * 0.90)); // Move to the far-right edge
        }

        public void addPoint(int x, int y) {
            points.add(new Point(x, y));
        }

        public Point getPoint(int index) {
            if (index < 0 || index >= points.size()) {
                return points.get(points.size() - 1); // Return last point if out of bounds
            }
            return points.get(index);
        }

        public int getLength() {
            return points.size();
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(163, 163, 163)); 
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

        // Removed duplicate method definition
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
            } else if (type.equals(BOSS_ENEMY_TYPE)) {
                this.color = new Color(75, 0, 130); // Boss color, overwritten in subclass
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
                    currentWaypoint = Math.min(currentWaypoint + 3, path.getLength() - 1);
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
        
        /** Helper for Farthest targeting: returns distance traveled *on the current waypoint* */
        public double getDistanceOnWaypoint() {
            if (currentWaypoint == 0) return 0;
            Point p1 = path.getPoint(currentWaypoint - 1);
            return Point2D.distance(p1.x, p1.y, x, y);
        }

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
    
    /** * NEW CLASS: Boss Enemy. 
     * Huge HP, very slow, high damage resistance. 
     */
    class BossEnemy extends Enemy {
        private static final int BOSS_DRAW_SIZE = 45; // Larger drawing size

        public BossEnemy(Path path, int bossWaveLevel) {
            // --- CUSTOMIZATION POINT ---
            // Base Health: 1500 + 500 per boss wave
            // Base Speed: 0.3 + 0.05 per boss wave (VERY SLOW)
            // Base Bounty: 300 + 50 per boss wave (HUGE REWARD)
            super(path, 
                  5000 + bossWaveLevel * 500, 
                  0.3 + bossWaveLevel * 0.05, 
                  300 + bossWaveLevel * 50,    
                  BOSS_ENEMY_TYPE); 

            this.maxHealth = this.health;
            this.color = new Color(75, 0, 130); // Dark Purple
        }

        @Override
        public void takeDamage(int damage) {
            if (isDead()) return;
            
            // --- CUSTOMIZATION POINT ---
            // Boss takes 60% of damage, effectively giving it 40% resistance
            double resistanceMultiplier = 0.60; 
            
            int finalDamage = (int) (damage * resistanceMultiplier);
            
            this.health -= Math.max(1, finalDamage);
        }

        @Override
        public void draw(Graphics2D g2d) {
            // Draw a much larger body
            g2d.setColor(this.color);
            g2d.fillOval((int)getX() - BOSS_DRAW_SIZE / 2, (int)getY() - BOSS_DRAW_SIZE / 2, BOSS_DRAW_SIZE, BOSS_DRAW_SIZE);
            
            // Draw a distinct pattern
            g2d.setColor(Color.RED.darker());
            g2d.drawRect((int)getX() - BOSS_DRAW_SIZE / 2, (int)getY() - BOSS_DRAW_SIZE / 2, BOSS_DRAW_SIZE, BOSS_DRAW_SIZE);
            // Draw BOSS text
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.setColor(Color.WHITE);
            g2d.drawString("TERRY", (int)getX() - 16, (int)getY() + 5);

            // Health bar (placed above the unit)
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect((int)getX() - BOSS_DRAW_SIZE / 2, (int)getY() - BOSS_DRAW_SIZE / 2 - 15, BOSS_DRAW_SIZE, 8);
            g2d.setColor(Color.RED);
            double healthPercent = (double)health / maxHealth;
            g2d.fillRect((int)getX() - BOSS_DRAW_SIZE / 2, (int)getY() - BOSS_DRAW_SIZE / 2 - 15, (int)(BOSS_DRAW_SIZE * healthPercent), 8);
        
            // Draw slow indicator (if applicable)
            if (System.currentTimeMillis() < slowEndTime) {
                g2d.setColor(new Color(0, 150, 255)); // Cyan
                g2d.fillOval((int)getX() - BOSS_DRAW_SIZE / 2 - 5, (int)getY() - BOSS_DRAW_SIZE / 2 - 5, 10, 10);
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
        
        // --- NEW: TARGETING AND ROTATION ---
        private String targetingMode = NEAREST; // Default

        // Removed duplicate method definition
        protected Enemy currentTarget = null;
        protected double currentAngle = 0.0; // Angle in radians
        // ----------------------------------
        
        public Tower(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        // --- NEW: Targeting Mode Getters/Setters ---
        public void setTargetingMode(String mode) {
            this.targetingMode = mode;
        }
        
        public String getTargetingMode() {
            return targetingMode;
        }
        // -----------------------------------------
        
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
                // NEW: Update target even if not firing, for rotation
                updateTarget();
                return; 
            }

            // Find a new target if current is null or dead
            updateTarget(); 
            
            if (currentTarget != null) {
                // Fire projectile
                projectiles.add(new Projectile(x, y, damage, currentTarget, attackColor));
                lastShotTime = currentTime;
            }
        }
        
        /** Ts updates the target angle so that the turret looks at the enemy. ALWAYS SET IT TO NEGATIVE OR ELSE it will look backwards**/
        protected void updateTargetAngle() {
            if (currentTarget != null && !currentTarget.isDead()) {
                double angle = Math.atan2(currentTarget.getY() - y, currentTarget.getX() - x);
                this.currentAngle = angle - -(Math.PI / 2);
            } else {
                currentTarget = null; // Clear target if it's dead or null
            }
        }
        
        // Target finder logic
        protected void updateTarget() {
             // Only find a new target if the current one is dead or out of range
            if (currentTarget == null || currentTarget.isDead() || 
                Point2D.distance(currentTarget.getX(), currentTarget.getY(), x, y) > this.range) {
                
                this.currentTarget = findTarget();
            }
        }

        // Fİnd tarhet uses targeting mode to find target
        protected Enemy findTarget() {
            // Filter enemies that are in range and not dead
            List<Enemy> inRangeEnemies = enemies.stream()
                .filter(enemy -> !enemy.isDead() && Point2D.distance(enemy.getX(), enemy.getY(), x, y) <= this.range)
                .toList();

            if (inRangeEnemies.isEmpty()) {
                return null;
            }
            
            // Select target based on targeting mode
            Optional<Enemy> target;
            switch (targetingMode) {
                case FARTHEST:
                    // Farthest down the path (highest currentWaypoint, then farthest distance on that waypoint)
                    target = inRangeEnemies.stream()
                        .max(Comparator.comparingInt((Enemy e) -> e.currentWaypoint)
                                     .thenComparingDouble(e -> e.getDistanceOnWaypoint()));
                    break;
                case STRONGEST:
                    // Highest current HP
                    target = inRangeEnemies.stream()
                        .max(Comparator.comparingInt(e -> e.health));
                    break;
                case NEAREST:
                default:
                    // Closest distance to the tower
                    target = inRangeEnemies.stream()
                        .min(Comparator.comparingDouble(e -> Point2D.distance(e.getX(), e.getY(), x, y)));
                    break;
            }
            return target.orElse(null);
        }

        public abstract void draw(Graphics2D g2d);
    }

    /** Basic Cannon Tower. */
    class BasicTower extends Tower {
        public static final int COST = 150;
        public static final int RANGE = 150; 

        public BasicTower(int x, int y) {
            super(x, y);
            this.damage = 25;
            this.fireRate = 1000; 
            this.range = RANGE;
            this.color = Color.CYAN;
            this.attackColor = Color.YELLOW;
            this.cost = COST;
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            updateTargetAngle(); // Update the angle first

            // Draw Base (does not rotate)
            g2d.setColor(new Color(50, 100, 50).darker());
            g2d.fillRect(x - 12, y - 5, 24, 15);
            g2d.setColor(Color.BLUE.darker());
            g2d.fillOval(x - 8, y - 12, 16, 16);

            // --- Draw Rotating Turret ---
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y); // Move origin to tower center
            g2dCopy.rotate(currentAngle); // Rotate canvas
            
            // Draw barrel (relative to 0,0)
            g2dCopy.setColor(Color.LIGHT_GRAY);
            g2dCopy.fillRect(-3, -20, 6, 18); // Centered barrel pointing "up"
            g2dCopy.setColor(Color.BLACK);
            g2dCopy.fillRect(-3, -22, 6, 2);
            
            g2dCopy.dispose(); // Restore original canvas state
            // -----------------------------

            // Draw range indicator (does not rotate)
            if (selectedTower == this) { // Only draw range if selected
                g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
                g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
            }
        }
    }

    /** Long-range, high-damage Sniper Tower. */
    class SniperTower extends Tower {
        public static final int COST = 700;
        public static final int RANGE = 4000;

        public SniperTower(int x, int y) {
            super(x, y);
            this.damage = 200;
            this.fireRate = 4000; 
            this.range = RANGE;
            this.color = Color.CYAN.darker();
            this.attackColor = Color.CYAN;
            this.cost = COST;
        }

        @Override
        public void draw(Graphics2D g2d) {
            updateTargetAngle();

            // Draw Base (does not rotate)
            g2d.setColor(new Color(200, 200, 200)); 
            g2d.fillRect(x - 10, y, 20, 10);
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(x - 2, y - 25, 4, 25); 
            g2d.setColor(this.color);
            g2d.fillOval(x - 7, y - 30, 14, 14);

            // --- Draw Rotating Turret ---
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y - 23); // Rotate around the head
            g2dCopy.rotate(currentAngle);
            
            g2dCopy.setColor(Color.BLACK);
            g2dCopy.fillRect(-1, -22, 2, 22); // Long thin barrel
            
            g2dCopy.dispose();
            // -----------------------------

            // Draw range indicator (does not rotate)
            if (selectedTower == this) {
                g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50)); 
                g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
            }
        }
    }
    
    /** Fast-firing Machine Gun Tower. */
    class MachineGunTower extends Tower {
        public static final int COST = 300;
        public static final int RANGE = 200; 

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
            updateTargetAngle();
            
            // Draw Base (does not rotate)
            g2d.setColor(this.color.darker());
            g2d.fillRect(x - 12, y - 12, 24, 24);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(x - 8, y - 8, 16, 16);

            // --- Draw Rotating Turret ---
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y);
            g2dCopy.rotate(currentAngle);
            
            g2dCopy.setColor(Color.BLACK);
            g2dCopy.fillRect(-7, -15, 3, 10);
            g2dCopy.fillRect(-1, -18, 3, 13);
            g2dCopy.fillRect(5, -15, 3, 10);
            
            g2dCopy.dispose();
            // -----------------------------

            // Draw range indicator
            if (selectedTower == this) {
                g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50)); 
                g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
            }
        }
    }

    /** Continuous-damage  Tower. */
    class InfernoTower extends Tower {
        public static final int COST = 400;
        public static final int RANGE = 200; 

        private long visualEndTime = 0; 

        

     

        public InfernoTower(int x, int y) {
            super(x, y);
            this.damage = 15; 
            this.fireRate = 100;
            this.range = RANGE;
            this.color = Color.ORANGE;
            this.attackColor = Color.YELLOW; 
            this.cost = COST;
        }

        public void startDamageIncrement() {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                damage++;
                System.out.println("DMG Increased --> : " + damage); // Optional: Log the damage
            }, 0, 250, TimeUnit.MILLISECONDS);
        }
        
        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (getActualFireRate() / GAME_SPEED_MULTIPLIER);

            // Update target for rotation even if not firing
            updateTarget();
            
            if (currentTime - lastShotTime < effectiveCooldown) {
                // If we still have a target, keep the visual alive
                if (currentTarget != null) {
                    visualEndTime = currentTime + 150; // Extend visual
                }
                return; 
            }
            
            if (currentTarget != null) {
                currentTarget.takeDamage(damage);
                visualEndTime = currentTime + 150;
                lastShotTime = currentTime;
            }
        }

        @Override
        public void draw(Graphics2D g2d) {
            updateTargetAngle();
            
            // Draw Base (does not rotate)
            g2d.setColor(new Color(150, 50, 0)); 
            g2d.fillOval(x - 15, y - 15, 30, 30); 
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(x - 8, y - 8, 16, 16);

            // --- Draw Rotating Turret ---
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y);
            g2dCopy.rotate(currentAngle);

            g2dCopy.setColor(Color.BLACK);
            int[] xPoints = {-5, 5, 0};
            int[] yPoints = {-15, -15, -25};
            g2dCopy.fillPolygon(xPoints, yPoints, 3);
            
            // Draw flame visual if attacking
            if (currentTarget != null && System.currentTimeMillis() < visualEndTime && !currentTarget.isDead()) {
                g2dCopy.setColor(attackColor);
                g2dCopy.setStroke(new BasicStroke(5));
                // Draw line "up" towards target
                g2dCopy.drawLine(0, -25, 0, -35 - (int)(Math.random() * 10)); 
            }
            g2dCopy.dispose();
            // -----------------------------
            
            // Draw range indicator
            if (selectedTower == this) {
                g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50)); 
                g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
            }
        }
    }

    class LaserBeamer extends Tower {
        public static final int COST = 500;
        public static final int RANGE = 300; 

        private long visualEndTime = 0; 

        public LaserBeamer(int x, int y) {
            super(x, y);
            this.damage = 40; 
            this.fireRate = 800;
            this.range = RANGE;
            this.color = Color.BLUE.darker();
            this.attackColor = Color.MAGENTA; 
            this.cost = COST;
        }
        
        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (getActualFireRate() / GAME_SPEED_MULTIPLIER);

            // Update target for rotation even if not firing
            updateTarget();
            
            if (currentTime - lastShotTime < effectiveCooldown) {
                // If we still have a target, keep the visual alive
                if (currentTarget != null) {
                    visualEndTime = currentTime + 150; // Extend visual
                }
                return; 
            }
            
            if (currentTarget != null) {
                currentTarget.takeDamage(damage);
                visualEndTime = currentTime + 150;
                lastShotTime = currentTime;
            }
        }

        @Override
        public void draw(Graphics2D g2d) {
            updateTargetAngle();
            
            // Draw Base (does not rotate)
            g2d.setColor(new Color(150, 50, 0)); 
            g2d.fillOval(x - 15, y - 15, 30, 30); 
            g2d.setColor(Color.BLUE);
            g2d.fillOval(x - 8, y - 8, 16, 16);

            // --- Draw Rotating Turret ---
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y);
            g2dCopy.rotate(currentAngle);

            // Draw turret body (circle)
            g2dCopy.setColor(Color.GRAY);
            g2dCopy.fillOval(-10, -10, 20, 20);

            // Draw guns (rectangles)
            g2dCopy.setColor(Color.DARK_GRAY);
            g2dCopy.fillRect(-15, -5, 10, 10); // Left gun
            g2dCopy.fillRect(5, -5, 10, 10);  // Right gun

            // Draw flame visual if attacking
            if (currentTarget != null && System.currentTimeMillis() < visualEndTime && !currentTarget.isDead()) {
                g2dCopy.setColor(attackColor);
                g2dCopy.setStroke(new BasicStroke(5));
                // Draw line "up" towards target
                g2dCopy.drawLine(0, -25, 0, -35 - (int)(Math.random() * 10)); 
            }
            g2dCopy.dispose();
            // -----------------------------
            
            // Draw range indicator
            if (selectedTower == this) {
                g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50)); 
                g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
            }
        }
    }

    /** Mortar Tower: Area of Effect. */
    class MortarTower extends Tower {
        public static final int COST = 750;
        public static final int RANGE = 5000; 
        private static final int AOE_RANGE = 100; 

        public MortarTower(int x, int y) {
            super(x, y);
            this.damage = 150;
            this.fireRate = 4000; 
            setTargetingMode(FARTHEST); // Mortars default to Farthest
            this.color = Color.GRAY;
            this.attackColor = Color.BLACK;
            this.cost = COST;
            setTargetingMode(FARTHEST); // Mortars default to Farthest
        }

        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (getActualFireRate() / GAME_SPEED_MULTIPLIER);
            
            updateTarget(); // Update target for rotation
            
            if (currentTime - lastShotTime < effectiveCooldown) {
                return; 
            }

            if (currentTarget != null) {
                projectiles.add(new MortarProjectile(x, y, damage, currentTarget, attackColor, AOE_RANGE)); 
                lastShotTime = currentTime;
            }
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            updateTargetAngle();

            // Draw Base (does not rotate)
            g2d.setColor(new Color(50, 50, 50)); 
            g2d.fillRect(x - 12, y - 5, 24, 15);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(x - 8, y - 12, 16, 16);

            // --- Draw Rotating Turret ---
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y);
            g2dCopy.rotate(currentAngle);
            
            g2dCopy.setColor(Color.DARK_GRAY);
            g2dCopy.fillRect(-4, -25, 8, 15); // Short, thick barrel
            
            g2dCopy.dispose();
            // -----------------------------

            // Draw range indicator
            if (selectedTower == this) {
                g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
                g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
            }
        }
    }

    class BombTower extends Tower {
        public static final int COST = 400;
        public static final int RANGE = 300; 
        private static final int AOE_RANGE = 50; 

        public BombTower(int x, int y) {
            super(x, y);
            this.damage = 150;
            this.fireRate = 1000; 
            setTargetingMode(FARTHEST); // Mortars default to Farthest
            this.color = Color.GRAY;
            this.attackColor = Color.BLACK;
            this.cost = COST;
            setTargetingMode(FARTHEST); // Mortars default to Farthest
        }

        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (getActualFireRate() / GAME_SPEED_MULTIPLIER);
            
            updateTarget(); // Update target for rotation
            
            if (currentTime - lastShotTime < effectiveCooldown) {
                return; 
            }

            if (currentTarget != null) {
                projectiles.add(new MortarProjectile(x, y, damage, currentTarget, attackColor, AOE_RANGE)); 
                lastShotTime = currentTime;
            }
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            updateTargetAngle();

            // Draw Base (does not rotate)
            g2d.setColor(new Color(50, 50, 50)); 
            g2d.fillRect(x - 12, y - 5, 24, 15);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(x - 8, y - 12, 16, 16);

            // --- Draw Rotating Turret ---
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y);
            g2dCopy.rotate(currentAngle);
            
            g2dCopy.setColor(Color.DARK_GRAY);
            g2dCopy.fillRect(-4, -25, 8, 15); // Short, thick barrel
            
            g2dCopy.dispose();
            // -----------------------------

            // Draw range indicator
            if (selectedTower == this) {
                g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
                g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
            }
        }
    }

    

    
    
    /** Slow Tower: Applies a slow debuff. */
    class SlowTower extends Tower {
        public static final int COST = 200;
        public static final int RANGE = 180; 

        public SlowTower(int x, int y) {
            super(x, y);
            this.damage = 0; 
            this.fireRate = 800; 
            this.range = RANGE;
            this.color = Color.blue.darker();
            this.attackColor = Color.CYAN;
            this.cost = COST;
        }
        
        @Override
        public void attack() {
            long currentTime = System.currentTimeMillis();
            long effectiveCooldown = (long) (getActualFireRate() / GAME_SPEED_MULTIPLIER);
            
            updateTarget(); // Update target for rotation

            if (currentTime - lastShotTime < effectiveCooldown) {
                return; 
            }
            
            if (currentTarget != null) {
                currentTarget.applySlow();
                projectiles.add(new Projectile(x, y, 0, currentTarget, attackColor)); // Visual only
                lastShotTime = currentTime;
            }
        }

        @Override
        public void draw(Graphics2D g2d) {
            updateTargetAngle();

            // Draw Base (Light Cyan Circle)
            g2d.setColor(new Color(224, 255, 255)); // Light Cyan
            g2d.fillOval(x - 15, y - 15, 30, 30);
        
            // Draw Diamond (Light Blue)
            g2d.setColor(new Color(173, 216, 230)); // Light Blue
            int[] xPoints = {x, x - 10, x, x + 10};
            int[] yPoints = {y - 10, y, y + 10, y};
            g2d.fillPolygon(xPoints, yPoints, 4);
        
            // Draw Turret (Black)
            g2d.setColor(Color.BLACK);
            g2d.fillRect(x - 2, y - 10, 4, 10); // Turret body
            g2d.fillRect(x - 1, y - 15, 2, 5); // Turret barrel
            // -------------------------------
            
            // Draw range indicator
            if (selectedTower == this) {
                g2d.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
                g2d.drawOval(x - this.range, y - this.range, this.range * 2, this.range * 2);
            }
        }
    }

    /** Money Farm: Generates money passively. (Does not rotate) */
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
            // This tower does not attack, it generates income
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
            // Money farm does not rotate and has no target
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(x - 10, y - 10, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString("$", x - 4, y + 5);
        }
    }
    
    /** Beacon Tower: Buffs nearby towers' fire rate. (Does not rotate) */
    class BeaconTower extends Tower {
        public static final int COST = 1000;
        public static final int RANGE = 200; 
        private double buffMultiplier = 1; 

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
       // Beacon does not rotate

        // Draw Base (Magenta Circle with a larger size)
        g2d.setColor(Color.MAGENTA.darker());
        g2d.fillOval(x - 30, y - 30, 60, 60); // Larger base (30 radius, 60 diameter)

        // Draw Inner Circle (White for a glowing effect)
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - 20, y - 20, 40, 40); // Smaller inner circle

        // Draw "B" in the center
        g2d.setColor(Color.MAGENTA.darker());
        g2d.drawString("⬆", x - 5, y + 5);

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
            if (target == null || target.isDead()) return;
            
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
            if (target == null || target.isDead()) return false;
            
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
            // Store impact point to draw explosion
            double impactX = this.x;
            double impactY = this.y;
            
            for (Enemy enemy : allEnemies) {
                if (enemy.isDead()) continue;
                double distance = Point2D.distance(impactX, impactY, enemy.getX(), enemy.getY());
                
                if (distance <= aoeRange) {
                    // Damage falls off based on distance, but keep it simple for now
                    enemy.takeDamage(damage); 
                }
            }
            
            // Add a temporary visual effect for the explosion (simplified)
            // A more robust way would be a separate "Effects" list
            // gamePanel.add(new ExplosionEffect(impactX, impactY, aoeRange));
        }
        
        // This is a kludge for a simple explosion visual.
        // A proper implementation would have an effects manager.
        class ExplosionEffect extends JComponent {
            public ExplosionEffect(double x, double y, int radius) {
                // This is not the right way to do it in this structure.
                // We will draw the explosion in the projectile's draw method on impact.
            }
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int)x - 5, (int)y - 5, 10, 10);
            
            // Draw a temporary explosion ring on impact for visual
            if (this.hasHitTarget()) {
                 g2d.setColor(new Color(255, 165, 0, 150));
                 g2d.drawOval((int)x - aoeRange, (int)y - aoeRange, aoeRange * 2, aoeRange * 2);
            }
        }
    }
}