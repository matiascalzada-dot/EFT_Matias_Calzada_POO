package vista;

import model.Usuario;

import javax.swing.*;
import java.awt.*;

public class MenuPrincipalView extends JFrame {

    private final Usuario usuario;

    public MenuPrincipalView(Usuario usuario) {

        this.usuario = usuario;

        setTitle("Biblioteca Escolar");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel bienvenida = new JLabel(
                "Bienvenido/a: " + usuario.getNombre()
                        + " | Rol: " + usuario.getRol(),
                SwingConstants.CENTER
        );

        bienvenida.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(bienvenida, BorderLayout.NORTH);

        JPanel opciones = new JPanel(new GridLayout(0, 2, 10, 10));

        if ("bibliotecario".equalsIgnoreCase(usuario.getRol())) {

            agregarBoton(opciones, "Gestión de categorías");
            agregarBoton(opciones, "Gestión de libros");
            agregarBoton(opciones, "Gestión de estudiantes");
            agregarBoton(opciones, "Préstamos y devoluciones");
            agregarBoton(opciones, "Reportes");

        } else if ("estudiante".equalsIgnoreCase(usuario.getRol())) {

            agregarBoton(opciones, "Consultar libros");
            agregarBoton(opciones, "Solicitar préstamo");
            agregarBoton(opciones, "Devolver libro");
        }

        panel.add(opciones, BorderLayout.CENTER);

        JButton cerrarSesion = new JButton("Cerrar sesión");

        cerrarSesion.addActionListener(e -> {
            new LoginView().setVisible(true);
            dispose();
        });

        panel.add(cerrarSesion, BorderLayout.SOUTH);

        add(panel);
    }

    private void agregarBoton(JPanel panel, String texto) {

        JButton boton = new JButton(texto);

        boton.addActionListener(e -> {

            switch (texto) {

                case "Gestión de estudiantes" ->
                        new EstudiantesView().setVisible(true);

                case "Gestión de categorías" ->
                        new CategoriasView().setVisible(true);

                case "Gestión de libros" ->
                        new LibrosView(true).setVisible(true);

                case "Consultar libros" ->
                        new LibrosView(false).setVisible(true);

                case "Préstamos y devoluciones" ->
                        new PrestamosView(usuario).setVisible(true);

                case "Solicitar préstamo" ->
                        new PrestamosView(usuario).setVisible(true);

                case "Devolver libro" ->
                        new PrestamosView(usuario).setVisible(true);

                case "Reportes" ->
                        new ReportesView().setVisible(true);

                default -> JOptionPane.showMessageDialog(
                        this,
                        "Este módulo lo conectaremos en la siguiente etapa."
                );
            }
        });

        panel.add(boton);
    }
}
