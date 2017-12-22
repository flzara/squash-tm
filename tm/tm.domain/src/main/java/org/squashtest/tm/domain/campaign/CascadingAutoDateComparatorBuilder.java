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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

final class CascadingAutoDateComparatorBuilder {

	private CascadingAutoDateComparatorBuilder() {
	}

	public static Comparator<IterationTestPlanItem> buildTestPlanFirstDateSorter() {

		return new Comparator<IterationTestPlanItem>() {

			private final NullSafeLowerDateComparator comparator = new NullSafeLowerDateComparator();

			@Override
			public int compare(IterationTestPlanItem o1, IterationTestPlanItem o2) {
				Date d1 = o1.getLastExecutedOn();
				Date d2 = o2.getLastExecutedOn();

				return comparator.compare(d1, d2);
			}

		};

	}

	public static Comparator<IterationTestPlanItem> buildTestPlanLastDateSorter() {
		return new Comparator<IterationTestPlanItem>() {

			private final NullSafeHigherDateComparator comparator = new NullSafeHigherDateComparator();

			@Override
			public int compare(IterationTestPlanItem o1, IterationTestPlanItem o2) {
				Date d1 = o1.getLastExecutedOn();
				Date d2 = o2.getLastExecutedOn();

				return comparator.compare(d1, d2);
			}

		};

	}

	public static Comparator<Iteration> buildIterationActualStartOrder() {

		return new Comparator<Iteration>() {

			private final NullSafeLowerDateComparator comparator = new NullSafeLowerDateComparator();

			@Override
			public int compare(Iteration o1, Iteration o2) {
				Date d1 = o1.getActualStartDate();
				Date d2 = o2.getActualStartDate();

				return comparator.compare(d1, d2);
			}

		};
	}

	public static Comparator<Iteration> buildIterationActualEndOrder() {
		return new Comparator<Iteration>() {

			private final NullSafeHigherDateComparator comparator = new NullSafeHigherDateComparator();

			@Override
			public int compare(Iteration o1, Iteration o2) {
				Date d1 = o1.getActualEndDate();
				Date d2 = o2.getActualEndDate();

				return comparator.compare(d1, d2);
			}

		};
	}



	/* ********************** tools of the trade ***************************** */

	/*
	 * policy on nulls : nulls have a higher rank.
	 *
	 */
	private static class NullSafeLowerDateComparator implements Comparator<Date> {
		@Override
		public int compare(Date o1, Date o2) {
			int result;

			if (o1 == null) {
				if (o2 == null) {
					result = 0;
				} else {
					result = 1;
				}
			} else {
				if (o2 == null) {
					result = -1;
				} else {
					result = o1.compareTo(o2);
				}
			}

			return result;

		}
	}

	/*
	 *  policy on nulls : nulls have a lower rank.
	 */
	private static class NullSafeHigherDateComparator implements Comparator<Date>, Serializable {
		@Override
		public int compare(Date o1, Date o2) {

			int result;

			if (o1 == null) {
				if (o2 == null) {
					result = 0;
				} else {
					result = -1;
				}
			} else {
				if (o2 == null) {
					result = 1;
				} else {
					result = o1.compareTo(o2);
				}
			}

			return result;
		}
	}


}
