package de.dhbw.rahmlab.geomalgelang.annotation.processing;

import de.dhbw.rahmlab.geomalgelang.api.annotation.CGA;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
public class CGAProcessor extends AbstractProcessor {

	protected Elements elementUtils;
	protected Filer filer;
	protected ExceptionHandler exceptionHandler;

	private static CGAMethodCodeGenerator.Factory methodCodeGeneratorFactory = null;

	protected static final Set<String> supportedAnnotationTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(new String[]{CGA.class.getCanonicalName()})));

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return supportedAnnotationTypes;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_17;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.elementUtils = processingEnv.getElementUtils();
		this.filer = processingEnv.getFiler();
		this.exceptionHandler = new ExceptionHandler(processingEnv.getMessager());
		this.exceptionHandler.handle(() -> {
			try {
				methodCodeGeneratorFactory = CGAMethodCodeGenerator.Factory.init(this.elementUtils);

				// Mitigates issues with Netbeans realtime codeanalysis.
			} catch (AnnotationException ex) {
				this.exceptionHandler.warn(ex.element, ex.getMessage());
			}
		});
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		this.exceptionHandler.handle(() -> {
			List<CGAAnnotatedMethod> annotatedMethods = computeAnnotatedMethodsFrom(roundEnv);
			Map<String, List<CGAAnnotatedMethod>> interfaceGroupedAnnotatedMethods = computeInterfaceGroupedAnnotatedMethodsFrom(annotatedMethods);
			List<ClassCodeGenerator> classCodeGenerators = computeClassCodeGeneratorsFrom(interfaceGroupedAnnotatedMethods);
			generateCode(classCodeGenerators);
		});

		// The return boolean value should be true if your annotation processor has processed all the passed annotations, and you don't want them to be passed to other annotation processors down the list.
		return true;
	}

	protected void generateCode(List<ClassCodeGenerator> classCodeGenerators) throws IOException, AnnotationException {
		for (ClassCodeGenerator classCodeGenerator : classCodeGenerators) {
			classCodeGenerator.generateCode(this.elementUtils, this.filer);
		}
	}

	protected List<ClassCodeGenerator> computeClassCodeGeneratorsFrom(Map<String, List<CGAAnnotatedMethod>> interfaceGroupedAnnotatedMethods) throws AnnotationException {
		List<ClassCodeGenerator> classCodeGenerators = new ArrayList<>(interfaceGroupedAnnotatedMethods.size());
		for (var methodGroupEntry : interfaceGroupedAnnotatedMethods.entrySet()) {
			String qualifiedInterfaceName = methodGroupEntry.getKey();
			var methodGroup = methodGroupEntry.getValue();
			List<CGAMethodCodeGenerator> methodCodeGenerators = computeMethodCodeGenerators(methodGroup);
			ClassCodeGenerator classCodeGenerator = new ClassCodeGenerator(qualifiedInterfaceName, methodCodeGenerators);
			classCodeGenerators.add(classCodeGenerator);
		}

		return classCodeGenerators;
	}

	protected List<CGAMethodCodeGenerator> computeMethodCodeGenerators(List<CGAAnnotatedMethod> methodGroup) throws AnnotationException {
		List<CGAMethodCodeGenerator> methodCodeGenerators = new ArrayList<>(methodGroup.size());
		for (CGAAnnotatedMethod cgaAnnotatedMethod : methodGroup) {
			CGAMethodCodeGenerator methodCodeGenerator = methodCodeGeneratorFactory.create(cgaAnnotatedMethod);
			methodCodeGenerators.add(methodCodeGenerator);
		}
		return methodCodeGenerators;
	}

	protected Map<String, List<CGAAnnotatedMethod>> computeInterfaceGroupedAnnotatedMethodsFrom(List<CGAAnnotatedMethod> annotatedMethods) {
		Map<String, List<CGAAnnotatedMethod>> interfaceGroupedAnnotatedMethods = annotatedMethods.stream()
			.collect(Collectors.groupingBy(am -> am.enclosingInterfaceQualifiedName));
		return interfaceGroupedAnnotatedMethods;
	}

	protected List<CGAAnnotatedMethod> computeAnnotatedMethodsFrom(RoundEnvironment roundEnv) throws AnnotationException {
		Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(CGA.class);
		List<CGAAnnotatedMethod> annotatedMethods = new ArrayList<>(annotatedElements.size());

		for (Element annotatedElement : annotatedElements) {
			// Already assured by @Target(ElementType.METHOD) in CGA.java
			/*
			if (annotatedElement.getKind() != ElementKind.METHOD) {
				error(annotatedElement, "Annotation needs to be on METHOD, but was on %s.", annotatedElement.getKind().toString());
				return true;
			}
			 */
			ExecutableElement method = (ExecutableElement) annotatedElement;
			CGAAnnotatedMethod annotatedMethod = new CGAAnnotatedMethod(method);

			annotatedMethods.add(annotatedMethod);
		}

		return annotatedMethods;
	}
}
