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
package org.squashtest.tm.service.security.acls.domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.ObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

/**
 * Creates {@link ObjectIdentity} objects using the
 *
 * @author Gregory Fouquet
 *
 */
@Component
public class AnnotatedPropertyObjectIdentityRetrievalStrategy implements ObjectIdentityRetrievalStrategy {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AnnotatedPropertyObjectIdentityRetrievalStrategy.class);

	private ObjectIdentityRetrievalStrategy delegate = new ObjectIdentityRetrievalStrategyImpl();

	private Map<Class<?>, Method> identityMethodMap = new ConcurrentHashMap<>();

	@Override
	public ObjectIdentity getObjectIdentity(Object domainObject) {
		Class<?> candidateClass = domainObject.getClass();

		Method targetProperty = getTargetProperty(candidateClass);

		Object identityHolder;

		if (targetProperty != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.trace("Found @AclConstrainedObject in class " + candidateClass.getName()
						+ " - OID will be generated using the annotated property");
			}

			identityHolder = getIdentityHolder(targetProperty, domainObject);
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.trace("Did not find any @AclConstrainedObject in class " + candidateClass.getName()
						+ " - OID will be the object's");
			}

			identityHolder = domainObject;
		}

		return delegate.getObjectIdentity(identityHolder);
	}

	private Method getTargetProperty(Class<?> candidateClass) {
		Method targetProperty;

		if (isMapped(candidateClass)){
			targetProperty = identityMethodMap.get(candidateClass);
		}
		else{
			targetProperty = findAnnotatedProperty(candidateClass);
			if (targetProperty!=null){
				mapClass(candidateClass, targetProperty);
			}
		}
		return targetProperty;
	}

	private Object getIdentityHolder(Method targetProperty, Object domainObject) {
		Object identityHolder;

		try {
			identityHolder = targetProperty.invoke(domainObject);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			//If e.cause is a reference to a non existing object in database,
			//continue the procedure to let spring cast a more friendly IdentityUnavailableException
			if (e.getCause().getClass() == ObjectNotFoundException.class) {
				identityHolder = new Identified() {
					@Override
					public Long getId() {
						return 0L;
					}
				};
			}
			else {
				throw new RuntimeException(e);
			}
		}

		return identityHolder;
	}

	private Method findAnnotatedProperty(Class<?> candidateClass) {

		Set<Class<?>> exploredClasses = new HashSet<>();

		LinkedList<Class<?>> explorationQueue= new LinkedList<>();
		Method targetProperty = null;
		Class<?> currentClass;

		explorationQueue.add(candidateClass);

		while ( targetProperty == null && ! explorationQueue.isEmpty()){

			currentClass = explorationQueue.removeFirst();

			if ( currentClass==null || exploredClasses.contains(currentClass)){ continue;}

			if (LOGGER.isDebugEnabled()){
				LOGGER.trace("Looking for @AclConstrainedObject in class '"+candidateClass.getName()+"'");
			}

			targetProperty = findAnnotatedPropertyInClass(currentClass);

			//next step is to explore the interfaces and classes
			if (targetProperty == null){
				explorationQueue.addAll(Arrays.asList(currentClass.getInterfaces()));
				explorationQueue.add(currentClass.getSuperclass());
			}

			//remember the class
			exploredClasses.add(currentClass);
		}

		return targetProperty;

	}

	private Method findAnnotatedPropertyInClass(Class<?> candidateClass) {
		Method targetProperty = null;

		for (Method meth : candidateClass.getMethods()) {
			AclConstrainedObject target = meth.getAnnotation(AclConstrainedObject.class);

			if (target != null) {
				targetProperty = meth;
				break;
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (targetProperty != null) {
				LOGGER.trace("Found @AclConstrainedObject in class " + candidateClass.getName());
			} else {
				LOGGER.trace("@AclConstrainedObject not found in class " + candidateClass.getName());
			}
		}

		return targetProperty;
	}

	//*********************************

	private boolean isMapped(Class<?> someClass){
		return identityMethodMap.containsKey(someClass);
	}

	private void mapClass(Class<?> someClass, Method identityMethod){
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("AnnotatedPropertyObjectIdentityRetrievalStrategy : identity method '"+identityMethod.getName()+"' found for class '"+someClass.getName()+"', registering now");
		}
		identityMethodMap.put(someClass, identityMethod);
		nukeMapIfTooLarge();
	}

	private void nukeMapIfTooLarge(){
		if (identityMethodMap.size()>50){
			if (LOGGER.isDebugEnabled()){
				LOGGER.debug("AnnotatedPropertyObjectIdentityRetrievalStrategy : identity method registry grew too large, reseting it");
			}
			identityMethodMap.clear();
		}
	}

}
