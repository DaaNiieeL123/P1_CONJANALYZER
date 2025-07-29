package Environment;

import Types.Output;
import Types.OutputError;

import java.util.*;
import Utils.*;

/**
 * Entorno de ejecución para el compilador de conjuntos.
 * Maneja conjuntos, operaciones y universo de elementos con soporte
 * para captura de operaciones desde gramática.
 * 
 * Esta clase actúa como el contexto principal donde se ejecutan
 * todas las operaciones de conjuntos y se mantiene el estado del sistema.
 * 
 * @author danie
 */
public class Environment {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // CONSTANTES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private static final int ASCII_START = 33;  // '!'
    private static final int ASCII_END = 126;   // '~'
    private static final int DIGIT_START = 0;
    private static final int DIGIT_END = 9;
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // ATRIBUTOS PRINCIPALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    // Estado del analisis actual
    private String currentAnalysis;
    private int operationsPerformed;
    private int totalSets;
    
    // Universo de elementos para operaciones de complemento
    private final Set<Object> currentUniverse;
    
    // Almacenamiento de conjuntos definidos
    private final Map<String, Set<Object>> sets;
    
    // Mapas para historial y elementos
    private final Map<String, List<Object>> setElements;
    private final Map<String, String> operationsHistory;
    private final Map<String, List<String>> operacionesStack;
    
    // Estado de la operacion actual
    private String currentOperationName;
    private String currentOperator;
    private List<String> currentOperands;
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // CONSTRUCTORES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Constructor con nombre personalizado del entorno.
     * 
     * @param name Nombre del entorno 
     */
    public Environment(String name) {
        this.operationsPerformed = 0;
        this.totalSets = 0;
        this.currentUniverse = new HashSet<>();
        this.sets = new TreeMap<>();
        this.setElements = new HashMap<>();
        this.operationsHistory = new HashMap<>();
        this.operacionesStack = new HashMap<>();
        this.currentOperands = new ArrayList<>();
        
        initializeUniverse();
    }
    
    /**
     * Constructor por defecto que crea un entorno global.
     */
    public Environment() {
        this("Global");
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Inicializa el universo con caracteres ASCII imprimibles.
     * Rango: ASCII 33 ('!') hasta ASCII 126 ('~')
     */
    private void initializeUniverse() {
        for (int i = ASCII_START; i <= ASCII_END; i++) {
            currentUniverse.add((char) i);
        }
    }
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE OPERACIONES ACTUALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Establece la operacion actual en el entorno.
     * 
     * @param operationName Nombre de la operacion
     */
    public void setCurrentOperation(String operationName) {
        this.currentOperationName = operationName;
        this.currentOperands.clear();
        this.currentOperator = null;
    }
    
    /**
     * Establece los datos completos de la operacion actual.
     * 
     * @param operationName Nombre de la operacion
     * @param operator Operador de la operacion
     * @param operands Lista de operandos
     */
    public void setCurrentOperationData(String operationName, String operator, List<String> operands) {
        this.currentOperationName = operationName;
        this.currentOperator = operator;
        this.currentOperands = new ArrayList<>(operands);
    }
    
    /**
     * Agrega un operando a la operacion actual.
     * 
     * @param operand Operando a agregar
     */
    public void addOperand(String operand) {
        this.currentOperands.add(operand);
    }
    
    /**
     * Obtiene el nombre de la operacion actual.
     * 
     * @return Nombre de la operacion actual
     */
    public String getCurrentOperationName() {
        return currentOperationName;
    }
    
    /**
     * Obtiene el operador actual.
     * 
     * @return Operador actual
     */
    public String getCurrentOperator() {
        return currentOperator;
    }
    
    /**
     * Obtiene una copia de los operandos actuales.
     * 
     * @return Lista de operandos actuales
     */
    public List<String> getCurrentOperands() {
        return new ArrayList<>(currentOperands);
    }
    
    /**
     * Obtiene la representacion en cadena de la operacion actual.
     * 
     * @return Cadena que representa la operacion actual
     */
    public String getCurrentOperationString() {
        if (currentOperator == null || currentOperands.isEmpty()) {
            return "";
        }
        return currentOperator + " " + String.join(" ", currentOperands);
    }
    
    /**
     * Limpia los datos de la operacion actual.
     */
    public void clearCurrentOperation() {
        this.currentOperationName = null;
        this.currentOperator = null;
        this.currentOperands.clear();
    }
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE CONJUNTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Guarda un conjunto en el entorno.
     * 
     * @param setName Nombre del conjunto
     * @param elements Elementos del conjunto
     * @return true si es un conjunto nuevo, false si ya existía
     */
    public boolean saveSet(String setName, Set<Object> elements) {
        boolean isNewSet = !sets.containsKey(setName);
        
        if (isNewSet) {
            totalSets++;
        }
        
        sets.put(setName, new HashSet<>(elements));
        return isNewSet;
    }
    
    /**
     * Obtiene una copia del conjunto especificado.
     * 
     * @param setName Nombre del conjunto
     * @return Copia del conjunto o null si no existe
     */
    public Set<Object> getSet(String setName) {
        if (!sets.containsKey(setName)) {
            OutputError.addMessage("No se encontró el conjunto '" + setName + "'.");
            return null;
        }
        
        return new HashSet<>(sets.get(setName));
    }
    
    /**
     * Verifica si existe un conjunto con el nombre especificado.
     * 
     * @param setName Nombre del conjunto
     * @return true si existe, false en caso contrario
     */
    public boolean existSet(String setName) {
        return sets.containsKey(setName);
    }
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // OPERACIONES DE CONJUNTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Ejecuta la operacion de unión entre dos conjuntos.
     * 
     * @param setA Primer conjunto
     * @param setB Segundo conjunto
     * @return Resultado de la unión
     */
    public Set<Object> executeUnion(Set<Object> setA, Set<Object> setB) {
        Set<Object> result = new HashSet<>(setA);
        result.addAll(setB);
        
        incrementOperationCounter();
        return result;
    }
    
    /**
     * Ejecuta la operacion de intersección entre dos conjuntos.
     * 
     * @param setA Primer conjunto
     * @param setB Segundo conjunto
     * @return Resultado de la intersección
     */
    public Set<Object> executeIntersection(Set<Object> setA, Set<Object> setB) {
        Set<Object> result = new HashSet<>();
        
        for (Object elementA : setA) {
            if (containsElement(setB, elementA)) {
                result.add(elementA);
            }
        }
        
        incrementOperationCounter();
        return result;
    }
    
    /**
     * Ejecuta la operacion de diferencia entre dos conjuntos.
     * 
     * @param setA Primer conjunto
     * @param setB Segundo conjunto
     * @return Resultado de la diferencia (setA - setB)
     */
    public Set<Object> computeDifference(Set<Object> setA, Set<Object> setB) {
        Set<Object> result = new HashSet<>(setA);
        
        for (Object elementB : setB) {
            result.removeIf(elementA -> areElementsEqual(elementA, elementB));
        }
        
        incrementOperationCounter();
        return result;
    }
    
    /**
     * Ejecuta la operacion de complemento de un conjunto.
     * 
     * @param set Conjunto del cual obtener el complemento
     * @return Complemento del conjunto
     */
    public Set<Object> executeComplement(Set<Object> set) {
        Set<Object> result = new HashSet<>(currentUniverse);
        
        for (Object element : set) {
            result.removeIf(universeElement -> areElementsEqual(universeElement, element));
        }
        
        incrementOperationCounter();
        return result;
    }
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // UTILIDADES Y FORMATEO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Verifica si un conjunto contiene un elemento específico.
     * 
     * @param set Conjunto donde buscar
     * @param element Elemento a buscar
     * @return true si el conjunto contiene el elemento
     */
    public boolean containsElement(Set<Object> set, Object element) {
        return containsNormalizedElement(set, element);
    }
    
    /**
     * Formatea un conjunto para su visualizacion.
     * 
     * @param set Conjunto a formatear
     * @return Cadena formateada del conjunto
     */
    public static String formatSet(Set<Object> set) {
        if (set.isEmpty()) {
            return "∅";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        List<Object> sortedElements = getSortedElementsStatic(set);
        
        for (int i = 0; i < sortedElements.size(); i++) {
            sb.append(formatStaticElement(sortedElements.get(i)));
            if (i < sortedElements.size() - 1) {
                sb.append(", ");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Limpia todos los datos del entorno y los reinicia.
     */
    public void resetEnvironment() {
        currentAnalysis = null;
        operationsPerformed = 0;
        totalSets = 0;
        sets.clear();
        setElements.clear();
        operationsHistory.clear();
        operacionesStack.clear();
        currentUniverse.clear();
        
        clearCurrentOperation();
        initializeUniverse();
        OperationsSimplifier.clearSimplifications();

        Output.Console.add("Datos del entorno limpiados correctamente.");
    }
    
    /**
     * Obtiene información sobre el universo actual.
     * 
     * @return Información descriptiva del universo
     */
    public String getUniverseInfo() {
        return String.format("Universo ASCII (%d-%d): De '!' (%d) hasta '~' (%d)%nTotal de elementos: %d",
                ASCII_START, ASCII_END, ASCII_START, ASCII_END, currentUniverse.size());
    }
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE STACK DE OPERACIONES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Agrega una parte de la operacion al stack.
     * 
     * @param operacion Nombre de la operacion
     * @param parte Parte de la operacion a agregar
     */
    public void addOperationPart(String operacion, String parte) {
        operacionesStack.computeIfAbsent(operacion, k -> new ArrayList<>()).add(parte);
    }
    
    /**
     * Obtiene la representacion completa de la operacion del stack.
     * 
     * @param operacion Nombre de la operacion
     * @return representacion completa de la operacion
     */
    public String getCompleteOperation(String operacion) {
        List<String> partes = operacionesStack.get(operacion);
        if (partes == null || partes.isEmpty()) {
            return "";
        }
        
        // Encontrar la parte mas larga (que sera la representacion completa)
        String fullRepresentation = "";
        int maxElementos = 0;
        
        for (String parte : partes) {
            int numElementos = parte.split(" ").length;
            if (numElementos > maxElementos) {
                maxElementos = numElementos;
                fullRepresentation = parte;
            }
        }
        
        return fullRepresentation;
    }
    
    /**
     * Limpia el stack de una operacion especifica.
     * 
     * @param operacion Nombre de la operacion a limpiar
     */
    public void cleanOperationStack(String operacion) {
        operacionesStack.remove(operacion);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Establece el analisis actual.
     * 
     * @param analysis analisis a establecer
     */
    public void setCurrentAnalysis(String analysis) {
        this.currentAnalysis = analysis;
    }
    
    /**
     * Obtiene el analisis actual.
     * 
     * @return analisis actual
     */
    public String getCurrentAnalysis() {
        return currentAnalysis;
    }
    
    /**
     * Obtiene el numero de operaciones realizadas.
     * 
     * @return numero de operaciones
     */
    public int getOperationCount() {
        return operationsPerformed;
    }
    
    /**
     * Obtiene el total de conjuntos definidos.
     * 
     * @return Total de conjuntos
     */
    public int getTotalSets() {
        return totalSets;
    }
    
    /**
     * Obtiene una copia del universo actual.
     * 
     * @return Copia del universo actual
     */
    public Set<Object> getCurrentUniverse() {
        return new HashSet<>(currentUniverse);
    }
    
    /**
     * Agrega elementos al universo actual.
     * 
     * @param elements Elementos a agregar
     */
    public void addToUniverse(Set<Object> elements) {
        currentUniverse.addAll(elements);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS PRIVADOS - UTILIDADES INTERNAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Incrementa el contador de operaciones realizadas
     */
    private void incrementOperationCounter() {
        operationsPerformed++;
    }
    
    /**
     * Verifica si un conjunto contiene un elemento usando comparacion normalizada
     */
    private boolean containsNormalizedElement(Set<Object> conjunto, Object elemento) {
        for (Object setElement : conjunto) {
            if (areElementsEqual(setElement, elemento)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Compara dos elementos después de normalizarlos
     */
    private boolean areElementsEqual(Object elemento1, Object elemento2) {
        if (elemento1 == null && elemento2 == null) return true;
        if (elemento1 == null || elemento2 == null) return false;
        
        Object value1 = normalizeElement(elemento1);
        Object value2 = normalizeElement(elemento2);
        
        return value1.equals(value2);
    }
    
    /**
     * Normaliza un elemento a su representacion estandar
     */
    private Object normalizeElement(Object elemento) {
        if (elemento instanceof String) {
            return normalizarElementoString((String) elemento);
        }
        
        if (elemento instanceof Integer) {
            return normalizeIntegerElement((Integer) elemento);
        }
        
        return elemento;
    }
    
    /**
     * Normaliza elementos de tipo String
     */
    private Object normalizarElementoString(String str) {
        if (str.length() == 1) {
            return str.charAt(0);
        }
        
        try {
            int number = Integer.parseInt(str);
            return isDigit(number) ? (char)('0' + number) : number;
        } catch (NumberFormatException e) {
            return str;
        }
    }
    
    /**
     * Normaliza elementos de tipo Integer
     */
    private Object normalizeIntegerElement(Integer numero) {
        return isDigit(numero) ? (char)('0' + numero) : numero;
    }
    
    /**
     * Verifica si un numero es un digito (0-9)
     */
    private boolean isDigit(int numero) {
        return numero >= DIGIT_START && numero <= DIGIT_END;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS STATIC DE UTILIDAD
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene una lista ordenada de elementos del conjunto 
     */
    private static List<Object> getSortedElementsStatic(Set<Object> conjunto) {
        List<Object> elements = new ArrayList<>(conjunto);
        elements.sort((a, b) -> {
            if (a instanceof Integer && b instanceof Integer) {
                return ((Integer) a).compareTo((Integer) b);
            } else if (a instanceof Character && b instanceof Character) {
                return ((Character) a).compareTo((Character) b);
            } else {
                return a.toString().compareTo(b.toString());
            }
        });
        return elements;
    }
    
    /**
     * Formatea un elemento individual para su visualizacion
     */
    private static String formatStaticElement(Object elemento) {
        if (elemento instanceof Character) {
            return "'" + elemento + "'";
        }
        return elemento.toString();
    }
    
}