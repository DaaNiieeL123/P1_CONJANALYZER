/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Abstract;
import Environment.Environment;
import Types.TypeInstrution;
import Types.TypeStatement;

/**
 * Instrucción en el lenguaje.
 * Las instrucciones son ejecutadas y no devuelven un valor.
 * @author danie
 */
public abstract class Instruction extends Statement{
    TypeInstrution typeInstrution;
    public Instruction(TypeInstrution typeInstrution) {
        super(TypeStatement.INSTRUCION);
        this.typeInstrution = typeInstrution;
    }

    public abstract void Execute(Environment entorno);
    
}