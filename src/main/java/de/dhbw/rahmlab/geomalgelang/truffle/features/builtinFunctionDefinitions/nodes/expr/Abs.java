package de.dhbw.rahmlab.geomalgelang.truffle.features.builtinFunctionDefinitions.nodes.expr;

import com.oracle.truffle.api.dsl.Specialization;
import de.dhbw.rahmlab.geomalgelang.truffle.features.builtinFunctionDefinitions.nodes.exprSuperClasses.BuiltinFunctionBody;
import de.orat.math.cga.api.CGAScalar;

public abstract class Abs extends BuiltinFunctionBody {

	@Specialization
	protected CGAScalar execute(CGAScalar input) {
		double value = Math.abs(input.value());
		return new CGAScalar(value);
	}
}
