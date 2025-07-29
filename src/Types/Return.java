/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Types;

/**
 *
 * @author danie
 */
public class Return {
    public Object value;
    public Type type;
    
    public Return(Object value, Type type) {
        this.value = value;
        this.type = type;
    }

     @Override
    public String toString() {
        return "Retorno{" +
                "valor=" + value +
                ", tipo=" + type +
                '}';
    }
}
