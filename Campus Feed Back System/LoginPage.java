package com.campusfeedback;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPage {
    private JFrame frame;
    private DatabaseHandler dbHandler;

    public LoginPage() {
        dbHandler = DatabaseHandler.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Login");
        frame.setSize(400, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 120, 215));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame.add(titleLabel, gbc);

        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        frame.add(roleLabel, gbc);

        String[] roles = {"Admin", "Student"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        frame.add(roleBox, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(userLabel, gbc);

        JTextField userField = GUIFactory.createStyledTextField();
        gbc.gridx = 1;
        frame.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(200, 30));
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1;
        frame.add(passField, gbc);

        JButton loginButton = GUIFactory.createStyledButton("Login", new Color(50, 150, 250));
        gbc.gridx = 0;
        gbc.gridy = 4;
        frame.add(loginButton, gbc);

        JButton signupButton = GUIFactory.createStyledButton("Sign Up", new Color(0, 180, 90));
        gbc.gridx = 1;
        frame.add(signupButton, gbc);

        loginButton.addActionListener(e -> {
            String role = (String) roleBox.getSelectedItem();
            String username = userField.getText();
            String password = new String(passField.getPassword());

            try {
                if (role.equals("Admin")) {
                    if (dbHandler.authenticateAdmin(username, password)) {
                        System.out.println("Admin login successful");
                        frame.dispose();
                        new AdminPage().show();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid Admin Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    if (dbHandler.authenticateStudent(username, password)) {
                        frame.dispose();
                        new StudentPage(username).show();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid Student Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Login failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        signupButton.addActionListener(e -> {
            frame.dispose();
            new com.campusfeedback.ui.RegistrationPage().show(); // Redirect to RegistrationPage
        });
    }

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}