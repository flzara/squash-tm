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

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;

/**
 * Interface for an object which can visit a functional node container including :
 * <ul>
 *  <li>{@linkplain CampaignLibrary}</li>
 * <li>{@linkplain RequirementLibrary}</li>
 * <li>{@linkplain TestCaseLibrary}</li>
 * <li>{@linkplain CampaignFolder}</li>
 * <li>{@linkplain RequirementFolder}</li>
 * <li>{@linkplain TestCaseFolder}</li>
 * <li>{@linkplain Campaign}</li>
 * <li>{@linkplain Iteration}</li>
 * </ul>
 * . GoF pattern Visitor.
 * 
 * @author mpagnon
 * 
 */
public interface NodeContainerVisitor {
	
	void visit(CampaignLibrary campaignLibrary);
	
	void visit(RequirementLibrary requirementLibrary);
	
	void visit(TestCaseLibrary testCaseLibrary);

	void visit(CampaignFolder campaignFolder);

	void visit(RequirementFolder requirementFolder);

	void visit(TestCaseFolder testCaseFolder);

	void visit(Campaign campaign);

	void visit(Iteration iteration);
	
	void visit(Requirement requirement);
}
