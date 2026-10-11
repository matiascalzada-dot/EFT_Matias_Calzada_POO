package controller;

import dao.CategoriaDAO;
import dao.LibroDAO;
import model.Categoria;
import model.Libro;

import java.sql.SQLException;
import java.util.List;

public class LibroController {

    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Libro> listarLibros() throws SQLException {
        return libroDAO.listar();
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listar();
    }

    public boolean crear(Libro libro) throws SQLException {

        validarLibro(libro);

        return libroDAO.insertar(libro);
    }

    public boolean actualizar(Libro libro) throws SQLException {

        validarLibro(libro);

        return libroDAO.actualizar(libro);
    }

    public boolean eliminar(int id) throws SQLException {
        return libroDAO.eliminar(id);
    }

    private void validarLibro(Libro libro) {

        if (libro.getTitulo() == null || libro.getTitulo().isBlank()
                || libro.getAutor() == null || libro.getAutor().isBlank()
                || libro.getIsbn() == null || libro.getIsbn().isBlank()
                || libro.getEditorial() == null || libro.getEditorial().isBlank()) {

            throw new IllegalArgumentException(
                    "Completa todos los campos del libro."
            );
        }

        if (libro.getStock() < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo."
            );
        }

        if (libro.getIdCategoria() <= 0) {
            throw new IllegalArgumentException(
                    "Selecciona una categoría."
            );
        }
    }
}
