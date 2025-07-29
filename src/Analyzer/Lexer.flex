// ══════════════════════════════════════════        IMPORTACIONES        ══════════════════════════════════════════
package Analyzer;

import java_cup.runtime.Symbol;
import Utils.ErrorHandler;

%%

%{
    // Código Java opcional
%}

// ══════════════════════════════════════════ DECLARACIONES DE CONFIGURACION ══════════════════════════════════════════
%class Lexer
%public
%unicode
%cup
%line
%char

// Constructor
%init{
    yyline = 1;
    yychar = 1;
%init}

// ══════════════════════════════════════════     EXPRESIONES REGULARES      ══════════════════════════════════════════
ID = [a-zA-Z][a-zA-Z0-9_]*
ASCII = [!-~]
ENTERO = [0-9]+
ESPACIOS_BLANCOS = [ \t\r\n]+
COMENTARIO_LINEA = "#"[^\n]*
COMENTARIOS_MULTIPLE = "<!"([^!]|"!"[^>])*"!>"

%%

// ══════════════════════════════════════════      PALABRAS RESERVADAS       ══════════════════════════════════════════
"CONJ"               {return new Symbol(sym.CONJ, yyline, (int) yychar, yytext());}
"OPERA"              {return new Symbol(sym.OPERA, yyline, (int) yychar, yytext());}
"EVALUAR"            {return new Symbol(sym.EVALUAR, yyline, (int) yychar, yytext());}

//══════════════════════════════════════════       OPERADORES y SIMBOLOS      ══════════════════════════════════════════
"U"                  {return new Symbol(sym.UNION, yyline, (int) yychar, yytext());}
"&"                  {return new Symbol(sym.INTERSECCION, yyline, (int) yychar, yytext());}
"->"                 {return new Symbol(sym.FLECHA, yyline, (int) yychar, yytext()); }
"-"                  {return new Symbol(sym.DIFERENCIA, yyline, (int) yychar, yytext()); }
"^"                  {return new Symbol(sym.COMPLEMENTO, yyline, (int) yychar, yytext()); }
"~"                  {return new Symbol(sym.RANGO, yyline, (int) yychar, yytext()); }
":"                  {return new Symbol(sym.DOS_PUNTOS, yyline, (int) yychar, yytext());}
";"                  {return new Symbol(sym.PUNTO_COMA, yyline, (int) yychar, yytext());}   
","                  {return new Symbol(sym.COMA, yyline, (int) yychar, yytext());}                     
"("                  {return new Symbol(sym.PAR_IZQ, yyline, (int) yychar, yytext());}
")"                  {return new Symbol(sym.PAR_DCHA, yyline, (int) yychar, yytext());}
"{"                  {return new Symbol(sym.LLAVE_IZQ, yyline, (int) yychar, yytext());}
"}"                  {return new Symbol(sym.LLAVE_DCHA, yyline, (int) yychar, yytext());}

// ══════════════════════════════════════════   IDENTIFICADORES Y VALORES      ══════════════════════════════════════════
{ID}                 {return new Symbol(sym.ID, yyline, (int) yychar, yytext());}
{ENTERO}             {return new Symbol(sym.ENTERO, yyline, (int) yychar, yytext());}
{ASCII}              {return new Symbol(sym.ASCII, yyline, (int) yychar, yytext());}

// ══════════════════════════════════════════   COMENTARIOS y ESPACIOS BLANCOS  ══════════════════════════════════════════
{COMENTARIO_LINEA}              { /* Comentario de línea ignorado */ }
{COMENTARIOS_MULTIPLE}          { /* Comentario múltiple ignorado */ }
{ESPACIOS_BLANCOS}              { /* Espacios blancos ignorados */ }
.                               { ErrorHandler.AddError(yytext(), "Léxico", yyline, (int) yychar,"El caracter \"" + yytext() + "\" No Pertenece al Lenguaje");
                                    System.out.println("Error Lexico: " + yytext()); }
                                
// ══════════════════════════════════════════                 FIN              ══════════════════════════════════════════