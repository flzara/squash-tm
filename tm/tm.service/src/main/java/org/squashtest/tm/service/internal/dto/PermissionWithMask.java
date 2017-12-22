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
package org.squashtest.tm.service.internal.dto;


import java.util.EnumSet;

public enum PermissionWithMask {
	READ("readable",1),
	WRITE("editable",2),
	CREATE("creatable",4),
	DELETE("deletable",8),
	ADMIN("administrable",16),
	MANAGEMENT("manageable",32),
	EXPORT("exportable",64),
	EXECUTE("executable",128),
	LINK("linkable",256),
	IMPORT("importable",512),
	ATTACH("attachable",1024);

	private final String quality;

	private final Integer mask;

	PermissionWithMask(String quality, Integer mask) {
		this.quality = quality;
		this.mask = mask;
	}

	/**
	 * @return the quality
	 */
	public String getQuality() {
		return quality;
	}

	public Integer getMask() {
		return mask;
	}

	public static PermissionWithMask findByMask(Integer mask){
		EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);

		for (PermissionWithMask permission : permissions) {
			if (permission.getMask().equals(mask)){
				return permission;
			}
		}
		return null;
	}

}
