/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.dhbw.rahmlab.geomalgelang.truffle.common.nodes;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import de.dhbw.rahmlab.geomalgelang.truffle.common.nodes.exprSuperClasses.ExpressionBaseNode;
import de.dhbw.rahmlab.geomalgelang.truffle.common.runtime.ExecutionValidation;

/**
 *
 * @author fabian
 */
public class ProgramRootNode extends RootNode {

	@Child
	private ExpressionBaseNode bodyNode;

	private final ExecutionValidation executionValidation;

	public ProgramRootNode(TruffleLanguage<?> language, FrameDescriptor frameDescriptor, ExpressionBaseNode bodyNode, ExecutionValidation executionValidation) {
		super(language, frameDescriptor);
		this.bodyNode = bodyNode;
		this.executionValidation = executionValidation;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		this.executionValidation.validate();
		return this.bodyNode.executeGeneric(frame);
	}
}