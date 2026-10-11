package vista;

import controller.EstudianteController;
import model.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class EstudiantesView extends JFrame {

    private final EstudianteController controller =
            new EstudianteController();

    private final JTextField campoNombre = new JTextField();
    private final JTextField campoRut = new JTextField();
    private final JTextField campoCurso = new JTextField();
    private final JTextField campoCorreo = new JTextField();
    private final JPasswordField campoContrasena = new JPasswordField();

    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new Object[]{"ID", "Nombre", "RUT", "Curso", "Correo"}, 0
            ) {
                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modelo);

    private int idSeleccionado = 0;
    private String rutOriginal = null;

    public EstudiantesView() {

        setTitle("Gestión de estudiantes");
        setSize(850, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel formulario = new JPanel(new GridLayout(5, 2, 6, 6));
        formulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("RUT:"));
        formulario.add(campoRut);

        formulario.add(new JLabel("Curso:"));
        formulario.add(campoCurso);

        formulario.add(new JLabel("Correo:"));
        formulario.add(campoCorreo);

        formulario.add(new JLabel("Contraseña (solo al crear):"));
        formulario.add(campoContrasena);

        JButton crear = new JButton("Registrar");
        JButton actualizar = new JButton("Modificar");
        JButton eliminar = new JButton("Eliminar");
        JButton limpiar = new JButton("Limpiar");

        JPanel botones = new JPanel(new FlowLayout());
        botones.add(crear);
        botones.add(actualizar);
        botones.add(eliminar);
        botones.add(limpiar);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        crear.addActionListener(e -> registrar());
        actualizar.addActionListener(e -> modificar());
        eliminar.addActionListener(e -> eliminar());
        limpiar.addActionListener(e -> limpiarFormulario());

        tabla.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()
                    && tabla.getSelectedRow() >= 0) {

                int fila = tabla.getSelectedRow();

                idSeleccionado =
                        ((Number) modelo.getValueAt(fila, 0)).intValue();

                campoNombre.setText(modelo.getValueAt(fila, 1).toString());
                campoRut.setText(modelo.getValueAt(fila, 2).toString());
                campoCurso.setText(modelo.getValueAt(fila, 3).toString());
                campoCorreo.setText(modelo.getValueAt(fila, 4).toString());

                rutOriginal = campoRut.getText();
                campoContrasena.setText("");
            }
        });

        cargarEstudiantes();
    }

    private Estudiante obtenerEstudiante() {

        return new Estudiante(
                idSeleccionado,
                campoNombre.getText(),
                campoRut.getText(),
                campoCurso.getText(),
                campoCorreo.getText()
        );
    }

    private void registrar() {

        try {

            Estudiante estudiante = obtenerEstudiante();

            boolean creado = controller.crear(
                    estudiante,
                    new String(campoContrasena.getPassword())
            );

            if (creado) {
                JOptionPane.showMessageDialog(
                        this,
                        "Estudiante y cuenta de acceso registrados."
                );
            }

            cargarEstudiantes();
            limpiarFormulario();

        } catch (SQLException | IllegalArgumentException e) {
            mostrarError(e);
        }
    }

    private void modificar() {

        if (idSeleccionado == 0) {
            JOptionPane.showMessageDialog(
                    this, "Selecciona un estudiante."
            );
            return;
        }

        try {

            boolean actualizado = controller.actualizar(
                    obtenerEstudiante(),
                    rutOriginal
            );

            if (actualizado) {
                JOptionPane.showMessageDialog(
                        this, "Estudiante actualizado."
                );
            }

            cargarEstudiantes();
            limpiarFormulario();

        } catch (SQLException | IllegalArgumentException e) {
            mostrarError(e);
        }
    }

    private void eliminar() {

        if (idSeleccionado == 0) {
            JOptionPane.showMessageDialog(
                    this, "Selecciona un estudiante."
            );
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar al estudiante y su cuenta de acceso?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean eliminado = controller.eliminar(
                    idSeleccionado,
                    rutOriginal
            );

            if (eliminado) {
                JOptionPane.showMessageDialog(
                        this, "Estudiante eliminado."
                );
            }

            cargarEstudiantes();
            limpiarFormulario();

        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void cargarEstudiantes() {

        try {

            modelo.setRowCount(0);

            for (Estudiante estudiante : controller.listar()) {
                modelo.addRow(new Object[]{
                        estudiante.getId(),
                        estudiante.getNombre(),
                        estudiante.getRut(),
                        estudiante.getCurso(),
                        estudiante.getCorreo()
                });
            }

        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void limpiarFormulario() {

        idSeleccionado = 0;
        rutOriginal = null;

        campoNombre.setText("");
        campoRut.setText("");
        campoCurso.setText("");
        campoCorreo.setText("");
        campoContrasena.setText("");

        tabla.clearSelection();
    }

    private void mostrarError(Exception e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
