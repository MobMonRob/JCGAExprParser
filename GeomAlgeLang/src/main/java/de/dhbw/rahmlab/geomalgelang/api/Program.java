package de.dhbw.rahmlab.geomalgelang.api;

import de.dhbw.rahmlab.geomalgelang.truffle.common.runtime.CgaTruffleBox;
import de.orat.math.cga.api.CGAMultivector;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.SourceSection;
import org.graalvm.polyglot.Value;

public class Program implements AutoCloseable {

	protected Context context;
	protected Value program;
	protected static final String LANGUAGE_ID = "geomalgelang";

	public Program(String source) {
		this(Source.create(LANGUAGE_ID, source));
	}

	public Program(Source source) {
		Engine engine = Engine.create(LANGUAGE_ID);

		Context.Builder builder = Context.newBuilder(LANGUAGE_ID)
			.allowAllAccess(true)
			.engine(engine);

		this.context = builder.build();
		this.context.initialize(LANGUAGE_ID);

		try {
			program = this.context.parse(source);
			// parsing succeeded
		} catch (PolyglotException e) {
			if (e.isSyntaxError()) {
				SourceSection location = e.getSourceLocation();
				// syntax error detected at location
			} else {
				// other guest error detected
			}
			this.context.close();
			throw e;
		}
	}

	@Override
	public void close() {
		this.context.close();
	}

	public Result invoke(Arguments arguments) {
		/// System.out.println("variable assignments: ");
		arguments.argsMap.forEach((name, value) -> {
			String varString = "\t" + name + " := " + value.toString();
			// System.out.println(varString);
		});

		// https://www.graalvm.org/truffle/javadoc/com/oracle/truffle/api/interop/InteropLibrary.html
		// https://www.graalvm.org/truffle/javadoc/com/oracle/truffle/api/library/package-summary.html
		// https://www.graalvm.org/truffle/javadoc/com/oracle/truffle/api/TruffleLanguage.Env.html#importSymbol-java.lang.String-
		// https://www.graalvm.org/truffle/javadoc/com/oracle/truffle/api/TruffleLanguage.Env.html#exportSymbol-java.lang.String-java.lang.Object-
		// https://www.graalvm.org/truffle/javadoc/com/oracle/truffle/api/TruffleLanguage.Env.html#getPolyglotBindings--
		// Env is available in GeomAlgeLang.java
		// program.invokeMember(identifier, arguments); // Alternative for main() function
		// Do it similar to simple language: launcher / SLmain.java
		Value bindings = this.context.getBindings(LANGUAGE_ID); //polyglotBindings

		arguments.argsMap.forEach((name, value) -> {
			bindings.putMember(name, new CgaTruffleBox(value));
		});

		CGAMultivector answer;

		try {
			// later: execute with arguments XOR getMember "main" and execute it with arguments (instead of bindings.putMember)
			Value result = this.program.execute();
			CgaTruffleBox box = result.as(CgaTruffleBox.class);
			answer = box.inner;
		} finally {
			// Will be executed regardless if an exception is thrown or not
			// context.close();
		}

		return new Result(answer);
	}
}
