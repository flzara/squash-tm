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
package org.squashtest.tm.security.acls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.AuditableAccessControlEntry;
import org.springframework.util.Assert;

public class Slf4jAuditLogger implements AuditLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jAuditLogger.class);

	@Override
	public void logIfNeeded(boolean granted, AccessControlEntry ace) {
		if (LOGGER.isDebugEnabled()) {
			Assert.notNull(ace, "AccessControlEntry required");

			logIfPossible(granted, ace);
		}
	}

	private void logIfPossible(boolean granted, AccessControlEntry ace) {
		if (ace instanceof AuditableAccessControlEntry) {
			AuditableAccessControlEntry auditableAce = (AuditableAccessControlEntry) ace;

			logRelevantInformation(granted, auditableAce);
		}
	}

	private void logRelevantInformation(boolean granted, AuditableAccessControlEntry auditableAce) {
		if (granted && auditableAce.isAuditSuccess()) {
			LOGGER.debug("GRANTED due to ACE: " + auditableAce);
		} else if (!granted && auditableAce.isAuditFailure()) {
			LOGGER.debug("DENIED due to ACE: " + auditableAce);
		}
	}

}
