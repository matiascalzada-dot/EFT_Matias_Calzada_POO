package vista;

import controller.PrestamoController;
import model.Estudiante;
import model.Libro;
import model.Prestamo;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

public class PrestamosView extends JFrame {

    private final Usuario usuario;
    private final boolean bibliotecario;
    private final PrestamoController controller = new PrestamoController();

    private final JComboBox<Estudiante> comboEstudiantes = new JComboBox<>();
    private final JComboBox<Libro> comboLibros = new JComboBox<>();

    private final JButton botonRegistrar = new JButton("Registrar préstamo");
    private final JButton botonDevolver = new JButton("Registrar devolución");
    private final JButton botonActualizar = new JButton("Actualizar listado");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{
                    "ID préstamo", "ID estudiante", "ID libro",
                    "Fecha préstamo", "Vencimiento", "Devuelto", "Estado"
            }, 0
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    public PrestamosView(Usuario usuario) {

        this.usuario = usuario;
        this.bibliotecario =
                "bibliotecario".equalsIgnoreCase(usuario.getRol());

        setTitle("Préstamos y devoluciones");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        configurarSelectores();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        if (bibliotecario) {
            formulario.add(new JLabel("Estudiante:"));
            formulario.add(comboEstudiantes);
        }

        formulario.add(new JLabel("Libro disponible:"));
        formulario.add(comboLibros);

        JPanel botones = new JPanel(new FlowLayout());
        botones.add(botonRegistrar);
        botones.add(botonDevolver);
        botones.add(botonActualizar);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        botonRegistrar.addActionListener(e -> registrarEnSegundoPlano());
        botonDevolver.addActionListener(e -> devolverEnSegundoPlano());
        botonActualizar.addActionListener(e -> cargarDatos());

        cargarDatos();
    }

    private void configurarSelectores() {

        comboEstudiantes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean foco) {

                super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, foco);

                if (valor instanceof Estudiante estudiante) {
                    setText(estudiante.getNombre() + " — " + estudiante.getRut());
                }

                return this;
            }
        });

        comboLibros.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean foco) {

                super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, foco);

                if (valor instanceof Libro libro) {
                    setText(libro.getTitulo() + " — stock: " + libro.getStock());
                }

                return this;
            }
        });
    }

    private void cargarDatos() {

        try {
            cargarSelectores();
            cargarTabla();

        } catch (SQLException e) {
            mostrarError("No fue posible cargar los datos.", e);
        }
    }

    private void cargarSelectores() throws SQLException {

        comboLibros.removeAllItems();

        for (Libro libro : controller.listarLibrosDisponibles()) {
            comboLibros.addItem(libro);
        }

        comboEstudiantes.removeAllItems();

        if (bibliotecario) {
            for (Estudiante estudiante : controller.listarEstudiantes()) {
                comboEstudiantes.addItem(estudiante);
            }
        }

        botonRegistrar.setEnabled(comboLibros.getItemCount() > 0);
    }

    private void cargarTabla() throws SQLException {

        modelo.setRowCount(0);

        for (Prestamo prestamo : controller.listarPrestamos(usuario)) {

            String estado;

            if (prestamo.isDevuelto()) {
                estado = "Devuelto";
            } else if (prestamo.getFechaDevolucion() != null
                    && prestamo.getFechaDevolucion().isBefore(LocalDate.now())) {
                estado = "Atrasado";
            } else {
                estado = "En préstamo";
            }

            modelo.addRow(new Object[]{
                    prestamo.getId(),
                    prestamo.getIdEstudiante(),
                    prestamo.getIdLibro(),
                    prestamo.getFechaPrestamo(),
                    prestamo.getFechaDevolucion(),
                    prestamo.isDevuelto() ? "Sí" : "No",
                    estado
            });
        }
    }

    private void registrarEnSegundoPlano() {

        Libro libro = (Libro) comboLibros.getSelectedItem();

        if (libro == null) {
            JOptionPane.showMessageDialog(
                    this, "No hay libros disponibles para prestar."
            );
            return;
        }

        Estudiante estudianteSeleccionado =
                bibliotecario
                        ? (Estudiante) comboEstudiantes.getSelectedItem()
                        : null;

        if (bibliotecario && estudianteSeleccionado == null) {
            JOptionPane.showMessageDialog(
                    this, "Selecciona un estudiante."
            );
            return;
        }

        botonRegistrar.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                if (bibliotecario) {
                    return controller.registrarPrestamo(usuario,
                            estudianteSeleccionado.getId(),
                            libro.getId()
                    );
                }

                return controller.registrarPrestamo(
                        usuario,
                        libro.getId()
                );
            }

            @Override
            protected void done() {

                try {
                    boolean registrado = get();

                    if (registrado) {
                        JOptionPane.showMessageDialog(
                                PrestamosView.this,
                                "Préstamo registrado correctamente."
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                PrestamosView.this,
                                "No hay stock disponible para este libro."
                        );
                    }

                    cargarDatos();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("Se interrumpió el proceso.", e);

                } catch (ExecutionException e) {
                    mostrarError(
                            "No se pudo registrar el préstamo.",
                            e.getCause() instanceof Exception
                                    ? (Exception) e.getCause() : e
                    );

                } finally {
                    botonRegistrar.setEnabled(comboLibros.getItemCount() > 0);
                }
            }
        };

        worker.execute();
    }

    private void devolverEnSegundoPlano() {

        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(
                    this, "Selecciona un préstamo de la tabla."
            );
            return;
        }

        int idPrestamo =
                ((Number) modelo.getValueAt(fila, 0)).intValue();

        String devuelto = String.valueOf(modelo.getValueAt(fila, 5));

        if ("Sí".equals(devuelto)) {
            JOptionPane.showMessageDialog(
                    this, "Este préstamo ya está devuelto."
            );
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Registrar la devolución del préstamo " + idPrestamo + "?",
                "Confirmar devolución",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        botonDevolver.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                return controller.devolverPrestamo(idPrestamo);
            }

            @Override
            protected void done() {

                try {
                    boolean resultado = get();

                    JOptionPane.showMessageDialog(
                            PrestamosView.this,
                            resultado
                                    ? "Devolución registrada."
                                    : "El préstamo ya fue devuelto o no existe."
                    );

                    cargarDatos();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("Se interrumpió el proceso.", e);

                } catch (ExecutionException e) {
                    mostrarError(
                            "No se pudo registrar la devolución.",
                            e.getCause() instanceof Exception
                                    ? (Exception) e.getCause() : e
                    );

                } finally {
                    botonDevolver.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void mostrarError(String mensaje, Exception e) {

        JOptionPane.showMessageDialog(
                this,
                mensaje + "\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

        e.printStackTrace();
    }
}
