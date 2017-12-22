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
package org.squashtest.tm.core.dynamicmanager.internal.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;

import org.springframework.util.ReflectionUtils;
import org.squashtest.tm.core.foundation.lang.PrimitiveTypeUtils;

/**
 * {@link InvocationHandler} which handles proxy calls which should modify an entity. These calls should have a
 * signature like : void changeSomeProperty(long id, SomeClass newValue) The handler then fetches the entity using the
 * given ID and sets its 'someProperty' property to 'newValue'
 *
 * @author Gregory Fouquet
 *
 */
public class EntityModifierHandler<ENTITY> implements DynamicComponentInvocationHandler {
	/**
	 * Pattern used to process service method which modify entities.
	 */
	private static final Pattern ENTITY_MODIFIER_SERVICE_PATTERN = Pattern.compile("^change(.*)");

	private final EntityManager em;

	private final Class<ENTITY> entityType;

	public EntityModifierHandler(@NotNull EntityManager em, @NotNull Class<ENTITY> entityType) {
		super();
		this.em = em;
		this.entityType = entityType;
	}

	/**
	 * Tells if this handler can handle the given method.
	 *
	 * @param method
	 * @return
	 */
	@Override
	public boolean handles(Method method) {
		return methodNameMatchesChangeMethodPattern(method) && mehtodParamsMatchChangeMethodParams(method)
				&& methodReturnTypeMatchesChangeMethodPattern(method);
	}

	private boolean mehtodParamsMatchChangeMethodParams(Method method) {
		Class<?>[] params = method.getParameterTypes();
		return params.length == 2 && long.class.isAssignableFrom(params[0]);
	}

	public boolean methodNameMatchesChangeMethodPattern(Method method) {
		String name = method.getName();
		Matcher m = ENTITY_MODIFIER_SERVICE_PATTERN.matcher(name);

		return m.find();
	}

	private boolean methodReturnTypeMatchesChangeMethodPattern(Method method) {
		return Void.TYPE.equals(method.getReturnType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { // NOSONAR : I dont choose what
																						// JDK interfaces throw
		ENTITY entity = em.getReference(entityType, args[0]);

		String prop = extractModifiedPropertyName(method);
		Method setter = findSetter(prop, method.getParameterTypes()[1]);
		return setter.invoke(entity, args[1]);
	}

	private String extractModifiedPropertyName(Method method) {
		Matcher m = ENTITY_MODIFIER_SERVICE_PATTERN.matcher(method.getName());
		m.find();
		return m.group(1);
	}

	private Method findSetter(String property, Class<?> paramType) throws NoSuchMethodException {
		String setterName = "set" + property;

		Method setter = ReflectionUtils.findMethod(entityType, setterName, paramType);

		if (setter == null) {
			setter = findPrimitiveTypeSetter(setterName, paramType);
		}

		if (setter == null) {
			throw new NoSuchMethodException("void " + entityType.getName() + '.' + setterName + '('
					+ paramType.getName() + ')');
		}

		return setter;
	}

	private Method findPrimitiveTypeSetter(String setterName, Class<?> paramType) {
		if (PrimitiveTypeUtils.isPrimitiveWrapper(paramType)) {
			Class<?> primitiveClass = PrimitiveTypeUtils.wrapperToPrimitive(paramType);

			return ReflectionUtils.findMethod(entityType, setterName, primitiveClass);
		}

		return null;
	}
}
