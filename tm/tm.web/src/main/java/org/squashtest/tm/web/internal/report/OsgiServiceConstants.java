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
package org.squashtest.tm.web.internal.report;

/**
 * Defines constants related to Osgi Services related properties.
 * 
 * @author Gregory Fouquet
 * 
 */
final class OsgiServiceConstants {
	private OsgiServiceConstants() {
		super();
	}

	/**
	 * Key to retrieve an OSGi service's id as passed to a service registration callback.
	 */
	public static final String SERVICE_ID_KEY = "osgi.service.blueprint.compname";

}
