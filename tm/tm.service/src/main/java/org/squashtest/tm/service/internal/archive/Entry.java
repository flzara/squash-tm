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
package org.squashtest.tm.service.internal.archive;

import java.io.InputStream;


/**
 * <p>An Entry must be capable of :</p>
 * <ul>
 * 	<li>Give its path. It shall not contain trailing '/' even for folders.</li>
 * 	<li>Give the parent node path.</li>
 * 	<li>Give the filename, without trailing '/', even for folders.</li>
 * 	<li>Tell if its a directory or a file</li>
 * 	<li>Give an InputStream on that entry</li>
 * 	<li>the root of the hierarchy this entry belongs to is always known as '/'</li> 
 * </ul>
 * 
 * 
 * @author bsiri
 *
 */
public interface Entry {

	String getName();
	
	String getShortName();
	
	Entry getParent();
	
	boolean isDirectory();
	
	boolean isFile();
	
	/**
	 * must return null if it's a directory, otherwise must return the stream
	 * @return
	 */
	InputStream getStream();

}
