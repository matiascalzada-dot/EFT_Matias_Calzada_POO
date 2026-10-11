package model;

import java.time.LocalDate;

public class ReportePrestamo {

    private int idPrestamo;
    private int idEstudiante;
    private String nombreEstudiante;
    private String rutEstudiante;
    private int idLibro;
    private String tituloLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean devuelto;

    public ReportePrestamo(
            int idPrestamo,
            int idEstudiante,
            String nombreEstudiante,
            String rutEstudiante,
            int idLibro,
            String tituloLibro,
            LocalDate fechaPrestamo,
            LocalDate fechaDevolucion,
            boolean devuelto) {

        this.idPrestamo = idPrestamo;
        this.idEstudiante = idEstudiante;
        this.nombreEstudiante = nombreEstudiante;
        this.rutEstudiante = rutEstudiante;
        this.idLibro = idLibro;
        this.tituloLibro = tituloLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public String getRutEstudiante() {
        return rutEstudiante;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public boolean isDevuelto() {
        return devuelto;
    }
}
