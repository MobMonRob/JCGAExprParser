package de.dhbw.rahmlab.geomalgelang.truffle.features.functionDefinitions.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import de.dhbw.rahmlab.geomalgelang.truffle.common.runtime.CgaTruffleBox;
import de.dhbw.rahmlab.geomalgelang.truffle.common.runtime.GeomAlgeLangException;
import de.orat.math.cga.api.CGAMultivector;

public class FunctionArgumentReader extends Node {

	private final int index;

	public FunctionArgumentReader(int index) {
		this.index = index;
	}

	public CGAMultivector executeReadFunctionArgument(VirtualFrame frame) {
		Object[] args = frame.getArguments();

		// class Function already ensures correct arity
		Object arg = args[index];

		if (arg instanceof Object[]) {
			Object[] actualArgs = (Object[]) arg;
			if (actualArgs.length > 0) {
				Object actualArg = actualArgs[0];
				if (actualArg instanceof CgaTruffleBox) {
					CgaTruffleBox truffleBox = (CgaTruffleBox) actualArg;
					return truffleBox.inner;
				}
			}
		}

		throw new GeomAlgeLangException("FunctionArgumentReader failed", this);
	}
}
