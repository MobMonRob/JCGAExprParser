parser grammar GeomAlgeParser;

options { tokenVocab=GeomAlgeLexer; }

program
    : expr (EOF | NEWLINE)
    ;

expr
    : L_PAREN expr R_PAREN               #parensExpr
    | left=expr op=(MUL|DIV) right=expr
    | left=expr op=(ADD|SUB) right=expr
    | value=DECIMAL_LITERAL
    ;
