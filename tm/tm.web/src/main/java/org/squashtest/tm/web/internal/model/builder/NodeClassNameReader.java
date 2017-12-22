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
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;

/**
 * This visitor determines actual class of a tree node, which can be some kind of hibernate proxy.<br />
 * 
 * A tree node is either an instance of TreeNode or NodeContainer<br />
 * 
 * It can be used in favor of the SelfClassAware interface, in particular when the node can be an Iteration (not
 * SelfClassAware) or when the actual type of the tree node is not known (ie generics)
 * 
 * @author Gregory Fouquet
 * 
 */
class NodeClassNameReader implements NodeVisitor, NodeContainerVisitor {
	private Class<?> readClass;

	/**
	 * 
	 */
	public NodeClassNameReader() {
		super();
	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeContainerVisitor#visit(org.squashtest.tm.domain.campaign.CampaignLibrary)
	 */
	@Override
	public void visit(CampaignLibrary campaignLibrary) {
		readClass = CampaignLibrary.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeContainerVisitor#visit(org.squashtest.tm.domain.requirement.RequirementLibrary)
	 */
	@Override
	public void visit(RequirementLibrary requirementLibrary) {
		readClass = RequirementLibrary.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeContainerVisitor#visit(org.squashtest.tm.domain.testcase.TestCaseLibrary)
	 */
	@Override
	public void visit(TestCaseLibrary testCaseLibrary) {
		readClass = TestCaseLibrary.class;
	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeVisitor#visit(org.squashtest.tm.domain.campaign.CampaignFolder)
	 */
	@Override
	public void visit(CampaignFolder campaignFolder) {
		readClass = CampaignFolder.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeVisitor#visit(org.squashtest.tm.domain.requirement.RequirementFolder)
	 */
	@Override
	public void visit(RequirementFolder requirementFolder) {
		readClass = RequirementFolder.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCaseFolder)
	 */
	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		readClass = TestCaseFolder.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeVisitor#visit(org.squashtest.tm.domain.campaign.Campaign)
	 */
	@Override
	public void visit(Campaign campaign) {
		readClass = Campaign.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeVisitor#visit(org.squashtest.tm.domain.campaign.Iteration)
	 */
	@Override
	public void visit(Iteration iteration) {
		readClass = Iteration.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeVisitor#visit(org.squashtest.tm.domain.campaign.TestSuite)
	 */
	@Override
	public void visit(TestSuite testSuite) {
		readClass = TestSuite.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeVisitor#visit(org.squashtest.tm.domain.requirement.Requirement)
	 */
	@Override
	public void visit(Requirement requirement) {
		readClass = Requirement.class;

	}

	/**
	 * @see org.squashtest.tm.domain.library.NodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCase)
	 */
	@Override
	public void visit(TestCase testCase) {
		readClass = TestCase.class;

	}

	public String getSimpleName() {
		return readClass.getSimpleName();
	}

	public String getQualifiedName() {
		return readClass.getName();
	}
}
