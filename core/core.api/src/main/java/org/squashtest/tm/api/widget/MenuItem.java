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
package org.squashtest.tm.api.widget;

import org.squashtest.tm.api.security.acls.AccessRule;

/**
 * Describes a menu item.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface MenuItem {
	/**
	 * Should return this menu item's label. Should never return <code>null</code>
	 * 
	 * @return
	 */
	String getLabel();

	/**
	 * May return this menu item's tooltip. Should not return <code>null</code>
	 * 
	 * @return
	 */
	String getTooltip();

	/**
	 * Should return the CONTEXT RELATIVE URL bound to this menu item.
	 * 
	 * @return
	 */
	String getUrl();

	/**
	 * Should return the access rule for this menu item.
	 * 
	 * @return
	 */
	AccessRule getAccessRule();
}
