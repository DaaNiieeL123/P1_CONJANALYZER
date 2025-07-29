/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Types;

/**
 *
 * @author danie
 */
public enum  TypeLaw {
    DOBLE_COMPLEMENTO,     // ^^ {A}
    DE_MORGAN,             // ^(A ∪ B) = ^A ∩ ^B y ^(A ∩ B) = ^A ∪ ^B
    COMPLEMENTO_UNIVERSO,  // A ∪ ^A = U
    COMPLEMENTO_VACIO,     // A ∩ ^A = ∅
    DIFERENCIA_PROPIA,     // A - A = ∅
    IDEMPOTENCIA,          // A ∪ A = A y A ∩ A = A
    ABSORCION,             // A ∪ (A ∩ B) = A y A ∩ (A ∪ B) = A
    DISTRIBUTIVA,          // A ∪ (B ∩ C) = (A ∪ B) ∩ (A ∪ C)
    ASOCIATIVA,            // A ∪ (B ∪ C) = (A ∪ B) ∪ C
    CONMUTATIVA,           // A ∪ B = B ∪ A
    NO_SIMPLIFICABLE
}
