package com.campusfeedback;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentPage {
    private JFrame frame;
    private String username;
    private DatabaseHandler dbHandler;

    public StudentPage(String username) {
        this.username = username;
        dbHandler = DatabaseHandler.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Student Dashboard");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(20, 20));
        frame.getContentPane().setBackground(new Color(240, 245, 250));
        frame.setLocationRelativeTo(null);

        // Header Panel with Student Name
        JPanel headerPanel = createHeaderPanel();
        frame.add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(255, 255, 255));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        try (ResultSet rs = dbHandler.getStudentInfo(username)) {
            if (rs.next()) {
                String department = rs.getString("department");

                // Teacher Information Section with Selection
                gbc.gridx = 0;
                gbc.gridy = 0;
                contentPanel.add(GUIFactory.createStyledLabel("Select Teacher:"), gbc);
                gbc.gridy = 1;
                JComboBox<String> teacherComboBox = createTeacherComboBox();
                contentPanel.add(teacherComboBox, gbc);

                // Feedback Section
                gbc.gridy = 2;
                contentPanel.add(GUIFactory.createStyledLabel("Feedback:"), gbc);
                gbc.gridy = 3;
                JTextArea feedbackArea = new JTextArea(5, 30);
                feedbackArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                feedbackArea.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);
                contentPanel.add(feedbackScrollPane, gbc);

                gbc.gridy = 4;
                gbc.gridwidth = 2;
                JButton submitButton = GUIFactory.createStyledButton("Submit Feedback", new Color(50, 150, 250));
                contentPanel.add(submitButton, gbc);

                submitButton.addActionListener(e -> {
                    String selectedTeacher = (String) teacherComboBox.getSelectedItem();
                    if (selectedTeacher == null || selectedTeacher.equals("No teachers assigned") || selectedTeacher.equals("Error loading teachers")) {
                        JOptionPane.showMessageDialog(frame, "Please select a valid teacher!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String feedbackText = feedbackArea.getText().trim();
                    if (feedbackText.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Feedback cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        dbHandler.submitFeedback(username, selectedTeacher, feedbackText);
                        JOptionPane.showMessageDialog(frame, "Feedback submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        feedbackArea.setText("");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading student info: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        frame.add(contentPanel, BorderLayout.CENTER);

        // Back Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 245, 250));
        JButton backButton = GUIFactory.createStyledButton("Back to Login", new Color(150, 150, 150));
        backButton.addActionListener(e -> {
            frame.dispose();
            new LoginPage().show();
        });
        buttonPanel.add(backButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(0, 120, 215));
        panel.setPreferredSize(new Dimension(0, 60));

        try (ResultSet rs = dbHandler.getStudentInfo(username)) {
            if (rs.next()) {
                String studentName = rs.getString("name");
                JLabel headerLabel = new JLabel("Welcome, " + studentName + "!");
                headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                headerLabel.setForeground(Color.WHITE);
                panel.add(headerLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Welcome, Unknown User!");
            errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            errorLabel.setForeground(Color.WHITE);
            panel.add(errorLabel);
        }

        return panel;
    }

    private JComboBox<String> createTeacherComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        // Store teacher names in a list to avoid ResultSet closure issues
        List<String> teacherNames = new ArrayList<>();
        try (ResultSet rs = dbHandler.getTeachersByDepartment(null)) {
            while (rs.next()) {
                teacherNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error Fetching Teachers: " + e.getMessage());
            teacherNames.add("Error Loading Teachers");
        }

        // Process the teacher names after closing the ResultSet
        if (teacherNames.isEmpty()) {
            model.addElement("No Teachers Assigned");
        } else {
            for (String teacherName : teacherNames) {
                model.addElement(teacherName);
            }
        }

        JComboBox<String> comboBox = new JComboBox<>(model);
        comboBox.setPreferredSize(new Dimension(200, 30));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return comboBox;
    }

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
