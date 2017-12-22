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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.squashtest.tm.core.dynamicmanager.internal.handler.CompositeInvocationHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.CustomMethodHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.DynamicComponentInvocationHandler;

/**
 * This class is an abstract Spring bean factory for "dynamic components". A "dynamic component" is a Spring managed
 * singleton (@Component) defined by its interface and which behaviour is dynamically determined by this interface.
 *
 * For example, a "dynamic manager" would define a <code>void changeXxx(entityId, newValue)</code>. This method would
 * fetch an entity from a predetermined type and change its <code>xxx</code> property to <code>newValue</code>.
 *
 * @author Gregory Fouquet
 *
 * @param <COMPONENT>
 */
public abstract class AbstractDynamicComponentFactoryBean<COMPONENT> implements FactoryBean<COMPONENT>,
		BeanFactoryAware, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDynamicComponentFactoryBean.class);

	private BeanFactory beanFactory;

	@PersistenceContext
	protected EntityManager entityManager;

	/**
	 * Type of Manager service interface which should be instanciated. Should be configured by Spring.
	 */
	private Class<COMPONENT> componentType;

	/**
	 * Should this factory lookup a custom manager in Spring's bean factory. Looked up type is the *FIRST*
	 * superinterface of the dynamic manager type. Can be configured by Spring.
	 */
	private boolean lookupCustomImplementation = true;

	/**
	 * Custom manager name, should either be intialized to handle custom services which cannot be adressed by dynamic
	 * methods or {@link #lookupCustomImplementation} should be set to true.
	 */
	private String customImplementationBeanName;

	/**
	 * The current object which handles invocations sent to the dynamic manager.
	 */
	private InvocationHandler componentInvocationHandler;
	/**
	 * The current dynamic component, for internal use only.
	 */
	private COMPONENT proxy;

	public final synchronized void setComponentType(Class<COMPONENT> componentType) {
		this.componentType = componentType;
	}

	protected final synchronized void initializeFactory() {
		LOGGER.info("Initializing Dynamic component of type {}", componentType.getSimpleName()); // NOSONAR
																									// componentType not
																									// sync'd
		initializeProperties();
		initializeComponentInvocationHandler();
		initializeComponentProxy();
		LOGGER.debug("Dynamic component is initialized");
	}

	/**
	 * Override if required
	 */
	protected void initializeProperties() {
		// NOOP
	}

	@Override
	public final synchronized COMPONENT getObject() {
		if (proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return proxy;
	}

	@SuppressWarnings("unchecked")
	private void initializeComponentProxy() {
		if (proxy == null) {
			proxy = (COMPONENT) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
					new Class[] { componentType }, componentInvocationHandler);
		}
	}

	private void initializeComponentInvocationHandler() {
		LOGGER.debug("Initializing invocation handlers");

		if (componentInvocationHandler == null) {
			List<DynamicComponentInvocationHandler> invocationHandlers = new ArrayList<>();
			addCustomImplementationHandler(invocationHandlers); // IT MUST BE THE FIRST !
			invocationHandlers.addAll(createInvocationHandlers());

			componentInvocationHandler = new CompositeInvocationHandler(invocationHandlers);
		}
	}

	protected abstract List<DynamicComponentInvocationHandler> createInvocationHandlers();

	/**
	 * Adds a handler which delegates to cistom manager if necessary. It must be the first handler to be added to the
	 * list / processed !
	 */
	private void addCustomImplementationHandler(List<DynamicComponentInvocationHandler> handlers) {
		if (hasCustomImplementation()) {
			handlers.add(new CustomMethodHandler(createCustomImplempentationProvider()));
		}
	}

	private Provider<Object> createCustomImplempentationProvider() {
		LOGGER.debug("Creating a custom implementation provider for dynamic component {}",
				componentType.getSimpleName());
		String beanName = null;

		if (customImplementationBeanName != null) {
			LOGGER.trace("Using configured customImplementationBeanName");
			beanName = customImplementationBeanName;
		} else {
			LOGGER.trace("Using automatic lookup for custom implementation");
			beanName = componentType.getInterfaces()[0].getSimpleName();
		}

		LOGGER.info("Dynamic component {} is bound to delegate to custom implementation named {}",
				componentType.getSimpleName(), customImplementationBeanName);

		return new DeferredLookupCustomImplementationProvider(beanFactory, beanName);
	}

	private boolean hasCustomImplementation() {
		if (customImplementationBeanName != null) {
			return true;
		}

		if (!lookupCustomImplementation) {
			return false;
		}

		// lookupCustomImplementation is true
		if (cannotDetermineCustomComponentType()) {
			LOGGER.warn(
					"No custom implementation type could be found in Dynamic component {}, will not apply custom implementation lookup",
					componentType.getSimpleName());
			return false;
		}

		return true;
	}

	/**
	 * @return
	 */
	private boolean cannotDetermineCustomComponentType() {
		return componentType.getInterfaces().length == 0;
	}

	@Override
	public final Class<?> getObjectType() {
		return componentType;
	}

	@Override
	public final boolean isSingleton() {
		return true;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initializeFactory();
	}

	/**
	 * @param customImplementationBeanName
	 *            the customImplementationBeanName to set
	 */
	public void setCustomImplementationBeanName(String customImplementationBeanName) {
		this.customImplementationBeanName = customImplementationBeanName;
	}

	/**
	 * @param lookupCustomImplementation
	 *            the lookupCustomImplementation to set
	 */
	public void setLookupCustomImplementation(boolean lookupCustomImplementation) {
		this.lookupCustomImplementation = lookupCustomImplementation;
	}

}
