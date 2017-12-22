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
package org.squashtest.tm.service.internal.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;

/**
 * This {@link ParameterNameDiscoverer} holds an ordererd collection of {@link ParameterNameDiscoverer}. It delegates to
 * each component of this collection until a component is able to resolve the parameters names i.e. it don't return
 * <code>null</code>)
 *
 * Note : {@link PrioritizedParameterNameDiscoverer} cannot be configured through XML AFAIK
 *
 * @author Gregory Fouquet
 *
 */
public class CompositeDelegatingParameterNameDiscoverer implements ParameterNameDiscoverer {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompositeDelegatingParameterNameDiscoverer.class);

	private final PrioritizedParameterNameDiscoverer delegate = new PrioritizedParameterNameDiscoverer();

	/**
	 * @param discoverers
	 */
	public CompositeDelegatingParameterNameDiscoverer(List<ParameterNameDiscoverer> discoverers) {
		super();

		if (discoverers == null || discoverers.isEmpty()) {
			LOGGER.warn("CompositeDelegatingParameterNameDiscoverer initialized with an empty list of delegate");

		} else {
			for(ParameterNameDiscoverer discoverer : discoverers) {
				LOGGER.info("Adding to CompositeDelegatingParameterNameDiscoverer a discoverer of type {}", discoverer.getClass());
				delegate.addDiscoverer(discoverer);
			}

		}
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.springframework.core.ParameterNameDiscoverer#getParameterNames(java.lang.reflect.Method)
	 */
	@Override
	public String[] getParameterNames(Method method) {
		return delegate.getParameterNames(method);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.springframework.core.ParameterNameDiscoverer#getParameterNames(java.lang.reflect.Constructor)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public String[] getParameterNames(Constructor ctor) {
		return delegate.getParameterNames(ctor);
	}

}
