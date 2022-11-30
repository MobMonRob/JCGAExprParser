package de.dhbw.rahmlab.geomalgelang.truffle.feature.function.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import de.dhbw.rahmlab.geomalgelang.truffle.GeomAlgeLang;
import de.dhbw.rahmlab.geomalgelang.truffle.feature.function.nodes.superClasses.FunctionBody;

public final class FunctionRootNode extends RootNode {

	@SuppressWarnings("FieldMayBeFinal")
	@Child
	private FunctionBody functionBodyNode;

	public FunctionRootNode(GeomAlgeLang truffleLanguage, FunctionBody functionBodyNode) {
		super(truffleLanguage);
		this.functionBodyNode = functionBodyNode;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return this.functionBodyNode.executeGeneric(frame);
	}
}
