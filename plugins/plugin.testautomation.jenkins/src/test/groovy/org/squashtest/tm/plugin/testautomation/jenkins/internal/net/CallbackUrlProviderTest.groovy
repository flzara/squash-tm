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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.net

import org.squashtest.tm.service.testautomation.spi.BadConfiguration
import spock.lang.Specification

class CallbackUrlProviderTest extends Specification {

	private CallbackUrlProvider urlProvider = new CallbackUrlProvider()

	def "getCallbackUrl() - Should throw BadConfiguration Exception because the properties are not set correctly."() {
		given:
		urlProvider.callbackUrlFromDatabase = urlFromDatabase
		urlProvider.callbackUrlFromConfFile = urlFromConfFile
		when:
		urlProvider.getCallbackUrl()
		then:
		BadConfiguration bc = thrown(BadConfiguration)
		bc.messageArgs()[0] == exceptionProperty

		where:
		urlFromDatabase 				| urlFromConfFile  						| exceptionProperty
		null							| null           						| null
		null							| ""             						| null
		null							| "invalid/url"  						| "tm.test.automation.server.callbackurl"
		""								| null          						| null
		""								| ""            						| null
		""								| "invalid/url" 						| "tm.test.automation.server.callbackurl"
		"invalid/url"					| null           						| "squashtest.tm.callbackurl"
		"invalid/url"					| ""             						| "squashtest.tm.callbackurl"
		"invalid/url"					| "invalid/url"  						| "squashtest.tm.callbackurl"
		"invalid/url"					| "http://configuration:8080/squash"	| "squashtest.tm.callbackurl"
	}

	def "getCallbackUrl() - Should find the property set correctly in Database or in Configuration file and return it."() {
		given:
		urlProvider.callbackUrlFromDatabase = urlFromDatabase
		urlProvider.callbackUrlFromConfFile = urlFromConfFile
		when:
		URL result = urlProvider.getCallbackUrl()

		then:
		result == new URL(resultUrl)

		where:
		urlFromDatabase 				| urlFromConfFile                     | resultUrl
		null							| "http://configuration:8080/squash"  | "http://configuration:8080/squash"
		""								| "http://configuration:8080/squash"  | "http://configuration:8080/squash"
		"http://database:8080/squash"	| null                                | "http://database:8080/squash"
		"http://database:8080/squash"	| ""                                  | "http://database:8080/squash"
		"http://database:8080/squash"	| "invalid/url"                       | "http://database:8080/squash"
		"http://database:8080/squash"	| "http://configuration:8080/squash"  | "http://database:8080/squash"
	}


}
