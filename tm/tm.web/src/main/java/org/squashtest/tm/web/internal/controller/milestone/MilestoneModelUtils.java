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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

public final class MilestoneModelUtils {

	protected static final String SEPARATOR = ", ";

	public static String timeIntervalToString(Collection<Milestone> milestones, InternationalizationHelper i18nHelper, Locale locale){

		if (milestones.isEmpty()){
			return "--";
		}

		Date minDate = null;
		Date maxDate = null;

		for (Milestone m : milestones) {
			Date date = m.getEndDate();
			if (minDate == null || date.before(minDate)) {
				minDate = date;
			}
			if (maxDate == null || date.after(maxDate)) {
				maxDate = date;
			}
		}

		String strMindate = i18nHelper.localizeShortDate(minDate, locale);
		String strMaxdate = i18nHelper.localizeShortDate(maxDate, locale);
		if(!strMaxdate.equals(strMindate)) {
			return strMindate + " - " + strMaxdate;
		}else{
			return strMaxdate;
		}
	}

	public static String milestoneLabelsOrderByDate(Set<Milestone> milestones) {

		final StringBuilder sb = new StringBuilder();
		ArrayList<Milestone> liste = new ArrayList<>(milestones);
		Collections.sort(liste, new Comparator<Milestone>() {
			@Override
			public int compare(Milestone m1, Milestone m2) {
				return m1.getEndDate().after(m2.getEndDate()) ? 1 : -1;
			}
		});

		CollectionUtils.forAllDo(liste, new Closure(){
			@Override
			public void execute(Object input) {
				Milestone m = (Milestone) input;
				sb.append(m.getLabel());
				sb.append(SEPARATOR);
			}});

		sb.delete(Math.max(sb.length() - SEPARATOR.length(), 0), sb.length());
		return sb.toString();
	}

}
