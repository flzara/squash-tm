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
package org.squashtest.tm.exception.customfield;

import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.exception.DomainException;

public class MandatoryCufException extends DomainException {


	private static final long serialVersionUID = -6101757181195458679L;
	private String key = "message.notBlank";


	public MandatoryCufException(String field) {
		super(field);
	}

	public MandatoryCufException(String message, String field){
		super(message, field);
	}

	public MandatoryCufException( String message, String field, String objectName) {
		this(message, field);
		this.setObjectName(objectName);
	}

	public MandatoryCufException(CustomFieldValue customFieldValue) {
		this("the custom field for value #"+customFieldValue.getId()+" is mandatory", String.valueOf(customFieldValue.getId()),  "cuf-value");
	}

	@Override
	public String getI18nKey() {
		return key;
	}

}
