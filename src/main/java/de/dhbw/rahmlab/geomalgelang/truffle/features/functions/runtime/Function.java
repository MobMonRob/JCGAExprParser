package de.dhbw.rahmlab.geomalgelang.truffle.features.functions.runtime;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import de.dhbw.rahmlab.geomalgelang.truffle.features.functions.nodes.FunctionRootNode;

@ExportLibrary(InteropLibrary.class)
public final class Function implements TruffleObject {

	public final String name;

	protected final FunctionRootNode functionRootNode;

	protected final int arity;

	public Function(FunctionRootNode functionRootNode, String name, int arity) {
		this.functionRootNode = functionRootNode;
		this.name = name;
		this.arity = arity;
	}

	protected void ensureArity(int presumedArity) throws ArityException {
		if (this.arity != presumedArity) {
			throw ArityException.create(this.arity, this.arity, presumedArity);
		}
	}

	protected RootCallTarget getCallTarget() {
		return functionRootNode.getCallTarget();
	}

	@ExportMessage
	protected boolean isExecutable() {
		return true;
	}

	@ReportPolymorphism
	@ExportMessage
	abstract static class Execute {

		@Specialization(limit = "2", guards = "function.getCallTarget() == cachedTarget")
		protected static Object doDirect(Function function, Object[] arguments,
			@Cached("function.getCallTarget()") RootCallTarget cachedTarget,
			@Cached("create(cachedTarget)") DirectCallNode callNode) throws ArityException {

			function.ensureArity(arguments.length);

			return callNode.call(arguments);
		}

		@Specialization(replaces = "doDirect")
		protected static Object doIndirect(Function function, Object[] arguments,
			@Cached IndirectCallNode callNode) throws ArityException {

			function.ensureArity(arguments.length);

			return callNode.call(function.getCallTarget(), arguments);
		}
	}
}
