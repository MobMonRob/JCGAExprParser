package de.dhbw.rahmlab.geomalgelang.annotation.processing;

import com.googlecode.concurrenttrees.common.KeyValuePair;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import de.dhbw.rahmlab.geomalgelang.api.Arguments;
import de.dhbw.rahmlab.geomalgelang.api.Result;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class CGAMethodCodeGenerator {

	protected final CGAAnnotatedMethod annotatedMethod;

	private static ClassRepresentation<Arguments> argumentsRepresentation;
	private static ClassRepresentation<Result> resultRepresentation;
	private static ClassName programClass;
	private static ClassName argumentsClass;
	private static ClassName resultClass;
	// private static TypeName IOExceptionTypeName;

	public static class Factory {

		private static Factory factory = null;

		private Factory() {

		}

		public static synchronized Factory init(Elements elementUtils) throws AnnotationException {
			if (factory != null) {
				throw AnnotationException.create(null, "CGAMethodCodeGenerator.Factory was already inited. Can be inited only once.");
			}

			CGAMethodCodeGenerator.init(elementUtils);

			// Ensures subsequent initing will work even if an exception is thrown in the init() method on the first invokation.
			if (factory == null) {
				factory = new Factory();
			}

			return factory;
		}

		public CGAMethodCodeGenerator create(CGAAnnotatedMethod annotatedMethod) throws AnnotationException {
			return new CGAMethodCodeGenerator(annotatedMethod);
		}
	}

	private CGAMethodCodeGenerator(CGAAnnotatedMethod annotatedMethod) throws AnnotationException {
		this.annotatedMethod = annotatedMethod;
	}

	private static void init(Elements elementUtils) {
		TypeElement argumentsTypeElement = elementUtils.getTypeElement(Arguments.class.getCanonicalName());
		argumentsRepresentation = new ClassRepresentation<>(argumentsTypeElement);
		List<MethodRepresentation> flattenedPublicMethods = argumentsRepresentation.publicMethods.stream()
			.flatMap(m -> m.getOverloadsView().stream())
			.toList();
		String stringTypeName = String.class.getCanonicalName();
		for (var method : flattenedPublicMethods) {
			List<ParameterRepresentation> parameters = method.parameters();
			int parametersSize = parameters.size();
			if (parametersSize < 1) {
				throw new IllegalArgumentException(String.format("Expected parameters.size() to be at least 1 for all overloads of \"%s\", but was %s for one.", method.identifier(), parametersSize));
			}
			String type = parameters.get(0).type();
			if (!type.equals(stringTypeName)) {
				throw new IllegalArgumentException(String.format("Expected first parameter of method \"%s\" of type \"%s\", but was of type \"%s\".", method.identifier(), stringTypeName, type));
			}
		}

		TypeElement resultTypeElement = elementUtils.getTypeElement(Result.class.getCanonicalName());
		resultRepresentation = new ClassRepresentation<>(resultTypeElement);

		programClass = ClassName.get(de.dhbw.rahmlab.geomalgelang.api.Program.class);
		argumentsClass = ClassName.get(de.dhbw.rahmlab.geomalgelang.api.Arguments.class);
		resultClass = ClassName.get(de.dhbw.rahmlab.geomalgelang.api.Result.class);

		// IOExceptionTypeName = TypeName.get(elementUtils.getTypeElement(IOException.class.getCanonicalName()).asType());
	}

	public MethodSpec generateCode() throws AnnotationException {
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
			.addStatement("String source = $S", this.annotatedMethod.cgaMethodAnnotation.value())
			.addCode("try ($1T program = new $1T(source)) {\n", programClass)
			.addCode("$>")
			.addCode(tryWithBody)
			.addCode("$<}")
			.build();

		return method;
	}

	protected String computeDecompositionMethodName() throws AnnotationException {
		MethodRepresentation method = this.annotatedMethod.methodRepresentation;
		String returnType = method.returnType();

		List<OverloadableMethodRepresentation> methods = resultRepresentation.returnTypeToMethods.get(returnType);
		if (methods == null) {
			throw AnnotationException.create(this.annotatedMethod.methodElement, "Return type \"%s\" is not supported.", returnType);
		}

		// Parameters are currently not supported.
		List<MethodRepresentation> flattenedMethods = methods.stream()
			.flatMap(m -> m.getOverloadsView().stream())
			.filter(m -> m.parameters().isEmpty())
			.toList();

		if (flattenedMethods.size() != 1) {
			throw AnnotationException.create(this.annotatedMethod.methodElement, "Found %s methods without parameters matching returnType \"%s\", but expected 1.", flattenedMethods.size(), returnType);
		}
		String identifier = flattenedMethods.get(0).identifier();

		return identifier;
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

	protected List<MethodInvocationData> computeArgumentsMethodInvocations() throws AnnotationException {
		List<DecomposedParameter> decomposedParameters = decomposeParameters();
		LinkedHashMap<String, List<DecomposedParameter>> cgaVarNameGroupedDecomposedParameters = groupDecomposedParameters(decomposedParameters);
		List<MethodInvocationData> argumentsMethodInvocations = matchMethods(cgaVarNameGroupedDecomposedParameters);
		return argumentsMethodInvocations;
	}

	protected record MethodInvocationData(MethodRepresentation method, String cgaVarName, List<String> arguments) {

	}

	protected List<MethodInvocationData> matchMethods(Map<String, List<DecomposedParameter>> cgaVarNameGroupedDecomposedParameters) throws AnnotationException {
		ArrayList<MethodInvocationData> methodInvocations = new ArrayList<>(cgaVarNameGroupedDecomposedParameters.size());
		for (var cgaVarNameGroupedDecomposedParameter : cgaVarNameGroupedDecomposedParameters.entrySet()) {
			String cgaVarName = cgaVarNameGroupedDecomposedParameter.getKey();
			List<DecomposedParameter> parameters = cgaVarNameGroupedDecomposedParameter.getValue();
			List<String> arguments = parameters.stream().map(a -> a.uncomposedParameter.identifier()).toList();
			MethodRepresentation method = matchMethodFrom(parameters, cgaVarName);
			MethodInvocationData argumentsMethodInvocationData = new MethodInvocationData(method, cgaVarName, arguments);
			methodInvocations.add(argumentsMethodInvocationData);
		}
		return methodInvocations;
	}

	protected MethodRepresentation matchMethodFrom(List<DecomposedParameter> callerParameters, String cgaVarName) throws AnnotationException {
		// Check that cgaConstructorMethod of the whole group is identical.
		Iterator<DecomposedParameter> groupIterator = callerParameters.iterator();
		// Safe assumption that callerParameters contains at least 1 element (groupDecomposedParameters()).
		String remainingVarName = groupIterator.next().remainingVarName;
		KeyValuePair<OverloadableMethodRepresentation> cgaConstructorMethod = argumentsRepresentation.methodsPrefixTrie.getKeyValuePairForLongestKeyPrefixing(remainingVarName);
		if (cgaConstructorMethod == null) {
			throw AnnotationException.create(this.annotatedMethod.methodElement, "No matching Methodname found for: %s", remainingVarName);
		}
		String methodName = cgaConstructorMethod.getKey().toString();
		boolean allSamePrefixed = toStream(groupIterator)
			.map(dp -> dp.remainingVarName.startsWith(methodName))
			.allMatch(b -> b == true);
		if (!allSamePrefixed) {
			throw AnnotationException.create(this.annotatedMethod.methodElement, "Methodname must be equal for all occurences of the same cga parameter but were not for the cga parameter with name \"%s\".", cgaVarName);
		}

		OverloadableMethodRepresentation callees = cgaConstructorMethod.getValue();

		// Cannot check equal returnType because is same for all (Refer to Arguments class).
		// Same identifier and parameter count and parameter types sequence implies identical method.
		int expectedCalleeParameterSize = 1 + callerParameters.size();
		List<MethodRepresentation> calleesWithMatchingParameterSize = callees.getOverloadsView().stream()
			.filter(m -> m.parameters().size() == expectedCalleeParameterSize)
			.toList();
		// Check matching caller and callee parameter size.
		if (calleesWithMatchingParameterSize.isEmpty()) {
			throw AnnotationException.create(this.annotatedMethod.methodElement, "Available overloads of method \"%s\" do not match the parameter count (%s) given for the variable \"%s\"", methodName, callerParameters.size(), cgaVarName);
		}

		// Check equal types
		int callerParametersSize = callerParameters.size();
		List<MethodRepresentation> matchedCallee = calleesWithMatchingParameterSize.stream()
			.filter(callee -> {
				for (int i = 0; i < callerParametersSize; ++i) {
					String calleeParameterType = callee.parameters().get(i + 1).type();
					String callerParameterType = callerParameters.get(i).javaType;
					if (!calleeParameterType.equals(callerParameterType)) {
						return false;
					}
				}
				return true;
			}).toList();
		if (matchedCallee.size() < 1) {
			throw AnnotationException.create(this.annotatedMethod.methodElement, "Available overloads of method \"%s\" do not match the parameter types given for the variable \"%s\"", methodName, cgaVarName);
		}
		// >1 not possible, because "Same identifier and parameter count and parameter types sequence implies identical method." implies that there can be at most 1 such method.

		return matchedCallee.get(0);
	}

	protected static <T> Stream<T> toStream(Iterator<T> iterator) {
		return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), false);
	}

	protected LinkedHashMap<String, List<DecomposedParameter>> groupDecomposedParameters(List<DecomposedParameter> decomposedParameters) throws AnnotationException {
		// Group cgaVarNames.
		LinkedHashMap<String, List<DecomposedParameter>> cgaVarNameGroupedDecomposedParameters = decomposedParameters.stream().collect(Collectors.groupingBy(dp -> dp.cgaVarName, LinkedHashMap::new, Collectors.toList()));

		return cgaVarNameGroupedDecomposedParameters;
	}

	protected record DecomposedParameter(String cgaVarName, String remainingVarName, String javaType, ParameterRepresentation uncomposedParameter) {

	}

	protected List<DecomposedParameter> decomposeParameters() throws AnnotationException {
		List<ParameterRepresentation> parameters = this.annotatedMethod.methodRepresentation.parameters();
		List<DecomposedParameter> decomposedParameters = new ArrayList<>(parameters.size());
		for (ParameterRepresentation parameter : parameters) {
			String[] identifierSplit = parameter.identifier().split("_", 2);
			if (identifierSplit.length < 2) {
				throw AnnotationException.create(this.annotatedMethod.methodElement, "Parameter name \"%s\" must contain at least one \"_\"", parameter.identifier());
			}

			String cgaVarName = identifierSplit[0];
			String remainingVarName = identifierSplit[1];
			String javaType = parameter.type();

			DecomposedParameter decomposedParameter = new DecomposedParameter(cgaVarName, remainingVarName, javaType, parameter);
			decomposedParameters.add(decomposedParameter);
		}
		return decomposedParameters;
	}
}
