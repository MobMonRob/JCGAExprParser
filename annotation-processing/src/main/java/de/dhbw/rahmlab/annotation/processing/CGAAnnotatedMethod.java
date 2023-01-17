package de.dhbw.rahmlab.annotation.processing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class CGAAnnotatedMethod {

	public record Parameter(String type, String identifier) {

	}

	protected final ExecutableElement methodElement;
	public final String enclosingInterfaceQualifiedName;
	// public final String enclosingInterfaceName;
	// public final String enclosingPackageName;
	public final CGA cgaMethodAnnotation;
	public final String returnType;
	public final String identifier;
	public final List<Parameter> parameters;

	public CGAAnnotatedMethod(ExecutableElement methodElement) throws CGAAnnotationException {
		this.methodElement = methodElement;
		this.enclosingInterfaceQualifiedName = getEnclosingInterfaceQualifiedName(methodElement);
		// int nameSeparatorIndex = this.enclosingInterfaceQualifiedName.lastIndexOf(".");
		// this.enclosingInterfaceName = this.enclosingInterfaceQualifiedName.substring(nameSeparatorIndex + 1, this.enclosingInterfaceQualifiedName.length());
		// this.enclosingPackageName = this.enclosingInterfaceQualifiedName.substring(0, nameSeparatorIndex);
		this.cgaMethodAnnotation = methodElement.getAnnotation(CGA.class);
		ensureModifiersContainPublic(methodElement);
		this.returnType = methodElement.getReturnType().toString();
		this.identifier = methodElement.getSimpleName().toString();
		this.parameters = getParameters(methodElement);
	}

	protected static List<Parameter> getParameters(ExecutableElement methodElement) {
		List<? extends VariableElement> variableElementParameters = methodElement.getParameters();
		List<Parameter> parameters = new ArrayList<>(variableElementParameters.size());
		for (VariableElement variableElementParameter : variableElementParameters) {
			String type = variableElementParameter.asType().toString();
			String name = variableElementParameter.getSimpleName().toString();
			Parameter parameter = new Parameter(type, name);
			parameters.add(parameter);
		}
		return parameters;
	}

	protected static void ensureModifiersContainPublic(ExecutableElement methodElement) throws CGAAnnotationException {
		Set<Modifier> modifiers = methodElement.getModifiers();
		boolean containsPublic = modifiers.contains(Modifier.PUBLIC);
		if (!containsPublic) {
			throw CGAAnnotationException.create(methodElement, "Method needs to be \"public\".");
		}
	}

	/*
	protected static String getEnclosingPackageName(ExecutableElement methodElement) throws CGAAnnotationException {
		PackageElement enclosingPackage = null;

		for (Element currentEnclosingElement = methodElement.getEnclosingElement(); currentEnclosingElement != null; currentEnclosingElement = currentEnclosingElement.getEnclosingElement()) {
			if (currentEnclosingElement.getKind() == ElementKind.PACKAGE) {
				enclosingPackage = (PackageElement) currentEnclosingElement;
				break;
			}
		}

		if (enclosingPackage == null) {
			CGAAnnotationException.create(methodElement, "Enclosing package not found");
		}

		return enclosingPackage.getQualifiedName().toString();
	}
	 */
	protected static String getEnclosingInterfaceQualifiedName(ExecutableElement methodElement) throws CGAAnnotationException {
		TypeElement enclosingInterface = null;

		Element directEnclosingElement = methodElement.getEnclosingElement();
		ElementKind directEnclosingElementKind = directEnclosingElement.getKind();

		if (directEnclosingElementKind == ElementKind.INTERFACE) {
			enclosingInterface = (TypeElement) directEnclosingElement;
		} else {
			throw CGAAnnotationException.create(methodElement, "Expected method to be enclosed by an INTERFACE, but was enclosed by %s", directEnclosingElementKind.toString());
		}

		return enclosingInterface.getQualifiedName().toString();
	}

	public MethodSpec generateCode() throws CGAAnnotationException {
		ClassName programClass = ClassName.get("de.dhbw.rahmlab.geomalgelang.api", "Progam");
		ClassName argumentsClass = ClassName.get("de.dhbw.rahmlab.geomalgelang.api", "Arguments");
		ClassName resultClass = ClassName.get("de.dhbw.rahmlab.geomalgelang.api", "Result");

		String answerDecompose = switch (this.returnType) {
			case "double" ->
				"decomposeScalar";
			case "iCGATangentOrRound.EuclideanParameters" ->
				"decomposeTangentOrRound";
			case "iCGAFlat.EuclideanParameters" ->
				"decomposeFlat";
			case "Vector3d" ->
				"decomposeAttitude";
			case "Quat4d" ->
				"decomposeRotor";
			case "PointPair" ->
				"decomposePointPair";
			default ->
				null;
		};
		if (answerDecompose == null) {
			throw CGAAnnotationException.create(this.methodElement, "Return type \"%s\" is not supported.", this.returnType);
		}

		List<CodeBlock> arguments = new ArrayList<>(this.parameters.size());
		for (Parameter parameter : parameters) {
			//
		}

		CodeBlock.Builder tryWithBodyBuilder = CodeBlock.builder()
			.addStatement("$1T arguments = new $1T()", argumentsClass);
		for (CodeBlock argument : arguments) {
			tryWithBodyBuilder.addStatement(argument);
		}
		tryWithBodyBuilder
			.addStatement("$T answer = program.invoke(arguments)", resultClass)
			.addStatement("var answerDecomposed = answer.$L()", answerDecompose)
			.addStatement("return answerDecomposed");
		CodeBlock tryWithBody = tryWithBodyBuilder.build();

		MethodSpec method = MethodSpec.overriding(methodElement)
			.addStatement("String source = $S", this.cgaMethodAnnotation.source())
			.addCode("try ($1T program = new $1T(source)) {\n", programClass)
			.addCode("$>")
			.addCode(tryWithBody)
			.addCode("$<}")
			.build();

		return method;
	}
}
