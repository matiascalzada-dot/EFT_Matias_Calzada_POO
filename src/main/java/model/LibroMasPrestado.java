package model;

public class LibroMasPrestado {

    private int idLibro;
    private String titulo;
    private String autor;
    private int totalPrestamos;

    public LibroMasPrestado(
            int idLibro, String titulo, String autor, int totalPrestamos) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.totalPrestamos = totalPrestamos;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public int getTotalPrestamos() {
        return totalPrestamos;
    }
}
