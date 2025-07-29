/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Expression;

import Abstract.Expression;
import Environment.Environment;
import Types.TypeExpression;
import Types.Return;
import Types.Type;
import Types.OutputError;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa un rango de valores (ejemplo: a~z, 0~9)
 * Genera todos los caracteres ASCII entre dos valores
 * 
 * @author danie
 */
public class RangeSet extends Expression {
    private final Expression startExpression;
    private final Expression endExpression;
    
    public RangeSet(Expression start, Expression end) {
        super(TypeExpression.RANGO);
        this.startExpression = start;
        this.endExpression = end;
    }
    
    @Override
    public Return Execute(Environment environment) {
        try {
            Return startReturn = startExpression.Execute(environment);
            Return endReturn = endExpression.Execute(environment);
            
            if (startReturn == null || endReturn == null) {
                return createErrorReturn("No se pudieron evaluar los límites del rango.");
            }
            
            int startAscii = convertToAscii(startReturn.value);
            int endAscii = convertToAscii(endReturn.value);
            
            if (startAscii == -1 || endAscii == -1) {
                return createErrorReturn("No se pudieron convertir los valores del rango a códigos ASCII.");
            }
            
            if (startAscii > endAscii) {
                return createErrorReturn("El valor de inicio del rango debe ser menor o igual al valor de fin.");
            }
            
            Set<Object> elements = generateRange(startAscii, endAscii);
            return new Return(elements, Type.RANGO);
            
        } catch (Exception e) {
            return createErrorReturn("Error al evaluar rango: " + e.getMessage());
        }
    }
    
    private Set<Object> generateRange(int start, int end) {
        Set<Object> elements = new HashSet<>();
        for (int i = start; i <= end; i++) {
            elements.add((char) i);
        }
        return elements;
    }
    
    private int convertToAscii(Object value) {
        if (value instanceof Character) {
            return (int) ((Character) value);
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        }
        
        if (value instanceof String) {
            String str = (String) value;
            if (str.length() == 1) {
                return (int) str.charAt(0);
            }
            
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                OutputError.addMessage("Error: No se puede convertir '" + str + "' a código ASCII.");
                return -1;
            }
        }
        
        OutputError.addMessage("Error: Tipo de valor no soportado para rango: " + value.getClass().getSimpleName());
        return -1;
    }
    
    private Return createErrorReturn(String message) {
        OutputError.addMessage("Error: " + message);
        return new Return(new HashSet<>(), Type.RANGO);
    }
    
    @Override
    public String toString() {
        return "RangeSet{start=" + startExpression + ", end=" + endExpression + '}';
    }
}