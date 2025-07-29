package Utils;

public class Token {
    private String value;
    private int line;
    private int column;
    private String type;

    public Token(String value, int line, int column, String type) {
        this.value = value;
        this.line = line;
        this.column = column;
        this.type = type;
    }               

    // Getters
    public String getValue() {
        return value;
    }               

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getType() {
        return type;
    }   
    
}
