package de.dhbw.rahmlab.geomalgelang.parsing.expr;

import de.dhbw.rahmlab.geomalgelang.parsing.AbstractParsingTest;
import static de.dhbw.rahmlab.geomalgelang.parsing._util.Asserts.parsePrintAssertSyntaxError;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

// @Disabled
public class WrongParenthesisLikeExpressionsTest extends AbstractParsingTest {

	@TestFactory
	Stream<DynamicTest> expectSyntaxError() {
		final List<String> programs = List.of(new String[]{"a(+)b", "(-)a", "a(˜)", "()", "(<a)>₀", "<>₀"});
		return parsePrintAssertSyntaxError(programs);
	}
}
