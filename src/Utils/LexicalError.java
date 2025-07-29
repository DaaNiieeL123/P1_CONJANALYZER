package Utils;

public class LexicalError {
    private String lexema;
    private String type;
    private String description;
    private int line;
    private int column;

    public LexicalError(String lexema, String tipo, int linea, int columna, String descripcion) {
        this.lexema = lexema;
        this.type = tipo;
        this.line = linea;
        this.column = columna;
        this.description = descripcion;
    }

    // Getters
    public String getLexema() { 
        return lexema; 
    }

    public String getType() { 
        return type;
    }       
                         
    public String getDescription() {
         return description; 
        }

    public int getLine() { 
        return line; 
    }

    public int getColumn() { 
        return column; 
    }

}
