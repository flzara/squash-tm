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
package org.squashtest.csp.core.bugtracker.net

import org.squashtest.tm.domain.servers.OAuth1aCredentials
import spock.lang.Specification

class OAuthUtilsTests extends Specification {

	// ******** test of signature ********************

	def "should agree that the URL is correct"(){

		given :
		def url = "http://somehost.com/valid/path?valid=parameter&multivalued=v1,v2,v3&moreparam=morevalue"

		when :
		OAuthUtils.validateUrlAndMethod(url, "POST")

		then :
		notThrown(Exception)
	}


	def "should find that the url has a regular malformation"(){

		given:
		def url = "invalid url"

		when:
		OAuthUtils.validateUrlAndMethod(url, "GET")

		then:
		thrown(IllegalArgumentException)

	}


	def "should find that the url declares the same query parameter multiple time"(){
		given:
		def url ="http://somehost.com/valid/path?invalid=value1&invalid=value2&invalid=value3"

		when:
		OAuthUtils.validateUrlAndMethod(url, "POST")

		then:
		thrown(IllegalArgumentException)
	}




}
