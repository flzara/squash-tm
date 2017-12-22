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
package org.squashtest.tm.domain.infolist;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ITEM_TYPE", discriminatorType = DiscriminatorType.STRING)
public abstract class InfoListItem implements Identified {

	public static final String NO_ICON = "noicon";

	@Id
	@Column(name = "ITEM_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "info_list_item_item_id_seq")
	@SequenceGenerator(name = "info_list_item_item_id_seq", sequenceName = "info_list_item_item_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "LIST_ID", insertable = false, updatable = false)
	private InfoList infoList;

	@Size(max = 100)
	@NotBlank
	private String label = "";

	@Size(max = 30)
	@NotBlank
	private String code = "";

	private boolean isDefault = false;

	@Size(max = 100)
	private String iconName = "";

	public InfoListItem() {
		super();
	}

	public InfoList getInfoList() {
		return infoList;
	}

	public void setInfoList(InfoList infoList) {
		this.infoList = infoList;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String getIconName() {
		if (! StringUtils.isBlank(iconName)){
			return iconName;
		}
		else{
			return NO_ICON;
		}
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	@Override
	public Long getId() {
		return id;
	}

	/**
	 * tests equality-by-code
	 * 
	 * @param other
	 * @return
	 */
	public boolean references(Object other) {
		if (other == null) {
			return false;
		}

		if (InfoListItem.class.isAssignableFrom(other.getClass())) {
			return ((InfoListItem) other).getCode().equals(getCode());
		} else {
			return false;
		}

	}

}
