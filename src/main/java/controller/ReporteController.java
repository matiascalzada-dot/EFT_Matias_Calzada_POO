package controller;

import dao.EstudianteDAO;
import dao.ReporteDAO;
import model.Estudiante;
import model.LibroMasPrestado;
import model.ReportePrestamo;

import java.sql.SQLException;
import java.util.List;

public class ReporteController {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public List<LibroMasPrestado> librosMasPrestados()
            throws SQLException {
        return reporteDAO.listarLibrosMasPrestados();
    }

    public List<Estudiante> listarEstudiantes()
            throws SQLException {
        return estudianteDAO.listar();
    }

    public List<ReportePrestamo> historialEstudiante(int idEstudiante)
            throws SQLException {
        return reporteDAO.historialPorEstudiante(idEstudiante);
    }

    public List<ReportePrestamo> prestamosActivos()
            throws SQLException {
        return reporteDAO.listarPrestamosActivos();
    }
}
