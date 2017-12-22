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
package org.squashtest.tm.domain.denormalizedfield;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.RenderingLocation;

@NamedQueries(value = {
		@NamedQuery(name = "DenormalizedFieldValue.deleteAllForEntity", query = "delete DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :entityId and dfv.denormalizedFieldHolderType = :entityType"),
		@NamedQuery(name = "DenormalizedFieldValue.findDFVForEntity", query = "from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :entityId and dfv.denormalizedFieldHolderType = :entityType order by dfv.position"),
		@NamedQuery(name = "DenormalizedFieldValue.findDFVForEntityAndRenderingLocation", query = "select dfv from DenormalizedFieldValue dfv join dfv.renderingLocations rl where dfv.denormalizedFieldHolderId = :entityId and dfv.denormalizedFieldHolderType = :entityType and rl = :renderingLocation order by dfv.position"),
		@NamedQuery(name = "DenormalizedFieldValue.findDFVForEntities", query = "select dfv from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId in (:entityIds) and dfv.denormalizedFieldHolderType = :entityType order by dfv.position"),
		@NamedQuery(name = "DenormalizedFieldValue.findDFVForEntitiesAndLocations", query = "select dfv from DenormalizedFieldValue dfv join dfv.renderingLocations rl where dfv.denormalizedFieldHolderId in (:entityIds) and dfv.denormalizedFieldHolderType = :entityType and rl in (:locations) order by dfv.position"),
		@NamedQuery(name = "DenormalizedFieldValue.countDenormalizedFields", query = "select count(dfv) from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = ?1 and dfv.denormalizedFieldHolderType = ?2")
})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "FIELD_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("CF")
public class DenormalizedFieldValue {

	private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizedFieldValue.class);
	@Id
	@Column(name = "DFV_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "denormalized_field_value_dfv_id_seq")
	@SequenceGenerator(name = "denormalized_field_value_dfv_id_seq", sequenceName = "denormalized_field_value_dfv_id_seq", allocationSize = 1)
	protected Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CFV_ID", nullable = true)
	protected CustomFieldValue customFieldValue;

	@NotBlank
	@Size(min = CustomField.MIN_CODE_SIZE, max = CustomField.MAX_CODE_SIZE)
	@Pattern(regexp = CustomField.CODE_REGEXP)
	protected String code = "";

	protected Long denormalizedFieldHolderId;

	@Enumerated(EnumType.STRING)
	protected DenormalizedFieldHolderType denormalizedFieldHolderType;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(updatable = false)
	protected InputType inputType;

	@NotBlank
	@Size(max = Sizes.LABEL_MAX)
	protected String label = "";

	protected int position;

	@Size(min = 0, max = 255)
	protected String value;

	@ElementCollection
	@CollectionTable(name = "DENORMALIZED_FIELD_RENDERING_LOCATION", joinColumns = @JoinColumn(name = "DFV_ID"))
	@Enumerated(EnumType.STRING)
	@Column(name = "RENDERING_LOCATION")
	protected Set<RenderingLocation> renderingLocations = new HashSet<>(5);

	/**
	 * For ORM purposes.
	 */
	protected DenormalizedFieldValue() {
		super();

	}

	/**
	 * Copies the attributes of the given customFieldValue and it's associated customField and customFieldBinding
	 *
	 * @param customFieldValue
	 *            : must be bound to it's customField
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 */
	public DenormalizedFieldValue(CustomFieldValue customFieldValue, Long denormalizedFieldHolderId,
			DenormalizedFieldHolderType denormalizedFieldHolderType) {
		super();
		this.customFieldValue = customFieldValue;
		CustomField cuf = customFieldValue.getCustomField();
		this.code = cuf.getCode();
		this.inputType = cuf.getInputType();
		this.label = cuf.getLabel();
		this.position = customFieldValue.getBinding().getPosition();
		this.renderingLocations = customFieldValue.getBinding().copyRenderingLocations();
		this.denormalizedFieldHolderId = denormalizedFieldHolderId;
		this.denormalizedFieldHolderType = denormalizedFieldHolderType;

		this.value = customFieldValue.getValue();
	}



	public Long getId() {
		return id;
	}

	public CustomFieldValue getCustomFieldValue() {
		return customFieldValue;
	}

	public String getCode() {
		return code;
	}

	public Long getDenormalizedFieldHolderId() {
		return denormalizedFieldHolderId;
	}

	public DenormalizedFieldHolderType getDenormalizedFieldHolderType() {
		return denormalizedFieldHolderType;
	}

	public InputType getInputType() {
		return inputType;
	}

	public String getLabel() {
		return label;
	}

	public int getPosition() {
		return position;
	}

	public String getValue() {
		return value;
	}

	/**
	 * Return the value as a Date or <code>null</code> if the input type is not Date-picker and if the parsing can't be
	 * done.
	 *
	 * @return a {@link Date} or <code>null</code> in case of ParseException and wrong input-type
	 */
	public Date getValueAsDate() {
		Date toReturn = null;
		if (this.inputType == InputType.DATE_PICKER) {
			try {
				toReturn = DateUtils.parseIso8601Date(value);
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
		return toReturn;
	}

	public Set<RenderingLocation> getRenderingLocations() {
		return renderingLocations;
	}

	public void setRenderingLocations(Set<RenderingLocation> renderingLocations) {
		this.renderingLocations = renderingLocations;
	}

	public void accept(DenormalizedFieldVisitor visitor) {
		visitor.visit(this);
	}


	public void setValue(String value) {
		this.value = value;
	}

}
