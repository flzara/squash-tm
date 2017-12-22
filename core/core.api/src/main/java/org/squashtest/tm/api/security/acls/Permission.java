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
package org.squashtest.tm.api.security.acls;

/**
 * Enum of Squash permission. Should match the one defined as Spring Security base / custom permissions
 * @author Gregory Fouquet
 *
 */
public enum Permission {
	READ("readable"),
	WRITE("editable"),
	CREATE("creatable"),
	DELETE("deletable"),
	ADMIN("administrable"),
	EXECUTE("executable"),
	EXPORT("exportable"),
	MANAGEMENT("manageable"),
	LINK("linkable"),
	IMPORT("importable"),
	ATTACH("attachable"),
	ANY("any");

	private final String quality;

	Permission(String quality) {
		this.quality = quality;
	}

	/**
	 * @return the quality
	 */
	public String getQuality() {
		return quality;
	}

}
