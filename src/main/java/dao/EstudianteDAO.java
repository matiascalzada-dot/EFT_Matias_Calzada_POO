package dao;

import config.DatabaseConnection;
import model.Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    private final Connection connection;

    public EstudianteDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean insertar(Estudiante estudiante) throws SQLException {

        String sql = "INSERT INTO estudiantes " +
                "(nombre, rut, curso, correo) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, estudiante.getNombre());
            statement.setString(2, estudiante.getRut());
            statement.setString(3, estudiante.getCurso());
            statement.setString(4, estudiante.getCorreo());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {

                try (ResultSet resultSet = statement.getGeneratedKeys()) {

                    if (resultSet.next()) {
                        estudiante.setId(resultSet.getInt(1));
                    }
                }

                return true;
            }

            return false;
        }
    }

    public List<Estudiante> listar() throws SQLException {

        List<Estudiante> estudiantes = new ArrayList<>();

        String sql = "SELECT id, nombre, rut, curso, correo " +
                "FROM estudiantes";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Estudiante estudiante = new Estudiante();

                estudiante.setId(resultSet.getInt("id"));
                estudiante.setNombre(resultSet.getString("nombre"));
                estudiante.setRut(resultSet.getString("rut"));
                estudiante.setCurso(resultSet.getString("curso"));
                estudiante.setCorreo(resultSet.getString("correo"));

                estudiantes.add(estudiante);
            }
        }

        return estudiantes;
    }

    public Estudiante buscarPorId(int id) throws SQLException {

        String sql = "SELECT id, nombre, rut, curso, correo " +
                "FROM estudiantes WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Estudiante estudiante = new Estudiante();

                    estudiante.setId(resultSet.getInt("id"));
                    estudiante.setNombre(resultSet.getString("nombre"));
                    estudiante.setRut(resultSet.getString("rut"));
                    estudiante.setCurso(resultSet.getString("curso"));
                    estudiante.setCorreo(resultSet.getString("correo"));

                    return estudiante;
                }
            }
        }

        return null;
    }

    public boolean actualizar(Estudiante estudiante) throws SQLException {

        String sql = "UPDATE estudiantes SET " +
                "nombre = ?, rut = ?, curso = ?, correo = ? " +
                "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, estudiante.getNombre());
            statement.setString(2, estudiante.getRut());
            statement.setString(3, estudiante.getCurso());
            statement.setString(4, estudiante.getCorreo());
            statement.setInt(5, estudiante.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM estudiantes WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }
    public synchronized boolean registrarConCuenta(
            Estudiante estudiante, String contrasena) throws SQLException {

        boolean autoCommitAnterior = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            String sqlEstudiante =
                    "INSERT INTO estudiantes (nombre, rut, curso, correo) " +
                            "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = connection.prepareStatement(
                    sqlEstudiante, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getRut());
                ps.setString(3, estudiante.getCurso());
                ps.setString(4, estudiante.getCorreo());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        estudiante.setId(rs.getInt(1));
                    }
                }
            }

            String sqlUsuario =
                    "INSERT INTO usuarios (nombre, rut, correo, contraseña, rol) " +
                            "VALUES (?, ?, ?, ?, 'estudiante')";

            try (PreparedStatement ps = connection.prepareStatement(sqlUsuario)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getRut());
                ps.setString(3, estudiante.getCorreo());
                ps.setString(4, contrasena);
                ps.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            connection.rollback();
            throw e;

        } finally {
            connection.setAutoCommit(autoCommitAnterior);
        }
    }
    public synchronized boolean actualizarConCuenta(
            Estudiante estudiante, String rutOriginal) throws SQLException {

        boolean autoCommitAnterior = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            String verificarUsuario =
                    "SELECT id FROM usuarios WHERE rut = ? AND rol = 'estudiante'";

            try (PreparedStatement ps = connection.prepareStatement(verificarUsuario)) {
                ps.setString(1, rutOriginal);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "No se encontró la cuenta del estudiante."
                        );
                    }
                }
            }
            String sqlEstudiante =
                    "UPDATE estudiantes SET nombre = ?, rut = ?, " +
                            "curso = ?, correo = ? WHERE id = ?";

            try (PreparedStatement ps = connection.prepareStatement(sqlEstudiante)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getRut());
                ps.setString(3, estudiante.getCurso());
                ps.setString(4, estudiante.getCorreo());
                ps.setInt(5, estudiante.getId());
                ps.executeUpdate();
            }

            String sqlUsuario =
                    "UPDATE usuarios SET nombre = ?, rut = ?, correo = ? " +
                            "WHERE rut = ? AND rol = 'estudiante'";

            try (PreparedStatement ps = connection.prepareStatement(sqlUsuario)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getRut());
                ps.setString(3, estudiante.getCorreo());
                ps.setString(4, rutOriginal);

                int filas = ps.executeUpdate();

                ps.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            connection.rollback();
            throw e;

        } finally {
            connection.setAutoCommit(autoCommitAnterior);
        }
    }
    public synchronized boolean eliminarConCuenta(
            int idEstudiante, String rut) throws SQLException {

        boolean autoCommitAnterior = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            String sqlPrestamos =
                    "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ?";

            try (PreparedStatement ps = connection.prepareStatement(sqlPrestamos)) {
                ps.setInt(1, idEstudiante);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException(
                                "No se puede eliminar al estudiante porque tiene historial de préstamos."
                        );
                    }
                }
            }

            String sqlUsuario =
                    "DELETE FROM usuarios WHERE rut = ? AND rol = 'estudiante'";

            try (PreparedStatement ps = connection.prepareStatement(sqlUsuario)) {
                ps.setString(1, rut);
                ps.executeUpdate();
            }

            String sqlEstudiante =
                    "DELETE FROM estudiantes WHERE id = ?";

            try (PreparedStatement ps = connection.prepareStatement(sqlEstudiante)) {
                ps.setInt(1, idEstudiante);

                if (ps.executeUpdate() == 0) {
                    throw new SQLException("No se encontró el estudiante.");
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            connection.rollback();
            throw e;

        } finally {
            connection.setAutoCommit(autoCommitAnterior);
        }
    }
    public Estudiante buscarPorRut(String rut) throws SQLException {

        String sql = "SELECT id, nombre, rut, curso, correo " +
                "FROM estudiantes WHERE rut = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, rut);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    Estudiante estudiante = new Estudiante();

                    estudiante.setId(rs.getInt("id"));
                    estudiante.setNombre(rs.getString("nombre"));
                    estudiante.setRut(rs.getString("rut"));
                    estudiante.setCurso(rs.getString("curso"));
                    estudiante.setCorreo(rs.getString("correo"));

                    return estudiante;
                }
            }
        }

        return null;
    }
}
