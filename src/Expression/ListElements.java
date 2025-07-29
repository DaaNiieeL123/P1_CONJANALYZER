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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Representa una lista de elementos que forman un conjunto
 * Evalúa cada elemento y los combina en una estructura de datos
 * 
 * @author danie
 */
public class ListElements extends Expression {
    private final List<Expression> elements;

    public ListElements(List<Expression> elements) {
        super(TypeExpression.LISTA);
        this.elements = elements;
    }

    @Override
    public Return Execute(Environment environment) {
        List<Object> values = new ArrayList<>();
        
        for (Expression element : elements) {
            addElementValues(element, environment, values);
        }
        
        return new Return(values, Type.LISTA);
    }

    private void addElementValues(Expression element, Environment environment, List<Object> values) {
        Return elementReturn = element.Execute(environment);
        
        if (elementReturn == null || elementReturn.value == null) {
            return;
        }
        
        Object value = elementReturn.value;
        
        if (value instanceof Set) {
            values.addAll((Set<?>) value);
        } else if (value instanceof List) {
            values.addAll((List<?>) value);
        } else {
            values.add(value);
        }
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return "ListElements{elements=" + elements + '}';
    }
}
