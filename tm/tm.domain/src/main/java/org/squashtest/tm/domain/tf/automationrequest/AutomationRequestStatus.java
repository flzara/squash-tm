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
import org.squashtest.tm.domain.Level;

public enum AutomationRequestStatus implements Internationalizable, Level {

	TRANSMITTED(1),             // The automation request is transmitted.
	AUTOMATION_IN_PROGRESS(2),  // The automation enginner is automating the test case.
	SUSPENDED(3),		        // The automation request is obsolete.
	REJECTED(4),                // The automation request is non automatable.
	AUTOMATED(5),               // The automated test case is executable.
	READY_TO_TRANSMIT(6),       // The automation request is valid.
	WORK_IN_PROGRESS(7);       // The automation request is to be validate.


	private static final String I18N_KEY_ROOT = "automation-request.request_status.";

	private final int level;

	AutomationRequestStatus(int level) {
		this.level = level;
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}

	@Override
	public int getLevel() {
		return level;
	}
}
