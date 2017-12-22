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
package org.squashtest.tm;
import groovy.xml.MarkupBuilder;

import org.junit.Test;

import static org.junit.Assert.*;

class XmlBuilderIT {
	@Test
	void toto() {
		new MarkupBuilder().macker() {
			var(name: 'squashtest', value: 'org.suashtest.csp.*')
			pattern(name: 'repository', value: '${squashtest}.internal.repository.*')
			pattern(name: 'service-api', value: '${squashtest}.service.**')
			pattern(name: 'service-impl', value: '${squashtest}.internal.service.**')
			pattern(name: 'presentation', value: '${squashtest}.web.**')

//			pattern(name: 'repo-api', value: '${squashtest}.internal.repository.*')
//			pattern(name: 'repo-hibernate-impl', value: '${squashtest}.internal.repository.hibernate.**')
//			pattern(name: 'domain', value: '${squashtest}.domain.**')
//			pattern(name: 'presentation-layer', value: '${squashtest}.web.**')
			
			ruleset(name: 'Layered architecture') {
				'access-rule'() {
					message('The persistence layer should not access the service layer')
					deny() {
						from(pattern: "repository")
						to {
							include(pattern: 'service-api')
							include(pattern: 'service-impl')
						}
					}
				}
				'access-rule'() {
					message('The persistence layer should not access the presentation layer')
					deny() {
						from(pattern: 'repository')
						to(pattern: 'presentation')
					}
				}
				'access-rule'() {
					message('The presentation layer should not access the persistence layer')
					deny() {
						from(pattern: 'presentation')
						to(pattern: 'repository')
					}
				}
			}
		}
	}
}
