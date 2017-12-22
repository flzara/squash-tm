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
package org.squashtest.tm.web.internal.exceptionresolver;

/**
 * @author bsiri
 *
 */
enum MimeType {
	APPLICATION_JSON(){
		@Override
		public String requestHeaderValue(){
			return "application/json";
		}
	},
	TEXT_PLAIN(){
		@Override
		public String requestHeaderValue() {
			return "text/plain";
		}
	},
	TEXT_HTML(){
		@Override
		public String requestHeaderValue() {
			return "text/html";
		}
	},
	ANYTHING(){
		@Override
		public String requestHeaderValue() {
			return "*/*";
		}
	};
	
	public abstract String requestHeaderValue();
}
