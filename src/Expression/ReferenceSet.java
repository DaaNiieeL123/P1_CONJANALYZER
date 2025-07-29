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
import java.util.Set;

/**
 * Representa una referencia a un conjunto previamente definido
 * Permite usar conjuntos por su nombre en expresiones
 * 
 * @author danie
 */
public class ReferenceSet extends Expression {
    private final String setName;
    
    public ReferenceSet(String setName) {
        super(TypeExpression.IDENTIFICADOR);
        this.setName = setName;
    }
    
    @Override
    public Return Execute(Environment environment) {
        try {
            if (!environment.existSet(setName)) {
                String errorMessage = "❌ Error: El conjunto '" + setName + "' no ha sido definido.";
                OutputError.addMessage(errorMessage);
                OutputError.addMessage("💡 Consejo: Debe definir el conjunto '" + setName + "' antes de usarlo en operaciones.");
                
                // Lanzar excepción para detener la ejecución
                throw new RuntimeException("Conjunto no definido: " + setName);
            }
            
            Set<Object> elements = environment.getSet(setName);
            
            if (elements == null) {
                String errorMessage = "❌ Error: No se pudo obtener el conjunto '" + setName + "'.";
                OutputError.addMessage(errorMessage);
                throw new RuntimeException("No se pudo obtener el conjunto: " + setName);
            }
            
            return new Return(elements, Type.CONJUNTO);
            
        } catch (RuntimeException e) {
            // Re-lanzar RuntimeException para que se propague
            throw e;
        } catch (Exception e) {
            String errorMessage = "❌ Error al referenciar conjunto '" + setName + "': " + e.getMessage();
            OutputError.addMessage(errorMessage);
            throw new RuntimeException("Error al referenciar conjunto: " + setName, e);
        }
    }
    
    public String getSetName() {
        return setName;
    }
    
    @Override
    public String toString() {
        return "ReferenceSet{setName='" + setName + "'}";
    }
}
