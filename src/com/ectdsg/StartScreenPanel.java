package com.ectdsg;

import javax.swing.*;
import java.awt.*;

public class StartScreenPanel extends JPanel {

    private TowerDefence game;

    public StartScreenPanel(TowerDefence game, int width, int height) {
        this.game = game;
        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(40, 40, 40));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridy = 0;

        JLabel title = new JLabel("Egors Crappy TDS gaem");
        title.setFont(new Font("Monospaced", Font.BOLD, 48));
        title.setForeground(new Color(50, 200, 255));
        add(title, gbc);

        gbc.gridy++;
        JLabel info = new JLabel("ts game sucks y r u playing");
        info.setFont(new Font("Arial", Font.ITALIC, 16));
        info.setForeground(Color.LIGHT_GRAY);
        add(info, gbc);

        gbc.gridy++;
        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.BOLD, 30));
        startButton.setBackground(new Color(0, 150, 0));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createRaisedBevelBorder());
        startButton.addActionListener(e -> game.startGame());
        add(startButton, gbc);
    }
}
