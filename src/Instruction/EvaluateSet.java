/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Instruction;

import Abstract.Instruction;
import Abstract.Expression;
import Environment.Environment;
import Types.TypeInstrution;
import Types.Return;
import Types.Output;
import Types.OutputError;
import Expression.ListElements;
import Expression.ReferenceSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Instrucción para evaluar elementos contra un conjunto resultado
 * Verifica si cada elemento pertenece al conjunto especificado
 * 
 * @author danie
 */
public class EvaluateSet extends Instruction {
    private final List<Expression> elements;
    private final String operationName;
    
    // Variable para controlar si ya se mostro el encabezado de evaluacion
    private static boolean evaluationHeaderShown = false;
    
    public EvaluateSet(Expression elementList, String operationName) {
        super(TypeInstrution.EVALUACION);
        this.operationName = operationName;
        this.elements = extractElements(elementList);
    }
    
    public EvaluateSet(List<Expression> elements, String operationName) {
        super(TypeInstrution.EVALUACION);
        this.elements = elements;
        this.operationName = operationName;
    }
    
    @Override
    public void Execute(Environment environment) {
        try {
            // Validar que la operación existe
            if (!isOperationDefined(environment)) {
                showOperationNotFoundError(operationName);
                return;
            }
            
            Set<Object> operationResult = environment.getSet(operationName);
            
            // Validar que el resultado no sea nulo o vacío
            if (operationResult == null) {
                OutputError.addMessage("❌ Error: No se pudo obtener el resultado de la operación '" + operationName + "'.");
                return;
            }
            
            // Validar que todos los elementos de evaluación sean válidos antes de proceder
            if (!validateAllElements(environment)) {
                return; // Los errores específicos ya fueron reportados en validateAllElements
            }
            
            displayEvaluationHeader();
            evaluateAllElements(environment, operationResult);
            displayEvaluationFooter();
            
        } catch (Exception e) {
            OutputError.addMessage("❌ Error al evaluar conjunto: " + e.getMessage());
        }
    }
    
    private List<Expression> extractElements(Expression elementList) {
        if (elementList instanceof ListElements) {
            return ((ListElements) elementList).getElements();
        } else {
            List<Expression> singleElement = new ArrayList<>();
            singleElement.add(elementList);
            return singleElement;
        }
    }
    
    private boolean isOperationDefined(Environment environment) {
        return environment.existSet(operationName);
    }
    
    /**
     * Valida que todos los elementos de evaluación sean válidos
     * @param environment El entorno de ejecución
     * @return true si todos los elementos son válidos, false en caso contrario
     */
    private boolean validateAllElements(Environment environment) {
        boolean allValid = true;
        
        for (Expression element : elements) {
            if (!validateSingleElement(element, environment)) {
                allValid = false;
                // No retornamos inmediatamente para reportar todos los errores posibles
            }
        }
        
        return allValid;
    }
    
    /**
     * Valida un elemento individual
     * @param element El elemento a validar
     * @param environment El entorno de ejecución
     * @return true si el elemento es válido, false en caso contrario
     */
    private boolean validateSingleElement(Expression element, Environment environment) {
        try {
            // Si es una referencia a conjunto, validar que existe
            if (element instanceof ReferenceSet) {
                ReferenceSet refSet = (ReferenceSet) element;
                String setName = refSet.getSetName();
                
                if (!environment.existSet(setName)) {
                    showSetNotFoundInEvaluationError(setName);
                    return false;
                }
            }
            
            // Intentar ejecutar el elemento para validar que se puede evaluar
            Return result = element.Execute(environment);
            if (result == null || result.value == null) {
                OutputError.addMessage("❌ Error: No se pudo evaluar el elemento: " + element.toString());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            OutputError.addMessage("❌ Error al validar elemento: " + e.getMessage());
            return false;
        }
    }
    
    private void displayEvaluationHeader() {
        if (!evaluationHeaderShown) {
            Output.Console.add("🔍═══════════ EVALUACIONES DE CONJUNTOS ═══════════🔍");
            evaluationHeaderShown = true;
        }
        
        Output.Console.add("🎯 Evaluar: " + operationName);
        Output.Console.add("─────────────────────────");
    }
    
    private void evaluateAllElements(Environment environment, Set<Object> operationResult) {
        for (Expression element : elements) {
            evaluateSingleElement(element, environment, operationResult);
        }
    }
    
    private void evaluateSingleElement(Expression element, Environment environment, Set<Object> operationResult) {
        Return result = element.Execute(environment);
        
        if (result == null || result.value == null) {
            return;
        }
        
        Object elementValue = result.value;
        String evaluationResult = determineEvaluationResult(environment, operationResult, elementValue);
        
        Output.Console.add("🔸 " + elementValue + " -> " + evaluationResult);
    }
    
    private String determineEvaluationResult(Environment environment, Set<Object> operationResult, Object elementValue) {
        if (environment.containsElement(operationResult, elementValue)) {
            return "✅ exitoso";
        } else {
            return "❌ fallo";
        }
    }
    
    private void displayEvaluationFooter() {
        Output.Console.add("─────────────────────────");
        Output.Console.add("");
    }

    // Método para resetear el estado del encabezado
    public static void resetEvaluationHeaderState() {
        evaluationHeaderShown = false;
    }
    
    // Método para verificar si el encabezado ya se mostró 
    public static boolean isEvaluationHeaderShown() {
        return evaluationHeaderShown;
    }
    
    /**
     * Método estático para mostrar error cuando una operación no existe
     * @param operationName Nombre de la operación que no existe
     */
    public static void showOperationNotFoundError(String operationName) {
        OutputError.addMessage("❌ Error: La operación '" + operationName + "' no ha sido definida.");
        OutputError.addMessage("💡 Consejo: Debe definir la operación primero usando OPERA.");
        OutputError.addMessage("   Ejemplo: OPERA : " + operationName + " -> U {A} {B};");
    }
    
    /**
     * Método estático para mostrar error cuando un conjunto no existe en la evaluación
     * @param setName Nombre del conjunto que no existe
     */
    public static void showSetNotFoundInEvaluationError(String setName) {
        OutputError.addMessage("❌ Error: El conjunto '" + setName + "' usado en la evaluación no existe.");
        OutputError.addMessage("💡 Consejo: Debe definir el conjunto '" + setName + "' antes de usarlo en la evaluación.");
        OutputError.addMessage("   Ejemplo: " + setName + " = {1, 2, 3};");
    }
    
    @Override
    public String toString() {
        return "EvaluateSet{operationName='" + operationName + "', elements=" + elements.size() + '}';
    }
}