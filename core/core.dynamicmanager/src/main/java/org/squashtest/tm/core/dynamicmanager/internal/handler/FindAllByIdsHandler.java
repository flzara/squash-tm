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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.squashtest.tm.core.foundation.collection.Sorting;

/**
 * {@link DynamicComponentInvocationHandler} which handles <code>List<ENTITY> findAllByIds(Collection<Long> id)</code>
 * method. Fetches all entities matching the ids of a collection.
 *
 * @author Gregory Fouquet
 *
 */
public class FindAllByIdsHandler<ENTITY> implements DynamicComponentInvocationHandler { // NOSONAR : I dont choose what
	// JDK interfaces throw
	private final Class<ENTITY> entityType;
	private final EntityManager em;

	/**
	 * @param entityType
	 * @param em
	 */
	public FindAllByIdsHandler(@NotNull Class<ENTITY> entityType, @NotNull EntityManager em) {
		super();
		this.entityType = entityType;
		this.em = em;
	}

	/**
	 * Performs an entity fetch using {@link #entityType} and the first arg as the collection of entities ids.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		Collection<Long> ids = (Collection<Long>) args[0];

		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria crit = em.unwrap(Session.class).createCriteria(entityType);
		crit.add(Restrictions.in("id", ids));
		if(method.getParameterTypes().length >1){
			Sorting sorting = (Sorting) args[1];
			Order order = null;
			switch(sorting.getSortOrder()){
			case DESCENDING :
				order = Order.desc(sorting.getSortedAttribute()).ignoreCase();
				break;
			case ASCENDING:
			default :
				order = Order.asc(sorting.getSortedAttribute()).ignoreCase();
				break;
			}
			crit.addOrder(order);

		}

		return crit.list();
	}

	/**
	 * @return <code>true</code> if method signature is <code>ENTITY findById(long id)</code>
	 */
	@Override
	public boolean handles(Method method) {
		return methodNameMatchesMethodPattern(method) && mehtodParamsMatchMethodParams(method)
				&& methodReturnTypeMatchesMethodPattern(method);
	}

	private boolean mehtodParamsMatchMethodParams(Method method) {
		Class<?>[] params = method.getParameterTypes();
		return params.length == 1 && Collection.class.isAssignableFrom(params[0])
				|| params.length == 2 && Collection.class.isAssignableFrom(params[0]) && Sorting.class
				.isAssignableFrom(params[1]);
	}

	public boolean methodNameMatchesMethodPattern(Method method) {
		return "findAllByIds".equals(method.getName());
	}

	private boolean methodReturnTypeMatchesMethodPattern(Method method) {
		return List.class.isAssignableFrom(method.getReturnType());
	}
}
