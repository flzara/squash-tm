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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.exception.customfield.BindableEntityMismatchException;
import org.squashtest.tm.exception.customfield.MandatoryCufException;
import org.squashtest.tm.exception.customfield.WrongCufDateFormatException;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.text.ParseException;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "FIELD_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("CF")
public class CustomFieldValue implements Identified, SingleValuedCustomFieldValue {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldValue.class);
	public static final int MAX_SIZE = 255;
	@Id
	@Column(name = "CFV_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_field_value_cfv_id_seq")
	@SequenceGenerator(name = "custom_field_value_cfv_id_seq", sequenceName = "custom_field_value_cfv_id_seq", allocationSize = 1)
	private Long id;

	protected Long boundEntityId;

	@Enumerated(EnumType.STRING)
	protected BindableEntity boundEntityType;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "CFB_ID")
	protected CustomFieldBinding binding;

	@Size(min = 0, max = MAX_SIZE)
	protected String value;

	@Column(name = "CF_ID")
	protected Long cufId;

	public CustomFieldValue() {
		super();
	}

	public CustomFieldValue(Long boundEntityId, BindableEntity boundEntityType, CustomFieldBinding binding, String value) {
		super();
		this.boundEntityId = boundEntityId;
		this.binding = binding;
		this.cufId = binding.getCustomField().getId();
		doSetBoundEntityType(boundEntityType);
		doSetValue(value);
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		doSetValue(value);
	}

	private void doSetValue(String value) {
		CustomField cuf = doGetCustomField();
		if (cuf != null) {
			if (!cuf.isOptional() && StringUtils.isBlank(value)) {
				throw new MandatoryCufException(this);
			}
			if (cuf.inputType == InputType.DATE_PICKER && !StringUtils.isBlank(value)) {
				try {
					DateUtils.parseIso8601Date(value);
				} catch (ParseException pe) {
					throw new WrongCufDateFormatException(pe);
				}
			}
		}
		this.value = value;
	}

	public CustomFieldBinding getBinding() {
		return binding;
	}

	public CustomField getCustomField() {
		return doGetCustomField();
	}

	protected CustomField doGetCustomField() {
		if (binding != null) {
			return binding.getCustomField();
		}
		return null;
	}

	public void setBinding(CustomFieldBinding binding) {
		this.binding = binding;
	}

	public Long getBoundEntityId() {
		return boundEntityId;
	}

	public Long getCufId() {
		return cufId;
	}

	public void setCufId(Long cufId) {
		this.cufId = cufId;
	}

	public BindableEntity getBoundEntityType() {
		return boundEntityType;
	}

	public void setBoundEntity(BoundEntity entity) {
		this.boundEntityId = entity.getBoundEntityId();
		doSetBoundEntityType(entity.getBoundEntityType());
	}

	private void doSetBoundEntityType(BindableEntity entityType) {
		if (entityType != binding.getBoundEntity()) {
			throw new BindableEntityMismatchException("attempted to bind '" + entityType
				+ "' while expected '" + binding.getBoundEntity() + "'");
		}

		this.boundEntityType = entityType;
	}


	public CustomFieldValue copy() {
		CustomFieldValue copy = new CustomFieldValue();
		copy.setBinding(binding);
		copy.setValue(this.value);
		copy.setCufId(this.getCufId());
		return copy;
	}

	public boolean representsSameBinding(CustomFieldValue otherValue) {
		return otherValue.getBinding().getId().equals(binding.getId());
	}

	public boolean representsSameCustomField(CustomFieldValue otherValue) {
		return otherValue.getCustomField().getId().equals(getCustomField().getId());
	}

	public Date getValueAsDate() {
		if (getCustomField() != null && getCustomField().getInputType() == InputType.DATE_PICKER) {
			try {
				return DateUtils.parseIso8601Date(value);
			} catch (ParseException e) {
				LOGGER.warn("Unable to parse date '" + value + "' of custom field value #" + id, e);
			}
		}
		return null;

	}

	@Override
	public RawValue asRawValue() {
		return new RawValue(value);
	}

	public void accept(CustomFieldValueVisitor visitor) {
		visitor.visit(this);
	}
}
