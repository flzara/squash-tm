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
package org.squashtest.tm.core.dynamicmanager.factory;

import javax.inject.Provider;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 * Provides a custom implementation for a dynamic manager. The custom implementation is fetched from the bean factory
 * using its bean name, and this fetch is deferred until {@link #get()} is invoked.
 * 
 * @author Gregory Fouquet
 * 
 */
class DeferredLookupCustomImplementationProvider implements Provider<Object> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeferredLookupCustomImplementationProvider.class);

	private final BeanFactory beanFactory;
	private final String customImplementationBeanName;
	private Object customImplementation;

	public DeferredLookupCustomImplementationProvider(@NotNull BeanFactory beanFactory,
			@NotNull String customImplementationBeanName) {
		super();
		this.beanFactory = beanFactory;
		this.customImplementationBeanName = customImplementationBeanName;
	}

	@Override
	public Object get() {
		if (customImplementation == null) {
			LOGGER.debug("Deferred initialization of custom implementation bean named {}", customImplementationBeanName);
			customImplementation = beanFactory.getBean(customImplementationBeanName);
		}

		return customImplementation;
	}

}
