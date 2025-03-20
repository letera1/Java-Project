package com.campusfeedback.ui;

import com.campusfeedback.DatabaseHandler;
import com.campusfeedback.GUIFactory;
import com.campusfeedback.LoginPage;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class RegistrationPage {
    private JFrame frame;
    private DatabaseHandler dbHandler;
    private JTextField deptField;
    private JComboBox<String> roleBox;

    public RegistrationPage() {
        dbHandler = DatabaseHandler.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Register");
        frame.setSize(400, 450);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(new Color(240, 240, 240));
        frame.setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Title
        JLabel titleLabel = new JLabel("Register");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 120, 215));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame.add(titleLabel, gbc);

        // Role Selection
        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        frame.add(roleLabel, gbc);

        roleBox = new JComboBox<>(new String[]{"Admin", "Student"});
        roleBox.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        frame.add(roleBox, gbc);

        // Username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(userLabel, gbc);

        JTextField userField = GUIFactory.createStyledTextField();
        gbc.gridx = 1;
        frame.add(userField, gbc);

        // Password
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

        // Name
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 4;
        frame.add(nameLabel, gbc);

        JTextField nameField = GUIFactory.createStyledTextField();
        gbc.gridx = 1;
        frame.add(nameField, gbc);

        // Age
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 5;
        frame.add(ageLabel, gbc);

        JTextField ageField = GUIFactory.createStyledTextField();
        gbc.gridx = 1;
        frame.add(ageField, gbc);

        // Department
        JLabel deptLabel = new JLabel("Department:");
        deptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 6;
        frame.add(deptLabel, gbc);

        deptField = GUIFactory.createStyledTextField();
        deptField.setVisible(false);
        gbc.gridx = 1;
        frame.add(deptField, gbc);

        // Register Button
        JButton registerButton = GUIFactory.createStyledButton("Register", new Color(0, 180, 90));
        gbc.gridx = 0;
        gbc.gridy = 7;
        frame.add(registerButton, gbc);

        // Back Button
        JButton backButton = GUIFactory.createStyledButton("Back", new Color(150, 150, 150));
        gbc.gridx = 1;
        frame.add(backButton, gbc);

        // Listener to toggle department field visibility
        roleBox.addActionListener(e -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            boolean isStudent = selectedRole.equals("Student");
            deptLabel.setVisible(isStudent);
            deptField.setVisible(isStudent);
            frame.pack();
        });

        // Register Button Action
        registerButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String name = nameField.getText().trim();
            String ageStr = ageField.getText().trim();
            String department = deptField.isVisible() ? deptField.getText().trim() : null;
            String role = (String) roleBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || ageStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields except Department (for Admin) are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (role.equals("Student") && (department == null || department.isEmpty())) {
                JOptionPane.showMessageDialog(frame, "Department is required for Student role!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);
                if (age <= 0) {
                    JOptionPane.showMessageDialog(frame, "Age must be a positive number!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (dbHandler.registerUser(username, password, name, age, role, department)) {
                    JOptionPane.showMessageDialog(frame, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    frame.dispose();
                    new LoginPage().show();
                } else {
                    JOptionPane.showMessageDialog(frame, "Registration failed! Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Age must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error registering user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            frame.dispose();
            new LoginPage().show();
        });

        // Initial call to set visibility based on default role (Admin)
        roleBox.getActionListeners()[0].actionPerformed(null);
    }

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}