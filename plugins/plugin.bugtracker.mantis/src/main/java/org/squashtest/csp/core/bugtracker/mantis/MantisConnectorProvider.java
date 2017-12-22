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
package org.squashtest.csp.core.bugtracker.mantis;

import javax.inject.Inject;
import javax.inject.Named;

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.internal.mantis.MantisConnector;
import org.squashtest.csp.core.bugtracker.internal.mantis.MantisExceptionConverter;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;



@Service
public class MantisConnectorProvider implements BugTrackerConnectorProvider {

	@Inject
	@Named("mantisInterfaceDescriptor")
	private BugTrackerInterfaceDescriptor interfaceDescriptor;

	@Inject
	private MantisExceptionConverter exceptionConverter;


	@Override
	public String getBugTrackerKind() {
		return "mantis";
	}

	@Override
	public String getLabel() {
		return "MantisBT connector";
	}

	@Override
	public BugTrackerConnector createConnector(BugTracker bugTracker) {
		if (bugTracker == null) {
			throw new NullArgumentException("bugTracker");
		}

		MantisConnector connector = new MantisConnector(bugTracker);
		connector.setInterfaceDescriptor(interfaceDescriptor);
		connector.setExceptionConverter(exceptionConverter);

		return connector;
	}

}
