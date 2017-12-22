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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.core.dynamicmanager.exception.UnsupportedMethodException;

/**
 * This {@link InvocationHandler} holds a list of {@link DynamicComponentInvocationHandler}. When this object is asked
 * to handle a method invocation, it iterates over its {@link #invocationHandlers} list, and delegates the invocation to
 * the first handler able to handle it.
 *
 * @author Gregory Fouquet
 *
 */
public class CompositeInvocationHandler implements InvocationHandler {
	/**
	 * @param invocationHandlers
	 */
	public CompositeInvocationHandler(@NotNull List<DynamicComponentInvocationHandler> invocationHandlers) {
		super();
		this.invocationHandlers = new ArrayList<>(invocationHandlers);
	}

	private final List<DynamicComponentInvocationHandler> invocationHandlers;

	/**
	 * Delegates to the first item of {@link #invocationHandlers} able to handle the invocation.
	 *
	 * {@link #equals(Object)} invocation is never delegated and performs a proxy reference check.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { // NOSONAR : I dont choose what JDK interfaces throw
		try {
			return doInvoke(proxy, method, args);

		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
			// otherwise, checked ITE will be wrapped into UndeclaredThrowableException

		}
	}

	private Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable { // NOSONAR : I dont choose what JDK interfaces throw
		if (isEqualsInvoked(method)) {
			return proxyEquals(proxy, args[0]);
		}

		for (DynamicComponentInvocationHandler handler : invocationHandlers) {
			if (handler.handles(method)) {
				return handler.invoke(proxy, method, args);
			}
		}

		throw new UnsupportedMethodException(method, args);
	}

	private Object proxyEquals(Object thisProxy, Object thatProxy) {
		return thisProxy == thatProxy; // NOSONAR We DO want to compare instances
	}

	private boolean isEqualsInvoked(Method method) {
		Class<?>[] paramsDefinition = method.getParameterTypes();

		return "equals".equals(method.getName()) && paramsDefinition.length == 1 && paramsDefinition[0] == Object.class;
	}
}
