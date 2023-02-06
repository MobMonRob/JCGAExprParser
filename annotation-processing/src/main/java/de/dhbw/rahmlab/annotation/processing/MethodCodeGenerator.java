package de.dhbw.rahmlab.annotation.processing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import de.dhbw.rahmlab.geomalgelang.api.Arguments;
import de.dhbw.rahmlab.geomalgelang.api.Result;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodCodeGenerator {

	protected final CGAAnnotatedMethod annotatedMethod;
	protected final ClassRepresentation<Arguments> argumentsRepresentation;
	protected final ClassRepresentation<Result> resultRepresentation;

	public MethodCodeGenerator(CGAAnnotatedMethod annotatedMethod, ClassRepresentation<Arguments> argumentsRepresentation, ClassRepresentation<Result> resultRepresentation) {
		this.annotatedMethod = annotatedMethod;
		this.argumentsRepresentation = argumentsRepresentation;
		this.resultRepresentation = resultRepresentation;
	}

	public MethodSpec generateCode() throws CGAAnnotationException {
		// Redundant to execute this for every MethodCodeGenerator
		ClassName programClass = ClassName.get(de.dhbw.rahmlab.geomalgelang.api.Program.class);
		ClassName argumentsClass = ClassName.get(de.dhbw.rahmlab.geomalgelang.api.Arguments.class);
		ClassName resultClass = ClassName.get(de.dhbw.rahmlab.geomalgelang.api.Result.class);

		List<MethodInvocationData> argumentMethodInvocations = computeArgumentsMethodInvocations();
		List<CodeBlock> argumentsMethodInvocationCode = createArgumentsMethodInvocationCode(argumentMethodInvocations);

		CodeBlock.Builder tryWithBodyBuilder = CodeBlock.builder()
			.addStatement("$1T arguments = new $1T()", argumentsClass);
		if (argumentsMethodInvocationCode.size() >= 1) {
			tryWithBodyBuilder.add("arguments");
		}
		tryWithBodyBuilder.add("$>");
		for (CodeBlock argumentsMethodInvocation : argumentsMethodInvocationCode) {
			tryWithBodyBuilder.add("\n");
			tryWithBodyBuilder.add(argumentsMethodInvocation);
		}
		if (argumentsMethodInvocationCode.size() >= 1) {
			tryWithBodyBuilder.add(";\n");
		}
		tryWithBodyBuilder.add("$<");
		tryWithBodyBuilder
			.addStatement("$T answer = program.invoke(arguments)", resultClass)
			.addStatement("var answerDecomposed = answer.$L()", computeDecompositionMethodName())
			.addStatement("return answerDecomposed");
		CodeBlock tryWithBody = tryWithBodyBuilder.build();

		MethodSpec method = MethodSpec.overriding(this.annotatedMethod.methodElement)
			.addStatement("String source = $S", this.annotatedMethod.cgaMethodAnnotation.source())
			.addCode("try ($1T program = new $1T(source)) {\n", programClass)
			.addCode("$>")
			.addCode(tryWithBody)
			.addCode("$<}")
			.build();

		return method;
	}

	protected String computeDecompositionMethodName() throws CGAAnnotationException {
		String returnType = this.annotatedMethod.methodRepresentation.returnType();
		String methodName = this.resultRepresentation.returnTypeToMethodName.get(returnType);
		if (methodName == null) {
			throw CGAAnnotationException.create(this.annotatedMethod.methodElement, "Return type \"%s\" is not supported.", returnType);
		}

		return methodName;
	}

	protected List<CodeBlock> createArgumentsMethodInvocationCode(List<MethodInvocationData> methodInvocations) {
		List<CodeBlock> cgaConstructionMethodInvocations = new ArrayList<>(methodInvocations.size());
		for (var methodInvocation : methodInvocations) {
			CodeBlock.Builder cgaConstructionMethodInvocation = CodeBlock.builder();
			cgaConstructionMethodInvocation.add(".$L($S", methodInvocation.method.identifier(), methodInvocation.cgaVarName);
			for (String argument : methodInvocation.arguments) {
				cgaConstructionMethodInvocation.add(", $L", argument);
			}
			cgaConstructionMethodInvocation.add(")");
			cgaConstructionMethodInvocations.add(cgaConstructionMethodInvocation.build());
		}

		return cgaConstructionMethodInvocations;
	}

	protected List<MethodInvocationData> computeArgumentsMethodInvocations() throws CGAAnnotationException {
		List<DecomposedParameter> decomposedParameters = decomposeParameters();
		LinkedHashMap<String, List<DecomposedParameter>> cgaVarNameGroupedDecomposedParameters = groupDecomposedParameters(decomposedParameters);
		List<MethodInvocationData> argumentsMethodInvocations = matchMethods(cgaVarNameGroupedDecomposedParameters);
		return argumentsMethodInvocations;
	}

	protected record MethodInvocationData(MethodRepresentation method, String cgaVarName, List<String> arguments) {

	}

	protected List<MethodInvocationData> matchMethods(Map<String, List<DecomposedParameter>> cgaVarNameGroupedDecomposedParameters) throws CGAAnnotationException {
		List<MethodInvocationData> methodInvocations = new ArrayList<>(cgaVarNameGroupedDecomposedParameters.size());
		for (var cgaVarNameGroupedDecomposedParameter : cgaVarNameGroupedDecomposedParameters.entrySet()) {

			String cgaVarName = cgaVarNameGroupedDecomposedParameter.getKey();
			List<DecomposedParameter> parameters = cgaVarNameGroupedDecomposedParameter.getValue();
			List<String> arguments = parameters.stream().map(a -> a.uncomposedParameter.identifier()).toList();
			MethodRepresentation method = matchMethod(parameters, cgaVarName);

			MethodInvocationData argumentsMethodInvocationData = new MethodInvocationData(method, cgaVarName, arguments);
			methodInvocations.add(argumentsMethodInvocationData);
		}
		return methodInvocations;
	}

	protected MethodRepresentation matchMethod(List<DecomposedParameter> callerParameters, String cgaVarName) throws CGAAnnotationException {
		// Safe assumption, List contains at least one parameter.
		// Safe assumption all parameters of the list have the same cgaType.
		String cgaType = callerParameters.get(0).cgaType;
		MethodRepresentation callee = this.argumentsRepresentation.methodNameToMethod.get(cgaType);
		// Check matching method name.
		if (callee == null) {
			throw CGAAnnotationException.create(this.annotatedMethod.methodElement, "No matching Methodname found for: %s", cgaType);
		}
		List<ParameterRepresentation> calleeParameters = callee.parameters();
		// Check matching paramter and arguments count.
		if (calleeParameters.size() != 1 + callerParameters.size()) {
			throw CGAAnnotationException.create(this.annotatedMethod.methodElement, "CGA type \"%s\" needs %d arguments, but only %d were given for cga variable \"%s\".", cgaType, calleeParameters.size() - 1, callerParameters.size(), cgaVarName);
		}
		// Check equal types
		int size = callerParameters.size();
		for (int i = 0; i < size; ++i) {
			ParameterRepresentation parameter = callee.parameters().get(i + 1);
			DecomposedParameter argument = callerParameters.get(i);
			if (!argument.javaType.equals(parameter.type())) {
				throw CGAAnnotationException.create(this.annotatedMethod.methodElement, "For cga variable \"%s\" at relative position %d: provided java type (\"%s\") differs from expected java type (\"%s\").", cgaVarName, i, argument.javaType, parameter.type());
			}
		}
		return callee;
	}

	protected LinkedHashMap<String, List<DecomposedParameter>> groupDecomposedParameters(List<DecomposedParameter> decomposedParameters) throws CGAAnnotationException {
		// Group cgaVarNames.
		LinkedHashMap<String, List<DecomposedParameter>> cgaVarNameGroupedDecomposedParameters = decomposedParameters.stream().collect(Collectors.groupingBy(dp -> dp.cgaVarName, LinkedHashMap::new, Collectors.toList()));

		// Check that cgaType of each cgaVarNameGroup is identical.
		// Equal to: there is exactly one cgaTypeGroup within each cgaVarNameGroup.
		for (Map.Entry<String, List<DecomposedParameter>> cgaVarNameGroup : cgaVarNameGroupedDecomposedParameters.entrySet()) {
			Map<String, List<DecomposedParameter>> cgaTypeGroups = cgaVarNameGroup.getValue().stream()
				.collect(Collectors.groupingBy(dp -> dp.cgaType));
			if (cgaTypeGroups.size() != 1) {
				throw CGAAnnotationException.create(this.annotatedMethod.methodElement, "CGA type name must be equal for all occurences of the same cga parameter but were not for the cga parameter with name \"%s\".", cgaVarNameGroup.getKey());
			}
		}

		return cgaVarNameGroupedDecomposedParameters;
	}

	protected record DecomposedParameter(String cgaVarName, String cgaType, String javaType, ParameterRepresentation uncomposedParameter) {

	}

	protected List<DecomposedParameter> decomposeParameters() throws CGAAnnotationException {
		List<ParameterRepresentation> parameters = this.annotatedMethod.methodRepresentation.parameters();
		List<DecomposedParameter> decomposedParameters = new ArrayList<>(parameters.size());
		for (ParameterRepresentation parameter : parameters) {
			String[] identifierSplit = parameter.identifier().split("_", 3);
			if (identifierSplit.length < 2) {
				throw CGAAnnotationException.create(this.annotatedMethod.methodElement, "Parameter name \"%s\" must contain at least one \"_\"", parameter.identifier());
			}

			String cgaVarName = identifierSplit[0];
			String cgaType = identifierSplit[1];
			String javaType = parameter.type();

			DecomposedParameter decomposedParameter = new DecomposedParameter(cgaVarName, cgaType, javaType, parameter);
			decomposedParameters.add(decomposedParameter);
		}
		return decomposedParameters;
	}
}
