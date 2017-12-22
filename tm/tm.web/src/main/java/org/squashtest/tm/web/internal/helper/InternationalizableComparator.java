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
package org.squashtest.tm.web.internal.helper;

import java.util.Comparator;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;


public class InternationalizableComparator implements
		Comparator<Internationalizable> {

	private InternationalizationHelper helper;
	private Locale locale;
	
	public InternationalizableComparator(){
		super();
		locale = LocaleContextHolder.getLocale();
	}
	
	public InternationalizableComparator(InternationalizationHelper helper){
		this();
		this.helper = helper;
	}
	
	
	
	public void setHelper(InternationalizationHelper helper) {
		this.helper = helper;
	}

	@Override
	public int compare(Internationalizable o1, Internationalizable o2) {
		
		String name1 = helper.internationalize(o1, locale);
		String name2 = helper.internationalize(o2, locale);
		
		return name1.compareTo(name2);
		
	}

}
