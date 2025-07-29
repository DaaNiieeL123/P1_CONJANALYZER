package Utils;

import Environment.Environment;
import Types.TypeLaw;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * Simplificador de operaciones para conjuntos.
 * 
 */
public class OperationsSimplifier {
    
    // Mapa para almacenar simplificaciones 
    private static final Map<String, SimplificationResult> simplifications = new LinkedHashMap<>();
    
    /**
     * Clase para representar el result de una simplificación
     */
    public static class SimplificationResult {
        public String[] laws;
        public String originalSet;
        public String simplifiedSet;
        
        public SimplificationResult(String[] laws, String original, String simplified) {
            this.laws = laws;
            this.originalSet = original;
            this.simplifiedSet = simplified;
        }
    }
    
    /**
     * Método principal para analizar y simplificar
     */
    public static String simplifyAndAnalyze(String operationName, String originalExpression, Environment environment) {
        try {
            // Verificar si ya existe una simplificación para esta operación
            if (simplifications.containsKey(operationName)) {
                SimplificationResult existingSimplification = simplifications.get(operationName);
                
                // Si la nueva expresión es más compleja, reemplazarla
                if (originalExpression.length() > existingSimplification.originalSet.length()) {
                    simplifications.remove(operationName);
                } else {
                    return operationName; 
                }
            }
            
            //  Aplicar simplificación iterativa
            SimplificationResult result = applyIterativeSimplification(originalExpression);
            
            // Guardar result
            simplifications.put(operationName, result);
            
            // Generar JSON
            createJsonFile();
            
            return operationName;
            
        } catch (Exception e) {
            System.err.println("❌ Error en simplificación: " + e.getMessage());
            return operationName;
        }
    }
    
    /**
     * Aplica simplificación iterativa hasta que no se pueda simplificar más
     *  Maneja reglas directas e iterativas correctamente
     */
    private static SimplificationResult applyIterativeSimplification(String originalExpression) {
        List<String> appliedLaws = new ArrayList<>();
        String currentExpression = originalExpression;
        final int MAX_ITERATIONS = 10;
        int iterationCount = 0;
        TypeLaw previousPattern = null;
        
        while (iterationCount < MAX_ITERATIONS) {
            iterationCount++;
            
            // Convertir formato para detección
            String convertedExpression = convertExpressionFormat(currentExpression);
            
            // Detectar patrón
            TypeLaw pattern = LawPatternMatcher.patternDetector (convertedExpression);
            
            // Si no hay patrón simplificable, terminar
            if (pattern == TypeLaw.NO_SIMPLIFICABLE) {
                break;
            }
            
            // Aplicar simplificación
            String newExpression = applyPatternSimplification(pattern, convertedExpression, currentExpression);
            String appliedLaw = getLawName(pattern);
            
            // Si no cambió, terminar
            if (newExpression.equals(currentExpression)) {
                break;
            }
            
            // Actualizar para siguiente iteración
            currentExpression = newExpression;
            appliedLaws.add(appliedLaw);
            
            //  Lógica mejorada para reglas directas vs iterativas
            if (isDirectRule(pattern)) {
                // Para reglas directas, solo aplicar si es la primera vez que aparece este patrón
                if (previousPattern == pattern) {
                    break;
                }
                // Si es una regla directa diferente, continuar (permite operaciones complejas)
            } else {
                //  Lógica especial para conmutativa - aplicar solo una vez
                if (pattern == TypeLaw.CONMUTATIVA) {
                    break;
                }
                
                // Si es el mismo patrón, podría ser un bucle infinito, así que terminar
                if (previousPattern == pattern && appliedLaws.size() > 1) {
                    break;
                }
            }
            
            previousPattern = pattern;
        }
        
        // Si no se aplicó ninguna ley, marcar como "No simplificable"
        if (appliedLaws.isEmpty()) {
            appliedLaws.add("No simplificable");
        }
        
        return new SimplificationResult(
            appliedLaws.toArray(new String[0]), 
            originalExpression, 
            currentExpression
        );
    }
    
    /**
     *  Determina si un patrón es una regla directa 
     *  Conmutativa NO debe ser directa para evitar bucles infinitos
     */
    private static boolean isDirectRule(TypeLaw lawType) {
        switch (lawType) {
            // Reglas completamente directas: se aplican una sola vez y terminan
            case DIFERENCIA_PROPIA:
            case COMPLEMENTO_UNIVERSO:
            case COMPLEMENTO_VACIO:
                return true;
            
            // Reglas que pueden ser directas o iterativas dependiendo del contexto
            case DOBLE_COMPLEMENTO:
            case DE_MORGAN:
            case IDEMPOTENCIA:
            case ABSORCION:
                return false; 
            
            // Reglas claramente iterativas: pueden aplicarse múltiples veces
            case DISTRIBUTIVA:
            case ASOCIATIVA:
            case CONMUTATIVA:  
                return false;
            
            default:
                return false; 
        }
    }
    
    /**
     *  Aplica simplificación específica según el patrón detectado
     */
    private static String applyPatternSimplification(TypeLaw lawPattern, String convertedExpression, String originalExpression) {
        List<String> setList = LawPatternMatcher.extractSets(convertedExpression);
        
        switch (lawPattern) {
            case DOBLE_COMPLEMENTO:
                // Para doble complemento, buscar el patrón ^^{A} y reemplazarlo por A
                if (convertedExpression.contains("^^{")) {
                    String result = convertedExpression.replaceAll("\\^\\^\\{([^}]+)\\}", "$1");
                    return result;
                }
                return setList.size() > 0 ? setList.get(0) : originalExpression;
            case DIFERENCIA_PROPIA:
                return "VACIO";
            case COMPLEMENTO_UNIVERSO:
                return "UNIVERSO";
            case COMPLEMENTO_VACIO:
                return "VACIO";
            case DE_MORGAN:
                return generateDeMorgan(convertedExpression, setList);
            case ABSORCION:
                return generateAbsorption(convertedExpression, setList);
            case DISTRIBUTIVA:
                return generateDistributive(convertedExpression, setList);
            case ASOCIATIVA:
                return generateAssociative(convertedExpression, setList);
            case IDEMPOTENCIA:
                // Para idempotencia, necesitamos conservar el formato original
                if (convertedExpression.matches("U \\^\\{([^}]+)\\} \\^\\{([^}]+)\\}")) {
                    // U ^{A} ^{A} → ^A
                    return "^ " + setList.get(0);
                } else if (convertedExpression.matches("& \\^\\{([^}]+)\\} \\^\\{([^}]+)\\}")) {
                    // & ^{A} ^{A} → ^A
                    return "^ " + setList.get(0);
                } else {
                    // U {A} {A} → A o & {A} {A} → A
                    return setList.size() > 0 ? setList.get(0) : originalExpression;
                }
            case CONMUTATIVA:
                return generateCommutative(convertedExpression, setList);
            default:
                return originalExpression;
        }
    }
    
    /**
     *  Obtiene el nombre de la ley según el patrón
     */
    private static String getLawName(TypeLaw lawType) {
        switch (lawType) {
            case DOBLE_COMPLEMENTO:
                return "Ley del Doble Complemento";
            case DIFERENCIA_PROPIA:
                return "Ley de Diferencia Propia";
            case COMPLEMENTO_UNIVERSO:
                return "Ley del Complemento Total";
            case COMPLEMENTO_VACIO:
                return "Ley del Complemento Total";
            case DE_MORGAN:
                return "Leyes de De Morgan";
            case ABSORCION:
                return "Propiedades de Absorción";
            case DISTRIBUTIVA:
                return "Propiedades Distributivas";
            case ASOCIATIVA:
                return "Propiedades Asociativas";
            case IDEMPOTENCIA:
                return "Propiedades Idempotentes";
            case CONMUTATIVA:
                return "Propiedades Conmutativas";
            default:
                return "No simplificable";
        }
    }
    
    /**
     * Convierte formato de entrada al formato esperado por DetectorPatrones
     */
    private static String convertExpressionFormat(String originalExpression) {
        String result = originalExpression;
        
        // Paso 1: Proteger operadores para que no se conviertan
        result = maskOperators(result);
        
        // Paso 2: Convertir letras individuales a setList con llaves 
        result = convertSets(result);
        
        // Paso 3: Restaurar operadores
        result = restoreOperators(result);
        
        // Paso 4: Manejar patrones de complemento
        result = fixComplementNotation(result);
        
        // Paso 5: Limpiar espacios múltiples
        result = result.replaceAll("\\s+", " ").trim();
        
        return result;
    }
    
    /**
     * Protege operadores temporalmente para evitar conversiones incorrectas
     */
    private static String maskOperators(String expression) {
        return expression
                .replaceAll("\\bU\\b", "UNION_OP")
                .replaceAll("\\b&\\b", "INTER_OP")
                .replaceAll("\\b-\\b", "DIFF_OP")
                .replaceAll("\\b\\^\\b", "COMP_OP");
    }
    
    /**
     * Convierte letras individuales a setList con llaves
     */
    private static String convertSets(String expression) {
        return expression.replaceAll("\\b([A-Z])\\b(?!\\})", "{$1}");
    }
    
    /**
     * Restaura operadores después de la conversión de setList
     */
    private static String restoreOperators(String expression) {
        return expression
                .replaceAll("UNION_OP", "U")
                .replaceAll("INTER_OP", "&")
                .replaceAll("DIFF_OP", "-")
                .replaceAll("COMP_OP", "^");
    }
    
    /**
     * Maneja patrones de complemento (doble y simple)
     */
    private static String fixComplementNotation(String expression) {
        // Primero doble complemento: ^ ^ {A} -> ^^{A}
        String result = expression.replaceAll("\\^ \\^ \\{([A-Z])\\}", "^^{$1}");
        // Luego complemento simple: ^ {A} -> ^{A}
        result = result.replaceAll("\\^ \\{([A-Z])\\}", "^{$1}");
        return result;
    }
    
    /**
     * Genera simplificación para De Morgan
     */
    private static String generateDeMorgan(String expression, List<String> setList) {
        if (setList.size() >= 2) {
            // ^(A ∩ B) = ^A ∪ ^B
            if (expression.matches("\\^ & \\{[^}]+\\} \\{[^}]+\\}")) {
                return "U ^ " + setList.get(0) + " ^ " + setList.get(1);
            }
            // ^(A ∪ B) = ^A ∩ ^B
            else if (expression.matches("\\^ U \\{[^}]+\\} \\{[^}]+\\}")) {
                return "& ^ " + setList.get(0) + " ^ " + setList.get(1);
            }
            // U ^{A}^{B} = ^A ∪ ^B (ya está en forma correcta)
            else if (expression.matches("U \\^\\{[^}]+\\}\\^\\{[^}]+\\}")) {
                return expression; // Ya está en forma correcta
            }
            // U ^{A} ^{B} = ^A ∪ ^B (ya está en forma correcta)
            else if (expression.matches("U \\^\\{[^}]+\\} \\^\\{[^}]+\\}")) {
                return expression; // Ya está en forma correcta
            }
            // & ^{A}^{B} = ^A ∩ ^B (ya está en forma correcta)
            else if (expression.matches("& \\^\\{[^}]+\\}\\^\\{[^}]+\\}")) {
                return expression; // Ya está en forma correcta
            }
            // & ^{A} ^{B} = ^A ∩ ^B (ya está en forma correcta)
            else if (expression.matches("& \\^\\{[^}]+\\} \\^\\{[^}]+\\}")) {
                return expression; // Ya está en forma correcta
            }
        }
        return expression;
    }
    
    /**
     * Genera simplificación para Absorción
     */
    private static String generateAbsorption(String expression, List<String> sets) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String set : sets) {
            frequencyMap.put(set, frequencyMap.getOrDefault(set, 0) + 1);
        }
        // Buscar el conjunto que aparece más veces (absorción)
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > 1) {
                return entry.getKey(); // Devolver sin llaves
            }
        }
        // Si no hay absorción, devolver el primer conjunto
        return sets.size() > 0 ? sets.get(0) : expression;
    }
    /**
     * Genera simplificación para Distributiva
     */
    private static String generateDistributive(String expression, List<String> setList) {
        // Caso 1: & U {A} {B} U {A} {C} → U {A} & {B} {C}
        if (expression.matches("& U \\{[^}]+\\} \\{[^}]+\\} U \\{[^}]+\\} \\{[^}]+\\}")) {
            Pattern pattern = Pattern.compile("& U \\{([^}]+)\\} \\{([^}]+)\\} U \\{([^}]+)\\} \\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(expression);
            if (matcher.matches() && matcher.group(1).equals(matcher.group(3))) {
                String A = matcher.group(1);
                String B = matcher.group(2);
                String C = matcher.group(4);
                return "U " + A + " & " + B + " " + C;
            }
        }
        
        // Caso 2: U & U {A} {B} {C} {A} → U & {C} {B} {A}
        if (expression.matches("U & U \\{[^}]+\\} \\{[^}]+\\} \\{[^}]+\\} \\{[^}]+\\}")) {
            Pattern pattern = Pattern.compile("U & U \\{([^}]+)\\} \\{([^}]+)\\} \\{([^}]+)\\} \\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(expression);
            if (matcher.matches() && matcher.group(1).equals(matcher.group(4))) {
                String A = matcher.group(1);
                String B = matcher.group(2);
                String C = matcher.group(3);
                return "U & " + C + " " + B + " " + A;
            }
        }
        
        // Caso 3: U {A} & {B} {C} → & U {A} {B} U {A} {C}
        if (expression.matches("U \\{[^}]+\\} & \\{[^}]+\\} \\{[^}]+\\}")) {
            if (setList.size() == 3) {
                String A = setList.get(0);
                String B = setList.get(1);
                String C = setList.get(2);
                return "& U " + A + " " + B + " U " + A + " " + C;
            }
        }
        
        // Caso 4: U & {C} {B} {A} → ya está en forma reducida, mantener igual
        if (expression.matches("U & \\{[^}]+\\} \\{[^}]+\\} \\{[^}]+\\}")) {
            return expression; // Ya está en forma reducida
        }
        
        return expression;
    }
    
    /**
     * Genera simplificación para Asociativa
     */
    private static String generateAssociative(String expression, List<String> setList) {
        
        if (setList.size() == 3) {
            // U {A} U {B} {C} → U U {A} {B} {C}
            if (expression.matches("U \\{[^}]+\\} U \\{[^}]+\\} \\{[^}]+\\}")) {
                String result = "U U " + setList.get(0) + " " + setList.get(1) + " " + setList.get(2);
                return result;
            }
            
            // U U {A} {B} {C} → U {A} U {B} {C}
            if (expression.matches("U U \\{[^}]+\\} \\{[^}]+\\} \\{[^}]+\\}")) {
                String result = "U " + setList.get(0) + " U " + setList.get(1) + " " + setList.get(2);
                return result;
            }
            
            // Similar para intersección
            if (expression.matches("& \\{[^}]+\\} & \\{[^}]+\\} \\{[^}]+\\}")) {
                String result = "& & " + setList.get(0) + " " + setList.get(1) + " " + setList.get(2);
                return result;
            }
            
            if (expression.matches("& & \\{[^}]+\\} \\{[^}]+\\} \\{[^}]+\\}")) {
                String result = "& " + setList.get(0) + " & " + setList.get(1) + " " + setList.get(2);
                return result;
            }
        }
        
        return expression;
    }
        
    /**
     * Genera simplificación para Conmutativa
     */
    private static String generateCommutative(String expression, List<String> setList) {
        
        String operator = LawPatternMatcher.getPrimaryOperator(expression);
        if (setList.size() >= 2) {
            // Intercambiar el orden de los setList
            String result = operator + " " + setList.get(1) + " " + setList.get(0);
            return result;
        }
        return expression;
    }
    
    /**
     * Genera archivo JSON con las simplificaciones en orden procesado
     */
    private static void createJsonFile() {
        try {
            // Crear objeto JSON con estructura ordenada
            Map<String, Map<String, Object>> jsonOutput = new LinkedHashMap<>();
            
            for (Map.Entry<String, SimplificationResult> entry : simplifications.entrySet()) {
                Map<String, Object> operationData = new LinkedHashMap<>();
                operationData.put("conjunto Original", entry.getValue().originalSet);
                operationData.put("conjunto Simplificado", entry.getValue().simplifiedSet);
                operationData.put("leyes", entry.getValue().laws);
                
                jsonOutput.put(entry.getKey(), operationData);
            }
            
            // Generar JSON formateado con indentación limpia
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()  
                    .create();
            String jsonString = gson.toJson(jsonOutput);
            
            // Crear la carpeta JSON si no existe
            File jsonFolder = new File("JSON");
            if (!jsonFolder.exists()) {
                jsonFolder.mkdir();
            }
            
            // Escribir archivo dentro de la carpeta JSON
            try (FileWriter writer = new FileWriter("JSON/simplificaciones.json")) {
                writer.write(jsonString);
                writer.flush();
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error al generar JSON: " + e.getMessage());
        }
    }
    
    /**
     * Limpia las simplificaciones
     */
    public static void clearSimplifications() {
        simplifications.clear();
    }
    
    /**
     * Método para saber si tiene simplificación
     */
    public static boolean hasSimplifications(String nombreOperacion) {
        return simplifications.containsKey(nombreOperacion);
    }
    
    /**
     * Obtiene todas las simplificaciones en el orden que se procesaron
     */
    public static Map<String, SimplificationResult> getSimplifications() {
        return new LinkedHashMap<>(simplifications);
    }
}