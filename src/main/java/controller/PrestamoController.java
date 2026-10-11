package controller;

import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import model.Estudiante;
import model.Libro;
import model.Prestamo;
import model.Usuario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrestamoController {

    private final PrestamoDAO prestamoDAO = PrestamoDAO.getInstance();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final LibroDAO libroDAO = new LibroDAO();

    public List<Estudiante> listarEstudiantes() throws SQLException {
        return estudianteDAO.listar();
    }

    public List<Libro> listarLibrosDisponibles() throws SQLException {

        List<Libro> disponibles = new ArrayList<>();

        for (Libro libro : libroDAO.listar()) {
            if (libro.getStock() > 0) {
                disponibles.add(libro);
            }
        }

        return disponibles;
    }

    public List<Prestamo> listarPrestamos(Usuario usuario)
            throws SQLException {

        if ("bibliotecario".equalsIgnoreCase(usuario.getRol())) {
            return prestamoDAO.listar();
        }

        Estudiante estudiante = estudianteDAO.buscarPorRut(usuario.getRut());

        if (estudiante == null) {
            return new ArrayList<>();
        }

        return prestamoDAO.listarPorEstudiante(estudiante.getId());
    }

    public boolean registrarPrestamo(Usuario usuario, int idLibro)
            throws SQLException {

        Estudiante estudiante;

        if ("bibliotecario".equalsIgnoreCase(usuario.getRol())) {
            throw new IllegalArgumentException(
                    "Selecciona el estudiante al que se prestará el libro."
            );
        }

        estudiante = estudianteDAO.buscarPorRut(usuario.getRut());

        if (estudiante == null) {
            throw new IllegalArgumentException(
                    "Esta cuenta no está vinculada a un estudiante registrado."
            );
        }

        Prestamo prestamo = new Prestamo(
                0,
                estudiante.getId(),
                idLibro,
                null,
                null,
                false
        );

        return prestamoDAO.registrarPrestamo(prestamo);
    }

    public boolean registrarPrestamo(
            Usuario usuario,
            int idEstudiante,
            int idLibro) throws SQLException {

        if (!"bibliotecario".equalsIgnoreCase(usuario.getRol())) {
            throw new IllegalArgumentException(
                    "Solo un bibliotecario puede seleccionar al estudiante."
            );
        }

        Prestamo prestamo = new Prestamo(
                0,
                idEstudiante,
                idLibro,
                null,
                null,
                false
        );

        return prestamoDAO.registrarPrestamo(prestamo);
    }

    public boolean devolverPrestamo(int idPrestamo) throws SQLException {
        return prestamoDAO.devolverPrestamo(idPrestamo);
    }
}
