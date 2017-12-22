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

import org.squashtest.tm.domain.requirement.VerificationCriterion;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.web.internal.annotation.ApplicationComponent;

/**
 * This Listener exposes enums into the ServletContext on application startup.
 *
 * @author Gregory Fouquet
 *
 */
@ApplicationComponent
public class EnumServletContextExposer implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// NOOP

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();

		context.setAttribute("testCaseImportanceEnum", TestCaseImportance.values());
		context.setAttribute("verificationCriterionEnum", VerificationCriterion.values());
	}

}
