package Graphics;

import java.util.*;

/**
 * Clase de datos para almacenar la información necesaria para generar un diagrama de Venn.
 * Contiene información sobre la operacion, operandos, resultado y conjuntos referenciados.
 * 
 * @author danie
 */
public class VennDiagramData {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // ATRIBUTOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private String operationName;
    private String operator;
    private List<String> operands;
    private Set<Object> result;
    private Map<String, Set<Object>> referencedSets;
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // CONSTRUCTORES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Constructor por defecto.
     */
    public VennDiagramData() {
        this.operands = new ArrayList<>();
        this.result = new HashSet<>();
        this.referencedSets = new HashMap<>();
    }
    
    /**
     * Constructor completo.
     * 
     * @param operationName Nombre de la operacion
     * @param operator Operador de la operacion
     * @param operands Lista de operandos
     * @param result Resultado de la operacion
     * @param referencedSets Conjuntos referenciados
     */
    public VennDiagramData(String operationName, String operator, List<String> operands, 
                          Set<Object> result, Map<String, Set<Object>> referencedSets) {
        this.operationName = operationName;
        this.operator = operator;
        this.operands = new ArrayList<>(operands);
        this.result = new HashSet<>(result);
        this.referencedSets = new HashMap<>(referencedSets);
    }
    
    /**
     * Constructor simplificado para compatibilidad con OperateSet
     * 
     * @param title Título del diagrama
     * @param setA Conjunto A (puede ser null)
     * @param setB Conjunto B (puede ser null)
     * @param result Resultado de la operacion (puede ser null)
     * @param operation Tipo de operacion
     */
    public VennDiagramData(String title, Object setA, Object setB, Object result, String operation) {
        this.operationName = title;
        this.operator = operation;
        this.operands = new ArrayList<>();
        this.result = new HashSet<>();
        this.referencedSets = new HashMap<>();
        
        // Agregar operandos si existen
        if (setA != null) {
            this.operands.add("A");
        }
        if (setB != null) {
            this.operands.add("B");
        }
        
        // Configurar resultado si existe
        if (result != null && result instanceof Set<?>) {
            this.result = new HashSet<>((Set<?>) result);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene el nombre de la operacion.
     * 
     * @return Nombre de la operacion
     */
    public String getOperationName() {
        return operationName;
    }
    
    /**
     * Establece el nombre de la operacion.
     * 
     * @param operationName Nombre de la operacion
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
    
    /**
     * Obtiene el operador de la operacion.
     * 
     * @return Operador de la operacion
     */
    public String getOperator() {
        return operator;
    }
    
    /**
     * Establece el operador de la operacion.
     * 
     * @param operator Operador de la operacion
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    /**
     * Obtiene la lista de operandos.
     * 
     * @return Lista de operandos
     */
    public List<String> getOperands() {
        return new ArrayList<>(operands);
    }
    
    /**
     * Establece la lista de operandos.
     * 
     * @param operands Lista de operandos
     */
    public void setOperands(List<String> operands) {
        this.operands = new ArrayList<>(operands);
    }
    
    /**
     * Obtiene el resultado de la operacion.
     * 
     * @return Resultado de la operacion
     */
    public Set<Object> getResult() {
        return new HashSet<>(result);
    }
    
    /**
     * Establece el resultado de la operacion.
     * 
     * @param result Resultado de la operacion
     */
    public void setResult(Set<Object> result) {
        this.result = new HashSet<>(result);
    }
    
    /**
     * Obtiene los conjuntos referenciados.
     * 
     * @return Mapa de conjuntos referenciados
     */
    public Map<String, Set<Object>> getReferencedSets() {
        return new HashMap<>(referencedSets);
    }
    
    /**
     * Establece los conjuntos referenciados.
     * 
     * @param referencedSets Mapa de conjuntos referenciados
     */
    public void setReferencedSets(Map<String, Set<Object>> referencedSets) {
        this.referencedSets = new HashMap<>(referencedSets);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // METODOS DE UTILIDAD
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Verifica si hay suficientes datos para generar el diagrama.
     * 
     * @return true si hay datos suficientes, false en caso contrario
     */
    public boolean hasValidData() {
        return operationName != null && operator != null && !operands.isEmpty();
    }
    
    /**
     * Obtiene el número de conjuntos referenciados.
     * 
     * @return Número de conjuntos referenciados
     */
    public int getSetCount() {
        return referencedSets.size();
    }
    
    /**
     * Verifica si es una operacion binaria (dos operandos).
     * 
     * @return true si es binaria, false en caso contrario
     */
    public boolean isBinaryOperation() {
        return operands.size() == 2 && referencedSets.size() == 2;
    }
    
    /**
     * Verifica si es una operacion unaria (un operando).
     * 
     * @return true si es unaria, false en caso contrario
     */
    public boolean isUnaryOperation() {
        return operands.size() == 1 && referencedSets.size() == 1;
    }
    
    /**
     * Obtiene el título del diagrama (alias para getOperationName)
     * 
     * @return Título del diagrama
     */
    public String getTitle() {
        return operationName;
    }
    
    /**
     * Obtiene la operacion (alias para getOperator)
     * 
     * @return operacion del diagrama
     */
    public String getOperation() {
        return operator;
    }
    
    /**
     * Obtiene una representacion en cadena de los datos.
     * 
     * @return Representacion en cadena
     */
    @Override
    public String toString() {
        return String.format("VennDiagramData{operationName='%s', operator='%s', operands=%s, setCount=%d}", 
                operationName, operator, operands, getSetCount());
    }
}
