package de.dhbw.rahmlab.geomalgelang.truffle.features.builtinFunctionCalls.nodes.expr;

import com.oracle.truffle.api.dsl.Specialization;
import de.dhbw.rahmlab.geomalgelang.truffle.common.runtime.GeomAlgeLangContext;
import de.dhbw.rahmlab.geomalgelang.truffle.features.functionCalls.nodes.exprSuperClasses.FunctionReferenceBaseNode;
import de.dhbw.rahmlab.geomalgelang.truffle.features.functionDefinitions.runtime.Function;

public abstract class GlobalBuiltinReference extends FunctionReferenceBaseNode {

	protected final Function builtin;

	protected GlobalBuiltinReference(String name, GeomAlgeLangContext context) {
		super(name);
		this.builtin = context.builtinRegistry.getBuiltinFunction(name);
	}

	@Specialization
	@Override
	protected Function getFunction() {
		return this.builtin;
	}
}
