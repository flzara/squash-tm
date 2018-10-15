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
package org.squashtest.tm.domain.tf.automationrequest;

import org.squashtest.tm.core.foundation.i18n.Internationalizable;

public enum AutomationRequestStatus implements Internationalizable {

	VALID,            // The automation request is valid.
	TO_VALIDATE,      // The automation request is to be validate.
	TRANSMITTED,      // The automation request is transmitted.
	WORK_IN_PROGRESS, // The automation enginner is automating the test case.
	EXECUTABLE,       // The automated test case is executable.
	CANCELED,         // The automation of test case is canceled.
	TO_UPDATE,        // The automation of test case to be updated.
	DELETED;          // The automation request is deleted.

	private static final String I18N_KEY_ROOT = "automation-request.request_status.";

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}
}
