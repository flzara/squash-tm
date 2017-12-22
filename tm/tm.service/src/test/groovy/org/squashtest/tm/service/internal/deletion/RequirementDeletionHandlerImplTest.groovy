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
package org.squashtest.tm.service.internal.deletion

import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.internal.repository.DeletionDao;
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class RequirementDeletionHandlerImplTest extends Specification {
	RequirementDeletionHandlerImpl handler = new RequirementDeletionHandlerImpl()
	RequirementDeletionDao deletionDao = Mock()

	def setup() {
		handler.deletionDao = deletionDao
	}

	def "Issue 2767 : clashing names should be replaced with 'clashing name (n)'"() {
		given:
		Requirement deleted = new Requirement(new RequirementVersion())
		deleted.name = "deleted"

		Requirement renamed = new Requirement(new RequirementVersion())
		renamed.name = "clashing name"
		deleted.content << renamed

		NodeContainer parent = Mock()
		parent.accept(_) >> {it[0].visit(new RequirementFolder())}
		parent.isContentNameAvailable("clashing name") >> false
		parent.isContentNameAvailable(_) >> true
		parent.contentNames >> ["whatever name", "clashing name"]

		and:
		OperationReport report = Mock()

		when:
		handler.renameContentIfNeededThenAttach parent, [renamed], report

		then:
		renamed.name == "clashing name (1)"
	}

}
