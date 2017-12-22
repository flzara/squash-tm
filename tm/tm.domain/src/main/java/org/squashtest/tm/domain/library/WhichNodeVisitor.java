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
package org.squashtest.tm.domain.library;

import org.squashtest.tm.domain.EntityType;
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

/**
 * Look, enough of that visitor logic b*******
 * 
 * @author bsiri
 */
public class WhichNodeVisitor implements NodeContainerVisitor, NodeVisitor {

	private EntityType nodeType = null;

	public <X extends NodeContainer<?>> EntityType getTypeOf(X container ){
		nodeType = null;
		container.accept(this);
		return nodeType;
	}

	public <X extends TreeNode> EntityType getTypeOf(X node){
		nodeType = null;
		node.accept(this);
		return nodeType;
	}

	@Override
	public void visit(CampaignLibrary campaignLibrary) {
		nodeType = EntityType.CAMPAIGN_LIBRARY;
	}

	@Override
	public void visit(RequirementLibrary requirementLibrary) {
		nodeType = EntityType.REQUIREMENT_LIBRARY;
	}

	@Override
	public void visit(TestCaseLibrary testCaseLibrary) {
		nodeType = EntityType.TEST_CASE_LIBRARY;
	}

	@Override
	public void visit(CampaignFolder campaignFolder) {
		nodeType = EntityType.CAMPAIGN_FOLDER;
	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		nodeType = EntityType.REQUIREMENT_FOLDER;
	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		nodeType = EntityType.TEST_CASE_FOLDER;
	}

	@Override
	public void visit(Campaign campaign) {
		nodeType = EntityType.CAMPAIGN;
	}

	@Override
	public void visit(Iteration iteration) {
		nodeType = EntityType.ITERATION;
	}

	@Override
	public void visit(Requirement requirement) {
		nodeType = EntityType.REQUIREMENT;
	}

	@Override
	public void visit(TestSuite testSuite) {
		nodeType = EntityType.TEST_SUITE;
	}

	@Override
	public void visit(TestCase testCase) {
		nodeType = EntityType.TEST_CASE;
	}



}
