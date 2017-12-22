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
package org.squashtest.tm.web.internal.controller.generic

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.api.security.acls.AccessRule;
import org.squashtest.tm.api.widget.MenuItem;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.web.internal.controller.campaign.CampaignWorkspaceController
import org.squashtest.tm.web.internal.controller.requirement.RequirementWorkspaceController;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseWorkspaceController;
import org.squashtest.tm.web.internal.plugins.manager.wizard.WorkspaceWizardManager;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class WorkspaceControllerTest extends Specification {
	WorkspaceWizardManager wizardManager = Mock()
	
	@Unroll
	def "#controller should return JSON'd workspace wizards menu items"() {
		given:
		injectDependencies controller

		AccessRule rule = new AccessRule() {};
		
		WorkspaceWizard w1 = Mock()
		w1.getId() >> "gdlf"
		MenuItem m1 = Mock()
		m1.getLabel() >> "gandalf"
		m1.getTooltip() >> "the grey sorcerer"
		m1.getUrl() >> "middle-earth"
		m1.getAccessRule() >> rule
		w1.getWizardMenu() >> m1
		
		WorkspaceWizard w2 = Mock()
		w2.getId() >> "grcm"
		MenuItem m2 = Mock()
		m2.getLabel() >> "garcimore"
		m2.getTooltip() >> "ptet ca marche"
		m2.getUrl() >> "tf1"
		m2.getAccessRule() >> rule
		w2.getWizardMenu() >> m2

		wizardManager.findAllByWorkspace(_) >> [w1, w2]
		
		when:
		def res = controller.getWorkspaceWizards()
		
		then:
		res*.id == ["gdlf", "grcm"]
		res*.label == ["gandalf", "garcimore"]
		res*.tooltip == ["the grey sorcerer", "ptet ca marche"]
		res*.url == ["middle-earth", "tf1"]
		res*.accessRule == [rule, rule]
		
		where: 
		controller << [new CampaignWorkspaceController(), new TestCaseWorkspaceController(), new RequirementWorkspaceController()]
	} 

	private injectDependencies(controller) {
		use (ReflectionCategory) {
			WorkspaceController.set field: "workspaceWizardManager", of: controller, to: wizardManager
		}
	}
	
	@Unroll
	def "#controller should return no workspace wizards menu items"() {
		given:
		injectDependencies controller

		List wizards = [] 
		wizardManager.findAllByWorkspace(_) >> wizards
		
		when:
		def res = controller.workspaceWizards
		
		then:
		res == []

		where: 
		controller << [new CampaignWorkspaceController(), new TestCaseWorkspaceController(), new RequirementWorkspaceController()]
	} 
	
}
