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
package org.squashtest.tm.service.security;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

/**
 * Lazy implementation of {@link AuthenticationManager} which gets a delegate authentication provider from the bean
 * factory when it really needs it.
 * 
 * We need this to break a cycle between ProviderManager --> DaoAuthenticationProvider --> JdbcUserDetailsManager --> ProviderManager
 * 
 * Shamelessly pasted from package-private GlobalMethodSecurityBeanDefinitionParser.AuthenticationManagerDelegator class
 * 
 * @author Luke Taylor
 * @author Gregory Fouquet
 * 
 */
public class AuthenticationManagerDelegator implements AuthenticationManager, BeanFactoryAware {
	private AuthenticationProvider delegate;
	private final Object delegateMonitor = new Object();
	private BeanFactory beanFactory;
	private final String authProviderBean;

	public AuthenticationManagerDelegator(@NotNull String authProviderBean) {
		this.authProviderBean = authProviderBean;
	}

	@Override
	public Authentication authenticate(Authentication authentication) {
		synchronized (delegateMonitor) {
			if (delegate == null) {
				Assert.state(beanFactory != null, "BeanFactory must be set to resolve " + authProviderBean);

				delegate = beanFactory.getBean(authProviderBean, AuthenticationProvider.class);
			}
		}

		return delegate.authenticate(authentication);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
