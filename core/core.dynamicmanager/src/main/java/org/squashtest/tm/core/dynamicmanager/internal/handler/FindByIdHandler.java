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

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;

import org.springframework.core.annotation.AnnotationUtils;

/**
 * {@link DynamicComponentInvocationHandler} which handles <code>@Entity findById(long id)</code> or <code>@Entity findById(Serializable id)</code> method. Fetches an entity using its id.
 * @author Gregory Fouquet
 *
 */
public class FindByIdHandler implements DynamicComponentInvocationHandler {
	private final EntityManager em;

	public FindByIdHandler(@NotNull EntityManager em) {
		super();
		this.em = em;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		return  em.getReference(method.getReturnType(), (Serializable) args[0]);
	}

	/**
	 * @return <code>true</code> if method signature is <code>ENTITY findById(long id)</code> or <code>ENTITY findById(Serializable id)</code>
	 */
	@Override
	public boolean handles(Method method) {
		return methodNameMatchesMethodPattern(method) && mehtodParamsMatchMethodParams(method)
				&& methodReturnTypeMatchesMethodPattern(method);
	}

	private boolean mehtodParamsMatchMethodParams(Method method) {
		Class<?>[] params = method.getParameterTypes();
		return params.length == 1 && paramIsAValidId(params[0]);
	}

	private boolean paramIsAValidId(Class<?> param) {
		return long.class.isAssignableFrom(param) || Serializable.class.isAssignableFrom(param);
	}

	public boolean methodNameMatchesMethodPattern(Method method) {
		return "findById".equals(method.getName());
	}

	private boolean methodReturnTypeMatchesMethodPattern(Method method) {
		return AnnotationUtils.findAnnotation(method.getReturnType(), Entity.class) != null;
	}

}
