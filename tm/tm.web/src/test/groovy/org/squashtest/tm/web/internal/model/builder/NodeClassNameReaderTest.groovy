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
package org.squashtest.tm.web.internal.model.builder;

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;

import spock.lang.Specification;
import spock.lang.Unroll;


/**
 * @author Gregory Fouquet
 *
 */
class NodeClassNameReaderTest extends Specification {
	NodeClassNameReader visitor = new NodeClassNameReader()

	@Unroll
	def "should return name #shortName for class #klass"() {
		given:
		def subject = Mock(klass)

		when:
		visitor.visit(subject)

		then:
		visitor.getSimpleName() == shortName

		where:
		klass | shortName
		CampaignLibrary | "CampaignLibrary"
		RequirementLibrary | "RequirementLibrary"
		TestCaseLibrary | "TestCaseLibrary"
		CampaignFolder | "CampaignFolder"
		RequirementFolder | "RequirementFolder"
		TestCaseFolder | "TestCaseFolder"
		Campaign | "Campaign"
		Iteration | "Iteration"
		TestSuite | "TestSuite"
		Requirement | "Requirement"
		TestCase | "TestCase"
	}
}
