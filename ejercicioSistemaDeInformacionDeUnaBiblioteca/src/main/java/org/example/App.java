package org.example;

import Entidades.Libro;
import Entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.util.List;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        GestorBiblioteca gestorBiblioteca = new GestorBiblioteca();
        boolean bandera = true;

        String tipoUsuario = gestorBiblioteca.definirUsuarioDeSesion();
        if (tipoUsuario.equals("administrador")) {
            while (bandera) {
                try {
                    System.out.println("\n===== MENÚ DE LA BIBLIOTECA USUARIO ADMINISTRADOR =====");
                    System.out.println("1. Gestión de Libros");
                    System.out.println("2. Gestión de Ejemplares");
                    System.out.println("3. Gestión de Usuarios");
                    System.out.println("4. Gestión de Préstamos");
                    System.out.println("5. Visualización de información");
                    System.out.println("6. Salir");
                    System.out.print("Seleccione una opción: ");

                    String opcion = sc.nextLine();
                    System.out.println(); //Salto de linea
                    switch (opcion) {

                        case "1":
                            gestorBiblioteca.gestionDeLibros();
                            break;
                        case "2":
                            gestorBiblioteca.gestionDeEjemplares();
                            break;
                        case "3":
                            gestorBiblioteca.gestionDeUsuarios();
                            break;
                        case "4":
                            gestorBiblioteca.gestionDePrestamos();
                            break;
                        case "5":
                            gestorBiblioteca.visualizacionDeInformacionUserAdmin();
                            break;
                        case "6":
                            System.out.println("CORRECTO: saliendo del sistema...");
                            bandera = false; // Termina el while
                            break;
                        default:
                            System.out.println("ERROR: opción inválida. Intente nuevamente.");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            while (bandera) {
                try {
                    System.out.println("\n===== MENÚ DE LA BIBLIOTECA USUARIO NORMAL =====");
                    System.out.println("1. Gestión de Préstamos");
                    System.out.println("2. Visualización de información");
                    System.out.println("3. Salir");
                    System.out.print("Seleccione una opción: ");

                    String opcion = sc.nextLine();
                    System.out.println(); //Salto de linea
                    switch (opcion) {
                        case "1":
                            gestorBiblioteca.gestionDePrestamosUsuarioNormal();
                            break;
                        case "2":
                            gestorBiblioteca.visualizacionDeInformacionUserNormal();
                            break;
                        case "3":
                            System.out.println("CORRECTO: saliendo del sistema...");
                            bandera = false; // Termina el while
                            break;
                        default:
                            System.out.println("ERROR: opción inválida. Intente nuevamente.");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }


    }
}