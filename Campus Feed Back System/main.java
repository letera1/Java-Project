package com.campusfeedback;

import javax.swing.*;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage login = new LoginPage();
            login.show();
        });
    }
}