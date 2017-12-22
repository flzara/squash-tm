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
package org.squashtest.tm.security.acls;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
/**
 * 
 * @author mpagnon
 *
 */
public class CustomPermission extends BasePermission {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Permission MANAGEMENT = new CustomPermission(1 << 5, 'M'); //NONSONAR 32
	public static final Permission EXPORT = new CustomPermission(1 << 6, 'X'); //NONSONAR 64
	public static final Permission EXECUTE = new CustomPermission(1 << 7, 'E'); //NONSONAR 128
	public static final Permission LINK = new CustomPermission(1 << 8, 'L'); //NONSONAR 256
	public static final Permission IMPORT = new CustomPermission(1 << 9, 'I'); //NONSONAR 512
	public static final Permission ATTACH = new CustomPermission(1 << 10, 'T'); //NONSONAR 1024
	public static final Permission EXTENDED_DELETE = new CustomPermission(1 << 11, 'S'); //NONSONAR 2048
	public static final Permission READ_UNASSIGNED = new CustomPermission(1 << 12, 'U'); //NONSONAR 4096
	
	public CustomPermission(int mask) {
		super(mask);
	}

	public CustomPermission(int mask, char code) {
		super(mask, code);
	}

}