package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    
    // 🔴 REEMPLAZA ESTOS 4 CAMPOS CON LOS DATOS QUE TE DIO NEON EN EL PASO 1:
    private static final String HOST = "INGENCASTIN"; 
    private static final String BD = "neondb"; 
    private static final String USUARIO = "capulin20.es@gmail.com"; 
    private static final String CONTRASENA = "PABLITO_MG_05_E";
    
    // El puerto de Postgres en la nube siempre es 5432. 
    // Añadimos ?sslmode=require porque las bases de datos en línea exigen encriptación de seguridad.
    private static final String URL = "jdbc:postgresql://" + HOST + ":5432/" + BD + "?sslmode=require";

    public static Connection getConexion() {
        Connection conexion = null;
        try {
            // Registramos el Driver de PostgreSQL que añadimos en el pom.xml
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
            System.out.println("¡Conexión exitosa a PostgreSQL en la nube de Neon!");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: No se encontró el Driver de Postgres: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos remota: " + e.getMessage());
        }
        return conexion;
    }
}