/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Abstract;

import Environment.Environment;
import Types.TypeExpression;
import Types.Return;
import Types.TypeStatement;

/**
 * Representa una expresión en el lenguaje.
 * Las expresiones son evaluadas y devuelven un valor.
 * @author danie
 */
public abstract class Expression extends Statement {
    TypeExpression typeExpression;
    public Expression(TypeExpression typeExpression) {
        super(TypeStatement.EXPRESION);
        this.typeExpression = typeExpression;
    }

    public abstract Return Execute(Environment entorno);
}
