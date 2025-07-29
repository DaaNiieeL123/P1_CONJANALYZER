/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Types;

/**
 *
 * @author danie
 */
public enum TypeExpression {
    RANGO,              // a~z, 0~9
    LISTA_ELEMENTOS,    // a, b, c, d
    IDENTIFICADOR,      // nombre de conjunto
    OPERACION_BINARIA,  // U {A} {B}, & {A} {B}, - {A} {B}
    OPERACION_UNARIA,   // ^ {A}
    CONJUNTO_LITERAL,    // {a, b, c}
    PRIMITIVO,         // 1, 2.5, true, false, C
    OPERACION,          // UNION, INTERSECCION, DIFERENCIA, COMPLEMENTO
    LISTA,             // [a, b, c]
}
