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
package org.squashtest.tm.web.internal.exceptionresolver;

import java.io.Serializable;

public final class FieldValidationErrorModel implements Serializable {
	private static final long serialVersionUID = -2424352395793715437L;

	public final String objectName; // NOSONAR Field is immutable
	public final String fieldName; // NOSONAR Field is immutable
	private String fieldValue;
	public final String errorMessage; // NOSONAR Field is immutable

	public FieldValidationErrorModel(String objectName, String fieldName, String errorMessage) {
		super();
		this.objectName = objectName;
		this.fieldName = fieldName;
		this.errorMessage = errorMessage;
		this.fieldValue = null;
	}

	public FieldValidationErrorModel(String objectName, String fieldName, String errorMessage, String fieldValue) {
		this(objectName, fieldName, errorMessage);
		this.fieldValue = fieldValue;
	}

	public FieldValidationErrorModel(String objectName, String fieldName, String errorMessage , Object fieldValue) {
		this(objectName, fieldName, errorMessage);
		if(fieldValue != null){
			this.fieldValue = fieldValue.toString();
		}
	}

	public String getFieldValue() {
		return fieldValue;
	}
}
