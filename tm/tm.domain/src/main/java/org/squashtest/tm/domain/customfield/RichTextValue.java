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
package org.squashtest.tm.domain.customfield;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.squashtest.tm.exception.customfield.MandatoryCufException;

@Entity
@DiscriminatorValue("RTF")
public class RichTextValue extends CustomFieldValue {

	@Lob
	@Type(type="org.hibernate.type.TextType")
	private String largeValue ;

	@Override
	public void setValue(String value){
		CustomField field = getCustomField();
		if (field != null && !field.isOptional() && StringUtils.isBlank(value)){
			throw new MandatoryCufException(this);
		}

		this.largeValue  = value;
	}

	@Override
	public String getValue(){
		return largeValue  != null ? largeValue  : "";
	}

	@Override
	public CustomFieldValue copy(){
		CustomFieldValue copy = new RichTextValue();
		copy.setBinding(getBinding());
		copy.setValue(getValue());
		copy.setCufId(binding.getCustomField().getId());
		return copy;
	}

	@Override
	public void accept(CustomFieldValueVisitor visitor) {
		visitor.visit(this);
	}
	
	
	@Override
	public RawValue asRawValue() {
		return new RawValue(largeValue);
	}
	

}
