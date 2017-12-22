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
package org.squashtest.tm.web.internal.model.json

import org.squashtest.tm.service.internal.dto.json.JsonProject
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class JsonProjectTest extends Specification {
	def "should build json project"() {
		given:
		Project p = new Project(name: "foo")
		use (ReflectionCategory) {
			GenericProject.set field: "id", of: p, to: 10000L
		}

		when:
		def res = JsonProject.toJson(p)

		then:
		res.id == 10000L
		res.uri == "/projects/10000"
		res.name == "foo"
	}
}
