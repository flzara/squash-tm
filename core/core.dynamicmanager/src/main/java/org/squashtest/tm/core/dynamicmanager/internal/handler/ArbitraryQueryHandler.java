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

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.core.foundation.collection.Paging;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

/**
 * Executes arbitrary queries, using named parameters if any. <h3>
 * How it works :</h3>
 * <p>
 * in most generic terms, it looks for a <u>{@link Query}</u>, applies the <u>parameters</u> and return the
 * <u>result</u>.
 * </p>
 *
 * <h3>Query</h3>
 * <p>
 * query name must be &lt;entityname&gt;.&lt;methodname&gt;
 * </p>
 *
 * <h3>Parameters</h3>
 * <p>
 * Accepts any number of parameters that may be :
 * <ul>
 * <li>a {@link Paging} (or subclass),</li>
 * <li>an annotated Collection (or subclass),</li>
 * <li>an annotated Object,</li>
 * </ul>
 *
 * In the third case an object will be treated as a scalar. A note about Collection arguments : if that collection is
 * empty the query will not be executed and a default return value will be returned.
 * </p>
 * <p>
 * {@link Paging} arguments don't have to be annotated. Multiple Paging will all be applied but only the last one will
 * count. Other arguments MUST all be annotated using @{@link QueryParam}. The value of that annotation must correspond
 * to the named parameter that will be looked for in the query. You may supply more annotations if you want to as long
 * as at least QueryParam is supplied.
 * </p>
 *
 * <h3>Result</h3>
 * <p>
 * It depends on the result type of the method. When the query is executed the returned values are :
 * <ul>
 * <li>void : returns null</li>
 * <li>Collection (or subclass) : returns a List</li>
 * <li>other : returns a scalar</li>
 * </ul>
 *
 * If the query was aborted because a collection argument is empty, the returned values will be instead :
 *
 * <ul>
 * <li>void : returns null</li>
 * <li>Collection (or sublcass) : returns an empty List</li>
 * <li>other : returns null or 0 if the result expects a type primitive</li>
 * </ul>
 * </p>
 *
 * @author bsiri
 *
 * @param <ENTITY>
 */
public class ArbitraryQueryHandler<ENTITY> implements DynamicComponentInvocationHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArbitraryQueryHandler.class);

	private final Class<ENTITY> entityType;
	private final EntityManager em;

	public ArbitraryQueryHandler(Class<ENTITY> entityType, EntityManager em) {
		super();
		this.entityType = entityType;
		this.em = em;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {

		try {
			Query q = setupQuery(method, args);
			return executeQuery(method, q);
		} catch (EmptyCollectionException ex) {
			return abortQuery(method);
		}

	}

	@Override
	public boolean handles(Method method) {
		return queryExistsCheck(method) && parametersCheck(method);
	}

	// ************************ private methods ***************************

	private Query findQuery(Method method) {
		String queryName = entityType.getSimpleName() + "." + method.getName();
		return em.createNamedQuery(queryName);
	}

	private QueryParam findQueryParam(Annotation[] paramAnnotations) {
		for (Annotation paramAnnotation : paramAnnotations) {
			if (isQueryParam(paramAnnotation)) {
				return (QueryParam) paramAnnotation;
			}
		}
		return null;
	}

	// ************************* predicates ************************

	private boolean queryExistsCheck(Method method) {
		try {
			Query q = findQuery(method);
			return q != null;
		} catch (HibernateException ex) {
			LOGGER.debug("Could not find a named query matching method name " + method.getName(), ex);
			return false;
		}
	}

	private boolean parametersCheck(Method method) {

		Annotation[][] allAnnotations = method.getParameterAnnotations();
		Class<?>[] allParamTypes = method.getParameterTypes();

		for (int i = 0; i < allParamTypes.length; i++) {
			if (!isPagingType(allParamTypes[i]) && findQueryParam(allAnnotations[i]) == null) {
				return false;
			}
		}

		return true;
	}

	private boolean isPagingType(Class<?> paramType) {
		return Paging.class.isAssignableFrom(paramType);
	}

	private boolean isCollectionType(Class<?> paramType) {
		return Collection.class.isAssignableFrom(paramType);
	}

	private boolean isPaging(Object argument) {
		return Paging.class.isAssignableFrom(argument.getClass());
	}

	private boolean isCollection(Object argument) {
		return Collection.class.isAssignableFrom(argument.getClass());
	}

	private boolean isVoid(Class<?> type) {
		return Void.TYPE.equals(type);
	}

	private boolean isQueryParam(Annotation ann) {
		return ann.annotationType().equals(QueryParam.class);
	}

	// ************************** Query processing check ***************

	private Query setupQuery(Method method, Object[] args) {

		Query query = findQuery(method);

		Annotation[][] allAnnotations = method.getParameterAnnotations();

		for (int i = 0; i < args.length; i++) {

			Object currentArg = args[i];

			if (isPaging(currentArg)) {
				processPaging(query, currentArg);
			} else if (isCollection(currentArg)) {
				setAsCollection(query, currentArg, allAnnotations[i]);
			} else {
				setAsScalar(query, currentArg, allAnnotations[i]);
			}
		}

		return query;
	}

	private Object executeQuery(Method method, Query query) {

		Object result;

		Class<?> returnType = method.getReturnType();

		if (isVoid(returnType)) {
			query.executeUpdate();
			result = null;
		} else if (isCollectionType(returnType)) {
			result = query.getResultList();
		} else {
			result = query.getSingleResult();
		}

		return result;

	}

	private Object abortQuery(Method method) {

		Object result;

		Class<?> returnType = method.getReturnType();

		if (isCollectionType(returnType)) {
			result = Collections.emptyList();
		} else if (returnType.isPrimitive()) {
			result = newPrimitiveZero(returnType);
		} else {
			result = null;
		}

		return result;
	}

	private void processPaging(Query query, Object arg) {
		Paging paging = (Paging) arg;
		PagingUtils.addPaging(query, paging);
	}

	@SuppressWarnings("rawtypes")
	private void setAsCollection(Query query, Object arg, Annotation[] paramAnnotations) {
		QueryParam paramName = findQueryParam(paramAnnotations);
		Collection argument = (Collection) arg;
		if (argument.isEmpty()) {
			throw new EmptyCollectionException();
		}
		query.setParameter(paramName.value(), argument);
	}

	private void setAsScalar(Query query, Object arg, Annotation[] paramAnnotations) {
		QueryParam paramName = findQueryParam(paramAnnotations);
		query.setParameter(paramName.value(), arg);
	}

	private Object newPrimitiveZero(Class<?> returnType) {
		Object res = null;

		if (returnType.equals(Short.TYPE)) {
			res = 0;
		} else if (returnType.equals(Integer.TYPE)) {
			res = 0;
		} else if (returnType.equals(Float.TYPE)) {
			res = 0f;
		} else if (returnType.equals(Double.TYPE)) {
			res = 0d;
		} else if (returnType.equals(Long.TYPE)) {
			res = 0L;
		} else if (returnType.equals(Byte.TYPE)) {
			res = (byte) 0;
		} else {
			res = (char) 0;
		}

		return res;
	}

	private static final class EmptyCollectionException extends RuntimeException {
		private static final long serialVersionUID = 2808245564237178860L;
	}
}
