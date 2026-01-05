package org.example;

import Entidades.Ejemplar;
import Entidades.Libro;
import Entidades.Prestamo;
import Entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import org.hibernate.query.ResultListTransformer;

import java.net.IDN;
import java.net.Socket;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestorBiblioteca {
    private static final String LINEA = "--------------------------------------------------";

    Scanner sc = new Scanner(System.in);
    final EntityManagerFactory emf = Persistence.createEntityManagerFactory("biblioteca");
    final EntityManager em = emf.createEntityManager();
    private int idUsuario;
    private String tipoUsuario;


    public GestorBiblioteca() {
    }

    // ===========================================================================

    public String definirUsuarioDeSesion() {
        while (true) {
            List<Usuario> usuario = null;
            try {
                System.out.println("== Definamos que usuario eres ==");
                System.out.print("Ingresa tu DNI de usuario: ");
                String DNI = sc.nextLine().trim();
                System.out.print("Ingresa tu contraseña de usuario: ");
                String clave = sc.nextLine().trim();

                em.getTransaction().begin();
                Query q = em.createQuery("SELECT u FROM Usuario u WHERE u.dni = :DNI AND u.password = :clave");
                q.setParameter("DNI", DNI);
                q.setParameter("clave", clave);
                usuario = q.getResultList();

                if (usuario.isEmpty()) {
                    throw new Exception("ERROR: usuario no encontrado");
                }

                em.getTransaction().commit();

                idUsuario = usuario.get(0).getId();
                tipoUsuario = usuario.get(0).getTipo();

                if (tipoUsuario.equalsIgnoreCase("administrador")) {
                    return tipoUsuario;
                } else {
                    return tipoUsuario;
                }

            } catch (Exception e) {
                em.getTransaction().rollback();
                System.out.println(e.getMessage());
            }
        }
    }
    // ===========================================================================

    public void gestionDeLibros() {
        try {


            System.out.println("== Gestión de Libros ==");
            System.out.println("** Mostrar todos los libros");
            mostrarTodosLosLibros();
            System.out.println(); // Salto de linea
            em.getTransaction().begin();
            Libro libro = new Libro();

            String ISBN = solicitarElISBN();

            System.out.print("Ingresa el nombre del titulo: ");
            String titulo = sc.nextLine().trim();

            System.out.print("Ingresa el nombre del autor: ");
            String autor = sc.nextLine().trim();

            libro.setIsbn(ISBN);
            libro.setTitulo(titulo);
            libro.setAutor(autor);

            em.persist(libro);
            em.getTransaction().commit();
            System.out.println("CORRECTO: dato insertado con éxito");
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e.getMessage());
        }
    }

    private String solicitarElISBN() throws Exception {
        String codigoISBN = ""; // Valor inicial
        int prefijo = 978; // En este caso, siempre es el mismo
        int codigoDeControl = 0; // Se calcula mas adelante

        System.out.println("** Construyamos el ISBN 13");
        codigoISBN += prefijo;
        System.out.println("* Codigo ISBN actual: " + codigoISBN);

        System.out.print("Ingrese el grupo/código de pais de ISBN 13: ");
        String codigoPais = sc.nextLine().trim();
        verificarDato(codigoPais);
        codigoISBN += codigoPais;
        verificarLongitud(codigoISBN);
        System.out.println("* Codigo ISBN actual: " + codigoISBN);

        System.out.print("Ingresa el código del editorial: ");
        String editorial = sc.nextLine().trim();
        verificarDato(editorial);
        codigoISBN += editorial;
        verificarLongitud(codigoISBN);
        System.out.println("* Codigo ISBN actual: " + codigoISBN);

        System.out.print("Ingresa el código del titulo: ");
        String titulo = sc.nextLine().trim();
        verificarDato(titulo);
        codigoISBN += titulo;
        if (codigoISBN.length() != 12) {
            throw new Exception("ERROR: longitud no valida para el ISBN 13");
        }
        System.out.println("* Codigo ISBN actual: " + codigoISBN);

        int sumaDeDigitos = 0;
        // En este punto el codigoISBN tiene solo los primeros 12 dígitos del ISBN-13
        for (int i = 0; i < codigoISBN.length(); i++) {
            int digito = Character.getNumericValue(codigoISBN.charAt(i));

            // Algoritmo oficial ISO 2108 (Obtenido de Wikipedia):
            if (i % 2 == 0) {
                sumaDeDigitos += digito; // índice par → multiplicar por 1
            } else {
                sumaDeDigitos += digito * 3; // índice impar → multiplicar por 3
            }
        }
        // Cálculo del dígito de control según ISO 2108
        codigoDeControl = (10 - (sumaDeDigitos % 10)) % 10;
        // ISBN-13 completo
        codigoISBN += codigoDeControl;
        System.out.println("* Codigo ISBN Completo: " + codigoISBN);


        return codigoISBN;
    }

    private void verificarLongitud(String codigoISBN) throws Exception {
        if (codigoISBN.length() >= 12) {
            throw new Exception("ERROR: se supero la longitud valida para el ISBN 13");
        }
    }

    private void verificarDato(String dato) throws Exception {
        try {
            Integer.parseInt(dato);
        } catch (NumberFormatException e) {
            throw new Exception("ERROR: Solo dígitos enteros");
        }
    }


    // ===========================================================================

    public void gestionDeEjemplares() {
        boolean errorISBN = true;
        try {

            System.out.println("== Gestión de Ejemplares ==");
            System.out.println("** Mostrar todos los ejemplares disponibles");
            mostrarTodosLosLibros();
            em.getTransaction().begin();

            System.out.print("Ingrese el ISBN del libro: ");
            String isbn = sc.nextLine().trim();

            System.out.print("Ingrese el estado (Disponible, Prestado o Dañado): ");
            String estado = sc.nextLine().trim().toLowerCase();

            Ejemplar ejemplar = new Ejemplar();
            Libro libro = em.find(Libro.class, isbn);

            switch (estado) {
                case "disponible":
                    ejemplar.setIsbn(libro);
                    ejemplar.setEstado("Disponible");
                    break;
                case "prestado":
                    ejemplar.setIsbn(libro);
                    ejemplar.setEstado("Prestado");
                    break;
                case "dañado":
                    ejemplar.setIsbn(libro);
                    ejemplar.setEstado("Dañado");
                    break;
                default:
                    errorISBN = false;
                    throw new Exception("ERROR: estado no valido");
            }

            em.persist(ejemplar);
            em.getTransaction().commit();

            System.out.println("CORRECTO: dato insertado con éxito");
        } catch (Exception e) {
            em.getTransaction().rollback(); // Cerramos la transacción actual
            String mensaje = e.getMessage();
            if (errorISBN) {
                mensaje = "ERROR: el ISBN no se encuentra en la Base de datos";
            }
            throw new RuntimeException(mensaje);
        }
    }


    // ==========================================================================

    public void gestionDeUsuarios() {
        try {


            System.out.println("==  Gestión de Usuarios ==");

            System.out.println("** Mostrar a todos los usuarios");
            mostrarTodosLosUsuarios();
            System.out.println(); // Salto de linea
            Usuario usuario = new Usuario();

            System.out.print("Ingrese el DNI: ");
            String dni = sc.nextLine().trim();

            verificarDNI(dni);

            System.out.print("Ingrese el nombre: ");
            String nombre = sc.nextLine().trim();

            System.out.print("Ingrese el email: ");
            String email = sc.nextLine().trim();

            System.out.print("Ingrese el password: ");
            String password = sc.nextLine().trim();

            System.out.print("Ingrese el tipo de usuario (normal o administrador): ");
            String tipoUsuario = sc.nextLine().trim().toLowerCase();
            em.getTransaction().begin();
            switch (tipoUsuario) {
                case "normal":
                    usuario.setDni(dni);
                    usuario.setNombre(nombre);
                    usuario.setEmail(email);
                    usuario.setPassword(password);
                    usuario.setTipo("normal");
                    break;
                case "administrador":
                    usuario.setDni(dni);
                    usuario.setNombre(nombre);
                    usuario.setEmail(email);
                    usuario.setPassword(password);
                    usuario.setTipo("administrador");
                    break;
                default:
                    throw new Exception("ERROR: tipo de usuario no valido");
            }
            em.persist(usuario);
            em.getTransaction().commit();
            System.out.println("CORRECTO: el usuario se registro con éxito");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        }

    }

    public void mostrarTodosLosUsuarios() {
        em.getTransaction().begin();
        Query q = em.createQuery("select u from Usuario u");
        List<Usuario> usuarios = q.getResultList();

        System.out.println("\nLISTADO DE USUARIOS");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-3s | %-10s | %-20s | %-25s | %-15s | %-12s%n",
                "ID", "DNI", "Nombre", "Email", "Password", "Tipo", "PenalizacionHasta");
        System.out.println("--------------------------------------------------------------------------------");

        for (Usuario usuario : usuarios) {
            System.out.printf("%-3s | %-10s | %-20s | %-25s | %-15s | %-12s | %-12s%n",
                    usuario.getId(),
                    usuario.getDni(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.getPassword(),
                    usuario.getTipo(),
                    usuario.getPenalizacionHasta() != null ? usuario.getPenalizacionHasta() : "N/A"
            );
        }

        System.out.println("--------------------------------------------------------------------------------");

        em.getTransaction().commit();
    }


    public void mostrarTodosLosLibros() {
        em.getTransaction().begin();
        Query q = em.createQuery("select l from Libro l");
        List<Libro> libros = q.getResultList();

        System.out.println("\nLISTADO DE LIBROS");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-15s | %-30s | %-20s%n", "ISBN", "Titulo", "Autor");
        System.out.println("------------------------------------------------------------");

        for (Libro libro : libros) {
            System.out.printf("%-15s | %-30s | %-20s%n",
                    libro.getIsbn(),
                    libro.getTitulo(),
                    libro.getAutor()
            );
        }

        System.out.println("------------------------------------------------------------");

        em.getTransaction().commit();
    }


    private void verificarDNI(String dni) throws Exception {
        int contador = 0;

        if (dni.length() != 9) {
            throw new Exception("ERROR: solo 8 digitos numericos y una Letra");
        } else {
            for (char caracter : dni.toCharArray()) {
                if (Character.isDigit(caracter)) {
                    contador++;
                }
            }

            if (contador != 8) {
                throw new Exception("ERROR: solo 8 digitos numericos y una Letra");
            }

            String[] arrayDNI = dni.split("");
            boolean errorConLetra = true;

            for (int i = 97; i <= 122; i++) {
                Character letra = (char) i;
                if (arrayDNI[dni.length() - 1].equalsIgnoreCase(letra + "")) {
                    errorConLetra = false;
                }
            }

            if (errorConLetra) {
                throw new Exception("ERROR: el ultimo carácter debe ser una letra");
            }
            em.getTransaction().begin();
            Query query = em.createQuery("SELECT u FROM Usuario u WHERE u.dni = :dni");
            query.setParameter("dni", dni);
            List<Usuario> usuarios = query.getResultList();
            if (!usuarios.isEmpty()) {
                throw new Exception("ERROR: el DNI ya estaba registrado, intenta de nuevo");
            }
            em.getTransaction().commit();

        }
    }

    // ===========================================================================

    public void gestionDePrestamosUsuarioNormal() throws Exception {
        System.out.println("== Gestión de Préstamos ==");
        System.out.println("1. Hacer un préstamo");
        System.out.println("2. Hacer una devolución");
        System.out.print("Indica tu opcion: ");
        String opcion = sc.nextLine().trim();

        System.out.println(); //Salto de linea
        switch (opcion) {
            case "1": {
                verificarSiPuedeHacerPrestamo(idUsuario);
                hacerUnPrestamoUsuarioNormal();
            }
            break;
            case "2": {
                hacerUnaDevolucionUsuarioNomral();
            }
            break;
            default:
                throw new Exception("ERROR: opcion no valido");

        }
    }

    public void verificarSiPuedeHacerPrestamo(int idUsuario) throws Exception {
        em.getTransaction().begin();
        Usuario usuario = em.find(Usuario.class, idUsuario);

        if (usuario.getPenalizacionHasta() != null) {
            if (usuario.getPenalizacionHasta().isAfter(LocalDate.now())) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new Exception("ERROR: Aun sigues penalizado, vuelve un dia despues de " + usuario.getPenalizacionHasta());
            } else {
                usuario.setPenalizacionHasta(null);
                em.getTransaction().commit();
                System.out.println("CORRECTO: este usuario si puede hacer prestamos");
            }
        }

        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }

    }

    public void hacerUnaDevolucionUsuarioNomral() throws Exception {
        System.out.println("** Mostrar tus prestamos");
        ArrayList[] ids = mostrarRegistrosValidosParaDevolverUsuarioNomral();

        if (ids[0].isEmpty() && ids[1].isEmpty()) {
            throw new Exception("INFO: No tienes prestamos registrados");
        }

        System.out.print("Indica el id del ejemplar que se devolverá: ");
        String idEjemplar = sc.nextLine().trim();

        if (!ids[1].contains(idEjemplar)) {
            throw new Exception("ERROR: los ids ingresados no son validos");
        }

        // Formalizamos la devolución añadiendo una fecha
        agregarFechaDevolucionUsuarioNomral(idUsuario, Integer.parseInt(idEjemplar));

        System.out.println("CORRECTO: devolución exitosa");
    }

    private void agregarFechaDevolucionUsuarioNomral(int idUsuario, int idEjemplar) {
        // Iniciar transacción
        em.getTransaction().begin();

        // Consulta: obtener todos los préstamos que coinciden con usuario y ejemplar
        Query q = em.createQuery(
                "SELECT p FROM Prestamo p " +
                        "WHERE p.usuario.id = :idUsuario " +
                        "AND p.ejemplar.id = :idEjemplar AND p.fechaDevolucion IS NULL"
        );
        q.setParameter("idUsuario", idUsuario);
        q.setParameter("idEjemplar", idEjemplar);

        // Obtener lista de resultados
        List<Prestamo> prestamos = q.getResultList();

        // Tomamos el primer préstamo de la lista
        Prestamo prestamo = em.find(Prestamo.class, prestamos.get(0).getId());

        LocalDate fechaDevolucion = LocalDate.now();

        // Modificar la fecha de devolución
        prestamo.setFechaDevolucion(fechaDevolucion);

        em.merge(prestamo);
        // Guardar los cambios
        em.getTransaction().commit();

        if (!fechaDevolucion.isAfter(prestamo.getFechaInicio().plusDays(15))) { // Quiere decir que se entrego antes de la fecha limite (fechaInicio + 15 dias)
            System.out.println("CORECTO: no se te penalizara");
        } else { // Quiere decir que se entrego tarde, mas de 15 dias tarde
            em.getTransaction().begin();

            Usuario usuario = em.find(Usuario.class, idUsuario);

            if (usuario.getPenalizacionHasta() != null && !usuario.getPenalizacionHasta().isAfter(LocalDate.now())) {
                System.out.println("INFO: se le va va a volver a penalizar por entregar el ejemplar tarde");
                usuario.setPenalizacionHasta(LocalDate.now().plusDays(15)); // No puede prestar durante 7 dias
            } else if (usuario.getPenalizacionHasta() != null && usuario.getPenalizacionHasta().isAfter(LocalDate.now())) {
                System.out.println("INFO: se le va a volver a penalizar por entregar el ejemplar tarde");
                usuario.setPenalizacionHasta(usuario.getPenalizacionHasta().plusDays(15)); // No puede prestar durante 7 dias
            } else if (usuario.getPenalizacionHasta() == null) {
                System.out.println("INFO: se le va penalizar por entregar el ejemplar tarde");
                usuario.setPenalizacionHasta(fechaDevolucion.plusDays(15)); // No puede prestar durante 15 dias
            }

            em.merge(usuario);
            em.getTransaction().commit();

        }
    }

    private ArrayList[] mostrarRegistrosValidosParaDevolverUsuarioNomral() {
        ArrayList[] ids = new ArrayList[2];
        ArrayList<String> idsUsuarios = new ArrayList<>();
        ArrayList<String> idsEjemplares = new ArrayList<>();

        em.getTransaction().begin();
        Query q = em.createQuery(
                "SELECT u.nombre, u.id, l.titulo, e.id FROM Usuario u " +
                        "JOIN u.prestamos p " +
                        "JOIN p.ejemplar e " +
                        "JOIN e.isbn l " +
                        "WHERE p.fechaDevolucion IS NULL AND p.usuario.id = :idUsuario"
        );
        q.setParameter("idUsuario", idUsuario);

        List<Object[]> datos = q.getResultList();

        System.out.println("\nPRÉSTAMOS PENDIENTES DEL USUARIO");
        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-20s | %-3s | %-30s | %-5s%n", "Nombre Usuario", "ID", "Titulo Libro", "ID Ejemplar");
        System.out.println("---------------------------------------------------------------");

        for (Object[] dato : datos) {
            System.out.printf("%-20s | %-3s | %-30s | %-5s%n",
                    dato[0], dato[1], dato[2], dato[3]
            );
            idsUsuarios.add(dato[1].toString());
            idsEjemplares.add(dato[3].toString());
        }

        System.out.println("---------------------------------------------------------------");

        ids[0] = idsUsuarios;
        ids[1] = idsEjemplares;

        em.getTransaction().commit();
        return ids;
    }


    public void hacerUnPrestamoUsuarioNormal() throws Exception {
        System.out.println("** Mostrando si eres un usuario apto para un préstamo");
        ArrayList<String> idsDeUsuariosValidos = mostrarUsuariosUsuarioNormal();

        if (!idsDeUsuariosValidos.contains(String.valueOf(idUsuario))) {
            throw new Exception("INFO: no puedes hacer mas prestamos, devuelve algo");
        }

        System.out.println(); // Salto de linea
        System.out.println("** Mostrando a todos los ejemplares disponibles");
        ArrayList<String> idsEjemplarValidos = mostrarEjemplar();
        System.out.print("Ingrese el id del ejemplar: ");
        String idEjemplar = sc.nextLine().trim();
        if (!idsEjemplarValidos.contains(idEjemplar)) {
            throw new Exception("ERROR: el id del ejemplar no es valido");
        }


        try {
            em.getTransaction().begin();

            Ejemplar ejemplar = em.find(Ejemplar.class, idEjemplar);
            Usuario usuario = em.find(Usuario.class, idUsuario);
            LocalDate fechaInicio = LocalDate.now();

            Prestamo prestamo = new Prestamo();
            prestamo.setUsuario(usuario);
            prestamo.setEjemplar(ejemplar);
            prestamo.setFechaInicio(fechaInicio);

            // Actualizamos los datos de las otras tablas
            ejemplar.setEstado("Prestado");

            em.persist(prestamo);
            em.getTransaction().commit();

            System.out.println("CORRECTO: el préstamo se registro con éxito");

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new Exception(e.getMessage());
        }

    }

    public ArrayList<String> mostrarUsuariosUsuarioNormal() {
        ArrayList<String> idsDeUsuariosValidos = new ArrayList<>();

        em.getTransaction().begin();

        Query q = em.createQuery(
                "SELECT u, COUNT(p.usuario) " +
                        "FROM Usuario u " +
                        "LEFT JOIN u.prestamos p WITH p.fechaDevolucion IS NULL " +
                        "WHERE u.id = :idUsuario " +
                        "GROUP BY u " +
                        "HAVING COUNT(p.usuario) < 3"
        );
        q.setParameter("idUsuario", idUsuario);
        List<Object[]> usuarios = q.getResultList();
        for (Object[] obj : usuarios) {
            if (obj[0] instanceof Usuario) {

                Usuario usuario = (Usuario) obj[0];
                System.out.println("** El usuario es apto");
                System.out.println("ID: " + usuario.getId() + " Nombre: " + usuario.getNombre() + " Dni: " + usuario.getDni());
                idsDeUsuariosValidos.add(usuario.getId().toString());
            }
        }

        em.getTransaction().commit();

        return idsDeUsuariosValidos;
    }

    // ===========================================================================

    public void gestionDePrestamos() throws Exception {
        System.out.println("== Gestión de Préstamos ==");
        System.out.println("1. Hacer un préstamo");
        System.out.println("2. Hacer una devolución");
        System.out.print("Indica tu opcion: ");
        String opcion = sc.nextLine().trim();

        System.out.println(); //Salto de linea
        switch (opcion) {
            case "1": {
                hacerUnPrestamo();
            }
            break;
            case "2": {
                hacerUnaDevolucion();
            }
            break;
            default:
                throw new Exception("ERROR: opcion no valido");

        }
    }

    public void hacerUnaDevolucion() throws Exception {
        System.out.println("** Mostrar todos los usuarios validos con sus prestamos");
        ArrayList[] ids = mostrarRegistrosValidosParaDevolver();

        System.out.print("Indica el id del usuario que hará devolución: ");
        String idUsuario = sc.nextLine().trim();

        System.out.print("Indica el id del ejemplar que se devolverá: ");
        String idEjemplar = sc.nextLine().trim();

        if (!ids[0].contains(idUsuario) || !ids[1].contains(idEjemplar)) {
            throw new Exception("ERROR: los ids ingresados no son validos");
        }

        // Formalizamos la devolución añadiendo una fecha
        agregarFechaDevolucion(Integer.parseInt(idUsuario), Integer.parseInt(idEjemplar));

        System.out.println("CORRECTO: devolución exitosa");
    }

    private void agregarFechaDevolucion(int idUsuario, int idEjemplar) {
        // Iniciar transacción
        em.getTransaction().begin();

        // Consulta: obtener todos los préstamos que coinciden con usuario y ejemplar
        Query q = em.createQuery(
                "SELECT p FROM Prestamo p " +
                        "WHERE p.usuario.id = :idUsuario " +
                        "AND p.ejemplar.id = :idEjemplar AND p.fechaDevolucion IS NULL"
        );
        q.setParameter("idUsuario", idUsuario);
        q.setParameter("idEjemplar", idEjemplar);

        // Obtener lista de resultados
        List<Prestamo> prestamos = q.getResultList();

        // Tomamos el primer préstamo de la lista
        Prestamo prestamo = em.find(Prestamo.class, prestamos.get(0).getId());

        // Modificar la fecha de devolución

        LocalDate fechaDevolucion = LocalDate.now();

        prestamo.setFechaDevolucion(fechaDevolucion);

        em.merge(prestamo);
        // Guardar los cambios
        em.getTransaction().commit();

        if (!fechaDevolucion.isAfter(prestamo.getFechaInicio().plusDays(15))) { // Quiere decir que se entrego antes de la fecha limite (fechaInicio + 15 dias)
            System.out.println("CORECTO: no se le penalizara");
        } else { // Quiere decir que se entrego tarde, mas de 15 dias tarde
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, idUsuario);
            if (usuario.getPenalizacionHasta() != null && !usuario.getPenalizacionHasta().isAfter(LocalDate.now())) {
                System.out.println("INFO: se le va va a volver a penalizar por entregar el ejemplar tarde");
                usuario.setPenalizacionHasta(LocalDate.now().plusDays(15)); // No puede prestar durante 7 dias
            } else if (usuario.getPenalizacionHasta() != null && usuario.getPenalizacionHasta().isAfter(LocalDate.now())) {
                System.out.println("INFO: se le va a volver a penalizar por entregar el ejemplar tarde");
                usuario.setPenalizacionHasta(usuario.getPenalizacionHasta().plusDays(15)); // No puede prestar durante 7 dias
            } else if (usuario.getPenalizacionHasta() == null) {
                System.out.println("INFO: se le va penalizar por entregar el ejemplar tarde");
                usuario.setPenalizacionHasta(fechaDevolucion.plusDays(15)); // No puede prestar durante 15 dias
            }

            em.merge(usuario);
            em.getTransaction().commit();

        }
    }

    private ArrayList[] mostrarRegistrosValidosParaDevolver() {
        ArrayList[] ids = new ArrayList[2];

        ArrayList<String> idsUsuarios = new ArrayList<>();
        ArrayList<String> idsEjemplares = new ArrayList<>();

        em.getTransaction().begin();
        Query q = em.createQuery(
                "SELECT u.nombre, u.id, l.titulo, e.id " +
                        "FROM Usuario u " +
                        "JOIN u.prestamos p " +
                        "JOIN p.ejemplar e " +
                        "JOIN e.isbn l " +
                        "WHERE p.fechaDevolucion IS NULL"
        );

        List<Object[]> datos = q.getResultList();

        // Encabezado
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-20s | %-9s | %-30s | %-10s%n",
                "Nombre Usuario", "ID Usuario", "Titulo Libro", "ID Ejemplar");
        System.out.println("-------------------------------------------------------------------------------");

        // Datos
        for (Object[] dato : datos) {
            System.out.printf("%-20s | %-9s | %-30s | %-10s%n",
                    dato[0], // Nombre Usuario
                    dato[1], // ID Usuario
                    dato[2], // Titulo Libro
                    dato[3]  // ID Ejemplar
            );

            idsUsuarios.add(dato[1].toString());
            idsEjemplares.add(dato[3].toString());
        }

        System.out.println("-------------------------------------------------------------------------------");

        ids[0] = idsUsuarios;
        ids[1] = idsEjemplares;

        em.getTransaction().commit();

        return ids;
    }


    public void hacerUnPrestamo() throws Exception {

        actualizarFechasDePenalizacion();

        System.out.println("** Mostrando a todos los usuarios validos");
        ArrayList<String> idsDeUsuariosValidos = mostrarUsuarios();
        System.out.print("Ingrese el id del usuario: ");
        String idUsuario = sc.nextLine().trim();
        if (!idsDeUsuariosValidos.contains(idUsuario)) {
            throw new Exception("ERROR: el id del usuario no es valido");
        }

        System.out.println(); // Salto de linea
        System.out.println("** Mostrando a todos los ejemplares disponibles");
        ArrayList<String> idsEjemplarValidos = mostrarEjemplar();
        System.out.print("Ingrese el id del ejemplar: ");
        String idEjemplar = sc.nextLine().trim();
        if (!idsEjemplarValidos.contains(idEjemplar)) {
            throw new Exception("ERROR: el id del ejemplar no es valido");
        }

        LocalDate fechaInicio = LocalDate.now();

        try {
            em.getTransaction().begin();

            Ejemplar ejemplar = em.find(Ejemplar.class, idEjemplar);
            Usuario usuario = em.find(Usuario.class, idUsuario);

            Prestamo prestamo = new Prestamo();
            prestamo.setUsuario(usuario);
            prestamo.setEjemplar(ejemplar);
            prestamo.setFechaInicio(fechaInicio);

            // Actualizamos los datos de las otras tablas
            ejemplar.setEstado("Prestado");

            em.persist(prestamo);
            em.getTransaction().commit();

            System.out.println("CORRECTO: el préstamo se registro con éxito");

        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new Exception(e.getMessage());
        }

    }

    public void actualizarFechasDePenalizacion() {
        em.getTransaction().begin();
        Query query = em.createQuery("SELECT u FROM Usuario u WHERE u.penalizacionHasta IS NOT NULL ");
        List<Usuario> usuarios = query.getResultList();

        for (Usuario usuario : usuarios) {

            if (!usuario.getPenalizacionHasta().isAfter(LocalDate.now())) {
                usuario.setPenalizacionHasta(null);
            }

        }

        em.getTransaction().commit();
    }

    public ArrayList<String> mostrarUsuarios() {
        ArrayList<String> idsDeUsuariosValidos = new ArrayList<>();

        em.getTransaction().begin();

        Query q = em.createQuery(
                "SELECT u, COUNT(p.usuario) " +
                        "FROM Usuario u " +
                        "LEFT JOIN u.prestamos p WITH p.fechaDevolucion IS NULL " +
                        "WHERE u.penalizacionHasta IS NULL " +
                        "GROUP BY u " +
                        "HAVING COUNT(p.usuario) < 3"
        );
        List<Object[]> usuarios = q.getResultList();

        System.out.println("\nUSUARIOS APTOS PARA PRÉSTAMO");
        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("%-3s | %-10s | %-20s | %-25s | %-15s | %-12s | %-12s%n",
                "ID", "DNI", "Nombre", "Email", "Password", "Tipo", "Penalizacion");
        System.out.println("---------------------------------------------------------------------------------------------");

        for (Object[] obj : usuarios) {
            if (obj[0] instanceof Usuario) {
                Usuario usuario = (Usuario) obj[0];

                System.out.printf("%-3s | %-10s | %-20s | %-25s | %-15s | %-12s | %-12s%n",
                        usuario.getId(),
                        usuario.getDni(),
                        usuario.getNombre(),
                        usuario.getEmail(),
                        usuario.getPassword(),
                        usuario.getTipo(),
                        usuario.getPenalizacionHasta() != null ? usuario.getPenalizacionHasta() : "N/A"
                );

                idsDeUsuariosValidos.add(usuario.getId().toString());
            }
        }

        System.out.println("---------------------------------------------------------------------------------------------");

        em.getTransaction().commit();

        return idsDeUsuariosValidos;
    }


    public ArrayList<String> mostrarEjemplar() {
        ArrayList<String> idsEjemplarValidos = new ArrayList<>();

        em.getTransaction().begin();
        Query q = em.createQuery(
                "SELECT e.id, l.isbn, l.titulo, e.estado " +
                        "FROM Ejemplar e " +
                        "JOIN e.isbn l " +
                        "WHERE e.estado = :estado"
        );
        q.setParameter("estado", "Disponible");

        List<Object[]> resultado = q.getResultList();

        System.out.println("\nLISTADO DE EJEMPLARES DISPONIBLES");
        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-5s | %-15s | %-30s | %-12s%n", "ID", "ISBN", "Titulo", "Estado");
        System.out.println("---------------------------------------------------------------");

        for (Object[] obj : resultado) {
            System.out.printf("%-5s | %-15s | %-30s | %-12s%n",
                    obj[0], obj[1], obj[2], obj[3]
            );
            idsEjemplarValidos.add(obj[0].toString());
        }

        System.out.println("---------------------------------------------------------------");

        em.getTransaction().commit();
        return idsEjemplarValidos;
    }


    // ===========================================================================

    public void visualizacionDeInformacionUserNormal() {
        System.out.println("\n== Visualización de información del usuario ==");

        em.getTransaction().begin();
        Query q = em.createQuery(
                "SELECT p.id, p.usuario.id, u.nombre, p.ejemplar.id, l.titulo, p.fechaInicio, p.fechaDevolucion " +
                        "FROM Prestamo p " +
                        "JOIN p.usuario u " +
                        "JOIN p.ejemplar e " +
                        "JOIN e.isbn l " +
                        "WHERE p.usuario.id = :idUsuario"
        );
        q.setParameter("idUsuario", idUsuario);
        List<Object[]> resultado = q.getResultList();

        // Encabezado uniforme con admin
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-9s | %-20s | %-10s | %-30s | %-12s | %-15s%n",
                "IDPréstamo", "IDUsuario", "Nombre Usuario", "IDEjemplar", "Titulo Ejemplar", "Fecha Inicio", "Fecha Devolución");
        System.out.println("----------------------------------------------------------------------------------------------------------");

        // Filas de datos
        for (Object[] obj : resultado) {
            System.out.printf("%-12s | %-9s | %-20s | %-10s | %-30s | %-12s | %-15s%n",
                    obj[0], // ID Prestamo
                    obj[1], // ID Usuario
                    obj[2], // Nombre Usuario
                    obj[3], // ID Ejemplar
                    obj[4], // Titulo Libro
                    obj[5] != null ? obj[5] : "N/A", // Fecha Inicio
                    obj[6] != null ? obj[6] : "N/A"  // Fecha Devolución
            );
        }

        System.out.println("----------------------------------------------------------------------------------------------------------");

        em.getTransaction().commit();
    }


    public void visualizacionDeInformacionUserAdmin() {
        System.out.println("\n== Visualización de información de todos los préstamos ==");

        em.getTransaction().begin();
        Query q = em.createQuery(
                "SELECT p.id, p.usuario.id, u.nombre, p.ejemplar.id, l.titulo, p.fechaInicio, p.fechaDevolucion " +
                        "FROM Prestamo p " +
                        "JOIN p.usuario u " +
                        "JOIN p.ejemplar e " +
                        "JOIN e.isbn l "
        );
        List<Object[]> resultado = q.getResultList();

        // Encabezado ajustado
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-9s | %-20s | %-10s | %-30s | %-12s | %-15s%n",
                "IDPréstamo", "IDUsuario", "Nombre Usuario", "IDEjemplar", "Titulo Ejemplar", "Fecha Inicio", "Fecha Devolución");
        System.out.println("----------------------------------------------------------------------------------------------------------");

        // Filas de datos
        for (Object[] obj : resultado) {
            System.out.printf("%-12s | %-9s | %-20s | %-10s | %-30s | %-12s | %-15s%n",
                    obj[0], // ID Prestamo
                    obj[1], // ID Usuario
                    obj[2], // Nombre Usuario
                    obj[3], // ID Ejemplar
                    obj[4], // Titulo Libro
                    obj[5] != null ? obj[5] : "N/A", // Fecha Inicio
                    obj[6] != null ? obj[6] : "N/A"  // Fecha Devolucion
            );
        }
        System.out.println("----------------------------------------------------------------------------------------------------------");

        em.getTransaction().commit();
    }


    // ===========================================================================
}
