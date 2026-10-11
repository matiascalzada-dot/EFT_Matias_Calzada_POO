package app;

import dao.CategoriaDAO;
import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;

import model.Categoria;
import model.Estudiante;
import model.Libro;
import model.Prestamo;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PruebaConcurrencia {

    public static void main(String[] args) throws Exception {

        CategoriaDAO categoriaDAO = new CategoriaDAO();
        EstudianteDAO estudianteDAO = new EstudianteDAO();
        LibroDAO libroDAO = new LibroDAO();
        PrestamoDAO prestamoDAO = PrestamoDAO.getInstance();

        List<Estudiante> estudiantes = estudianteDAO.listar();

        if (estudiantes.isEmpty()) {
            System.out.println("Primero debes registrar un estudiante.");
            return;
        }

        Estudiante estudiante = estudiantes.get(0);

        Categoria categoria = new Categoria(
                0, "TEMP-CONC-" + System.currentTimeMillis()
        );

        categoriaDAO.insertar(categoria);

        Libro libro = new Libro(
                0,
                "Libro temporal de concurrencia",
                "Prueba",
                "TC" + System.currentTimeMillis(),
                "Temporal",
                1,
                categoria.getId()
        );

        libroDAO.insertar(libro);

        Prestamo p1 = new Prestamo(
                0, estudiante.getId(), libro.getId(),
                null, null, false
        );

        Prestamo p2 = new Prestamo(
                0, estudiante.getId(), libro.getId(),
                null, null, false
        );

        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> solicitud1 = ejecutor.submit(() -> {
                inicio.await();
                return prestamoDAO.registrarPrestamo(p1);
            });

            Future<Boolean> solicitud2 = ejecutor.submit(() -> {
                inicio.await();
                return prestamoDAO.registrarPrestamo(p2);
            });

            inicio.countDown();

            boolean resultado1 = solicitud1.get();
            boolean resultado2 = solicitud2.get();

            int exitosos =
                    (resultado1 ? 1 : 0) + (resultado2 ? 1 : 0);

            int stockFinal = libroDAO.buscarPorId(libro.getId()).getStock();

            System.out.println("Préstamo 1 aceptado: " + resultado1);
            System.out.println("Préstamo 2 aceptado: " + resultado2);
            System.out.println("Préstamos aceptados: " + exitosos);
            System.out.println("Stock final: " + stockFinal);

            if (exitosos == 1 && stockFinal == 0) {
                System.out.println("PRUEBA SUPERADA");
            } else {
                System.out.println("PRUEBA NO SUPERADA");
            }

            if (resultado1) {
                prestamoDAO.devolverPrestamo(p1.getId());
                prestamoDAO.eliminar(p1.getId());
            }

            if (resultado2) {
                prestamoDAO.devolverPrestamo(p2.getId());
                prestamoDAO.eliminar(p2.getId());
            }

        } finally {
            ejecutor.shutdownNow();

            libroDAO.eliminar(libro.getId());
            categoriaDAO.eliminar(categoria.getId());
        }
    }
}
