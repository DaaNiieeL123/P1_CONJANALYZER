package Utils;

import java.util.*;
import java.util.regex.*;
import Types.TypeLaw;

/**
 * Matcher para detectar patrones de simplificación de operaciones
 * Esta clase contiene métodos para identificar patrones específicos
 */
public class LawPatternMatcher {
    
    /**
     * Detecta todos los patrones de simplificación especificados
     */
    public static TypeLaw patternDetector (String expression) {
        expression = expression.trim().replaceAll("\\s+", " ");
  
        // 1. DOBLE COMPLEMENTO (máxima prioridad)
        if (isDoubleComplement(expression)) {
            return TypeLaw.DOBLE_COMPLEMENTO;
        }
        
        // 2. COMPLEMENTO VACÍO
        if (isEmptyComplement(expression)) {
            return TypeLaw.COMPLEMENTO_VACIO;
        }
        
        // 3. COMPLEMENTO UNIVERSO
        if (isUniversalComplement(expression)) {
            return TypeLaw.COMPLEMENTO_UNIVERSO;
        }
        
        // 4. IDEMPOTENCIA
        if (isIdempotent(expression)) {
            return TypeLaw.IDEMPOTENCIA;
        }
        
        // 5. DIFERENCIA PROPIA
        if (isSelfDifference(expression)) {
            return TypeLaw.DIFERENCIA_PROPIA;
        }
        
        // 6. ABSORCIÓN
        if (isAbsorption(expression)) {
            return TypeLaw.ABSORCION;
        }
        
        // 7. DE MORGAN
        if (isDeMorgan(expression)) {
            return TypeLaw.DE_MORGAN;
        }
        
        // 8. DISTRIBUTIVA
        if (isDistributive(expression)) {
            return TypeLaw.DISTRIBUTIVA;
        }
        
        // 9. ASOCIATIVA
        if (isAssociative(expression)) {
            return TypeLaw.ASOCIATIVA;
        }
        
        // 10. CONMUTATIVA (última prioridad)
        if (isCommutative(expression)) {
                return TypeLaw.CONMUTATIVA;
        }
        
        return TypeLaw.NO_SIMPLIFICABLE;
    }
    
    /**
     * Detecta patrones de doble complemento
     */
    private static boolean isDoubleComplement(String expression) {
        // Patrón simple: ^^{A}
        if (expression.matches("\\^\\^\\{[^}]+\\}")) {
            return true;
        }
        
        // Patrón con espacio: ^^ {A}
        if (expression.matches("\\^\\^ \\{[^}]+\\}")) {
            return true;
        }
        
        // Patrón en expressiones complejas
        return expression.contains("^^{") && expression.matches(".*\\^\\^\\{[^}]+\\}.*");
    }
    
    private static boolean isSelfDifference(String expression) {
        // - {A} {A}
        Pattern pattern = Pattern.compile("- \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(expression);
        return matcher.matches() && matcher.group(1).equals(matcher.group(2));
    }
    
    private static boolean isUniversalComplement(String expression) {
        // U {A} ^ {A} - con espacio entre ^ y {A}
        Pattern pattern1 = Pattern.compile("U \\{([^}]+)\\} \\^ \\{([^}]+)\\}");
        Matcher matcher1 = pattern1.matcher(expression);
        if (matcher1.matches() && matcher1.group(1).equals(matcher1.group(2))) {
            return true;
        }
        
        // U {A} ^{A} - sin espacio entre ^ y {A}
        Pattern pattern2 = Pattern.compile("U \\{([^}]+)\\} \\^\\{([^}]+)\\}");
        Matcher matcher2 = pattern2.matcher(expression);
        if (matcher2.matches() && matcher2.group(1).equals(matcher2.group(2))) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isEmptyComplement(String expression) {
        // & {A} ^ {A} - con espacio entre ^ y {A}
        Pattern pattern1 = Pattern.compile("& \\{([^}]+)\\} \\^ \\{([^}]+)\\}");
        Matcher matcher1 = pattern1.matcher(expression);
        if (matcher1.matches() && matcher1.group(1).equals(matcher1.group(2))) {
            return true;
        }
        
        // & {A} ^{A} - sin espacio entre ^ y {A}
        Pattern pattern2 = Pattern.compile("& \\{([^}]+)\\} \\^\\{([^}]+)\\}");
        Matcher matcher2 = pattern2.matcher(expression);
        if (matcher2.matches() && matcher2.group(1).equals(matcher2.group(2))) {
            return true;
        }
        
        //  & ^^{A} ^{A} - doble complemento con complemento simple
        Pattern pattern3 = Pattern.compile("& \\^\\^\\{([^}]+)\\} \\^\\{([^}]+)\\}");
        Matcher matcher3 = pattern3.matcher(expression);
        if (matcher3.matches() && matcher3.group(1).equals(matcher3.group(2))) {
            return true;
        }
        
        //  & {A} ^{A} - conjunto con su complemento (caso ya cubierto arriba)
        //  & A ^{A} - conjunto A con su complemento sin llaves adicionales
        Pattern pattern4 = Pattern.compile("& ([A-Z]) \\^\\{([^}]+)\\}");
        Matcher matcher4 = pattern4.matcher(expression);
        if (matcher4.matches() && matcher4.group(1).equals(matcher4.group(2))) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isDeMorgan(String expression) {
        // ^ & {A} {B} (De Morgan 1)
        if (expression.matches("\\^ & \\{[^}]+\\} \\{[^}]+\\}")) {
            return true;
        }
        
        // U ^{A}^{B} (De Morgan 1 resultado)
        if (expression.matches("U \\^\\{[^}]+\\}\\^\\{[^}]+\\}")) {
            return true;
        }
        
        // U ^{A} ^{B} (De Morgan 1 resultado con espacio)
        if (expression.matches("U \\^\\{[^}]+\\} \\^\\{[^}]+\\}")) {
            return true;
        }
        
        // ^ U {A} {B} (De Morgan 2)
        if (expression.matches("\\^ U \\{[^}]+\\} \\{[^}]+\\}")) {
            return true;
        }
        
        // & ^{A}^{B} (De Morgan 2 resultado)
        if (expression.matches("& \\^\\{[^}]+\\}\\^\\{[^}]+\\}")) {
            return true;
        }
        
        // & ^{A} ^{B} (De Morgan 2 resultado con espacio)
        if (expression.matches("& \\^\\{[^}]+\\} \\^\\{[^}]+\\}")) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isAbsorption(String expression) {
        // & U {A} {B} {A} (Absorción 1)
        Pattern pattern1 = Pattern.compile("& U \\{([^}]+)\\} \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher1 = pattern1.matcher(expression);
        if (matcher1.matches() && matcher1.group(1).equals(matcher1.group(3))) {
            return true;
        }
        
        // & {A} U {A} {B} (Absorción 1 variante)
        Pattern pattern2 = Pattern.compile("& \\{([^}]+)\\} U \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher2 = pattern2.matcher(expression);
        if (matcher2.matches() && matcher2.group(1).equals(matcher2.group(2))) {
            return true;
        }
        
        // U & {A} {B} {A} (Absorción 2)
        Pattern pattern3 = Pattern.compile("U & \\{([^}]+)\\} \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher3 = pattern3.matcher(expression);
        if (matcher3.matches() && matcher3.group(1).equals(matcher3.group(3))) {
            return true;
        }
        
        // U {A} & {A} {B} (Absorción 2 variante)
        Pattern pattern4 = Pattern.compile("U \\{([^}]+)\\} & \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher4 = pattern4.matcher(expression);
        if (matcher4.matches() && matcher4.group(1).equals(matcher4.group(2))) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isDistributive(String expression) {
        // U {A} & {B} {C} (Distributiva 1)
        if (expression.matches("U \\{[^}]+\\} & \\{[^}]+\\} \\{[^}]+\\}")) {
            return true;
        }
        
        // & U {A} {B} U {A} {C} (Distributiva 1 resultado)
        Pattern pattern1 = Pattern.compile("& U \\{([^}]+)\\} \\{([^}]+)\\} U \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher1 = pattern1.matcher(expression);
        if (matcher1.matches() && matcher1.group(1).equals(matcher1.group(3))) {
            return true;
        }
        
        // U & {C} {B} {A} (Distributiva 2)
        if (expression.matches("U & \\{[^}]+\\} \\{[^}]+\\} \\{[^}]+\\}")) {
            return true;
        }
        
        // U & U {A} {B} {C} {A} (Distributiva 2 variante)
        Pattern pattern2 = Pattern.compile("U & U \\{([^}]+)\\} \\{([^}]+)\\} \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher2 = pattern2.matcher(expression);
        if (matcher2.matches() && matcher2.group(1).equals(matcher2.group(4))) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isAssociative(String expression) {
        // U {A} U {B} {C} (Asociativa) - ESTE ES EL CASO DE op11
        if (expression.matches("U \\{[^}]+\\} U \\{[^}]+\\} \\{[^}]+\\}")) {
            return true;
        }
        
        // U U {A} {B} {C} (Asociativa variante) - ESTE ES EL CASO DE op12
        if (expression.matches("U U \\{[^}]+\\} \\{[^}]+\\} \\{[^}]+\\}")) {
            return true;
        }
        
        // Para intersección también
        if (expression.matches("& \\{[^}]+\\} & \\{[^}]+\\} \\{[^}]+\\}")) {
            return true;
        }
        
        if (expression.matches("& & \\{[^}]+\\} \\{[^}]+\\} \\{[^}]+\\}")) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isIdempotent(String expression) {
        // U {A} {A} (Idempotente 1)
        Pattern pattern1 = Pattern.compile("U \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher1 = pattern1.matcher(expression);
        if (matcher1.matches() && matcher1.group(1).equals(matcher1.group(2))) {
            return true;
        }
        
        // & {A}{A} (Idempotente 2) - note que puede no tener espacio
        Pattern pattern2 = Pattern.compile("& \\{([^}]+)\\}\\s*\\{([^}]+)\\}");
        Matcher matcher2 = pattern2.matcher(expression);
        if (matcher2.matches() && matcher2.group(1).equals(matcher2.group(2))) {
            return true;
        }
        
        //  Casos específicos de idempotencia con complementos
        // U ^{A} ^{A} (Idempotente con complementos)
        Pattern pattern3 = Pattern.compile("U \\^\\{([^}]+)\\} \\^\\{([^}]+)\\}");
        Matcher matcher3 = pattern3.matcher(expression);
        if (matcher3.matches() && matcher3.group(1).equals(matcher3.group(2))) {
            return true;
        }
        
        // & ^{A} ^{A} (Idempotente con complementos)
        Pattern pattern4 = Pattern.compile("& \\^\\{([^}]+)\\} \\^\\{([^}]+)\\}");
        Matcher matcher4 = pattern4.matcher(expression);
        if (matcher4.matches() && matcher4.group(1).equals(matcher4.group(2))) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isCommutative(String expression) {
        // U {A} {B} donde A ≠ B
        Pattern pattern1 = Pattern.compile("U \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher1 = pattern1.matcher(expression);
        if (matcher1.matches() && !matcher1.group(1).equals(matcher1.group(2))) {
            return true;
        }
        
        // & {A} {B} donde A ≠ B
        Pattern pattern2 = Pattern.compile("& \\{([^}]+)\\} \\{([^}]+)\\}");
        Matcher matcher2 = pattern2.matcher(expression);
        if (matcher2.matches() && !matcher2.group(1).equals(matcher2.group(2))) {
            return true;
        }

        return false;
    }
    
    /**
     * Extrae los conjuntos de una expresión
     */
    public static List<String> extractSets(String expression) {
        List<String> setList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(expression);
        
        while (matcher.find()) {
            setList.add(matcher.group(1));
        }
        
        return setList;
    }
    
    /**
     * Extrae el operador principal de una expresión
     */
    public static String getPrimaryOperator(String expression) {
        expression = expression.trim();
        
        if (expression.startsWith("^^")) return "^^";
        if (expression.startsWith("^")) return "^";
        if (expression.startsWith("U")) return "U";
        if (expression.startsWith("&")) return "&";
        if (expression.startsWith("-")) return "-";
        
        return "?";
    }
}