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
package org.squashtest.tm.web.internal.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

/**
 * Should be used to return an empty JSON response.
 * As per XHR spec, an empty JSON response body should be "null", yet Spring produces an empty body.
 * 
 * @author Gregory Fouquet
 * 
 */
public class JsonEmptyResponseEntity extends ResponseEntity<String> {

	public JsonEmptyResponseEntity(MultiValueMap<String, String> headers, HttpStatus statusCode) {
		super("null", headers, statusCode);
	}

	/**
	 * @param statusCode
	 */
	public JsonEmptyResponseEntity(HttpStatus statusCode) {
		super("null", statusCode);
	}

}
