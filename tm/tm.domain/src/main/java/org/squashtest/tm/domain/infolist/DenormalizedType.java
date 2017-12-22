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
import javax.persistence.Embeddable;

@Embeddable
public class DenormalizedType implements DenormalizedInfoListItem {

	@Column(name="TC_TYP_LABEL")
	private String label;

	@Column(name="TC_TYP_CODE")
	private String code;

	@Column(name="TC_TYP_ICON_NAME")
	private String iconName;

	public DenormalizedType(){
		super();
	}

	public DenormalizedType(String label, String code, String iconName) {
		super();
		this.label = label;
		this.code = code;
		this.iconName = iconName;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String getIconName() {
		return iconName;
	}

	@Override
	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

}
