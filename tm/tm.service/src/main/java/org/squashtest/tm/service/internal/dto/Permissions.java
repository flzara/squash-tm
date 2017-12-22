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

import java.util.List;

public class Permissions {

	private boolean readable;
	private boolean editable;
	private boolean creatable;
	private boolean deletable;


	public static Permissions fromMaskList(List<Integer> masks){
		Permissions permissions = new Permissions();
		if(masks.contains(PermissionWithMask.READ.getMask())){
			permissions.setReadable(true);
		}
		return permissions;
	}

	// we could statically initialize fields at true to avoid this method,
	// but as we are working with permissions i prefer to set anything at false by default
	public static Permissions adminPermissions(){
		Permissions permissions = new Permissions();
		permissions.setReadable(true);
		return permissions;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}
}
