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
package org.squashtest.tm.web.internal.controller.attachment;

import org.squashtest.tm.service.attachment.RawAttachment;

import java.io.InputStream;

/**
 * @author Gregory Fouquet
 *
 */

// XSS OK
public class UploadedData implements RawAttachment {
	private final InputStream stream;
	private final String name;
	private final long sizeInBytes;

	public UploadedData(InputStream stream, String name, long sizeInBytes) {
		super();
		this.stream = stream;
		this.name = name;
		this.sizeInBytes = sizeInBytes;
	}

	/**
	 * @return the stream
	 */
	@Override
	public InputStream getStream() {
		return stream;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the sizeInBytes
	 */
	@Override
	public long getSizeInBytes() {
		return sizeInBytes;
	}

}
