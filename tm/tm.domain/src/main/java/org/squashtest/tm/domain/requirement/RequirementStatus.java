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
package org.squashtest.tm.domain.requirement;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.squashtest.tm.domain.Level;

public enum RequirementStatus implements Level {

	WORK_IN_PROGRESS(1) {
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(UNDER_REVIEW);
			return next;
		}
		
		@Override
		public boolean isRequirementModifiable() {
			return true;
		}

		@Override
		public boolean getAllowsStatusUpdate() {
			return true;
		}

		@Override
		public boolean isRequirementLinkable() {
			return true;
		}

		@Override
		public Set<RequirementStatus> getDisabledStatus() {
			return returnDisabledStatus();
		}
	},

	UNDER_REVIEW(2) {
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(WORK_IN_PROGRESS);
			next.add(APPROVED);
			return next;
		}

		@Override
		public boolean isRequirementModifiable() {
			return true;
		}

		@Override
		public boolean getAllowsStatusUpdate() {
			return true;
		}

		@Override
		public boolean isRequirementLinkable() {
			return true;
		}

		@Override
		public Set<RequirementStatus> getDisabledStatus() {
			return returnDisabledStatus();
		}
	},

	APPROVED(3) {
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(UNDER_REVIEW);
			next.add(WORK_IN_PROGRESS);
			return next;
		}

		@Override
		public boolean isRequirementModifiable() {
			return false;
		}

		@Override
		public boolean getAllowsStatusUpdate() {
			return true;
		}

		@Override
		public boolean isRequirementLinkable() {
			return true;
		}

		@Override
		public Set<RequirementStatus> getDisabledStatus() {
			return returnDisabledStatus();
		}
	},

	OBSOLETE(4) {
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(UNDER_REVIEW);
			next.add(WORK_IN_PROGRESS);
			next.add(APPROVED);
			return next;
		}

		@Override
		public boolean isRequirementModifiable() {
			return false;
		}

		@Override
		public boolean getAllowsStatusUpdate() {
			return true;
		}

		@Override
		public boolean isRequirementLinkable() {
			return false;
		}

		@Override
		public Set<RequirementStatus> getDisabledStatus() {
			return returnDisabledStatus();
		}
	};

	private static final String I18N_KEY_ROOT = "requirement.status.";

	private final int level;

	private RequirementStatus(int level) {
		this.level = level;
	}
	/**
	 * @see org.squashtest.tm.domain.Level#getLevel()
	 */
	@Override
	public int getLevel() {
		return level;
	}
	
	/**
	 * the set of the available status transition. As for 1.1.0 and until further notice, should also include
	 * <i>this</i>
	 * 
	 * @return the availableTransition.
	 */
	public abstract Set<RequirementStatus> getAvailableNextStatus();

	/**
	 * tells whether this status allows the owner to be modified, i.e. its intrinsic properties can be changed.
	 * 
	 * @return yay or nay.
	 */
	public abstract boolean isRequirementModifiable();

	/**
	 * tells whether the status could be changed regardless of {@link #isRequirementModifiable()};
	 * 
	 * @return yay or nay.
	 */
	public abstract boolean getAllowsStatusUpdate();

	/**
	 * 
	 * @return the owning Requirement can be (un)linked to Test Cases
	 */
	public abstract boolean isRequirementLinkable();

	protected Set<RequirementStatus> defaultAvailableSet() {
		Set<RequirementStatus> next = new TreeSet<>();
		if (RequirementStatus.OBSOLETE != this){
			next.add(RequirementStatus.OBSOLETE);}
		next.add(this);
		return next;
	}

	public static StringComparator stringComparator() {
		return new StringComparator();
	}
	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}
	/**
	 * inner class used to sort RequirementStatus over their string representation. In case we have to sort stringified
	 * statuses with other arbitrary strings, stringified statuses will have a lower rank than other strings.
	 */
	private static class StringComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			RequirementStatus status1, status2;
			
			try {
				String comparableString1 = removeDisableString(o1);
				status1 = RequirementStatus.valueOf(comparableString1);
			} catch (IllegalArgumentException iae) {
				return 1;
			}
			
			try {
				String comparableString2 = removeDisableString(o2);
				status2 = RequirementStatus.valueOf(comparableString2);
			} catch (IllegalArgumentException iae) {
				return -1;
			}

			return status1.compareTo(status2);
		}

		private String removeDisableString(String o) {
			String newString = o;
			String disabled = "disabled.";
			if (o.startsWith(disabled)) {
				newString = o.substring(disabled.length());
			}
			return newString;
		}
	}

	/**
	 * will check if the transition from this status to new status is legal
	 * 
	 * @return true if it's okay
	 */
	public boolean isTransitionLegal(RequirementStatus newStatus) {
		return this.getAvailableNextStatus().contains(newStatus);
	}

	/**
	 * the set of the NON-available status transition. As for 1.1.0 and until further notice, should NOT include
	 * <i>this</i>
	 * 
	 * @return the NON-availableTransition.
	 */
	public abstract Set<RequirementStatus> getDisabledStatus();

	protected Set<RequirementStatus> returnDisabledStatus() {
		Set<RequirementStatus> disabledStatus = new TreeSet<>();
		for (RequirementStatus next : RequirementStatus.values()) {
			if (!this.isTransitionLegal(next)) {
				disabledStatus.add(next);
			}
		}
		return disabledStatus;
	}

}
