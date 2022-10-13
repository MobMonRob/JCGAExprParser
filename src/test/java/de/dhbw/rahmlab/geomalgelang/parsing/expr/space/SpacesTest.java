/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.dhbw.rahmlab.geomalgelang.parsing.expr.space;

import de.dhbw.rahmlab.geomalgelang.parsing.ParsingService;
import de.dhbw.rahmlab.geomalgelang.parsing._util.AstStringBuilder;
import static de.dhbw.rahmlab.geomalgelang.parsing._util.Util.parsePrintAssert;
import java.util.List;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 *
 * @author fabian
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpacesTest {

	Context context;

	@BeforeAll
	void setup() {
		context = Context.create();
		context.enter();
	}

	@AfterAll
	void desetup() {
		context.leave();
		context.close();
	}

	@Test
	void R1() {
		String expected = """

		Addition
			GlobalVariableReference
			GlobalVariableReference
		""";

		final List<String> programs = List.of(new String[]{"a+a", "a +a", "a+ a", "a + a", "a  +  a"});

		for (String program : programs) {
			parsePrintAssert(program, expected);
		}
	}

	// Without spaces
	@Test
	void R2_1() {
		String expected = """

		Reverse
		""";

		String program = "a˜";

		parsePrintAssert(program, expected, 1);
	}

	// With spaces
	@Test
	void R2_2() {

		String program = "a ˜";

		System.out.println("program: " + program + "\n");

		assertThrows(ParseCancellationException.class, () -> {
			AstStringBuilder.getAstString(ParsingService.sourceCodeToRootNode(program));
		});
	}

	@Test
	void R3() {
		String expected = """

		GlobalVariableReference
		""";

		final List<String> programs = List.of(new String[]{"(a)", "( a)", "(a )", "( a )", "( a )"});

		for (String program : programs) {
			parsePrintAssert(program, expected);
		}
	}

	@Test
	void R4() {
		String expected = """

		GlobalVariableReference
		""";

		final List<String> programs = List.of(new String[]{"a", "  a", "a  "});

		for (String program : programs) {
			parsePrintAssert(program, expected);
		}
	}
}