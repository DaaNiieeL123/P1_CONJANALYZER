package Utils;

import java.util.*;
import java.util.regex.*;

/**
 * Analizador COMPLETAMENTE CORREGIDO - Versión Final
 */
public class OperationsAnalyzer {
    
    /**
     * Estructura para representar una operación parseada
     */
    public static class ParsedOperation {
        public final String operator;
        public final List<String> operands;
        public final String originalExpression;
        public ParsedOperation(String operator, List<String> operands, String originalExpression) {
            this.operator = operator;
            this.operands = new ArrayList<>(operands);
            this.originalExpression = originalExpression;
        }
        
        @Override
        public String toString() {
            return operator + " " + String.join(" ", operands);
        }
    }
    
    /**
     * MÉTODO PRINCIPAL - Analiza una expresión y devuelve su representación parseada
     */
    public static ParsedOperation parseExpression(String expressionInput) {

        String primaryOperator = getPrimaryOperator(expressionInput);

        List<String> operands = extractValidOperands(expressionInput);

        return new ParsedOperation(primaryOperator, operands, expressionInput);
    }
    
    /**
     * Extrae operador principal de la expresión
     */
    private static String getPrimaryOperator(String expression) {
        Pattern pattern = Pattern.compile(".*operator='([^']+)'\\}$");
        Matcher matcher = pattern.matcher(expression);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "?";
    }
    
    /**
     *  Extrae operandos con la lógica correcta
     */
    private static List<String> extractValidOperands(String expression) {
        List<String> operands = new ArrayList<>();
        
        // Extraer operando izquierdo
        String leftOperand = extractSafeOperand(expression, "left=");
        if (leftOperand.startsWith("Operators{")) {
            List<String> nestedOperands = extractValidOperands(leftOperand);
            operands.addAll(nestedOperands);
        } else if (!leftOperand.equals("null")) {
            operands.add(leftOperand);
        }
        
        //  Extraer operando derecho de forma independiente
        String rightOperand = extractSafeOperand(expression, "right=");
        if (!rightOperand.equals("null")) {
            if (rightOperand.startsWith("Operators{")) {
                List<String> nestedOperands = extractValidOperands(rightOperand);
                operands.addAll(nestedOperands);
            } else {
                operands.add(rightOperand);
            }
        }
        
        return operands;
    }
    
    /**
     *  Extrae operando de forma más precisa
     */
    private static String extractSafeOperand(String expression, String operandPrefix) {
        int index = expression.indexOf(operandPrefix);
        if (index == -1) return "null";
        
        index += operandPrefix.length();
        String resto = expression.substring(index);
        
        if (resto.startsWith("null")) {
            return "null";
        }
        
        if (resto.startsWith("ReferenceSet{")) {
            // Extraer el nombre del conjunto
            Pattern pattern = Pattern.compile("ReferenceSet\\{setName='([^']+)'\\}");
            Matcher matcher = pattern.matcher(resto);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        if (resto.startsWith("Operators{")) {
            // Extraer la expresión completa de operadores
            return extractCompleteOperator(resto);
        }
        
        return "?";
    }
    
    /**
     *  Extrae operador completo con balanceo perfecto
     */
    private static String extractCompleteOperator(String text) {
        int nivel = 0;
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            result.append(c);
            
            if (c == '{') {
                nivel++;
            } else if (c == '}') {
                nivel--;
                if (nivel == 0) {
                    return result.toString();
                }
            } else if (c == ',' && nivel == 0) {
                // Remover la coma final y retornar
                result.setLength(result.length() - 1);
                return result.toString();
            }
        }
        
        return result.toString();
    }
}