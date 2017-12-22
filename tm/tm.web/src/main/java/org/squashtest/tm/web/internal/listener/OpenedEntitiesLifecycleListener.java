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
package org.squashtest.tm.web.internal.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.squashtest.tm.web.internal.interceptor.openedentity.OpenedEntities;

/**
 * This lisner acts when the ServletContext is created or when a user's session is ending.
 * <ul>
 * <li>When the servletContext is created : a OpenedEntities instance is created and stored in the servletContext for
 * each entry in the {@linkplain OpenedEntities#MANAGED_ENTITIES_LIST}.</li>
 * <li>When a user's session is ending, all his stored views on the existing OpenedEntities are removed</li>
 * </ul>
 * The aim of all this is to notify a user when someone else is viewing the same object than him. See
 * {@linkplain OpenedEntities}'s java doc for more details.
 * 
 * @author mpagnon
 * 
 */
public class OpenedEntitiesLifecycleListener implements HttpSessionListener, ServletContextListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenedEntitiesLifecycleListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		// NOOP

	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		ServletContext context = event.getSession().getServletContext();
		HttpSession session = event.getSession();
		SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");

		String login = retrieveLogin(securityContext);
		
		unregisterLogin(context, login);
	}

	private void unregisterLogin(ServletContext context, String login) {
		if (login != null) {
			LOGGER.debug("Session Closed for user {}", login);
			
			for (String managedEntityKey : OpenedEntities.MANAGED_ENTITIES_LIST) {
				removeUserFromViewers(managedEntityKey, login, context);
			}
		}
	}

	private String retrieveLogin(SecurityContext securityContext) {
		String login = null;
		
		if (securityContext != null) {
			Authentication authentication = securityContext.getAuthentication();
			if (authentication != null) {
				login = authentication.getName();
			}
		}
		return login;
	}

	private void removeUserFromViewers(String managedEntityKey, String login, ServletContext context) {
		OpenedEntities openedEntities = (OpenedEntities) context.getAttribute(managedEntityKey);
		if (openedEntities != null) {
			openedEntities.removeViewer(login);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// NOOP

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();

		for (String contextAttributeName : OpenedEntities.MANAGED_ENTITIES_LIST) {
			OpenedEntities entities = new OpenedEntities();
			context.setAttribute(contextAttributeName, entities);
		}

	}

}
