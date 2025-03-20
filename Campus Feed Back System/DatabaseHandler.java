package com.campusfeedback;

import javax.swing.*;
import java.sql.*;

public class DatabaseHandler {
    private Connection connection;
    private static DatabaseHandler instance;

    private DatabaseHandler() {
        initializeConnection();
        createTablesIfNotExist();
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    private void initializeConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/campus_feedback";
            String username = "root";
            String password = "";
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createTablesIfNotExist() {
        String[] createTableSQLs = {
                "CREATE TABLE IF NOT EXISTS admin (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "username VARCHAR(50) NOT NULL UNIQUE," +
                        "password VARCHAR(255) NOT NULL" +
                        ")",
                "CREATE TABLE IF NOT EXISTS student (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "username VARCHAR(50) NOT NULL UNIQUE," +
                        "password VARCHAR(255) NOT NULL," +
                        "name VARCHAR(100) NOT NULL," +
                        "age INT NOT NULL," +
                        "department VARCHAR(50)" +
                        ")",
                "CREATE TABLE IF NOT EXISTS teacher (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(100) NOT NULL," +
                        "department VARCHAR(50) NOT NULL" +
                        ")",
                "CREATE TABLE IF NOT EXISTS feedback (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "student_username VARCHAR(50) NOT NULL," +
                        "teacher_name VARCHAR(100) NOT NULL," +
                        "feedback TEXT NOT NULL," +
                        "submitted_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : createTableSQLs) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to create tables: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean authenticateAdmin(String username, String password) {
        try {
            String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                return stmt.executeQuery().next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean authenticateStudent(String username, String password) {
        try {
            String query = "SELECT * FROM student WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                return stmt.executeQuery().next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet getStudentInfo(String username) throws SQLException {
        String query = "SELECT * FROM student WHERE username = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, username);
        return stmt.executeQuery();
    }

    public ResultSet getTeachersByDepartment(String department) throws SQLException {
        String query = "SELECT * FROM teacher";
        PreparedStatement stmt = connection.prepareStatement(query);
        return stmt.executeQuery();
    }

    public ResultSet getAllFeedback() throws SQLException {
        String query = "SELECT * FROM feedback";
        PreparedStatement stmt = connection.prepareStatement(query);
        return stmt.executeQuery();
    }

    public void addTeacher(String name, String department) throws SQLException {
        String query = "INSERT INTO teacher (name, department) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, department);
            stmt.executeUpdate();
        }
    }

    public void deleteTeacher(String name) throws SQLException {
        String query = "DELETE FROM teacher WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        }
    }

    public boolean registerUser(String username, String password, String name, int age, String role, String department) throws SQLException {
        String table = role.equalsIgnoreCase("admin") ? "admin" : "student";
        String checkSql = "SELECT COUNT(*) FROM " + table + " WHERE username = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }
        }

        String insertSql = role.equalsIgnoreCase("admin")
                ? "INSERT INTO admin (username, password) VALUES (?, ?)"
                : "INSERT INTO student (username, password, name, age, department) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            if (role.equalsIgnoreCase("student")) {
                insertStmt.setString(3, name);
                insertStmt.setInt(4, age);
                insertStmt.setString(5, department != null ? department : "");
            }
            int rowsAffected = insertStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public void deleteFeedback(int feedbackId) throws SQLException {
        String query = "DELETE FROM feedback WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, feedbackId);
            stmt.executeUpdate();
        }
    }

    public void submitFeedback(String studentUsername, String teacherName, String feedback) throws SQLException {
        String query = "INSERT INTO feedback (student_username, teacher_name, feedback) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, studentUsername);
            stmt.setString(2, teacherName);
            stmt.setString(3, feedback);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert feedback: No rows affected.");
            }

            // Retrieve the generated id (for debugging)
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    System.out.println("Generated feedback ID: " + generatedKeys.getInt(1));
                } else {
                    System.out.println("No ID was generated.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Check if the specific error is related to 'id' not having a default value
            if (e.getMessage().contains("Field 'id' doesn't have a default value")) {
                throw new SQLException("Error submitting feedback: The 'id' field is not set to auto-increment. Please check the database schema.", e);
            }
            throw new SQLException("Error submitting feedback: " + e.getMessage(), e);
        }
    }
}