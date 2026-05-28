package controladores;

import config.ConexionBD;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql. some text ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Configurar las cabeceras para responder en formato JSON plano al Frontend
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Capturar los datos enviados desde el formulario HTML
        String correo = request.getParameter("correo");
        String contrasenia = request.getParameter("contrasenia");
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // Usar nuestro puente del Paso 2
            conn = ConexionBD.conectar();
            
            // Consulta SQL estructurada para buscar las credenciales reales
            String sql = "SELECT nombre, rol FROM usuarios WHERE correo = ? AND contrasenia = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, correo);
            ps.setString(2, contrasenia);
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                // Si el usuario existe en MySQL, extraemos sus datos
                String nombreReal = rs.getString("nombre");
                String rolAsignado = rs.getString("rol");
                
                // Enviamos respuesta de éxito en formato JSON
                out.print("{\"status\":\"success\", \"rol\":\"" + rolAsignado + "\", \"nombre\":\"" + nombreReal + "\"}");
            } else {
                // Si no coincide ningún registro
                out.print("{\"status\":\"error\", \"message\":\"El correo o la contrasenia son incorrectos.\"}");
            }
            
        } catch (SQLException e) {
            out.print("{\"status\":\"error\", \"message\":\"Error de base de datos: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            out.print("{\"status\":\"error\", \"message\":\"Error interno en el servidor: " + e.getMessage() + "\"}");
        } finally {
            // Buenas prácticas: Cerrar los flujos de datos siempre
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
}

