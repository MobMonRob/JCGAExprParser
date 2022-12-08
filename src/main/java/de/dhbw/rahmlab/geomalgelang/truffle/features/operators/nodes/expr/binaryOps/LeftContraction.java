package de.dhbw.rahmlab.geomalgelang.truffle.features.operators.nodes.expr.binaryOps;

import de.dhbw.rahmlab.geomalgelang.truffle.features.operators.nodes.exprSuperClasses.BinaryOp;
import com.oracle.truffle.api.dsl.Specialization;
import de.orat.math.cga.api.CGAMultivector;

public abstract class LeftContraction extends BinaryOp {

	@Specialization
	@Override
	protected CGAMultivector execute(CGAMultivector left, CGAMultivector right) {
		return left.lc(right);
	}
}
