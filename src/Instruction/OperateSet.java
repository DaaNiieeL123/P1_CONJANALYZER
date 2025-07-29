package Instruction;

import Abstract.Instruction;
import Abstract.Expression;
import Environment.Environment;
import Types.TypeInstrution;
import Utils.OperationsSimplifier;
import Types.Return;
import Types.Output;
import Types.OutputError;
import Graphics.ImageDiagramManager;
import Graphics.VennDiagramData;
import Expression.ListElements;
import Expression.Operators;
import Expression.ReferenceSet;
import Expression.Primitive;
import Types.Type;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * Instrucción para ejecutar operaciones entre conjuntos
 * Maneja unión, intersección, diferencia y complemento
 * 
 * @author danie
 */
public class OperateSet extends Instruction {
    private final String operationName;
    private final Expression expression;
    
    // Variable para controlar si ya se mostro el encabezado de resultados
    private static boolean resultsHeaderShown = false;
    
    public OperateSet(String operationName, Expression expression) {
        super(TypeInstrution.DEFINICION_OPERACION);
        this.operationName = operationName;
        this.expression = expression;
    }
    
    @Override
    public void Execute(Environment environment) {
        try {
            environment.setCurrentOperation(operationName);
            
            // Validar operandos antes de ejecutar la operación
            if (!validateOperands(environment)) {
                return; // Los errores específicos ya fueron reportados
            }
            
            Set<Object> operationResult = executeOperation(environment);
            
            if (operationResult == null) {
                OutputError.addMessage("❌ Error: No se pudo evaluar la operación '" + operationName + "'.");
                return;
            }
            
            String operacionCompleta = environment.getCompleteOperation(operationName);
            
            if (!operacionCompleta.isEmpty()) {
                
                OperationsSimplifier.simplifyAndAnalyze(
                    operationName, 
                    operacionCompleta, 
                    environment
                );
            }
            
            environment.saveSet(operationName, operationResult);
            environment.cleanOperationStack(operationName);
            
            logOperationSuccess(operationResult);
            
            // Generar diagrama de Venn automaticamente después de la operacion
            generateVennDiagramImage(environment, operationResult);
            
        } catch (RuntimeException e) {
            // Manejar errores de conjuntos no definidos y otros errores de tiempo de ejecución
            OutputError.addMessage("❌ Error al ejecutar operación '" + operationName + "': " + e.getMessage());
            environment.cleanOperationStack(operationName); // Limpiar el stack en caso de error
        } catch (Exception e) {
            OutputError.addMessage("❌ Error inesperado al ejecutar operación '" + operationName + "': " + e.getMessage());
            environment.cleanOperationStack(operationName); // Limpiar el stack en caso de error
        }
    }

    private Set<Object> executeOperation(Environment environment) {
        Return result = expression.Execute(environment);
        
        if (result == null || result.value == null) {
            return null;
        }
        
        return convertToSet(result.value);
    }
    
    @SuppressWarnings("unchecked") // Safe cast: we ensure value is a Set
    private Set<Object> convertToSet(Object value) {
        if (value instanceof Set<?>) {
            return (Set<Object>) value;
        } else {
            Set<Object> singleElementSet = new HashSet<>();
            singleElementSet.add(value);
            return singleElementSet;
        }
    }
    
    /**
     * Valida que todos los operandos (conjuntos referenciados) existan antes de ejecutar la operación
     * @param environment El entorno de ejecución
     * @return true si todos los operandos son válidos, false en caso contrario
     */
    private boolean validateOperands(Environment environment) {
        try {
            Set<String> referencedSets = extractAllReferencedSets(expression);
            boolean allValid = true;
            int missingCount = 0;
            
            for (String setName : referencedSets) {
                if (!environment.existSet(setName)) {
                    showSetNotFoundInOperationError(setName);
                    allValid = false;
                    missingCount++;
                    // No retornamos inmediatamente para reportar todos los conjuntos faltantes
                }
            }
            
            if (!allValid) {
                showIncompleteOperationError(operationName, missingCount);
            }
            
            return allValid;
            
        } catch (Exception e) {
            OutputError.addMessage("❌ Error al validar operandos para la operación '" + operationName + "': " + e.getMessage());
            return false;
        }
    }
    
    private void logOperationSuccess(Set<Object> operationResult) {
        if (!resultsHeaderShown) {
            Output.Console.add("⚡═══════════ RESULTADOS DE OPERACIONES ═══════════⚡");
            resultsHeaderShown = true;
        }
        
        String operationIcon = getOperationIcon();
        Output.Console.add(operationIcon + " Operación '" + operationName + "' ejecutada correctamente.");
        Output.Console.add("🎯 Resultado: " + Environment.formatSet(operationResult));
        Output.Console.add("─────────────────────────────────────");
    }
    
    /**
     * Obtiene el icono emoji apropiado según el tipo de operación
     * @return String con el emoji correspondiente
     */
    private String getOperationIcon() {
        String operationType = getOperationType(operationName);
        
        switch (operationType) {
            case "UNION":
                return "🔗"; 
            case "INTERSECTION":
                return "🔄"; 
            case "DIFFERENCE":
                return "➖"; 
            case "COMPLEMENT":
                return "🔄"; 
            default:
                return "⚙️"; 
        }
    }
    
    /**
     * Genera una imagen del diagrama de Venn para la operación actual
     * @param environment Entorno de ejecución
     * @param operationResult Resultado de la operación
     */
    private void generateVennDiagramImage(Environment environment, Set<Object> operationResult) {
        try {
            // Obtener conjuntos de entrada de la operación
            ListElements setA = getInputSetA(environment);
            ListElements setB = getInputSetB(environment);
            
            String operationType = getOperationType(operationName);
            
            // Crear datos del diagrama
            VennDiagramData diagramData = new VennDiagramData(
                operationName,
                operationType,
                Arrays.asList("A", "B"),
                operationResult,
                createReferencedSetsMap(environment, setA, setB)
            );
            
            // Generar la imagen
            ImageDiagramManager imageManager = ImageDiagramManager.getInstance();
            String imagePath = imageManager.generateDiagramImage(diagramData);
            
            if (imagePath != null) {
                System.out.println("");
            } else {
                Output.Console.add("⚠️ No se pudo generar el diagrama de Venn");
            }
            
        } catch (Exception e) {
            Output.Console.add("⚠️ Error generando diagrama de Venn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene el conjunto A de entrada para la operación
     * @param environment Entorno de ejecución
     * @return Conjunto A como ListElements
     */
    private ListElements getInputSetA(Environment environment) {
        try {            
            // Extraer todos los conjuntos referenciados recursivamente
            Set<String> allReferencedSets = extractAllReferencedSets(expression);

            if (!allReferencedSets.isEmpty()) {
                String firstSetName = allReferencedSets.iterator().next();
                Set<Object> setData = environment.getSet(firstSetName);
                
                if (setData != null) {
                    return convertSetToListElements(setData, firstSetName);
                }
            }
            
            return null;
        } catch (Exception e) {
            Output.Console.add("⚠️ Error obteniendo conjunto A: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene el conjunto B de entrada para la operación
     * @param environment Entorno de ejecución
     * @return Conjunto B como ListElements
     */
    private ListElements getInputSetB(Environment environment) {
        try {
            // Extraer todos los conjuntos referenciados recursivamente
            Set<String> allReferencedSets = extractAllReferencedSets(expression);
            
            if (allReferencedSets.size() >= 2) {
                String[] setNames = allReferencedSets.toArray(new String[0]);
                String secondSetName = setNames[1];
                
                // Obtener el conjunto del environment
                Set<Object> setData = environment.getSet(secondSetName);
                
                if (setData != null) {
                    return convertSetToListElements(setData, secondSetName);
                }
            } else {
                System.out.println("");
            }
            
            return null;
        } catch (Exception e) {
            Output.Console.add("⚠️ Error obteniendo conjunto B: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrae todos los conjuntos referenciados recursivamente de una expresión
     * @param expr Expresión a analizar
     * @return Conjunto de nombres de conjuntos referenciados en orden
     */
    private Set<String> extractAllReferencedSets(Abstract.Expression expr) {
        Set<String> referencedSets = new LinkedHashSet<>();
        extractAllReferencedSetsRecursive(expr, referencedSets);
        return referencedSets;
    }
    
    /**
     * Método recursivo para extraer conjuntos referenciados
     * @param expr Expresión a analizar
     * @param referencedSets Conjunto donde agregar los nombres encontrados
     */
    private void extractAllReferencedSetsRecursive(Abstract.Expression expr, Set<String> referencedSets) {
        if (expr instanceof ReferenceSet) {
            ReferenceSet refSet = (ReferenceSet) expr;
            referencedSets.add(refSet.getSetName());
        } else if (expr instanceof Operators) {
            Operators op = (Operators) expr;
            
            // Procesar operando izquierdo
            if (op.getLeft() != null) {
                extractAllReferencedSetsRecursive(op.getLeft(), referencedSets);
            }
            
            // Procesar operando derecho
            if (op.getRight() != null) {
                extractAllReferencedSetsRecursive(op.getRight(), referencedSets);
            }
        }
    }
    
    /**
     * Convierte un Set<Object> a ListElements
     * @param set Conjunto a convertir
     * @param name Nombre del conjunto
     * @return ListElements equivalente
     */
    private ListElements convertSetToListElements(Set<Object> set, String name) {
        if (set == null) return null;
        
        try {
            ListElements listElements = new ListElements(new ArrayList<>());
            
            // Convertir cada elemento del Set a un objeto compatible
            for (Object element : set) {
                Primitive primitive = new Primitive(element.toString(), Type.SIMBOLO);
                listElements.getElements().add(primitive);
            }
            
            return listElements;
            
        } catch (Exception e) {
            Output.Console.add("⚠️ Error convirtiendo conjunto '" + name + "': " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Crea un mapa de conjuntos referenciados para el diagrama
     * @param environment Entorno de ejecución
     * @param setA Conjunto A
     * @param setB Conjunto B
     * @return Mapa de conjuntos referenciados
     */
    private Map<String, Set<Object>> createReferencedSetsMap(Environment environment, ListElements setA, ListElements setB) {
        Map<String, Set<Object>> referencedSets = new HashMap<>();
        
        try {
            Set<String> allReferencedSets = extractAllReferencedSets(expression);
            
            // Agregar todos los conjuntos encontrados al mapa
            for (String setName : allReferencedSets) {
                Set<Object> setData = environment.getSet(setName);
                if (setData != null) {
                    referencedSets.put(setName, setData);
                } else {
                   System.out.println("⚠️ No se encontró el conjunto '" + setName + "' en el entorno.");
                }
            }
                        
        } catch (Exception e) {
            Output.Console.add("⚠️ DEBUG: Error creando mapa de conjuntos: " + e.getMessage());
        }
        
        return referencedSets;
    }
    
    /**
     * Obtiene el tipo de operación basado en el nombre
     * @param operationName Nombre de la operación
     * @return Tipo de operación para el diagrama
     */
    private String getOperationType(String operationName) {
        // Primero intentar detectar por el símbolo en la expresión
        if (expression instanceof Operators) {
            Operators op = (Operators) expression;
            String operatorSymbol = op.getOperator().toString();

            if (operatorSymbol.equals("UNION") || operatorSymbol.equals("U")) {
                return "UNION";
            } else if (operatorSymbol.equals("INTERSECTION") || operatorSymbol.equals("&")) {
                return "INTERSECTION";
            } else if (operatorSymbol.equals("DIFFERENCE") || operatorSymbol.equals("-")) {
                return "DIFFERENCE";
            } else if (operatorSymbol.equals("COMPLEMENT") || operatorSymbol.equals("^")) {
                return "COMPLEMENT";
            }
        }
        
        // Si no se detecta por símbolo, intentar por nombre
        String name = operationName.toLowerCase();
        
        if (name.contains("u") || name.contains("∪") || name.contains("union")) {
            return "UNION";
        } else if (name.contains("&") || name.contains("∩") || name.contains("intersect")) {
            return "INTERSECTION";
        } else if (name.contains("-") || name.contains("difference")) {
            return "DIFFERENCE";
        } else if (name.contains("^") || name.contains("'") || name.contains("complement")) {
            return "COMPLEMENT";
        }
        
        return "OPERATION";
    }
    
    // Método para resetear el estado del encabezado
    public static void resetResultsHeaderState() {
        resultsHeaderShown = false;
    }
    
    // Método para verificar si el encabezado ya se mostro 
    public static boolean isResultsHeaderShown() {
        return resultsHeaderShown;
    }
    
    /**
     * Método estático para mostrar error cuando un conjunto no existe en una operación
     * @param setName Nombre del conjunto que no existe
     */
    public static void showSetNotFoundInOperationError(String setName) {
        OutputError.addMessage("❌ Error: El conjunto '" + setName + "' usado en la operación no existe.");
        OutputError.addMessage("💡 Consejo: Debe definir el conjunto '" + setName + "' antes de usarlo en operaciones.");
        OutputError.addMessage("   Ejemplo: " + setName + " = {1, 2, 3};");
    }
    
    /**
     * Método estático para mostrar error de operación incompleta
     * @param operationName Nombre de la operación que falló
     * @param missingSetCount Número de conjuntos faltantes
     */
    public static void showIncompleteOperationError(String operationName, int missingSetCount) {
        OutputError.addMessage("❌ No se puede ejecutar la operación '" + operationName + "' porque faltan " + missingSetCount + " conjunto(s) por definir.");
        OutputError.addMessage("💡 Consejo: Defina todos los conjuntos necesarios antes de crear la operación.");
    }
    
    @Override
    public String toString() {
        return "OperateSet{operationName='" + operationName + "', expression=" + expression + '}';
    }
}