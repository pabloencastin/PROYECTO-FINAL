package controladores;

import config.ConexionBD;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/RegistroUsuarioServlet")
public class RegistroUsuarioServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Obtener los parámetros enviados desde el Frontend
        String nombre = request.getParameter("nombre");
        String correo = request.getParameter("correo");
        String contrasenia = request.getParameter("contrasenia");
        String rol = request.getParameter("rol");

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = ConexionBD.conectar();
            
            // Query seguro parametrizado para evitar inyecciones SQL
            String sql = "INSERT INTO usuarios (nombre, correo, contrasenia, rol) VALUES (?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, contrasenia);
            ps.setString(4, rol);

            int filasInsertadas = ps.executeUpdate();

            if (filasInsertadas > 0) {
                // Si la base de datos aceptó el registro, devolvemos confirmación de éxito
                out.print("{\"status\":\"success\", \"message\":\"Usuario registrado correctamente en el ecosistema.\"}");
            } else {
                out.print("{\"status\":\"error\", \"message\":\"No se pudo completar el registro en la tabla usuarios.\"}");
            }

        } catch (SQLException e) {
            out.print("{\"status\":\"error\", \"message\":\"Error de persistencia SQL: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            out.print("{\"status\":\"error\", \"message\":\"Fallo general del backend: " + e.getMessage() + "\"}");
        } finally {
            // Cierre de flujos e hilos de procesamiento
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
}