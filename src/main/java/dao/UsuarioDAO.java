package dao;
import config.DatabaseConnection;
import model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    private final Connection connection;

    public UsuarioDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean insertar(Usuario usuario) throws SQLException {

        String sql = "INSERT INTO usuarios " +
                "(nombre, rut, correo, contraseña, rol) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getRut());
            statement.setString(3, usuario.getCorreo());
            statement.setString(4, usuario.getContrasena());
            statement.setString(5, usuario.getRol());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {

                try (ResultSet resultSet = statement.getGeneratedKeys()) {

                    if (resultSet.next()) {
                        usuario.setId(resultSet.getInt(1));
                    }
                }

                return true;
            }

            return false;
        }
    }

    public List<Usuario> listar() throws SQLException {

        List<Usuario> usuarios = new ArrayList<>();

        String sql = "SELECT id, nombre, rut, correo, contraseña, rol " +
                "FROM usuarios";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Usuario usuario = new Usuario();

                usuario.setId(resultSet.getInt("id"));
                usuario.setNombre(resultSet.getString("nombre"));
                usuario.setRut(resultSet.getString("rut"));
                usuario.setCorreo(resultSet.getString("correo"));
                usuario.setContrasena(resultSet.getString("contraseña"));
                usuario.setRol(resultSet.getString("rol"));

                usuarios.add(usuario);
            }
        }

        return usuarios;
    }

    public Usuario buscarPorId(int id) throws SQLException {

        String sql = "SELECT id, nombre, rut, correo, contraseña, rol " +
                "FROM usuarios WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Usuario usuario = new Usuario();

                    usuario.setId(resultSet.getInt("id"));
                    usuario.setNombre(resultSet.getString("nombre"));
                    usuario.setRut(resultSet.getString("rut"));
                    usuario.setCorreo(resultSet.getString("correo"));
                    usuario.setContrasena(resultSet.getString("contraseña"));
                    usuario.setRol(resultSet.getString("rol"));

                    return usuario;
                }
            }
        }

        return null;
    }

    public boolean actualizar(Usuario usuario) throws SQLException {

        String sql = "UPDATE usuarios SET " +
                "nombre = ?, rut = ?, correo = ?, contraseña = ?, rol = ? " +
                "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getRut());
            statement.setString(3, usuario.getCorreo());
            statement.setString(4, usuario.getContrasena());
            statement.setString(5, usuario.getRol());
            statement.setInt(6, usuario.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    public Usuario autenticar(String rut, String contrasena) throws SQLException {

        String sql = "SELECT id, nombre, rut, correo, contraseña, rol " +
                "FROM usuarios " +
                "WHERE rut = ? AND contraseña = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, rut);
            statement.setString(2, contrasena);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Usuario usuario = new Usuario();

                    usuario.setId(resultSet.getInt("id"));
                    usuario.setNombre(resultSet.getString("nombre"));
                    usuario.setRut(resultSet.getString("rut"));
                    usuario.setCorreo(resultSet.getString("correo"));
                    usuario.setContrasena(resultSet.getString("contraseña"));
                    usuario.setRol(resultSet.getString("rol"));

                    return usuario;
                }
            }
        }

        return null;
    }
}
