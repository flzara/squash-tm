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
package org.squashtest.tm.service.deletion;

import java.util.Locale;

import org.springframework.context.MessageSource;


public abstract class MilestoneRuleReport implements SuppressionPreviewReport{

	// please choose one of "test-cases", "requirements", "campaigns"
	// please don't make me another enum just for that.
	private String nodeType = "test-cases";

	public MilestoneRuleReport(String type){
		this.nodeType = type;
	}

	protected abstract String getKey();

	@Override
	public String toString(MessageSource source, Locale locale) {
		return source.getMessage(getKey()+"."+nodeType, null, locale);
	}
}
