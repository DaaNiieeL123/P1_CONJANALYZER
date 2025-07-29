/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Types;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author danie
 */
public class OutputError {
    public static Set<String> OutputError = new LinkedHashSet<>();
    // Método para limpiar la salida de la consola
    public static void ResetErrors() {
        OutputError.clear();
    }   
    public static void addMessage(String mensaje) {
        OutputError.add(mensaje); 
    }
}
