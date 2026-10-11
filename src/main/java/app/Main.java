package app;

import vista.LoginView;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            LoginView ventana = new LoginView();
            ventana.setVisible(true);
        });
    }
}