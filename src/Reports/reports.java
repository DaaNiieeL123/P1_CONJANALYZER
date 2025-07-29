package Reports;

import Utils.Token;
import Utils.ErrorHandler;
import Utils.LexicalError;
import Utils.SyntacticalError;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generador de reportes HTML para tokens y errores
 * @author danie
 */
public class reports {
    
    private static final String HTML_DIR = "Html";
    
    /**
     * Genera un reporte HTML de tokens
     */
    public static String createTokenReportHTML(List<Token> tokens) {
        try {
            // Crear directorio si no existe
            File htmlDir = new File(HTML_DIR);
            if (!htmlDir.exists()) {
                htmlDir.mkdirs();
            }
            
            // 🗑️ LIMPIAR ARCHIVOS ANTERIORES DE TOKENS
            limpiarArchivosAnteriores(htmlDir, "reporte_tokens_");
            
            // Generar nombre de archivo único
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = HTML_DIR + File.separator + "reporte_tokens_" + timestamp + ".html";
            
            StringBuilder html = new StringBuilder();
            
            // Encabezado HTML con CSS moderno
            html.append(getHTMLHeader("Reporte de Tokens"));
            
            // Contenido del reporte
            html.append("<div class='container'>\n");
            html.append("<div class='header'>\n");
            html.append("<h1>📝 Reporte de Tokens</h1>\n");
            html.append("<p class='timestamp'>Generado el: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("</p>\n");
            html.append("</div>\n");
            
            if (tokens.isEmpty()) {
                html.append("<div class='no-data'>\n");
                html.append("<p>⚠️ No se encontraron tokens para mostrar.</p>\n");
                html.append("</div>\n");
            } else {
                // Estadísticas
                html.append("<div class='stats'>\n");
                html.append("<div class='stat-card'>\n");
                html.append("<h3>").append(tokens.size()).append("</h3>\n");
                html.append("<p>Total de Tokens</p>\n");
                html.append("</div>\n");
                html.append("</div>\n");
                
                // Tabla de tokens
                html.append("<div class='table-container'>\n");
                html.append("<table class='tokens-table'>\n");
                html.append("<thead>\n");
                html.append("<tr>\n");
                html.append("<th>#</th>\n");
                html.append("<th>Lexema</th>\n");
                html.append("<th>Tipo</th>\n");
                html.append("<th>Línea</th>\n");
                html.append("<th>Columna</th>\n");
                html.append("</tr>\n");
                html.append("</thead>\n");
                html.append("<tbody>\n");
                
                int contador = 1;
                for (Token token : tokens) {
                    html.append("<tr>\n");
                    html.append("<td>").append(contador++).append("</td>\n");
                    html.append("<td class='lexema'>").append(escapeHtml(token.getValue())).append("</td>\n");
                    html.append("<td class='tipo'>").append(escapeHtml(token.getType())).append("</td>\n");
                    html.append("<td>").append(token.getLine()).append("</td>\n");
                    html.append("<td>").append(token.getColumn()).append("</td>\n");
                    html.append("</tr>\n");
                }
                
                html.append("</tbody>\n");
                html.append("</table>\n");
                html.append("</div>\n");
            }
            
            html.append("</div>\n");
            html.append(getHTMLFooter());
            
            // Escribir archivo
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(html.toString());
            }
            
            return fileName;
            
        } catch (IOException e) {
            System.err.println("Error generando reporte de tokens: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera un reporte HTML de errores
     */
    public static String generateErrorReportHTML() {
        try {
            // Crear directorio si no existe
            File htmlDir = new File(HTML_DIR);
            if (!htmlDir.exists()) {
                htmlDir.mkdirs();
            }
            
            // 🗑️ LIMPIAR ARCHIVOS ANTERIORES DE ERRORES
            limpiarArchivosAnteriores(htmlDir, "reporte_errores_");
            
            // Generar nombre de archivo único
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = HTML_DIR + File.separator + "reporte_errores_" + timestamp + ".html";
            
            StringBuilder html = new StringBuilder();
            
            // Encabezado HTML con CSS moderno
            html.append(getHTMLHeader("Reporte de Errores"));
            
            // Contenido del reporte
            html.append("<div class='container'>\n");
            html.append("<div class='header'>\n");
            html.append("<h1>⚠️ Reporte de Errores</h1>\n");
            html.append("<p class='timestamp'>Generado el: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("</p>\n");
            html.append("</div>\n");
            
            List<LexicalError> erroresLexicos = ErrorHandler.erroresLexicos;
            List<SyntacticalError> erroresSintacticos = ErrorHandler.erroresSintacticos;
            
            int totalErrores = erroresLexicos.size() + erroresSintacticos.size();
            
            if (totalErrores == 0) {
                html.append("<div class='no-errors'>\n");
                html.append("<p>✅ No se encontraron errores en el análisis.</p>\n");
                html.append("</div>\n");
            } else {
                // Estadísticas
                html.append("<div class='stats'>\n");
                html.append("<div class='stat-card error'>\n");
                html.append("<h3>").append(totalErrores).append("</h3>\n");
                html.append("<p>Total de Errores</p>\n");
                html.append("</div>\n");
                html.append("<div class='stat-card lexical'>\n");
                html.append("<h3>").append(erroresLexicos.size()).append("</h3>\n");
                html.append("<p>Errores Léxicos</p>\n");
                html.append("</div>\n");
                html.append("<div class='stat-card syntactic'>\n");
                html.append("<h3>").append(erroresSintacticos.size()).append("</h3>\n");
                html.append("<p>Errores Sintácticos</p>\n");
                html.append("</div>\n");
                html.append("</div>\n");
                
                // Errores Léxicos
                if (!erroresLexicos.isEmpty()) {
                    html.append("<div class='error-section'>\n");
                    html.append("<h2>🔍 Errores Léxicos</h2>\n");
                    html.append("<div class='table-container'>\n");
                    html.append("<table class='errors-table'>\n");
                    html.append("<thead>\n");
                    html.append("<tr>\n");
                    html.append("<th>#</th>\n");
                    html.append("<th>Descripción</th>\n");
                    html.append("<th>Línea</th>\n");
                    html.append("<th>Columna</th>\n");
                    html.append("<th>Lexema</th>\n");
                    html.append("</tr>\n");
                    html.append("</thead>\n");
                    html.append("<tbody>\n");
                    
                    int contador = 1;
                    for (LexicalError error : erroresLexicos) {
                        html.append("<tr class='lexical-error'>\n");
                        html.append("<td>").append(contador++).append("</td>\n");
                        html.append("<td class='descripcion'>").append(escapeHtml(error.getDescription())).append("</td>\n");
                        html.append("<td>").append(error.getLine()).append("</td>\n");
                        html.append("<td>").append(error.getColumn()).append("</td>\n");
                        html.append("<td class='lexema'>").append(escapeHtml(error.getLexema())).append("</td>\n");
                        html.append("</tr>\n");
                    }
                    
                    html.append("</tbody>\n");
                    html.append("</table>\n");
                    html.append("</div>\n");
                    html.append("</div>\n");
                }
                
                // Errores Sintácticos
                if (!erroresSintacticos.isEmpty()) {
                    html.append("<div class='error-section'>\n");
                    html.append("<h2>⚙️ Errores Sintácticos</h2>\n");
                    html.append("<div class='table-container'>\n");
                    html.append("<table class='errors-table'>\n");
                    html.append("<thead>\n");
                    html.append("<tr>\n");
                    html.append("<th>#</th>\n");
                    html.append("<th>Descripción</th>\n");
                    html.append("<th>Línea</th>\n");
                    html.append("<th>Columna</th>\n");
                    html.append("<th>Token</th>\n");
                    html.append("</tr>\n");
                    html.append("</thead>\n");
                    html.append("<tbody>\n");
                    
                    int contador = 1;
                    for (SyntacticalError error : erroresSintacticos) {
                        html.append("<tr class='syntactic-error'>\n");
                        html.append("<td>").append(contador++).append("</td>\n");
                        html.append("<td class='descripcion'>").append(escapeHtml(error.getDescription())).append("</td>\n");
                        html.append("<td>").append(error.getLine()).append("</td>\n");
                        html.append("<td>").append(error.getColumn()).append("</td>\n");
                        html.append("<td class='lexema'>").append(escapeHtml(error.getType())).append("</td>\n");
                        html.append("</tr>\n");
                    }
                    
                    html.append("</tbody>\n");
                    html.append("</table>\n");
                    html.append("</div>\n");
                    html.append("</div>\n");
                }
            }
            
            html.append("</div>\n");
            html.append(getHTMLFooter());
            
            // Escribir archivo
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(html.toString());
            }
            
            return fileName;
            
        } catch (IOException e) {
            System.err.println("Error generando reporte de errores: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera el encabezado HTML con CSS moderno
     */
    private static String getHTMLHeader(String titulo) {
        return "<!DOCTYPE html>\n" +
               "<html lang='es'>\n" +
               "<head>\n" +
               "    <meta charset='UTF-8'>\n" +
               "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
               "    <title>" + titulo + "</title>\n" +
               "    <style>\n" +
               getCSSStyles() +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n";
    }
    
    /**
     * Genera el pie HTML
     */
    private static String getHTMLFooter() {
        return "</body>\n</html>";
    }
    
/**
 * Estilos CSS modernos para los reportes - TEMA IGUAL A LA INTERFAZ
 */
private static String getCSSStyles() {
    return """
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }
    
    body {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        background-color: #2b2b2b;
        color: #ffffff;
        min-height: 100vh;
        line-height: 1.6;
    }
    
    .container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 20px;
    }
    
    .header {
        background-color: #3c3c3c;
        border: 1px solid #555555;
        border-radius: 8px;
        padding: 30px;
        margin-bottom: 25px;
        text-align: center;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
    }
    
    .header h1 {
        color: #ffffff;
        font-size: 2.5em;
        font-weight: bold;
        margin-bottom: 15px;
        text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
    }
    
    .timestamp {
        color: #cccccc;
        font-size: 1.1em;
        font-style: italic;
    }
    
    .stats {
        display: flex;
        gap: 20px;
        margin-bottom: 25px;
        flex-wrap: wrap;
        justify-content: center;
    }
    
    .stat-card {
        background-color: #3c3c3c;
        border: 1px solid #555555;
        border-radius: 8px;
        padding: 25px;
        text-align: center;
        min-width: 180px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
        transition: all 0.3s ease;
    }
    
    .stat-card:hover {
        background-color: #454545;
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
    }
    
    .stat-card h3 {
        font-size: 2.2em;
        font-weight: bold;
        margin-bottom: 10px;
        color: #ffffff;
    }
    
    .stat-card.error h3 { color: #ff6b6b; }
    .stat-card.lexical h3 { color: #ffd93d; }
    .stat-card.syntactic h3 { color: #6bcf7f; }
    
    .stat-card p {
        color: #ffffff;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 1px;
        font-size: 0.9em;
    }
    
    .table-container {
        background-color: #3c3c3c;
        border: 1px solid #555555;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 25px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
        overflow-x: auto;
    }
    
    .error-section h2 {
        color: #ffffff;
        margin: 25px 0 20px 0;
        font-size: 2em;
        font-weight: bold;
        text-align: center;
    }
    
    table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 15px;
        background-color: #2b2b2b;
        border-radius: 6px;
        overflow: hidden;
    }
    
    th, td {
        padding: 12px;
        text-align: left;
        border-bottom: 1px solid #555555;
        color: #ffffff;
    }
    
    th {
        background-color: #1e1e1e;
        color: #ffffff;
        font-weight: bold;
        font-size: 0.95em;
        text-transform: uppercase;
        letter-spacing: 1px;
        border-bottom: 2px solid #555555;
    }
    
    tr:nth-child(even) {
        background-color: #333333;
    }
    
    tr:nth-child(odd) {
        background-color: #2b2b2b;
    }
    
    tr:hover {
        background-color: #454545;
        transition: background-color 0.3s ease;
    }
    
    .lexema {
        font-family: 'Consolas', 'Courier New', monospace;
        background-color: #1e1e1e;
        padding: 6px 10px;
        border-radius: 4px;
        font-weight: bold;
        color: #ffffff;
        border: 1px solid #555555;
        font-size: 0.9em;
    }
    
    .tipo {
        color: #87ceeb;
        font-weight: bold;
    }
    
    .descripcion {
        color: #ffffff;
        font-weight: 600;
    }
    
    .lexical-error {
        border-left: 4px solid #ffd93d;
        background-color: rgba(255, 217, 61, 0.1);
    }
    
    .syntactic-error {
        border-left: 4px solid #6bcf7f;
        background-color: rgba(107, 207, 127, 0.1);
    }
    
    .no-data, .no-errors {
        background-color: #3c3c3c;
        border: 1px solid #555555;
        border-radius: 8px;
        padding: 40px;
        text-align: center;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
    
    .no-data p {
        font-size: 1.3em;
        color: #cccccc;
        font-weight: 600;
    }
    
    .no-errors p {
        font-size: 1.3em;
        color: #6bcf7f;
        font-weight: 600;
    }
    
    /* Scrollbar personalizada para que coincida con la interfaz */
    ::-webkit-scrollbar {
        width: 8px;
        height: 8px;
    }
    
    ::-webkit-scrollbar-track {
        background: #2b2b2b;
    }
    
    ::-webkit-scrollbar-thumb {
        background: #555555;
        border-radius: 4px;
    }
    
    ::-webkit-scrollbar-thumb:hover {
        background: #666666;
    }
    
    @media (max-width: 768px) {
        .container {
            padding: 15px;
        }
        
        .header h1 {
            font-size: 2em;
        }
        
        .stats {
            flex-direction: column;
            align-items: center;
        }
        
        .stat-card {
            width: 100%;
            max-width: 280px;
        }
        
        th, td {
            padding: 10px 8px;
            font-size: 0.9em;
        }
        
        .lexema {
            padding: 4px 6px;
            font-size: 0.8em;
        }
    }
    """;
}
    
    /**
     * Escapa caracteres especiales HTML
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    /**
     * 🗑️ MÉTODO PARA LIMPIAR ARCHIVOS ANTERIORES
     * Elimina todos los archivos HTML anteriores del mismo tipo para evitar acumulación
     * @param htmlDir Directorio donde están los archivos HTML
     * @param prefix Prefijo del archivo (ej: "reporte_tokens_", "reporte_errores_")
     */
    private static void limpiarArchivosAnteriores(File htmlDir, String prefix) {
        try {
            // Verificar que el directorio existe
            if (!htmlDir.exists() || !htmlDir.isDirectory()) {
                return;
            }
            
            // Obtener todos los archivos del directorio
            File[] archivos = htmlDir.listFiles();
            if (archivos == null) {
                return;
            }
            
            // Contador para estadísticas
            int archivosEliminados = 0;
            
            // Buscar y eliminar archivos que coincidan con el prefijo
            for (File archivo : archivos) {
                if (archivo.isFile() && 
                    archivo.getName().startsWith(prefix) && 
                    archivo.getName().endsWith(".html")) {
                    
                    if (archivo.delete()) {
                        archivosEliminados++;
                        System.out.println("🗑️ Archivo eliminado: " + archivo.getName());
                    } else {
                        System.err.println("⚠️ No se pudo eliminar: " + archivo.getName());
                    }
                }
            }
            
            // Mostrar estadísticas si se eliminaron archivos
            if (archivosEliminados > 0) {
                System.out.println("✅ Se eliminaron " + archivosEliminados + " archivo(s) anterior(es) de " + prefix.replace("_", ""));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error limpiando archivos anteriores: " + e.getMessage());
        }
    }
}
