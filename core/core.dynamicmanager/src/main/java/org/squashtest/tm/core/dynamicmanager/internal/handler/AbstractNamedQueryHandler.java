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
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import org.squashtest.tm.core.dynamicmanager.exception.NamedQueryLookupException;
import org.squashtest.tm.core.foundation.collection.Paging;

/**
 * This {@link InvocationHandler} looks up a hibernate named query which name matches <code>EntityType.methodName</code>
 *
 * @author Gregory Fouquet
 *
 * @param <ENTITY>
 */
abstract class AbstractNamedQueryHandler<ENTITY> implements DynamicComponentInvocationHandler {
	private final EntityManager entityManager;
	/**
	 * This property is prepended to the invoked method's name for query lookup.
	 */
	private final String queryNamespace;

	/**
	 * @param entityManager
	 * @param entityType
	 *            this class's simple name will be used as this object's {@link #queryNamespace}
	 */
	public AbstractNamedQueryHandler(@NotNull Class<ENTITY> entityType, @NotNull EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
		this.queryNamespace = entityType.getSimpleName();
	}

	/**
	 * Runs a named query which name matches the invoked method's name and returns this query's unique result. No arg
	 * may be a <code>null</code> value.
	 */
	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) {
		args = args == null ? new Object[] {} : args; // NOSONAR protecting against bad args using a bad practice
		Query query = lookupNamedQuery(method);

		processPaging(query, args);
		processQueryParameters(query, args);

		return executeQuery(query);
	}

	/**
	 * @param query
	 * @param args
	 */
	private void processPaging(Query query, Object[] args) {
		if (pagedQuery(args)) {
			Paging paging = (Paging) lastArg(args);
			if(!paging.shouldDisplayAll()){
				query.setFirstResult(paging.getFirstItemIndex());
				query.setMaxResults(paging.getPageSize());
			}
		}
	}

	private boolean pagedQuery(Object[] args) {
		return args.length > 0 && lastArg(args) instanceof Paging;
	}

	private Object lastArg(Object[] args) {
		return args[args.length - 1];
	}

	protected abstract Object executeQuery(Query query);

	private void processQueryParameters(Query query, Object[] args) {
		int lastArgIndex = pagedQuery(args) ? args.length - 1 : args.length;

		for (int i = 0; i < lastArgIndex ; i++) {
			Object arg = args[i];
			Integer j = i+1;
			query.setParameter(j.toString(), arg);
		}

	}

	private Query lookupNamedQuery(Method method) {
		Query query = entityManager.createNamedQuery(queryName(method));

		if (query == null) {
			throw new NamedQueryLookupException(queryName(method));
		}

		return query;
	}

	/**
	 * @param method
	 * @return
	 */
	private String queryName(Method method) {
		return queryNamespace + '.' + method.getName();
	}

	/**
	 * Due to limitation of Query api, we cannot handle collection params (query.setParameterList requires a named
	 * parameter).
	 */
	@Override
	public final boolean handles(Method method) {
		return noCollectionParam(method) && canHandle(method);
	}

	/**
	 * @param method
	 * @return
	 */
	private boolean noCollectionParam(Method method) {
		for (Class<?> paramType : method.getParameterTypes()) {
			if (Collection.class.isAssignableFrom(paramType)) {
				return false;
			}
		}

		return true;
	}

	protected abstract boolean canHandle(Method method);

}
