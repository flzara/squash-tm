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
package org.squashtest.tm.service.internal.scmserver

import org.squashtest.tm.core.scm.spi.ScmConnectorProvider
import spock.lang.Specification

class ScmConnectorRegistryTest extends Specification {

	ScmConnectorRegistry scmRegistry = new ScmConnectorRegistry()

	def '#registerScmConnectors() - [Nominal] Should register several connectors'() {
		given: "Mock some providers"
			ScmConnectorProvider gitProvider = Mock()
			gitProvider.getScmKind() >> "git"
			ScmConnectorProvider hgProvider = Mock()
			hgProvider.getScmKind() >> "mercurial"
			ScmConnectorProvider svProvider = Mock()
			svProvider.getScmKind() >> "subversion"
		and: "Assume these providers were collected"
			Collection<ScmConnectorProvider> providersCollection = [gitProvider, hgProvider, svProvider]
			scmRegistry.collectedScmConnectors = providersCollection
		when:
			scmRegistry.registerScmConnectorProviders()
		then:
			Map<String, ScmConnectorProvider> resultMap = scmRegistry.registeredScmConnectorsMap
			resultMap.entrySet().size() == providersCollection.size()
			resultMap.get("git") == gitProvider
			resultMap.get("mercurial") == hgProvider
			resultMap.get("subversion") == svProvider
	}

	def '#registerScmConnectors() - [Empty] Should register no connectors'() {
		given: "Assume no providers were collected"
			scmRegistry.collectedScmConnectors = []
		when:
			scmRegistry.registerScmConnectorProviders()
		then:
			scmRegistry.registeredScmConnectorsMap.isEmpty()
	}

	def '#registerScmConnectors() - [Exception] Should try to register a connector with no kind and throw an IllegalArgumentException'() {
		given: "Mock a provider with no kind"
			ScmConnectorProvider illegalProvider = Mock()
			illegalProvider.getScmKind() >> null
		and: "Assume this provider was collected"
			scmRegistry.collectedScmConnectors = [illegalProvider]
		when:
			scmRegistry.registerScmConnectorProviders()
		then:
			thrown IllegalArgumentException
	}


	def "#getRegisteredScmKinds() - [Nominal] Should get several kinds"() {
		given: "Mock some providers"
			ScmConnectorProvider gitProvider = Mock()
			gitProvider.getScmKind() >> "git"
			ScmConnectorProvider hgProvider = Mock()
			hgProvider.getScmKind() >> "mercurial"
			ScmConnectorProvider svProvider = Mock()
			svProvider.getScmKind() >> "subversion"
		and: "Fill the map"
			scmRegistry.registeredScmConnectorsMap.put(gitProvider.getScmKind(), gitProvider)
			scmRegistry.registeredScmConnectorsMap.put(hgProvider.getScmKind(), hgProvider)
			scmRegistry.registeredScmConnectorsMap.put(svProvider.getScmKind(), svProvider)
		when:
			Set<String> resultSet = scmRegistry.getRegisteredScmKinds()
		then:
			resultSet == ["git", "mercurial", "subversion"] as Set
	}

	def "#getRegisteredScmKinds() - [Empty] Should get no kinds"() {
		given: "Empty the map"
			scmRegistry.registeredScmConnectorsMap.clear()
		when:
			Set<String> resultSet = scmRegistry.getRegisteredScmKinds()
		then:
			resultSet == [] as Set
	}
}
