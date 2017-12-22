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

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.validation.constraint.HasDefaultAsRequired;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.text.ParseException;
import java.util.Date;

/**
 * @author Gregory Fouquet
 */

@NamedQueries({@NamedQuery(name = "CustomField.findAllOrderedByName", query = "from CustomField cf order by cf.name"),
	@NamedQuery(name = "CustomField.countCustomFields", query = "select count(*) from CustomField"),
	@NamedQuery(name = "CustomField.findByCode", query = "from CustomField where code = ?1")})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "FIELD_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("CF")
@HasDefaultAsRequired
public class CustomField implements Identified{
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomField.class);

	public static final String CODE_REGEXP = "^[A-Za-z0-9_^;]*$";
	public static final String OPTION_REGEXP = "^[A-Za-z0-9_]*$";
	public static final int MIN_CODE_SIZE = 1;
	public static final int MAX_CODE_SIZE = 30;

	@Id
	@Column(name = "CF_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_field_cf_id_seq")
	@SequenceGenerator(name = "custom_field_cf_id_seq", sequenceName = "custom_field_cf_id_seq", allocationSize = 1)
	protected Long id;

	@NotBlank
	@Size(min = 0, max = Sizes.NAME_MAX)
	protected String name;

	@NotBlank
	@Size(min = 0, max = Sizes.LABEL_MAX)
	protected String label = "";

	protected boolean optional = true;

	@Size(min = 0, max = 255)
	protected String defaultValue;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(updatable = false)
	protected InputType inputType = InputType.PLAIN_TEXT;

	@NotBlank
	@Size(min = MIN_CODE_SIZE, max = MAX_CODE_SIZE)
	@Pattern(regexp = CODE_REGEXP)
	protected String code = "";

	/**
	 * For ORM purposes.
	 */
	protected CustomField() {
		super();
	}

	public CustomField(@NotNull InputType inputType) {
		super();
		this.inputType = inputType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isOptional() {
		if (inputType == InputType.CHECKBOX) {
			return false;
		}
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public Date getDefaultValueAsDate() {
		// TODO copypasta, slap this into utility class
		if (this.inputType == InputType.DATE_PICKER) {
			try {
				return DateUtils.parseIso8601Date(defaultValue);
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
		return null;

	}

	public void setDefaultValue(String defaultValue) {
		String dValue = defaultValue;
		if (this.inputType == InputType.DATE_PICKER) {
			try {
				DateUtils.parseIso8601Date(defaultValue);
			} catch (ParseException e) {
				dValue = "";
			}
		}

		this.defaultValue = dValue;
	}

	@Override
	public Long getId() {
		return id;
	}

	public InputType getInputType() {
		return inputType;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void accept(CustomFieldVisitor visitor) {
		visitor.visit(this);
	}
}
