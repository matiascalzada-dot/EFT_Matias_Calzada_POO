package dao;

import config.DatabaseConnection;
import model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    private final Connection connection;

    public CategoriaDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean insertar(Categoria categoria) throws SQLException {

        String sql = "INSERT INTO categorias (nombre) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, categoria.getNombre());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {

                try (ResultSet resultSet = statement.getGeneratedKeys()) {

                    if (resultSet.next()) {
                        categoria.setId(resultSet.getInt(1));
                    }
                }

                return true;
            }

            return false;
        }
    }

    public List<Categoria> listar() throws SQLException {

        List<Categoria> categorias = new ArrayList<>();

        String sql = "SELECT id, nombre FROM categorias";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Categoria categoria = new Categoria();

                categoria.setId(resultSet.getInt("id"));
                categoria.setNombre(resultSet.getString("nombre"));

                categorias.add(categoria);
            }
        }

        return categorias;
    }

    public Categoria buscarPorId(int id) throws SQLException {

        String sql = "SELECT id, nombre " +
                "FROM categorias WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Categoria categoria = new Categoria();

                    categoria.setId(resultSet.getInt("id"));
                    categoria.setNombre(resultSet.getString("nombre"));

                    return categoria;
                }
            }
        }

        return null;
    }

    public boolean actualizar(Categoria categoria) throws SQLException {

        String sql = "UPDATE categorias SET nombre = ? " +
                "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, categoria.getNombre());
            statement.setInt(2, categoria.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM categorias WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}
