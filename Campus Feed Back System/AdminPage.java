package com.campusfeedback;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPage {
    private JFrame frame;
    private DatabaseHandler dbHandler;
    private JPanel teacherListPanel;

    public AdminPage() {
        dbHandler = DatabaseHandler.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Admin Dashboard");
        frame.setSize(850, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(20, 20));
        frame.getContentPane().setBackground(new Color(240, 245, 250));
        frame.setLocationRelativeTo(null);

        // Teacher Management Panel (Top)
        JPanel teacherManagementPanel = createTeacherManagementPanel();
        frame.add(teacherManagementPanel, BorderLayout.NORTH);

        // Teacher List Panel (Left)
        teacherListPanel = createTeacherListPanel();
        JScrollPane teacherScrollPane = new JScrollPane(teacherListPanel);
        teacherScrollPane.setBorder(BorderFactory.createEmptyBorder());
        teacherScrollPane.setPreferredSize(new Dimension(320, 0));
        frame.add(teacherScrollPane, BorderLayout.WEST);

        // Populate the teacher list after initializing teacherListPanel
        refreshTeacherList();

        // Feedback Table (Center)
        JTable feedbackTable = createFeedbackTable();
        JScrollPane feedbackScrollPane = new JScrollPane(feedbackTable);
        feedbackScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 120, 215), 2),
                "Student Feedback",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 18),
                new Color(0, 120, 215)
        ));
        frame.add(feedbackScrollPane, BorderLayout.CENTER);

        // Control Panel (Bottom)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        controlPanel.setBackground(new Color(240, 245, 250));
        JButton logoutButton = GUIFactory.createStyledButton("Logout", new Color(255, 150, 65));
        controlPanel.add(logoutButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        logoutButton.addActionListener(e -> {
            frame.dispose();
            new LoginPage().show();
        });
    }

    private JPanel createTeacherManagementPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(0, 180));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Manage Teachers");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 120, 215));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(GUIFactory.createStyledLabel("Teacher Name:"), gbc);
        gbc.gridx = 1;
        JTextField teacherNameField = GUIFactory.createStyledTextField();
        panel.add(teacherNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(GUIFactory.createStyledLabel("Department:"), gbc);
        gbc.gridx = 1;
        JTextField deptField = GUIFactory.createStyledTextField();
        panel.add(deptField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        JButton addButton = GUIFactory.createStyledButton("Add Teacher", new Color(46, 204, 113));
        panel.add(addButton, gbc);

        addButton.addActionListener(e -> {
            String name = teacherNameField.getText().trim();
            String dept = deptField.getText().trim();
            if (name.isEmpty() || dept.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                dbHandler.addTeacher(name, dept);
                JOptionPane.showMessageDialog(frame, "Teacher added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                teacherNameField.setText("");
                deptField.setText("");
                refreshTeacherList();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error adding teacher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createTeacherListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 245, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JLabel titleLabel = new JLabel("Current Teachers");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 120, 215));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        return panel;
    }

    private void refreshTeacherList() {
        if (teacherListPanel == null) {
            System.err.println("teacherListPanel is null in refreshTeacherList");
            return;
        }
        teacherListPanel.removeAll();

        // Store teacher data in a list to avoid ResultSet closure issues
        List<String[]> teachers = new ArrayList<>();
        try (ResultSet rs = dbHandler.getTeachersByDepartment(null)) {
            while (rs.next()) {
                String teacherName = rs.getString("name");
                String department = rs.getString("department");
                teachers.add(new String[]{teacherName, department});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading teachers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            teacherListPanel.add(new JLabel("Error loading teachers"));
            teacherListPanel.revalidate();
            teacherListPanel.repaint();
            return;
        }

        // Process the teacher data after closing the ResultSet
        if (teachers.isEmpty()) {
            teacherListPanel.add(new JLabel("No teachers available"));
        } else {
            for (String[] teacher : teachers) {
                String teacherName = teacher[0];
                String department = teacher[1];

                JPanel teacherRow = new JPanel(new BorderLayout(10, 0));
                teacherRow.setBackground(new Color(255, 255, 255));
                teacherRow.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                teacherRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                teacherRow.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel teacherLabel = new JLabel("<html><b>" + teacherName + "</b><br>" + department + "</html>");
                teacherLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                teacherLabel.setForeground(new Color(44, 62, 80));
                teacherRow.add(teacherLabel, BorderLayout.CENTER);

                JButton removeButton = GUIFactory.createStyledButton("Remove", new Color(69, 137, 146));
                removeButton.setPreferredSize(new Dimension(100, 30));
                teacherRow.add(removeButton, BorderLayout.EAST);

                removeButton.addActionListener(e -> {
                    try {
                        dbHandler.deleteTeacher(teacherName);
                        JOptionPane.showMessageDialog(frame, "Teacher removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshTeacherList();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error removing teacher: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                teacherListPanel.add(teacherRow);
                teacherListPanel.add(Box.createVerticalStrut(8));
            }
        }

        teacherListPanel.revalidate();
        teacherListPanel.repaint();
    }

    private JTable createFeedbackTable() {
        String[] columns = {"ID", "Student", "Teacher", "Feedback", "Date", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Action column editable
            }
        };

        // Store feedback data in a list to avoid ResultSet closure issues
        List<Object[]> feedbackData = new ArrayList<>();
        try (ResultSet rs = dbHandler.getAllFeedback()) {
            if (rs == null || !rs.next()) {
                feedbackData.add(new Object[]{"-", "No data", "No data", "No feedback available", "-", "N/A"});
            } else {
                do {
                    int id = rs.getInt("id");
                    String student = rs.getString("student_username");
                    String teacher = rs.getString("teacher_name");
                    String feedback = rs.getString("feedback");
                    String date = rs.getString("submitted_date");
                    feedbackData.add(new Object[]{id, student, teacher, feedback, date, "Remove"});
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            feedbackData.add(new Object[]{"Error", "Error", "Error", "Failed to load feedback: " + e.getMessage(), "-", "N/A"});
        }

        // Add the data to the table model after closing the ResultSet
        for (Object[] row : feedbackData) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(new Color(255, 255, 255));
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(0, 120, 215));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? new Color(245, 250, 255) : new Color(255, 255, 255));
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Student
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Teacher
        table.getColumnModel().getColumn(3).setPreferredWidth(300); // Feedback
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Date
        table.getColumnModel().getColumn(5).setPreferredWidth(80); // Action

        // Add button renderer and editor for the "Action" column
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), dbHandler, model, table));

        return table;
    }

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    // Button Renderer
    static class ButtonRenderer extends DefaultTableCellRenderer {
        private final JButton button;

        public ButtonRenderer() {
            button = new JButton("Remove");
            button.setBackground(new Color(69, 137, 146));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return button;
        }
    }

    // Button Editor
    static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private DatabaseHandler dbHandler;
        private DefaultTableModel model;
        private JTable table;
        private int row;

        public ButtonEditor(JCheckBox checkBox, DatabaseHandler dbHandler, DefaultTableModel model, JTable table) {
            super(checkBox);
            this.dbHandler = dbHandler;
            this.model = model;
            this.table = table;
            button = new JButton("Remove");
            button.setBackground(new Color(69, 137, 146));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int id = (int) model.getValueAt(row, 0); // Get the feedback ID from the first column
                    try {
                        dbHandler.deleteFeedback(id); // Call the correct method in DatabaseHandler
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(table, "Feedback removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(table, "Error removing feedback: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Remove";
        }
    }
}