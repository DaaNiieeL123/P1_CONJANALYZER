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
import Analyzer.Parser;
import java.util.*;

/**
 * Representa operaciones entre conjuntos
 * Esta clase maneja operaciones como unión, intersección, diferencia y complemento
 *  
 * @author danie
 */

public class Operators extends Expression {
    private Expression left;
    private Expression right;
    private String operator;
    
    // Usar el stack de la gramatica
    private List<String> capturedOperands = new ArrayList<>();
    
    public Operators(Expression left, String operator, Expression right) {
        super(TypeExpression.OPERACION);
        
        this.left = left;
        this.right = right;
        this.operator = operator;
        
        // Capturar operandos usando el stack de la gramatica
        captureOperandsFromGrammar();
    }
    
    /**
     * Captura operandos desde el stack de la gramatica
     */ 
    private void captureOperandsFromGrammar() {
        if (Parser.operandosCapturados != null && !Parser.operandosCapturados.isEmpty()) {
            this.capturedOperands = new ArrayList<>(Parser.operandosCapturados);
        } else {
            extractTraditionalOperands();
        }
    }
    
    /**
     * Método tradicional de captura
     */
    private void extractTraditionalOperands() {
        List<String> foundOperands = new ArrayList<>();
        extractOperandsRecursively(this, foundOperands);
        this.capturedOperands = foundOperands;
    }
    
    /**
     * Extrae operandos recursivamente de forma correcta
     */
    private void extractOperandsRecursively(Expression expr, List<String> operandsList) {
        if (expr instanceof ReferenceSet) {
            operandsList.add(((ReferenceSet) expr).getSetName());
        } else if (expr instanceof Operators) {
            Operators op = (Operators) expr;
            
            // Procesar operando izquierdo
            if (op.getLeft() != null) {
                extractOperandsRecursively(op.getLeft(), operandsList);
            }
            
            // Procesar operando derecho
            if (op.getRight() != null) {
                extractOperandsRecursively(op.getRight(), operandsList);
            }
        }
    }
    
    public List<String> getCapturedOperands() {
        return new ArrayList<>(capturedOperands);
    }
    
    /**
     * Método para obtener representacion simplificada correcta
     */
    public String getSimplifiedRepresentation() {
        StringBuilder representation = new StringBuilder();
        buildCompleteRepresentation(this, representation);
        return representation.toString().trim();
    }
    
    /**
     * Construye la representacion completa recursivamente
     */
    private void buildCompleteRepresentation(Expression expr, StringBuilder sb) {
        if (expr instanceof Operators) {
            Operators op = (Operators) expr;
            
            // Agregar operador
            sb.append(op.getOperator()).append(" ");
            
            // Procesar operando izquierdo
            if (op.getLeft() != null) {
                if (op.getLeft() instanceof Operators) {
                    buildCompleteRepresentation(op.getLeft(), sb);
                } else if (op.getLeft() instanceof ReferenceSet) {
                    sb.append(((ReferenceSet) op.getLeft()).getSetName()).append(" ");
                }
            }
            
            // Procesar operando derecho
            if (op.getRight() != null) {
                if (op.getRight() instanceof Operators) {
                    buildCompleteRepresentation(op.getRight(), sb);
                } else if (op.getRight() instanceof ReferenceSet) {
                    sb.append(((ReferenceSet) op.getRight()).getSetName()).append(" ");
                }
            }
            
        } else if (expr instanceof ReferenceSet) {
            sb.append(((ReferenceSet) expr).getSetName()).append(" ");
        }
    }
    
    /**
     * Metodo auxiliar para realizar cast seguro a Set<Object>
     */
    @SuppressWarnings("unchecked")
    private Set<Object> safeSetCast(Object obj, String operandName) {
        if (obj instanceof Set<?>) {
            return (Set<Object>) obj;
        }
        throw new ClassCastException("El operando " + operandName + " no es un conjunto valido. Tipo encontrado: " + 
                                   (obj != null ? obj.getClass().getSimpleName() : "null"));
    }
    
    /**
     * Valida que el Return contenga un conjunto valido
     */
    private void validateReturn(Return returnValue, String operandName) {
        if (returnValue == null) {
            throw new IllegalArgumentException("El resultado del operando " + operandName + " es nulo");
        }
        if (returnValue.type != Type.CONJUNTO) {
            throw new IllegalArgumentException("El operando " + operandName + " no es de tipo CONJUNTO. Tipo: " + returnValue.type);
        }
    }
    
    @Override
    public Return Execute(Environment environment) {
        try {
            String OperationName = environment.getCurrentOperationName();
            
            if (OperationName != null) {
                String representacionParte = getSimplifiedRepresentation();
                environment.addOperationPart(OperationName, representacionParte);
            }
            
            Set<Object> resultado = executeOperation(environment);
            
            return new Return(resultado, Type.CONJUNTO);
            
        } catch (RuntimeException e) {
            // Re-lanzar RuntimeException para que se propague 
            throw e;
        } catch (Exception e) {
            System.err.println("Error en Execute: " + e.getMessage());
            throw new RuntimeException("Error ejecutando operación: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ejecuta la operacion original y retorna Set<Object>
     */
    private Set<Object> executeOperation(Environment environment) {
        
        if (operator.equals("U")) {
            Return leftReturn = left.Execute(environment);
            Return rightReturn = right.Execute(environment);
            
            // Validar que los returns sean validos
            validateReturn(leftReturn, "izquierdo");
            validateReturn(rightReturn, "derecho");
            
            
            Set<Object> leftResult = safeSetCast(leftReturn.value, "izquierdo");
            Set<Object> rightResult = safeSetCast(rightReturn.value, "derecho");
            
            Set<Object> result = new HashSet<>(leftResult);
            result.addAll(rightResult);
            
            return result;
            
        } else if (operator.equals("&")) {
            Return leftReturn = left.Execute(environment);
            Return rightReturn = right.Execute(environment);

            // Validar que los returns sean validos
            validateReturn(leftReturn, "izquierdo");
            validateReturn(rightReturn, "derecho");
            
            Set<Object> leftResult = safeSetCast(leftReturn.value, "izquierdo");
            Set<Object> rightResult = safeSetCast(rightReturn.value, "derecho");
            
            Set<Object> result = environment.executeIntersection(leftResult, rightResult);
            
            return result;
            
        } else if (operator.equals("-")) {
            Return leftReturn = left.Execute(environment);
            Return rightReturn = right.Execute(environment);

            // Validar que los returns sean validos
            validateReturn(leftReturn, "izquierdo");
            validateReturn(rightReturn, "derecho");
            
            Set<Object> leftResult = safeSetCast(leftReturn.value, "izquierdo");
            Set<Object> rightResult = safeSetCast(rightReturn.value, "derecho");
            
            Set<Object> result = environment.computeDifference(leftResult, rightResult);
            
            return result;
            
        } else if (operator.equals("^")) {
            Return leftReturn = left.Execute(environment);

            // Validar que el return sea valido
            validateReturn(leftReturn, "izquierdo");
             
            Set<Object> leftResult = safeSetCast(leftReturn.value, "izquierdo");
            
            Set<Object> result = environment.executeComplement(leftResult);
            
            return result;
        }
        
        // Si no se reconoce el operador, retornar conjunto vacío
        System.err.println("Operador no reconocido: " + operator);
        return new HashSet<>();
    }
    
    // Getters para los operandos y operador
    public Expression getLeft() { 
        return left; 
    }
    public Expression getRight() { 
        return right; 
    }
    public String getOperator() { 
        return operator; 
    }
    
    @Override
    public String toString() {
        return "Operators{left=" + left + ", right=" + right + ", operator='" + operator + "'}";
    }
}