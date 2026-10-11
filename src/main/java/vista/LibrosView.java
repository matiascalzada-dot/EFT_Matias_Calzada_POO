package vista;

import controller.LibroController;
import model.Categoria;
import model.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class LibrosView extends JFrame {

    private final LibroController controller = new LibroController();
    private final boolean puedeGestionar;

    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoAutor = new JTextField();
    private final JTextField campoIsbn = new JTextField();
    private final JTextField campoEditorial = new JTextField();
    private final JTextField campoStock = new JTextField();

    private final JComboBox<Categoria> comboCategorias = new JComboBox<>();

    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new Object[]{
                            "ID", "Título", "Autor", "ISBN",
                            "Editorial", "Stock", "Categoría"
                    },
                    0
            ) {
                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };

    private final JTable tabla = new JTable(modelo);

    private int idSeleccionado = 0;

    public LibrosView(boolean puedeGestionar) {

        this.puedeGestionar = puedeGestionar;

        setTitle(puedeGestionar ? "Gestión de libros" : "Catálogo de libros");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        comboCategorias.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista,
                    Object valor,
                    int indice,
                    boolean seleccionado,
                    boolean tieneFoco) {

                super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, tieneFoco
                );

                if (valor instanceof Categoria categoria) {
                    setText(categoria.getNombre());
                }

                return this;
            }
        });

        JPanel contenido = new JPanel(new BorderLayout(8, 8));

        if (puedeGestionar) {

            JPanel formulario = new JPanel(new GridLayout(6, 2, 5, 5));

            formulario.add(new JLabel("Título:"));
            formulario.add(campoTitulo);

            formulario.add(new JLabel("Autor:"));
            formulario.add(campoAutor);

            formulario.add(new JLabel("ISBN:"));
            formulario.add(campoIsbn);

            formulario.add(new JLabel("Editorial:"));
            formulario.add(campoEditorial);

            formulario.add(new JLabel("Stock:"));
            formulario.add(campoStock);

            formulario.add(new JLabel("Categoría:"));
            formulario.add(comboCategorias);

            JButton crear = new JButton("Crear");
            JButton actualizar = new JButton("Actualizar");
            JButton eliminar = new JButton("Eliminar");
            JButton limpiar = new JButton("Limpiar");

            JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
            botones.add(crear);
            botones.add(actualizar);
            botones.add(eliminar);
            botones.add(limpiar);

            JPanel superior = new JPanel(new BorderLayout(5, 5));
            superior.add(formulario, BorderLayout.CENTER);
            superior.add(botones, BorderLayout.SOUTH);

            contenido.add(superior, BorderLayout.NORTH);

            crear.addActionListener(e -> guardar(false));
            actualizar.addActionListener(e -> guardar(true));
            eliminar.addActionListener(e -> eliminarLibro());
            limpiar.addActionListener(e -> limpiarFormulario());
        }

        contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton refrescar = new JButton("Actualizar catálogo");
        refrescar.addActionListener(e -> cargarDatos());

        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inferior.add(refrescar);

        contenido.add(inferior, BorderLayout.SOUTH);

        add(contenido);

        tabla.getSelectionModel().addListSelectionListener(e -> {

            if (puedeGestionar
                    && !e.getValueIsAdjusting()
                    && tabla.getSelectedRow() >= 0) {

                cargarSeleccion();
            }
        });

        cargarDatos();
    }

    private void cargarDatos() {

        try {

            comboCategorias.removeAllItems();

            for (Categoria categoria : controller.listarCategorias()) {
                comboCategorias.addItem(categoria);
            }

            modelo.setRowCount(0);

            for (Libro libro : controller.listarLibros()) {

                modelo.addRow(new Object[]{
                        libro.getId(),
                        libro.getTitulo(),
                        libro.getAutor(),
                        libro.getIsbn(),
                        libro.getEditorial(),
                        libro.getStock(),
                        buscarNombreCategoria(libro.getIdCategoria())
                });
            }

        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private String buscarNombreCategoria(int idCategoria) {

        for (int i = 0; i < comboCategorias.getItemCount(); i++) {

            Categoria categoria = comboCategorias.getItemAt(i);

            if (categoria.getId() == idCategoria) {
                return categoria.getNombre();
            }
        }

        return "Categoría " + idCategoria;
    }

    private void cargarSeleccion() {

        int fila = tabla.getSelectedRow();

        idSeleccionado = ((Number) modelo.getValueAt(fila, 0)).intValue();

        campoTitulo.setText(modelo.getValueAt(fila, 1).toString());
        campoAutor.setText(modelo.getValueAt(fila, 2).toString());
        campoIsbn.setText(modelo.getValueAt(fila, 3).toString());
        campoEditorial.setText(modelo.getValueAt(fila, 4).toString());
        campoStock.setText(modelo.getValueAt(fila, 5).toString());

        for (int i = 0; i < comboCategorias.getItemCount(); i++) {

            Categoria categoria = comboCategorias.getItemAt(i);

            if (categoria.getId() ==
                    idCategoriaDeFila(fila)) {

                comboCategorias.setSelectedItem(categoria);
                break;
            }
        }
    }

    private int idCategoriaDeFila(int fila) {

        Object nombre = modelo.getValueAt(fila, 6);

        for (int i = 0; i < comboCategorias.getItemCount(); i++) {

            Categoria categoria = comboCategorias.getItemAt(i);

            if (categoria.getNombre().equals(nombre)) {
                return categoria.getId();
            }
        }

        return -1;
    }

    private Libro obtenerLibroFormulario() {

        Categoria categoria = (Categoria) comboCategorias.getSelectedItem();

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "Primero debes registrar una categoría."
            );
        }

        int stock;

        try {
            stock = Integer.parseInt(campoStock.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El stock debe ser un número entero."
            );
        }

        return new Libro(
                idSeleccionado,
                campoTitulo.getText(),
                campoAutor.getText(),
                campoIsbn.getText(),
                campoEditorial.getText(),
                stock,
                categoria.getId()
        );
    }

    private void guardar(boolean actualizar) {

        try {

            if (actualizar && idSeleccionado == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Selecciona un libro de la tabla."
                );
                return;
            }

            Libro libro = obtenerLibroFormulario();

            boolean resultado = actualizar
                    ? controller.actualizar(libro)
                    : controller.crear(libro);

            JOptionPane.showMessageDialog(
                    this,
                    resultado
                            ? (actualizar ? "Libro actualizado." : "Libro creado.")
                            : "No se realizaron cambios."
            );

            cargarDatos();
            limpiarFormulario();

        } catch (SQLException | IllegalArgumentException e) {
            mostrarError(e);
        }
    }

    private void eliminarLibro() {

        if (idSeleccionado == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un libro."
            );
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas eliminar el libro seleccionado?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean eliminado = controller.eliminar(idSeleccionado);

            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Libro eliminado.");
            }

            cargarDatos();
            limpiarFormulario();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo eliminar el libro. Puede tener préstamos asociados.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarFormulario() {

        idSeleccionado = 0;

        campoTitulo.setText("");
        campoAutor.setText("");
        campoIsbn.setText("");
        campoEditorial.setText("");
        campoStock.setText("");

        tabla.clearSelection();
        comboCategorias.setSelectedIndex(-1);
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
