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

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class UploadedDataPropertyEditorSupport extends PropertyEditorSupport {
	/**
	 *
	 * @see java.beans.PropertyEditorSupport#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		UploadedData data;

		if (value instanceof MultipartFile) {
			MultipartFile multipartFile = (MultipartFile) value;
			try {
				data = new UploadedData(multipartFile.getInputStream(), multipartFile.getOriginalFilename(),
						multipartFile.getSize());
			} catch (IOException ex) {
				throw new IllegalArgumentException("Cannot read contents of multipart file", ex);
			}

		} else if (value instanceof byte[]) {
			byte[] bval = (byte[]) value;

			data = new UploadedData(new ByteArrayInputStream(bval), "anonymous", bval.length);

		} else if (value == null) {
			data = null;

		} else {
			byte[] bval = value.toString().getBytes();

			data= new UploadedData(new ByteArrayInputStream(bval), "anonymous", bval.length);

		}

		super.setValue(data);
	}

	/**
	 *
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	@Override
	public String getAsText() {
		// XXX this looks wrong
		InputStream value = (InputStream) getValue();
		return value != null ? value.toString() : "";
	}

	/**
	 *
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) {
		// XXX this looks wrong
	}

}
