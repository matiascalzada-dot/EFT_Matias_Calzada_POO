package dao;

import config.DatabaseConnection;
import model.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    private final Connection connection;

    public LibroDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean insertar(Libro libro) throws SQLException {

        String sql = "INSERT INTO libros " +
                "(titulo, autor, isbn, editorial, stock, id_categoria) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, libro.getTitulo());
            statement.setString(2, libro.getAutor());
            statement.setString(3, libro.getIsbn());
            statement.setString(4, libro.getEditorial());
            statement.setInt(5, libro.getStock());
            statement.setInt(6, libro.getIdCategoria());

            int filasAfectadas = statement.executeUpdate();

            if (filasAfectadas > 0) {

                try (ResultSet resultSet = statement.getGeneratedKeys()) {

                    if (resultSet.next()) {
                        libro.setId(resultSet.getInt(1));
                    }
                }

                return true;
            }

            return false;
        }
    }

    public List<Libro> listar() throws SQLException {

        List<Libro> libros = new ArrayList<>();

        String sql = "SELECT id, titulo, autor, isbn, editorial, stock, id_categoria " +
                "FROM libros";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Libro libro = new Libro();

                libro.setId(resultSet.getInt("id"));
                libro.setTitulo(resultSet.getString("titulo"));
                libro.setAutor(resultSet.getString("autor"));
                libro.setIsbn(resultSet.getString("isbn"));
                libro.setEditorial(resultSet.getString("editorial"));
                libro.setStock(resultSet.getInt("stock"));
                libro.setIdCategoria(resultSet.getInt("id_categoria"));

                libros.add(libro);
            }
        }

        return libros;
    }

    public Libro buscarPorId(int id) throws SQLException {

        String sql = "SELECT id, titulo, autor, isbn, editorial, stock, id_categoria " +
                "FROM libros WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Libro libro = new Libro();

                    libro.setId(resultSet.getInt("id"));
                    libro.setTitulo(resultSet.getString("titulo"));
                    libro.setAutor(resultSet.getString("autor"));
                    libro.setIsbn(resultSet.getString("isbn"));
                    libro.setEditorial(resultSet.getString("editorial"));
                    libro.setStock(resultSet.getInt("stock"));
                    libro.setIdCategoria(resultSet.getInt("id_categoria"));

                    return libro;
                }
            }
        }

        return null;
    }

    public boolean actualizar(Libro libro) throws SQLException {

        String sql = "UPDATE libros SET " +
                "titulo = ?, autor = ?, isbn = ?, editorial = ?, " +
                "stock = ?, id_categoria = ? " +
                "WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, libro.getTitulo());
            statement.setString(2, libro.getAutor());
            statement.setString(3, libro.getIsbn());
            statement.setString(4, libro.getEditorial());
            statement.setInt(5, libro.getStock());
            statement.setInt(6, libro.getIdCategoria());
            statement.setInt(7, libro.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM libros WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}
