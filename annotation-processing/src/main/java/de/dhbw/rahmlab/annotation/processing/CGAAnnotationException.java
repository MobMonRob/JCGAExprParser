package de.dhbw.rahmlab.annotation.processing;

import javax.lang.model.element.Element;

public class CGAAnnotationException extends Exception {

	public final Element element;

	protected CGAAnnotationException(Element element, String message, Object... args) {
		super(String.format(message, args));
		this.element = element;
	}

	public static CGAAnnotationException create(Element element, String message, Object... args) throws CGAAnnotationException {
		return new CGAAnnotationException(element, message, args);
	}
}
