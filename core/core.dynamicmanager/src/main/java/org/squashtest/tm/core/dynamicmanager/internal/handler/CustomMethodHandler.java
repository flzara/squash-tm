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

import java.lang.reflect.Method;

import javax.inject.Provider;
import javax.validation.constraints.NotNull;

/**
 * This handler delegates to a custom manager when possible.
 * 
 * @author Gregory Fouquet
 * 
 */
public class CustomMethodHandler implements DynamicComponentInvocationHandler {
	private final Provider<Object> customImplementationProvider;
	
	public CustomMethodHandler(@NotNull Provider<Object> customImplementationProvider) {
		super();
		this.customImplementationProvider = customImplementationProvider;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { // NOSONAR : I don't choose what JDK interfaces throw
		return method.invoke(customImplementation(), args);
	}

	private Object customImplementation() {
		return customImplementationProvider.get();
	}


	@Override
	public boolean handles(Method method) {
		return isMethodOfCustomManager(method);
	}

	private boolean isMethodOfCustomManager(Method method) {
		return method.getDeclaringClass().isAssignableFrom(customImplementation().getClass());
	}

}
