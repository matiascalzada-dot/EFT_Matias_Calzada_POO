package vista;

import controller.UsuarioController;
import model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginView extends JFrame {

    private final UsuarioController controller;
    private final JTextField campoRut;
    private final JPasswordField campoContrasena;

    public LoginView() {

        controller = new UsuarioController();

        setTitle("Biblioteca Escolar - Inicio de sesión");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        campoRut = new JTextField();
        campoContrasena = new JPasswordField();

        JButton botonIngresar = new JButton("Ingresar");

        panel.add(new JLabel("RUT:"));
        panel.add(campoRut);

        panel.add(new JLabel("Contraseña:"));
        panel.add(campoContrasena);

        panel.add(new JLabel());
        panel.add(botonIngresar);

        add(panel);

        botonIngresar.addActionListener(e -> iniciarSesion());

        campoContrasena.addActionListener(e -> iniciarSesion());
    }

    private void iniciarSesion() {

        String rut = campoRut.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());

        try {

            Usuario usuario = controller.autenticar(rut, contrasena);

            if (usuario == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "RUT o contraseña incorrectos.",
                        "Error de inicio de sesión",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Bienvenido/a, " + usuario.getNombre()
            );

            new MenuPrincipalView(usuario).setVisible(true);
            dispose();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible validar el usuario en la base de datos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}
