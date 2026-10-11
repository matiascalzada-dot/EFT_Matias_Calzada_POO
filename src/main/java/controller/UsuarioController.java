package controller;

import dao.UsuarioDAO;
import model.Usuario;

import java.sql.SQLException;

public class UsuarioController {

    private final UsuarioDAO usuarioDAO;

    public UsuarioController() {
        usuarioDAO = new UsuarioDAO();
    }

    public Usuario autenticar(String rut, String contrasena)
            throws SQLException {

        if (rut == null || rut.isBlank()
                || contrasena == null || contrasena.isBlank()) {

            return null;
        }

        return usuarioDAO.autenticar(
                rut.trim(),
                contrasena
        );
    }

    public boolean esBibliotecario(Usuario usuario) {

        return usuario != null
                && "bibliotecario".equalsIgnoreCase(usuario.getRol());
    }

    public boolean esEstudiante(Usuario usuario) {

        return usuario != null
                && "estudiante".equalsIgnoreCase(usuario.getRol());
    }
}
