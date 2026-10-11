package controller;

import dao.EstudianteDAO;
import model.Estudiante;

import java.sql.SQLException;
import java.util.List;

public class EstudianteController {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public List<Estudiante> listar() throws SQLException {
        return estudianteDAO.listar();
    }

    public boolean crear(Estudiante estudiante, String contrasena)
            throws SQLException {

        validar(estudiante);

        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        return estudianteDAO.registrarConCuenta(estudiante, contrasena);
    }

    public boolean actualizar(Estudiante estudiante, String rutOriginal)
            throws SQLException {

        validar(estudiante);

        return estudianteDAO.actualizarConCuenta(
                estudiante, rutOriginal
        );
    }

    public boolean eliminar(int id, String rut) throws SQLException {
        return estudianteDAO.eliminarConCuenta(id, rut);
    }

    private void validar(Estudiante estudiante) {

        if (estudiante.getNombre() == null
                || estudiante.getNombre().isBlank()
                || estudiante.getRut() == null
                || estudiante.getRut().isBlank()
                || estudiante.getCurso() == null
                || estudiante.getCurso().isBlank()
                || estudiante.getCorreo() == null
                || estudiante.getCorreo().isBlank()) {

            throw new IllegalArgumentException(
                    "Completa todos los campos del estudiante."
            );
        }

        estudiante.setNombre(estudiante.getNombre().trim());
        estudiante.setRut(estudiante.getRut().trim());
        estudiante.setCurso(estudiante.getCurso().trim());
        estudiante.setCorreo(estudiante.getCorreo().trim());
    }
}
