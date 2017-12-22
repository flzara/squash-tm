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
package org.squashtest.tm.web.internal.wizard;

import org.squashtest.tm.api.plugin.EntityReference;
import org.squashtest.tm.api.widget.MenuItem;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType
import org.squashtest.tm.web.internal.plugins.manager.wizard.WorkspaceWizardManagerImpl;

import static org.squashtest.tm.api.workspace.WorkspaceType.*;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.service.project.GenericProjectFinder;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class WorkspaceWizardManagerImplTest extends Specification {

	WorkspaceWizardManagerImpl manager
	GenericProjectFinder pfinder

	GenericProject project = Mock()
	TestCaseLibrary tclib = Mock()
	RequirementLibrary rlib = Mock()
	CampaignLibrary clib = Mock()


	def setup(){
		manager = new WorkspaceWizardManagerImpl()
		pfinder = Mock()
		initProjectFinder()
		manager.projectFinder = pfinder;
	}

	def "find all should never return null"() {
		expect:
		manager.findAllByWorkspace(WorkspaceType.CAMPAIGN_WORKSPACE) != null
	}


	def "should find all #workspace wizards"() {
		given:
		registerAll([new WorkspaceWizard() {
					String getId() {
						"campaign"
					}
					WorkspaceType getDisplayWorkspace() {
						CAMPAIGN_WORKSPACE
					}
					MenuItem getWizardMenu() {

					}
					String getName() {

					}
					String getVersion(){
						return "1.0"
					}
					String getFilename(){
						return "myjar"
					}
					void validate(EntityReference ref, Map<String, String> conf){

					}
					void validate(EntityReference ref){

					}
					String getType(){
						return "R"
					}
					String getConfigurationPath(EntityReference ref){
						return "/toto"
					}
					Map getProperties() { return [:]}
					String getModule(){
						return "module";
					}
				}, new WorkspaceWizard() {
					String getId() {
						"requirement"
					}
					WorkspaceType getDisplayWorkspace() {
						REQUIREMENT_WORKSPACE
					}
					MenuItem getWizardMenu() {

					}
					String getName() {

					}
					String getVersion(){
						return "1.0"
					}
					String getFilename(){
						return "myjar"
					}
					void validate(EntityReference ref, Map<String, String> conf){

					}
					void validate(EntityReference ref){

					}
					Map getProperties() { return [:]}
					String getType(){
						return "safe"
					}
					String getConfigurationPath(EntityReference ref){
						return "/toto"
					}
					String getModule(){
						return "module";
					}
				}, new WorkspaceWizard() {
					String getId() {
						"test case"
					}
					WorkspaceType getDisplayWorkspace() {
						TEST_CASE_WORKSPACE
					}
					MenuItem getWizardMenu() {

					}
					String getName() {

					}
					String getVersion(){
						return "1.0"
					}
					String getFilename(){
						return "myjar"
					}
					void validate(EntityReference ref, Map<String, String> conf){

					}
					void validate(EntityReference ref){

					}
					String getType(){
						return "R"
					}
					String getConfigurationPath(EntityReference ref){
						return "/toto"
					}
					Map getProperties() { return [:]}
					String getModule(){
						return "module";
					}
				}])

		when:
		def res = manager.findAllByWorkspace(workspace)

		then:
		res*.id == [ id ]

		where:
		workspace                           | id
		TEST_CASE_WORKSPACE   				| "test case"
		REQUIREMENT_WORKSPACE 				| "requirement"
		CAMPAIGN_WORKSPACE    				| "campaign"
	}



	def "should find the enabled wizards for test case library"(){

		given :
		def allwizs = batchMockWizards()
		registerAll(allwizs)
		enableTCWizs(["alpha-test-case", "charlie-test-case"])

		when :
		def res = manager.findEnabledWizards(14l, TEST_CASE_WORKSPACE)

		then :
		res.collect{ return [ it.getId(), it.getDisplayWorkspace()] as Set } as Set ==
		[
			["alpha-test-case", TEST_CASE_WORKSPACE] as Set,
			["charlie-test-case", TEST_CASE_WORKSPACE] as Set,
		] as Set
	}

	def "should find all the enabled wizards"(){

		given :
		def allwizs = batchMockWizards()
		registerAll(allwizs)
		enableTCWizs(["alpha-test-case"])
		enableRWizs(["bravo-requirement"])
		enableCWizs(["charlie-campaign"])

		when :
		def res = manager.findEnabledWizards(14l)

		then :
		res.collect{ return [ it.getId(), it.getDisplayWorkspace()] as Set } as Set ==
		[
			["alpha-test-case", TEST_CASE_WORKSPACE] as Set,
			["bravo-requirement", REQUIREMENT_WORKSPACE] as Set,
			["charlie-campaign", CAMPAIGN_WORKSPACE] as Set
		] as Set
	}

	def "should find the disabled wizards for test case library"(){

		given :
		def allwizs = batchMockWizards()
		registerAll(allwizs)
		enableTCWizs(["alpha-test-case", "charlie-test-case"])

		when :
		def res = manager.findDisabledWizards(14l, TEST_CASE_WORKSPACE)

		then :
		res.collect{ return [ it.getId(), it.getDisplayWorkspace()] as Set } as Set ==
		[
			["bravo-test-case", TEST_CASE_WORKSPACE] as Set
		] as Set
	}

	def "should find all the disabled wizards"(){

		given :
		def allwizs = batchMockWizards()
		registerAll(allwizs)
		enableTCWizs(["alpha-test-case"])
		enableRWizs(["bravo-requirement"])
		enableCWizs(["charlie-campaign"])

		when :
		def res = manager.findDisabledWizards(14l)

		then :
		res.collect{ return [ it.getId(), it.getDisplayWorkspace()] as Set } as Set ==
		[
			["bravo-test-case", TEST_CASE_WORKSPACE] as Set,
			["charlie-test-case", TEST_CASE_WORKSPACE] as Set,
			["alpha-requirement", REQUIREMENT_WORKSPACE] as Set,
			["charlie-requirement", REQUIREMENT_WORKSPACE] as Set,
			["alpha-campaign", CAMPAIGN_WORKSPACE] as Set,
			["bravo-campaign", CAMPAIGN_WORKSPACE] as Set
		] as Set
	}



	// *************** utilities *****************************

	def mockWizard = { id, type ->
		WorkspaceWizard wizard = Mock()
		wizard.getId() >> id
		wizard.getDisplayWorkspace() >> type
		return wizard
	}

	def fromwktypename = {
		if (it.equals("test-case")) 	return TEST_CASE_WORKSPACE
		if (it.equals("requirement")) 	return REQUIREMENT_WORKSPACE
		if (it.equals("campaign")) 		return CAMPAIGN_WORKSPACE
	}

	def batchMockWizards = {
		def allwizzs = []
		["alpha", "bravo", "charlie"].each{ id ->
			["test-case", "requirement", "campaign"].each{ wkp ->
				allwizzs << mockWizard("$id-$wkp",fromwktypename(wkp))
			}
		}
		return allwizzs
	}

	def registerAll(ws) {
		manager.wizards = ws
		manager.registerWizards()
	}


	def initProjectFinder(){

		project = Mock()
		tclib = Mock()
		rlib = Mock()
		clib = Mock()

		project.getTestCaseLibrary() >> tclib
		project.getRequirementLibrary() >> rlib
		project.getCampaignLibrary() >> clib

		tclib.getEnabledPlugins()
		rlib.getEnabledPlugins()
		clib.getEnabledPlugins()

		pfinder.findById(_) >> project
	}


	def enableTCWizs(wizIds){
		tclib.getEnabledPlugins() >> wizIds
	}

	def enableRWizs(wizIds){
		rlib.getEnabledPlugins() >> wizIds
	}

	def enableCWizs(wizIds){
		clib.getEnabledPlugins() >> wizIds
	}

}
