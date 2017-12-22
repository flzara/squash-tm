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

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;

/**
 * Consumes {@link DynamicManager} annotated interfacrs and produces spring configuration accordlingly.
 * 
 * @author Gregory Fouquet
 * 
 * 
 */
@SupportedAnnotationTypes(value = { "org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager", })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DynamicManagerProcessor extends DynamicComponentProcessor<DynamicManager> {
	private static final String DYNAMIC_MANAGER_BEAN_FACTORY = "org.squashtest.tm.core.dynamicmanager.factory.DynamicManagerFactoryBean";

	@Override
	protected String beanFactoryClass() {
		return DYNAMIC_MANAGER_BEAN_FACTORY;
	}

	@Override
	protected Class<DynamicManager> annotationClass() {
		return DynamicManager.class;
	}

	@Override
	protected String generatedFileName() {
		return "dynamicmanager-context.xml";
	}
	

	/**
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#entityClass(java.lang.annotation.Annotation)
	 */
	@Override
	protected Class<?> entityClass(DynamicManager componentDefinition) {
		return componentDefinition.entity();
	}

	/**
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#beanName(java.lang.annotation.Annotation)
	 */
	@Override
	protected String beanName(DynamicManager componentDefinition) {
		return componentDefinition.name();
	}

	/**
	 * @see org.squashtest.tm.tools.annotation.processor.DynamicComponentProcessor#lookupCustomImplementation(java.lang.annotation.Annotation)
	 */
	@Override
	protected boolean lookupCustomImplementation(DynamicManager definition) {
		return definition.hasCustomImplementation();
	}

	@Override
	protected String primaryAttribute(DynamicManager definition) {
		return definition.primary() ? "primary=\"true\"" : "";
	}

	
}
