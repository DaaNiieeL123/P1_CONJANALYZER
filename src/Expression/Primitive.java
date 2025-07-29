/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Expression;

import Abstract.Expression;
import Environment.Environment;
import Types.Type;
import Types.TypeExpression;
import Types.Return;

/**
 * Representa valores primitivos (enteros, letras, símbolos)
 * Normaliza todos los valores a Character cuando es posible
 * 
 * @author danie
 */
public class Primitive extends Expression {
    private final Object value;
    private final Type type;

    public Primitive(Object value, Type type) {
        super(TypeExpression.PRIMITIVO);
        this.value = value;
        this.type = type;
    }

    @Override
    public Return Execute(Environment environment) {
        Object normalizedValue = normalizeValue();
        return new Return(normalizedValue, type);
    }

    private Object normalizeValue() {
        String stringValue = value.toString();
        
        switch (type) {
            case ENTERO:
                return normalizeNumber(stringValue);
            case LETRA:
            case SIMBOLO:
                return normalizeCharacter(stringValue);
            default:
                return value;
        }
    }

    private Object normalizeNumber(String numberString) {
        if (isSingleDigit(numberString)) {
            return numberString.charAt(0);
        }
        return Integer.parseInt(numberString);
    }

    private Object normalizeCharacter(String characterString) {
        if (characterString.length() == 1) {
            return characterString.charAt(0);
        }
        return characterString;
    }

    private boolean isSingleDigit(String str) {
        return str.length() == 1 && str.charAt(0) >= '0' && str.charAt(0) <= '9';
    }

    @Override
    public String toString() {
        return value.toString();
    }
}