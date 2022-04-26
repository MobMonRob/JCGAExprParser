/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.dhbw.rahmlab.geomalgelang.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.source.Source;
import de.dhbw.rahmlab.geomalgelang.parsing.ParsingService;
import de.dhbw.rahmlab.geomalgelang.truffle.nodes.BaseNode;
import de.dhbw.rahmlab.geomalgelang.truffle.nodes.GeomAlgeLangRootNode;
import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

/**
 *
 * @author fabian
 */
@TruffleLanguage.Registration(
	id = "geomalgelang",
	name = "GeomAlgeLang",
	version = "0.0.1")
public class GeomAlgeLang extends TruffleLanguage<GeomAlgeLangContext> {

	@Override
	protected GeomAlgeLangContext createContext(Env env) {
		return new GeomAlgeLangContext();
	}

	@Override
	protected Object getScope(GeomAlgeLangContext context) {
		return context.globalVariableScope;
	}

	@Override
	protected CallTarget parse(ParsingRequest request) throws Exception {
		GeomAlgeLangRootNode rootNode = parseSource(request.getSource());
		return rootNode.getCallTarget();
	}

	private GeomAlgeLangRootNode parseSource(Source source) throws IOException {
		CharStream inputStream = CharStreams.fromReader(source.getReader());
		ParsingService parsingService = new ParsingService(inputStream);
		BaseNode topNode = parsingService.getTruffleTopNode();
		GeomAlgeLangRootNode rootNode = new GeomAlgeLangRootNode(this, new FrameDescriptor(), topNode);
		return rootNode;
	}
}
