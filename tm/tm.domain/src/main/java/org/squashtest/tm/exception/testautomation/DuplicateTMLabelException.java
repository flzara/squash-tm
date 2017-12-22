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
package org.squashtest.tm.exception.testautomation;

import org.squashtest.tm.exception.DomainException;

public class DuplicateTMLabelException extends DomainException {

	private static final String LABEL = "label";

	private static final long serialVersionUID = 2815263509542519285L;

	public DuplicateTMLabelException(String fieldValue) {
		super(makeMessage(fieldValue), LABEL);
		setFieldValue(fieldValue);
	}

	public DuplicateTMLabelException() {
		super(LABEL);
	}

	private static String makeMessage(String label) {
		return "Cannot add test automation project with tm label : " + label
				+ " because it already exists in this tm project";
	}

	@Override
	public String getI18nKey() {
		return "squashtm.domain.exception.duplicate.tmlabel";
	}



}
