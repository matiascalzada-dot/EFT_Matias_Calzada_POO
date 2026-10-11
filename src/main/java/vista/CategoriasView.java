package vista;

import controller.CategoriaController;
import model.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class CategoriasView extends JFrame {

    private final CategoriaController controller = new CategoriaController();

    private final JTextField campoNombre = new JTextField(20);

    private final DefaultTableModel modelo =
            new DefaultTableModel(new Object[]{"ID", "Nombre"}, 0) {
                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modelo);

    private int idSeleccionado = 0;

    public CategoriasView() {

        setTitle("Gestión de categorías");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel formulario = new JPanel(new FlowLayout());
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        JButton crear = new JButton("Crear");
        JButton actualizar = new JButton("Actualizar");
        JButton eliminar = new JButton("Eliminar");
        JButton limpiar = new JButton("Limpiar");

        JPanel botones = new JPanel(new FlowLayout());
        botones.add(crear);
        botones.add(actualizar);
        botones.add(eliminar);
        botones.add(limpiar);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(formulario, BorderLayout.NORTH);
        superior.add(botones, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        crear.addActionListener(e -> guardar(false));
        actualizar.addActionListener(e -> guardar(true));
        eliminar.addActionListener(e -> eliminarCategoria());
        limpiar.addActionListener(e -> limpiarFormulario());

        tabla.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {

                int fila = tabla.getSelectedRow();

                idSeleccionado = ((Number) modelo.getValueAt(fila, 0)).intValue();

                campoNombre.setText(
                        modelo.getValueAt(fila, 1).toString()
                );
            }
        });

        cargarCategorias();
    }

    private void cargarCategorias() {

        try {

            modelo.setRowCount(0);

            for (Categoria categoria : controller.listar()) {
                modelo.addRow(new Object[]{
                        categoria.getId(),
                        categoria.getNombre()
                });
            }

        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void guardar(boolean actualizar) {

        try {

            boolean resultado;

            if (actualizar) {

                if (idSeleccionado == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Selecciona una categoría de la tabla."
                    );
                    return;
                }

                Categoria categoria = new Categoria(
                        idSeleccionado,
                        campoNombre.getText()
                );

                resultado = controller.actualizar(categoria);

            } else {

                resultado = controller.crear(campoNombre.getText());
            }

            if (resultado) {
                JOptionPane.showMessageDialog(
                        this,
                        actualizar
                                ? "Categoría actualizada."
                                : "Categoría creada."
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se realizaron cambios."
                );
            }

            cargarCategorias();
            limpiarFormulario();

        } catch (SQLException | IllegalArgumentException e) {
            mostrarError(e);
        }
    }

    private void eliminarCategoria() {

        if (idSeleccionado == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una categoría."
            );
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas eliminar esta categoría?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean eliminado = controller.eliminar(idSeleccionado);

            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Categoría eliminada.");
            }

            cargarCategorias();
            limpiarFormulario();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo eliminar. Comprueba si existen libros asociados.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarFormulario() {

        idSeleccionado = 0;
        campoNombre.setText("");
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