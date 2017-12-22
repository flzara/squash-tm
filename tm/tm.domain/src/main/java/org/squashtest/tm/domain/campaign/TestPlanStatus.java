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
package org.squashtest.tm.domain.campaign;

import org.squashtest.tm.core.foundation.i18n.Internationalizable;

public enum TestPlanStatus implements Internationalizable {
	DONE,
	READY,
	RUNNING;

	private static final String I18N_KEY_ROOT = "label.testPlanStatus.";
	/**
	 *
	 * @param statistics a {@link TestPlanStatistics}
	 * @return DONE if all test-plan-item are "settled", "untestable", "failed", "success" or "blocked"<br>
	 * 		   READY if all test-plan item are "ready"
	 * 		   RUNNING otherwise
	 */
	public static TestPlanStatus getStatus(TestPlanStatistics statistics) {
		if (allReady(statistics)) {
			return TestPlanStatus.READY;
		} else if (allDone(statistics)) {
			return TestPlanStatus.DONE;
		} else {
			return TestPlanStatus.RUNNING;
		}
	}

	/**
	 *
	 * @param statistics
	 *            an instance of {@linkplain TestPlanStatistics}
	 * @return <b><code>true</code></b> : if all testPlanItem are ready.
	 */
	private static boolean allReady(TestPlanStatistics statistics) {
		return statistics.getNbReady() == statistics.getNbTestCases();
	}

	/**
	 *
	 * @param statistics
	 *            an instance of {@linkplain TestPlanStatistics}
	 * @return <b><code>true</code></b> : if <b>all</b> TestPlanItem have are done (untestable, blocked, failed or
	 *         success)
	 */
	private static boolean allDone(TestPlanStatistics statistics) {
		return statistics.getNbDone() == statistics.getNbTestCases();
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}

}
