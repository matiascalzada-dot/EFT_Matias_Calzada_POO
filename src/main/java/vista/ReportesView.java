package vista;

import controller.ReporteController;
import model.Estudiante;
import model.LibroMasPrestado;
import model.ReportePrestamo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReportesView extends JFrame {

    private final ReporteController controller = new ReporteController();

    private final JComboBox<Estudiante> comboEstudiantes = new JComboBox<>();

    private final DefaultTableModel modeloLibros = crearModelo(
            "ID", "Título", "Autor", "Total de préstamos"
    );

    private final DefaultTableModel modeloHistorial = crearModelo(
            "ID préstamo", "Libro", "Fecha préstamo",
            "Vencimiento", "Devuelto", "Estado"
    );

    private final DefaultTableModel modeloActivos = crearModelo(
            "ID préstamo", "Estudiante", "RUT", "Libro",
            "Fecha préstamo", "Vencimiento", "Estado"
    );

    private final JTable tablaLibros = new JTable(modeloLibros);
    private final JTable tablaHistorial = new JTable(modeloHistorial);
    private final JTable tablaActivos = new JTable(modeloActivos);

    public ReportesView() {

        setTitle("Reportes de biblioteca");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane pestañas = new JTabbedPane();

        pestañas.addTab(
                "Libros más prestados",
                new JScrollPane(tablaLibros)
        );

        JPanel panelHistorial = new JPanel(new BorderLayout(8, 8));

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selector.add(new JLabel("Estudiante:"));

        JButton buscar = new JButton("Consultar historial");
        JButton limpiarHistorial = new JButton("Limpiar");

        selector.add(comboEstudiantes);
        selector.add(buscar);
        selector.add(limpiarHistorial);

        panelHistorial.add(selector, BorderLayout.NORTH);
        panelHistorial.add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);

        buscar.addActionListener(e -> cargarHistorial());
        limpiarHistorial.addActionListener(e -> modeloHistorial.setRowCount(0));

        pestañas.addTab("Historial por estudiante", panelHistorial);

        JPanel panelActivos = new JPanel(new BorderLayout());
        JButton actualizarActivos = new JButton("Actualizar informe");

        actualizarActivos.addActionListener(e -> cargarActivos());

        panelActivos.add(actualizarActivos, BorderLayout.NORTH);
        panelActivos.add(new JScrollPane(tablaActivos), BorderLayout.CENTER);

        pestañas.addTab("Préstamos actuales", panelActivos);

        JButton actualizarTodo = new JButton("Actualizar todos los reportes");
        actualizarTodo.addActionListener(e -> cargarTodo());

        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inferior.add(actualizarTodo);

        add(pestañas, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);

        configurarEstudiantes();
        cargarTodo();
    }

    private static DefaultTableModel crearModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private void configurarEstudiantes() {

        comboEstudiantes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista,
                    Object valor,
                    int indice,
                    boolean seleccionado,
                    boolean foco) {

                super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, foco
                );

                if (valor instanceof Estudiante estudiante) {
                    setText(estudiante.getNombre() + " — " + estudiante.getRut());
                }

                return this;
            }
        });
    }

    private void cargarTodo() {

        try {

            cargarLibrosMasPrestados();
            cargarEstudiantes();
            cargarActivos();

        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void cargarLibrosMasPrestados() throws SQLException {

        modeloLibros.setRowCount(0);

        for (LibroMasPrestado libro : controller.librosMasPrestados()) {

            modeloLibros.addRow(new Object[]{
                    libro.getIdLibro(),
                    libro.getTitulo(),
                    libro.getAutor(),
                    libro.getTotalPrestamos()
            });
        }
    }

    private void cargarEstudiantes() throws SQLException {

        Integer idActual = null;

        Estudiante seleccionado =
                (Estudiante) comboEstudiantes.getSelectedItem();

        if (seleccionado != null) {
            idActual = seleccionado.getId();
        }

        comboEstudiantes.removeAllItems();

        for (Estudiante estudiante : controller.listarEstudiantes()) {
            comboEstudiantes.addItem(estudiante);
        }

        if (idActual != null) {
            for (int i = 0; i < comboEstudiantes.getItemCount(); i++) {
                if (comboEstudiantes.getItemAt(i).getId() == idActual) {
                    comboEstudiantes.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void cargarHistorial() {

        Estudiante estudiante =
                (Estudiante) comboEstudiantes.getSelectedItem();

        if (estudiante == null) {
            JOptionPane.showMessageDialog(
                    this, "No hay estudiantes para consultar."
            );
            return;
        }

        try {

            modeloHistorial.setRowCount(0);

            for (ReportePrestamo prestamo :
                    controller.historialEstudiante(estudiante.getId())) {

                modeloHistorial.addRow(new Object[]{
                        prestamo.getIdPrestamo(),
                        prestamo.getTituloLibro(),
                        prestamo.getFechaPrestamo(),
                        prestamo.getFechaDevolucion(),
                        prestamo.isDevuelto() ? "Sí" : "No",
                        obtenerEstado(prestamo)
                });
            }

        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void cargarActivos() {

        try {

            modeloActivos.setRowCount(0);

            for (ReportePrestamo prestamo : controller.prestamosActivos()) {

                modeloActivos.addRow(new Object[]{
                        prestamo.getIdPrestamo(),
                        prestamo.getNombreEstudiante(),
                        prestamo.getRutEstudiante(),
                        prestamo.getTituloLibro(),
                        prestamo.getFechaPrestamo(),
                        prestamo.getFechaDevolucion(),
                        obtenerEstado(prestamo)
                });
            }

        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private String obtenerEstado(ReportePrestamo prestamo) {

        if (prestamo.isDevuelto()) {
            return "Devuelto";
        }

        LocalDate vencimiento = prestamo.getFechaDevolucion();

        if (vencimiento != null && vencimiento.isBefore(LocalDate.now())) {
            return "Atrasado";
        }

        return "En préstamo";
    }

    private void mostrarError(Exception e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Error al generar el reporte",
                JOptionPane.ERROR_MESSAGE
        );

        e.printStackTrace();
    }
}
