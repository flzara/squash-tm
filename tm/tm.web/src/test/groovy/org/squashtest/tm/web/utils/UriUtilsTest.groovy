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
package org.squashtest.tm.web.utils

import org.springframework.mock.web.MockHttpServletRequest
import org.squashtest.tm.web.internal.util.UriUtils
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class UriUtilsTest extends Specification{

	def "should canonicalize an URL"(){

		given :
		def url = "HTTPS://MY.SERVER.org:443/app"

		when :
		def res = UriUtils.canonicalize url

		then :
		res == "https://my.server.org/app"

	}


	def "should return a canonicalized base URL"(){

		given :
			HttpServletRequest request = new MockHttpServletRequest(
				scheme: "HTTPS",
				serverName: "MY.SERVER.ORG",
				serverPort: 443,
				contextPath: "/app",
				servletPath: "/whatever/resources/2"
			)

		when :
			def res = UriUtils.extractBaseUrl request


		then :
			res == "https://my.server.org/app"

	}


}
