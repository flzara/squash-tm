/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.tools.annotation.processor;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Gregory Fouquet
 * 
 */
public abstract class DynamicComponentProcessor<ANNOTATION extends Annotation> extends AbstractProcessor {

	private static final String FILE_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ "  xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\">\n\n";

	
	private static final String FILE_FOOTER = "</beans>\n";

	protected static final String DYNAMIC_COMPONENT_TEMPLATE = "  <bean id=\"{0}\" {5} class=\"{1}\" depends-on=\"entityManagerFactory\">\n"
			+ "    <property name=\"componentType\" value=\"{2}\" />\n"
			+ "    <property name=\"entityType\" value=\"{3}\" />\n"
			+ "    <property name=\"lookupCustomImplementation\" value=\"{4}\" />\n" 
			+ "  </bean>\n";

	private Filer filer;
	private Messager messager;
	private List<Element> dynamicComponents = new ArrayList<Element>();

	/**
	 * 
	 */
	public DynamicComponentProcessor() {
		super();
	}

	@Override
	public final synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();

	}

	/**
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
	 *      javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		enqueueComponents(roundEnvironment);

		if (roundEnvironment.processingOver()) {
			processComponents();
		}

		return true;
	}

	private void enqueueComponents(RoundEnvironment roundEnvironment) {
		for (Element annotated : roundEnvironment.getElementsAnnotatedWith(annotationClass())) {
//			messager.printMessage(Kind.NOTE, "INFO Enqueued dynamic component " + annotationClass().getSimpleName(),
//					annotated);

			if (checkTarget(annotationClass(), annotated)) {
				dynamicComponents.add(annotated);
			}
		}
	}

	/**
	 * @param annotation
	 * @param annotated
	 * @return
	 */
	private boolean checkTarget(Class<ANNOTATION> annotation, Element annotated) {
		if (!annotated.getKind().isInterface()) {
			messager.printMessage(Kind.ERROR, "ERROR Only interfaces can be annotated @" + annotation.getSimpleName(),
					annotated);

			return false;
		}

		return true;
	}

	private void processComponents() {
		Writer writer = null;

		try {
			writer = openWriter();
			outputSpringContextFile(writer);
		} catch (IOException e) {
			messager.printMessage(Kind.WARNING, "WARNING Error during processing of @" + annotationClass().getSimpleName()
					+ " annotations");
			e.printStackTrace(); // NOSONAR : I dont want no logger
		} finally {
			if (writer != null) {
				noFailCloseFile(writer);
			}
		}

	}

	private void outputSpringContextFile(Writer writer) throws IOException {
		writer.append(FILE_HEADER);
		
		for (Element manager : dynamicComponents) {
//			messager.printMessage(Kind.NOTE,
//					"INFO Processing @" + annotationClass().getSimpleName() + ' ' + manager.getSimpleName(), manager);

			String beanDefinition = buildBeanDefinition(manager);

			writer.append(beanDefinition);
		}

		writer.append(FILE_FOOTER);
	}

	private Writer openWriter() throws IOException {
		FileObject file;
		Writer writer;
		file = filer.createResource(StandardLocation.SOURCE_OUTPUT, "spring", generatedFileName(), (Element[]) null);

		writer = file.openWriter();
		return writer;
	}

	private void noFailCloseFile(Writer writer) {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); // NOSONAR : I dont want no logger
		}
	}

	protected abstract Class<ANNOTATION> annotationClass();

	private String buildBeanDefinition(Element component) {
		ANNOTATION definition = component.getAnnotation(annotationClass());

		CharSequence beanName = StringUtils.isBlank(beanName(definition)) ? defaultBeanName(component)
				: beanName(definition);
		CharSequence managerClass = ((TypeElement) component).getQualifiedName();
		TypeMirror entityClass = extractEntityClass(definition);

		boolean lookupCustomImplementation = lookupCustomImplementation(definition);
		String primary = primaryAttribute(definition);

		String beanDefinition = MessageFormat.format(DYNAMIC_COMPONENT_TEMPLATE, beanName, beanFactoryClass(),
				managerClass, entityClass, lookupCustomImplementation, primary);
		return beanDefinition;
	}

	protected abstract String primaryAttribute(ANNOTATION definition);

	private String defaultBeanName(Element manager) {
		return StringUtils.uncapitalize(manager.getSimpleName().toString());
	}

	private TypeMirror extractEntityClass(ANNOTATION definition) {
		// explanations for the following turd :
		// http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
		TypeMirror entityClass = null;

		try {
			entityClass(definition);
		} catch (MirroredTypeException e) {
			entityClass = e.getTypeMirror();
		}
		return entityClass;
	}

	/**
	 * Should get the entity property of the given component definition. Which should puke a
	 * {@link MirroredTypeException} but it's OK.
	 * 
	 * @param componentDefinition
	 * @return
	 */
	protected abstract Class<?> entityClass(ANNOTATION componentDefinition);

	/**
	 * Should get the "name" property of the annotation.
	 * 
	 * @param componentDefinition
	 * @return
	 */
	protected abstract String beanName(ANNOTATION componentDefinition);

	protected abstract String beanFactoryClass();

	protected abstract String generatedFileName();

	/**
	 * @param definition
	 * @return whether dynamic component factory should lookup custom implementation or not.
	 */
	protected abstract boolean lookupCustomImplementation(ANNOTATION definition);


	/**
	 * @return the messager
	 */
	protected final Messager getMessager() {
		return messager;
	}


}