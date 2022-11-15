parser grammar GeomAlgeParser;

options { tokenVocab=GeomAlgeLexer; }

program
	:	SPACE*
		expr
		SPACE*
		(EOF | NEWLINE)
	;

///////////////////////////////////////////////////////////////////////////
// Expr
///////////////////////////////////////////////////////////////////////////

expr
	:	nonOuterRecursiveExpr
	|	unOpExpr
	|	binOpExpr
	;

///////////////////////////////////////////////////////////////////////////
// UnOp | singleSideRecursive
///////////////////////////////////////////////////////////////////////////

unOpExpr
	:	unOpRExpr //Precedence 6
	|	unOpLExpr //Precedence 5
	;

// rightSideRecursive (sic)
unOpLExpr
	:	<assoc=right>
		op=	HYPHEN_MINUS
		(
			nonOuterRecursiveExpr
			|unOpExpr
		)
		#UnOpL
	;

// leftSideRecursive (sic)
unOpRExpr
	:	nonOuterRecursiveExpr
		unOpRSymbolExpr //Without this seemingly redundant line occurs ambiguity
	|	unOpRExpr
		unOpRSymbolExpr
	;

unOpRSymbolExpr
	:	op=	(SUPERSCRIPT_MINUS__SUPERSCRIPT_ONE
			|ASTERISK
			|SMALL_TILDE
			|DAGGER
			|SUPERSCRIPT_MINUS__ASTERISK
			|SUPERSCRIPT_TWO
			|CIRCUMFLEX_ACCENT
			)
		#UnOpR
	;

///////////////////////////////////////////////////////////////////////////
// BinOp | doubleSideRecursive
///////////////////////////////////////////////////////////////////////////

binOpExpr
	:	nonOuterRecursiveExpr	#BinOpExprDummy //Closure
	|	unOpExpr				#BinOpExprDummy //Closure
	|	binOpExpr
		SPACE*
		(
			nonOuterRecursiveExpr
			|unOpRExpr
		)					#GP		//Precedence 4
	|	binOpExpr
		SPACE*
		op=	(DOT_OPERATOR
			|LOGICAL_AND
			|INTERSECTION
			|UNION
			|R_FLOOR
			|L_FLOOR
			|LOGICAL_OR
			)
		SPACE*
		binOpExpr			#BinOp	//Precedence 3
	|	binOpExpr
		SPACE*
		op=	SOLIDUS
		SPACE*
		binOpExpr			#BinOp	//Precedence 2
	|	binOpExpr
		SPACE*
		op=	(PLUS_SIGN
			|HYPHEN_MINUS
			)
		SPACE*
		binOpExpr			#BinOp	//Precedence 1
	;

///////////////////////////////////////////////////////////////////////////
// composite / abstract / higherOrder / meta Expr
///////////////////////////////////////////////////////////////////////////

// literalExpr are the only nonRecursive Expr
nonOuterRecursiveExpr
	:	literalExpr
	|	innerRecursiveExpr
	;

innerRecursiveExpr
	:	parenExpr
	|	gradeExtractionExpr
	;

///////////////////////////////////////////////////////////////////////////
// atomic / terminal / firstOrder Expr
///////////////////////////////////////////////////////////////////////////

literalExpr
	:	type=	(SMALL_EPSILON__SUBSCRIPT_ZERO
				|SMALL_EPSILON__SUBSCRIPT_SMALL_I
				|SMALL_EPSILON__SUBSCRIPT_ONE
				|SMALL_EPSILON__SUBSCRIPT_TWO
				|SMALL_EPSILON__SUBSCRIPT_THREE
				|SMALL_PI
				|INFINITY
				|SMALL_O
				|SMALL_N
				|SMALL_N_TILDE
				|CAPITAL_E__SUBSCRIPT_ZERO
				)					#LiteralConstant
	|	value=	DECIMAL_LITERAL		#LiteralDecimal
	|	name=	IDENTIFIER			#VariableReference
	;

parenExpr
	:	L_PARENTHESIS
		SPACE*
		expr
		SPACE*
		R_PARENTHESIS
	;

gradeExtractionExpr
	:	LESS_THAN_SIGN
		SPACE*
		expr
		SPACE*
		GREATER_THAN_SIGN
		grade=	(SUBSCRIPT_ZERO
				|SUBSCRIPT_ONE
				|SUBSCRIPT_TWO
				|SUBSCRIPT_THREE
				|SUBSCRIPT_FOUR
				|SUBSCRIPT_FIVE
				)
		#gradeExtraction
	;
