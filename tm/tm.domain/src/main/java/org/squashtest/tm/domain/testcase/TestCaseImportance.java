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
package org.squashtest.tm.domain.testcase;

import java.util.Arrays;
import java.util.List;

import org.squashtest.tm.core.foundation.i18n.Abbreviated;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.LevelComparator;
import org.squashtest.tm.domain.requirement.RequirementCriticality;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
public enum TestCaseImportance implements Level, Abbreviated {
	VERY_HIGH(1), HIGH(2), MEDIUM(3), LOW(4);

	private static final String I18N_KEY_ROOT = "test-case.importance.";
	private static final LevelComparator LEVEL_COMPARATOR = LevelComparator.getInstance();

	private static final String ABBR_SUFIX = ".short";

	private final int level;

	private TestCaseImportance(int value) {
		this.level = value;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.core.foundation.i18n.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + this.name();
	}

	@Override
	public String getAbbreviatedI18nKey() {
		return I18N_KEY_ROOT + this.name() + ABBR_SUFIX;
	}

	/**
	 * @return the level
	 */
	@Override
	public int getLevel() {
		return level;
	}

	public static TestCaseImportance defaultValue() {
		return LOW;
	}

	/**
	 * 
	 * @param rCriticalities
	 *            a list of requirement criticalities
	 * @return the deduced test case importance
	 */
	public static TestCaseImportance deduceTestCaseImportance(List<RequirementCriticality> rCriticalities) {

		TestCaseImportance importance = TestCaseImportance.LOW;
		if (!rCriticalities.isEmpty()) {
			if (rCriticalities.contains(RequirementCriticality.CRITICAL)) {
				importance = TestCaseImportance.HIGH;
			} else {
				if (rCriticalities.contains(RequirementCriticality.MAJOR)) {
					importance = TestCaseImportance.MEDIUM;
				}
			}
		}
		return importance;
	}

	/**
	 * will deduce the new TestCase importance when a new RequirementCriticality has been added to the associated
	 * RequirementCriticality list of the TestCase.
	 * 
	 * @param newCriticality
	 *            the new requirement criticality that might change the importance
	 * 
	 * @param oldImportance
	 *            the ancient importance of the test case
	 * @return the new importace if it has changed.
	 */
	public TestCaseImportance deduceNewImporanceWhenAddCriticality(RequirementCriticality newCriticality) {
		TestCaseImportance importance = deduceTestCaseImportance(Arrays.asList(newCriticality));
		TestCaseImportance newImportance = this;
		if (LEVEL_COMPARATOR.compare(importance, this) < 0) {
			newImportance = importance;
		}
		return newImportance;
	}

	/**
	 * will check if the change of criticality of the associated requirement can change the auto-computed testCase
	 * importance (this)
	 * 
	 * @param oldRequirementCriticality
	 * @param newCriticality
	 * @return true if the auto-computed test case importance will change after the requirement criticality changes.
	 */
	public boolean changeOfCriticalityCanChangeImportanceAuto(RequirementCriticality oldRequirementCriticality,
			RequirementCriticality newCriticality) {
		TestCaseImportance oldCriticalityImp = deduceTestCaseImportance(Arrays.asList(oldRequirementCriticality));
		TestCaseImportance newCriticaltyImp = deduceTestCaseImportance(Arrays.asList(newCriticality));
		boolean canChange = true;
		if (LEVEL_COMPARATOR.compare(this, oldCriticalityImp) < 0
				&& LEVEL_COMPARATOR.compare(this, newCriticaltyImp) <= 0) {
			canChange = false;
		}
		return canChange;
	}

}
