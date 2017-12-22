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
package org.squashtest.tm.service.project;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.it.basespecs.DbunitServiceSpecification;
import org.squashtest.tm.service.project.ProjectManagerService;
import org.squashtest.tm.service.project.ProjectTemplateManagerService;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
@Transactional
class ProjectTemplateManagerServiceIT extends DbunitServiceSpecification {

	@Inject ProjectTemplateManagerService manager

	@DataSet("ProjectTemplateManagerIT.xml")
	def"should find all references"(){
		when:
		def res = manager.findAllReferences()

		then:
		res.collect { it.class } == [ NamedReference, NamedReference ]
		res*.name == [ "FOUR", "TWO" ]
	}
}
