package controller;

import dao.CategoriaDAO;
import model.Categoria;

import java.sql.SQLException;
import java.util.List;

public class CategoriaController {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Categoria> listar() throws SQLException {
        return categoriaDAO.listar();
    }

    public boolean crear(String nombre) throws SQLException {

        validarNombre(nombre);

        return categoriaDAO.insertar(
                new Categoria(0, nombre.trim())
        );
    }

    public boolean actualizar(Categoria categoria) throws SQLException {

        validarNombre(categoria.getNombre());

        categoria.setNombre(categoria.getNombre().trim());

        return categoriaDAO.actualizar(categoria);
    }

    public boolean eliminar(int id) throws SQLException {
        return categoriaDAO.eliminar(id);
    }

    private void validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio."
            );
        }
    }
}
