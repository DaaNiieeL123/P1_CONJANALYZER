/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Project;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import Analyzer.Lexer;
import Analyzer.Parser;
import Environment.Environment;
import Types.Output;
import Types.OutputError;
import Utils.ErrorHandler;
import Utils.OperationsSimplifier;
import Utils.Token;
import Graphics.ImageDiagramManager;
import javax.swing.SwingUtilities;
import java_cup.runtime.Symbol;
import Gui.ApplicationUI;
import javax.swing.JTextPane;

/**
 * Clase principal del compilador de conjuntos
 * Maneja la interfaz grafica y procesamiento de código
 * Incluye soporte para propiedades de la teoría de conjuntos
 * 
 * @author danie
 */
public class Project {
    
    /**
     * Método principal - Lanza la interfaz grafica
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ApplicationUI().setVisible(true));
    }

    /**
     * Procesa el input desde la interfaz grafica
     * Método principal llamado desde la interfaz grafica
     */
    public static List<Token> parseInput(String input, JTextPane txtConsola) throws IOException {
        List<Token> tokenList = new ArrayList<>();
        Set<String> erroresUnicos = new LinkedHashSet<>();
        
        // Validar entrada
        if (input.trim().isEmpty()) {
            txtConsola.setText("⚠️ El código está vacío. Por favor ingrese código válido.");
            return tokenList;
        }
        
        try {
            // Limpiar salidas previas
            resetData();
            
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            // ANÁLISIS LÉXICO - Generar tokens
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            
            Lexer scanner = new Lexer(new StringReader(input));
            Symbol token;
            
            do {
                token = scanner.next_token();
                if (token.value != null) {
                    tokenList.add(new Token(
                        token.value.toString(), 
                        token.left, 
                        token.right, 
                        Analyzer.sym.terminalNames[token.sym]
                    ));
                }
            } while (token.value != null);
            
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            // ANÁLISIS SINTÁCTICO
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            
            Parser parser = new Parser(new Lexer(new StringReader(input)));
            parser.parse();
            
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            // EJECUCIÓN
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            
            Environment execute = new Environment("Ejecutar");
            execute.resetEnvironment();
            Output.ClearOutput();
            
            StringBuilder result = new StringBuilder();
            
            // Primera pasada: ejecutar instrucciones y recopilar salida
            for (var instruccion : parser.sentencias) {
                try {
                    instruccion.Execute(execute);
                    
                    // Recopilar salida de consola
                    for (String salida : Output.Console) {
                        result.append(salida).append("\n");
                    }
                    
                    // Limpiar salida para la siguiente instrucción
                    Output.ClearOutput();
                    
                } catch (Exception e) {
                    erroresUnicos.add("Error en instrucción: " + e.getMessage());
                    System.err.println("Error ejecutando instrucción: " + e);
                }
            }
            
            // Mostrar resultados en la consola
            txtConsola.setText(txtConsola.getText() + "\n" + result.toString());
            
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            // MOSTRAR ERRORES SI EXISTEN
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            
            // Verificar si hay errores de cualquier tipo
            boolean hayErrores = !erroresUnicos.isEmpty() || 
                               !ErrorHandler.erroresLexicos.isEmpty() || 
                               !ErrorHandler.erroresSintacticos.isEmpty() || 
                               !OutputError.OutputError.isEmpty();
            
            if (hayErrores) {
                StringBuilder errores = new StringBuilder();
                errores.append("\n❌ Se encontraron errores durante el análisis:\n");
                errores.append("═══════════════════════════════════════════════════════════════════════════════════════════\n");
                
                // Mostrar errores de ejecución si los hay
                if (!erroresUnicos.isEmpty()) {
                    for (String error : erroresUnicos) {
                        errores.append("🔸 ").append(error).append("\n");
                    }
                }
                
                // Mensaje para dirigir al usuario a los reportes
                errores.append("\n📋 Para ver el análisis detallado de errores:\n");
                errores.append("   • Vaya al menú 'REPORTES' → 'Errores'\n");
                errores.append("   • Los reportes se abren automáticamente en su navegador\n");
                errores.append("   • Revise tokens y errores léxicos/sintácticos\n");
                errores.append("═══════════════════════════════════════════════════════════════════════════════════════════\n");
                
                txtConsola.setText(txtConsola.getText() + errores.toString());
            } else {
                txtConsola.setText(txtConsola.getText() + "\n✅ Análisis completado sin errores\n");
            }
            
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            // MOSTRAR ESTADÍSTICAS FINALES
            // ═══════════════════════════════════════════════════════════════════════════════════════════
            
            showFinalStatistics(txtConsola, execute); 
            
        } catch (Exception e) {
            txtConsola.setText("💥 Error durante el análisis: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tokenList;
    }
    
    /**
     * Limpia los datos del sistema antes de una nueva ejecución
     */
    private static void resetData() {
        ErrorHandler.ResetError();
        Output.ClearOutput();
        OutputError.ResetErrors();
        ImageDiagramManager.getInstance().deleteAllGeneratedImages();
        OperationsSimplifier.clearSimplifications();
    }
    
    /**
     * Muestra las estadísticas finales en la interfaz
     */
    private static void showFinalStatistics(JTextPane txtConsola, Environment environment) {
        ImageDiagramManager imageManager = ImageDiagramManager.getInstance();
        
        txtConsola.setText(txtConsola.getText() + "\n" + "═══════════════════════════════════════════════════════════════════════════════════════════\n");
        txtConsola.setText(txtConsola.getText() + "📊 ESTADÍSTICAS FINALES:\n");
        txtConsola.setText(txtConsola.getText() + "═══════════════════════════════════════════════════════════════════════════════════════════\n");
        txtConsola.setText(txtConsola.getText() + "📈 Operaciones realizadas: " + environment.getOperationCount() + "\n");
        txtConsola.setText(txtConsola.getText() + "📋 Conjuntos definidos: " + environment.getTotalSets() + "\n");
        txtConsola.setText(txtConsola.getText() + "📊 Diagramas generados: " + imageManager.getGeneratedImagesCount() + "\n");
        txtConsola.setText(txtConsola.getText() + "✅ Análisis exitoso: " + (isSuccessfulAnalysis() ? "Sí" : "No") + "\n");
        txtConsola.setText(txtConsola.getText() + "═══════════════════════════════════════════════════════════════════════════════════════════\n");
        
        // Mostrar información adicional si hay diagramas generados
        if (imageManager.getGeneratedImagesCount() > 0) {
            txtConsola.setText(txtConsola.getText() + "💡 Usa el menú 'DIAGRAMAS' para ver y gestionar los diagramas generados\n");
        }
    }
    
    /**
     * Verifica si el análisis fue exitoso
     */
    private static boolean isSuccessfulAnalysis() {
        return ErrorHandler.erroresLexicos.isEmpty() && 
               ErrorHandler.erroresSintacticos.isEmpty() && 
               OutputError.OutputError.isEmpty();
    }
}
