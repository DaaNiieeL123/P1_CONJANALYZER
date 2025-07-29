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
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Instrucción para definir un conjunto con elementos específicos
 * Permite crear conjuntos con elementos individuales o rangos
 * 
 * @author danie
 */
public class DefineSet extends Instruction {
    private final String setName;
    private final List<Expression> elements;
    
    // Variable para controlar si ya se mostro el encabezado
    private static boolean headerShown = false;
    
    public DefineSet(String setName, Expression elementList) {
        super(TypeInstrution.DEFINICION_CONJUNTO);
        this.setName = setName;
        this.elements = extractElements(elementList);
    }
    
    public DefineSet(String setName, List<Expression> elements) {
        super(TypeInstrution.DEFINICION_CONJUNTO);
        this.setName = setName;
        this.elements = elements;
    }
    
    @Override
    public void Execute(Environment environment) {
        try {
            Set<Object> setElements = evaluateAllElements(environment);
            saveSetToEnvironment(environment, setElements);
            logSetCreation(setElements);
            
        } catch (Exception e) {
            OutputError.addMessage("❌ Error al definir conjunto '" + setName + "': " + e.getMessage());
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
    
    private Set<Object> evaluateAllElements(Environment environment) {
        Set<Object> setElements = new HashSet<>();
        
        for (Expression element : elements) {
            addElementToSet(element, environment, setElements);
        }
        
        return setElements;
    }
    
    @SuppressWarnings("unchecked") // Safe cast: ya verificamos que es Set
    private void addElementToSet(Expression element, Environment environment, Set<Object> setElements) {
        Return result = element.Execute(environment);
        
        if (result == null || result.value == null) {
            return;
        }
        
        if (result.value instanceof Set<?>) {
            setElements.addAll((Set<Object>) result.value);
        } else if (result.value instanceof List<?>) {
            setElements.addAll((List<Object>) result.value);
        } else {
            setElements.add(result.value);
        }
    }
    
    private void saveSetToEnvironment(Environment environment, Set<Object> setElements) {
        environment.saveSet(setName, setElements);
    }
    
    private void logSetCreation(Set<Object> setElements) {
        if (!headerShown) {
            Output.Console.add("🎯═══════════ CONJUNTOS DEFINIDOS ═══════════🎯");
            headerShown = true;
        }
        
        Output.Console.add("📦 Conjunto: '" + setName + "' ✨ Elementos: " + setElements);
    }
    
    // Método para resetear el estado del encabezado
    public static void resetHeaderState() {
        headerShown = false;
    }
    
    // Método para verificar si el encabezado ya se mostró
    public static boolean isHeaderShown() {
        return headerShown;
    }
    
    @Override
    public String toString() {
        return "DefineSet{setName='" + setName + "', elements=" + elements.size() + '}';
    }
}