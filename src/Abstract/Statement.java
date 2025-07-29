/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Abstract;

import Types.TypeStatement;

/**
 * Representa una declaración en el lenguaje.
 * Las declaraciones son instrucciones que se ejecutan en el entorno.
 * @author danie
 */
public abstract class Statement {
    TypeStatement typeStatement;
    public Statement(TypeStatement typeStatement) {
        this.typeStatement = typeStatement;
    }
}
