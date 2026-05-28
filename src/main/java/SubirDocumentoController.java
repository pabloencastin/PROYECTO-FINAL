

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

// Reemplaza "modelo.Conexion" por el paquete real donde tengas tu clase de conexión
import modelo.Conexion; 

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/SubirDocumentoController")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB de caché
    maxFileSize = 1024 * 1024 * 10,       // 10MB máximo por archivo
    maxRequestSize = 1024 * 1024 * 50     // 50MB máximo total
)
public class SubirDocumentoController extends HttpServlet {

    // Nombre de la carpeta física donde se guardarán los PDFs en el servidor
    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Obtener el archivo del formulario
        Part filePart = request.getPart("archivo"); 
        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect("panelAlumno.jsp?error=NoArchivo");
            return;
        }

        // Obtener el nombre original del PDF (ej: "mi_tramite.pdf")
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

        // 2. Definir rutas físicas en el servidor Tomcat
        String appPath = request.getServletContext().getRealPath("");
        String savePath = appPath + File.separator + UPLOAD_DIR;

        // Crear la carpeta 'uploads' si no existe en la estructura del servidor
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }

        // Ruta final donde se escribirá el archivo físico
        String filePath = savePath + File.separator + fileName;

        // 3. Guardar el archivo físicamente en el servidor
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("panelAlumno.jsp?error=ErrorGuardadoFisico");
            return;
        }

        // 4. GENERAR FECHA AUTOMÁTICA Y GUARDAR EN BASE DE DATOS
        Connection conn = null;
        PreparedStatement ps = null;

        // Capturamos la fecha exacta de HOY
        LocalDate fechaHoy = LocalDate.now();
        java.sql.Date fechaSQL = java.sql.Date.valueOf(fechaHoy);

        // Definimos un tipo de documento por defecto (puedes cambiarlo o recibirlo de un input)
        String tipoDocumento = "Trámite Eco-Legal";
        String estadoDocumento = "Pendiente";
        
        // La ruta relativa que guardaremos en la BD para poder leer el archivo después
        String rutaRelativa = UPLOAD_DIR + "/" + fileName; 

        try {
            // Intentar obtener la conexión (Ajusta esto según cómo se llame tu método de conexión)
            conn = Conexion.getConexion(); 
            
            // Query SQL (Ajusta los nombres de tus columnas y tabla si varían)
            String sql = "INSERT INTO documentos (nombre_archivo, fecha_envio, tipo_documento, estado, ruta_archivo) VALUES (?, ?, ?, ?, ?)";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, fileName);
            ps.setDate(2, fechaSQL); // <--- Aquí se inserta la fecha del día de hoy automáticamente
            ps.setString(3, tipoDocumento);
            ps.setString(4, estadoDocumento);
            ps.setString(5, rutaRelativa);

            int filasInsertadas = ps.executeUpdate();

            if (filasInsertadas > 0) {
                // Éxito: Redirecciona de nuevo al panel del alumno
                response.sendRedirect("panelAlumno.jsp?subida=exitosa");
            } else {
                response.sendRedirect("panelAlumno.jsp?error=ErrorBD");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("panelAlumno.jsp?error=" + e.getMessage());
        } finally {
            // Cerrar recursos de la base de datos de forma segura
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}