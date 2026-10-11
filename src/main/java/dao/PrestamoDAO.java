package dao;

import config.DatabaseConnection;
import model.Prestamo;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private final Connection connection;
    private static PrestamoDAO instance;


    private static final int DIAS_PRESTAMO = 7;

    private PrestamoDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public static synchronized PrestamoDAO getInstance() {

        if (instance == null) {
            instance = new PrestamoDAO();
        }

        return instance;
    }
    public boolean insertar(Prestamo prestamo) throws SQLException {

        String sql = "INSERT INTO prestamos " +
                "(id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, prestamo.getIdEstudiante());
            statement.setInt(2, prestamo.getIdLibro());
            statement.setDate(
                    3,
                    Date.valueOf(prestamo.getFechaPrestamo())
            );
            statement.setDate(
                    4,
                    Date.valueOf(prestamo.getFechaDevolucion())
            );
            statement.setBoolean(5, prestamo.isDevuelto());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {

                try (ResultSet resultSet = statement.getGeneratedKeys()) {

                    if (resultSet.next()) {
                        prestamo.setId(resultSet.getInt(1));
                    }
                }

                return true;
            }

            return false;
        }
    }

    public List<Prestamo> listar() throws SQLException {

        List<Prestamo> prestamos = new ArrayList<>();

        String sql = "SELECT id, id_estudiante, id_libro, " +
                "fecha_prestamo, fecha_devolucion, devuelto " +
                "FROM prestamos";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                prestamos.add(mapearPrestamo(resultSet));
            }
        }

        return prestamos;
    }

    public Prestamo buscarPorId(int id) throws SQLException {

        String sql = "SELECT id, id_estudiante, id_libro, " +
                "fecha_prestamo, fecha_devolucion, devuelto " +
                "FROM prestamos WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapearPrestamo(resultSet);
                }
            }
        }

        return null;
    }

    public boolean actualizar(Prestamo prestamo) throws SQLException {

        String sql = "UPDATE prestamos SET " +
                "id_estudiante = ?, " +
                "id_libro = ?, " +
                "fecha_prestamo = ?, " +
                "fecha_devolucion = ?, " +
                "devuelto = ? " +
                "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, prestamo.getIdEstudiante());
            statement.setInt(2, prestamo.getIdLibro());
            statement.setDate(
                    3,
                    Date.valueOf(prestamo.getFechaPrestamo())
            );
            statement.setDate(
                    4,
                    Date.valueOf(prestamo.getFechaDevolucion())
            );
            statement.setBoolean(5, prestamo.isDevuelto());
            statement.setInt(6, prestamo.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM prestamos WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    public synchronized boolean registrarPrestamo(Prestamo prestamo) throws SQLException {

        boolean autocommitAnterior = connection.getAutoCommit();

        try {

            connection.setAutoCommit(false);

            LocalDate fechaPrestamo = LocalDate.now();
            LocalDate fechaDevolucion = fechaPrestamo.plusDays(DIAS_PRESTAMO);

            String sqlStock =
                    "SELECT stock FROM libros WHERE id = ? FOR UPDATE";

            try (PreparedStatement statementStock =
                         connection.prepareStatement(sqlStock)) {

                statementStock.setInt(1, prestamo.getIdLibro());

                try (ResultSet resultSet = statementStock.executeQuery()) {

                    if (!resultSet.next()) {

                        connection.rollback();

                        throw new SQLException(
                                "El libro no existe."
                        );
                    }

                    int stockActual = resultSet.getInt("stock");

                    if (stockActual <= 0) {

                        connection.rollback();

                        return false;
                    }
                }
            }

            String sqlPrestamo =
                    "INSERT INTO prestamos " +
                            "(id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) " +
                            "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statementPrestamo =
                         connection.prepareStatement(
                                 sqlPrestamo,
                                 Statement.RETURN_GENERATED_KEYS)) {

                statementPrestamo.setInt(
                        1,
                        prestamo.getIdEstudiante()
                );

                statementPrestamo.setInt(
                        2,
                        prestamo.getIdLibro()
                );

                statementPrestamo.setDate(
                        3,
                        Date.valueOf(fechaPrestamo)
                );

                statementPrestamo.setDate(
                        4,
                        Date.valueOf(fechaDevolucion)
                );

                statementPrestamo.setBoolean(
                        5,
                        false
                );

                int filas = statementPrestamo.executeUpdate();

                if (filas == 0) {

                    connection.rollback();

                    return false;
                }

                try (ResultSet generatedKeys =
                             statementPrestamo.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        prestamo.setId(
                                generatedKeys.getInt(1)
                        );
                    }
                }
            }

            String sqlStockUpdate =
                    "UPDATE libros " +
                            "SET stock = stock - 1 " +
                            "WHERE id = ? AND stock > 0";

            try (PreparedStatement statementStockUpdate =
                         connection.prepareStatement(sqlStockUpdate)) {

                statementStockUpdate.setInt(
                        1,
                        prestamo.getIdLibro()
                );

                int filas = statementStockUpdate.executeUpdate();

                if (filas == 0) {

                    connection.rollback();

                    return false;
                }
            }

            connection.commit();

            prestamo.setFechaPrestamo(fechaPrestamo);
            prestamo.setFechaDevolucion(fechaDevolucion);
            prestamo.setDevuelto(false);

            return true;

        } catch (SQLException e) {

            connection.rollback();

            throw e;

        } finally {

            connection.setAutoCommit(autocommitAnterior);
        }
    }

    public synchronized boolean devolverPrestamo(int idPrestamo) throws SQLException {

        boolean autocommitAnterior = connection.getAutoCommit();

        try {

            connection.setAutoCommit(false);

            int idLibro;
            boolean devuelto;

            String sqlBuscar =
                    "SELECT id_libro, devuelto " +
                            "FROM prestamos " +
                            "WHERE id = ? FOR UPDATE";

            try (PreparedStatement statement =
                         connection.prepareStatement(sqlBuscar)) {

                statement.setInt(1, idPrestamo);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (!resultSet.next()) {

                        connection.rollback();

                        throw new SQLException(
                                "El préstamo no existe."
                        );
                    }

                    idLibro = resultSet.getInt("id_libro");
                    devuelto = resultSet.getBoolean("devuelto");
                }
            }

            if (devuelto) {

                connection.rollback();

                return false;
            }

            String sqlDevolver =
                    "UPDATE prestamos " +
                            "SET devuelto = true " +
                            "WHERE id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(sqlDevolver)) {

                statement.setInt(1, idPrestamo);

                int filas = statement.executeUpdate();

                if (filas == 0) {

                    connection.rollback();

                    return false;
                }
            }

            String sqlStock =
                    "UPDATE libros " +
                            "SET stock = stock + 1 " +
                            "WHERE id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(sqlStock)) {

                statement.setInt(1, idLibro);

                int filas = statement.executeUpdate();

                if (filas == 0) {

                    connection.rollback();

                    return false;
                }
            }

            connection.commit();

            return true;

        } catch (SQLException e) {

            connection.rollback();

            throw e;

        } finally {

            connection.setAutoCommit(autocommitAnterior);
        }
    }

    private Prestamo mapearPrestamo(ResultSet resultSet) throws SQLException {

        Prestamo prestamo = new Prestamo();

        prestamo.setId(resultSet.getInt("id"));
        prestamo.setIdEstudiante(resultSet.getInt("id_estudiante"));
        prestamo.setIdLibro(resultSet.getInt("id_libro"));

        Date fechaPrestamo = resultSet.getDate("fecha_prestamo");
        Date fechaDevolucion = resultSet.getDate("fecha_devolucion");

        if (fechaPrestamo != null) {
            prestamo.setFechaPrestamo(
                    fechaPrestamo.toLocalDate()
            );
        }

        if (fechaDevolucion != null) {
            prestamo.setFechaDevolucion(
                    fechaDevolucion.toLocalDate()
            );
        }

        prestamo.setDevuelto(
                resultSet.getBoolean("devuelto")
        );

        return prestamo;
    }
    public List<Prestamo> listarPorEstudiante(int idEstudiante)
            throws SQLException {

        List<Prestamo> prestamos = new ArrayList<>();

        String sql = "SELECT id, id_estudiante, id_libro, " +
                "fecha_prestamo, fecha_devolucion, devuelto " +
                "FROM prestamos WHERE id_estudiante = ? " +
                "ORDER BY fecha_prestamo DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idEstudiante);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapearPrestamo(rs));
                }
            }
        }

        return prestamos;
    }
}
