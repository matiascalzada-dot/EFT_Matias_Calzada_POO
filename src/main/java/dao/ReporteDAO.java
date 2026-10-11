package dao;

import config.DatabaseConnection;
import model.LibroMasPrestado;
import model.ReportePrestamo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    private final Connection connection;

    public ReporteDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<LibroMasPrestado> listarLibrosMasPrestados()
            throws SQLException {

        List<LibroMasPrestado> lista = new ArrayList<>();

        String sql =
                "SELECT l.id, l.titulo, l.autor, COUNT(p.id) AS total_prestamos " +
                        "FROM libros l " +
                        "INNER JOIN prestamos p ON p.id_libro = l.id " +
                        "GROUP BY l.id, l.titulo, l.autor " +
                        "ORDER BY total_prestamos DESC, l.titulo ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new LibroMasPrestado(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getInt("total_prestamos")
                ));
            }
        }

        return lista;
    }

    public List<ReportePrestamo> historialPorEstudiante(int idEstudiante)
            throws SQLException {

        String sql =
                "SELECT p.id AS id_prestamo, " +
                        "e.id AS id_estudiante, e.nombre AS nombre_estudiante, " +
                        "e.rut AS rut_estudiante, " +
                        "l.id AS id_libro, l.titulo AS titulo_libro, " +
                        "p.fecha_prestamo, p.fecha_devolucion, p.devuelto " +
                        "FROM prestamos p " +
                        "INNER JOIN estudiantes e ON p.id_estudiante = e.id " +
                        "INNER JOIN libros l ON p.id_libro = l.id " +
                        "WHERE p.id_estudiante = ? " +
                        "ORDER BY p.fecha_prestamo DESC";

        return consultarPrestamos(sql, idEstudiante);
    }

    public List<ReportePrestamo> listarPrestamosActivos()
            throws SQLException {

        String sql =
                "SELECT p.id AS id_prestamo, " +
                        "e.id AS id_estudiante, e.nombre AS nombre_estudiante, " +
                        "e.rut AS rut_estudiante, " +
                        "l.id AS id_libro, l.titulo AS titulo_libro, " +
                        "p.fecha_prestamo, p.fecha_devolucion, p.devuelto " +
                        "FROM prestamos p " +
                        "INNER JOIN estudiantes e ON p.id_estudiante = e.id " +
                        "INNER JOIN libros l ON p.id_libro = l.id " +
                        "WHERE p.devuelto = FALSE " +
                        "ORDER BY p.fecha_devolucion ASC";

        return consultarPrestamos(sql, null);
    }

    private List<ReportePrestamo> consultarPrestamos(
            String sql, Integer idEstudiante) throws SQLException {

        List<ReportePrestamo> lista = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            if (idEstudiante != null) {
                ps.setInt(1, idEstudiante);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Date fechaPrestamo = rs.getDate("fecha_prestamo");
                    Date fechaDevolucion = rs.getDate("fecha_devolucion");

                    LocalDate prestamo = fechaPrestamo == null
                            ? null : fechaPrestamo.toLocalDate();

                    LocalDate devolucion = fechaDevolucion == null
                            ? null : fechaDevolucion.toLocalDate();

                    lista.add(new ReportePrestamo(
                            rs.getInt("id_prestamo"),
                            rs.getInt("id_estudiante"),
                            rs.getString("nombre_estudiante"),
                            rs.getString("rut_estudiante"),
                            rs.getInt("id_libro"),
                            rs.getString("titulo_libro"),
                            prestamo,
                            devolucion,
                            rs.getBoolean("devuelto")
                    ));
                }
            }
        }

        return lista;
    }
}