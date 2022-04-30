/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.dhbw.rahmlab.geomalgelang;

import de.dhbw.rahmlab.geomalgelang.parsing.ParsingService;
import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 *
 * @author fabian
 */
//@TestInstance(Lifecycle.PER_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
public class GrammarMixedTest {
	// https://www.baeldung.com/junit
	// https://www.baeldung.com/junit-5
	// https://stackoverflow.com/questions/51669154/graalvm-using-polyglot-value-without-a-context
	// https://www.baeldung.com/junit-testinstance-annotation

	Context context;

	@BeforeAll
	public void setup() {
		context = Context.create();
		context.enter();
	}

	@AfterAll
	public void desetup() {
		context.leave();
		context.close();
	}

	@Test
	void test() {
		String program = "a b ⋅ (1,5 + 5,3)†";
		String actualAstString = ParsingService.getAstString(program);

		// Kann ich einen Builder basteln für den desiredAstString?
		// addChild ->Automatisch eine Einrückung mehr
		// addChildx2 -> 2 mal das gleiche Child
		// Einrückungssymbbol holen aus dem AstStringBuilder
		// Evtl. sogar addInnerProduct und so.
		String expectedAstString = ""
			+ "InnerProduct\n"
			+ "	GeometricProduct\n"
			+ "		GlobalVariableReference\n"
			+ "		GlobalVariableReference\n"
			+ "	CliffordConjugate\n"
			+ "		Add\n"
			+ "			DecimalLiteral\n"
			+ "			DecimalLiteral\n";

		Assertions.assertEquals(expectedAstString, actualAstString);
	}
}
