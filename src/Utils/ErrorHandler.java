package Utils;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    public static final String CARACTER_NO_RECONOCIDO = "CARACTER_NO_RECONOCIDO";
    public static List<LexicalError> erroresLexicos = new ArrayList<>();
    public static List<SyntacticalError> erroresSintacticos = new ArrayList<>();
    
    public static void AddError(String lexema, String tipo, int linea, int columna, String descripcion) {
        if (!lexema.trim().isEmpty() && !Character.isWhitespace(lexema.charAt(0))) {
            erroresLexicos.add(new LexicalError(lexema, tipo, linea, columna, descripcion));
        }
    }
     public static void AddSyntacticalError(String tipo, String descripcion, int linea, int columna) {
        erroresSintacticos.add(new SyntacticalError(tipo, descripcion, linea, columna));
    }
    
    public static void ResetError() {
        erroresLexicos.clear();
        erroresSintacticos.clear();
    }
}
