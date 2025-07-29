package Utils;

public class SyntacticalError {
    private String type;
    private String description;
    private int line;
    private int column;

    public SyntacticalError(String type, String description, int line, int column) {
        this.type = type;
        this.description = description;
        this.line = line;
        this.column = column;
    }

    // Getters
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
    
    @Override
    public String toString() {
        return String.format("[%s] Línea %d, Columna %d: %s", 
                            type, line, column, description);
    }
}
