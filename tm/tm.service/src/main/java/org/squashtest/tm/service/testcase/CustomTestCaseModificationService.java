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
package org.squashtest.tm.service.testcase;

import org.squashtest.tm.domain.bdd.Keyword;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseAutomatable;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;


/**
 * Test-Case modification services which cannot be dynamically generated.
 * @author Gregory Fouquet
 *
 */
public interface CustomTestCaseModificationService extends CustomTestCaseFinder {

	String TEST_CASE_IS_WRITABLE = "hasPermission(#arg0, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE')" + OR_HAS_ROLE_ADMIN;

	String getPrerequisite(long testCaseId);

	void rename(long testCaseId, String newName);

	void changeReference(long testCaseId, String reference);

	void changeSourceCodeRepositoryUrl(long testCaseId, String sourceCodeRepositoryUrl);

	void changeAutomatedTestReference(long testCaseId, String automatedTestReference);

	void changeAutomatedTestTechnology(long testCaseId, long automatedTestTechnologyId);

	void changeImportance(long testCaseId, TestCaseImportance importance);

	/**
	 * Inserts the given {@link KeywordTestStep} in last position of the {@link TestCase} identified by the given id.
	 * @param parentTestCaseId The id of the parent TestCase
	 * @param keyword The new step Keyword
	 * @param actionWord The new step ActionWord
	 * @return The created KeywordTestStep
	 */
	KeywordTestStep addKeywordTestStep(@Id long parentTestCaseId, @NotNull String keyword, @NotNull String actionWord);

	/**
	 * Inserts the given {@link KeywordTestStep} at the index value of the {@link TestCase} identified by the given id.
	 * @param parentTestCaseId The id of the parent TestCase
	 * @param keyword The new step Keyword
	 * @param actionWord The new step ActionWord
	 * @param index Position of the keywordTestStep in the testCase
	 * @return The created KeywordTestStep
	 */
	KeywordTestStep addKeywordTestStep(@Id long parentTestCaseId, @NotNull String keyword, @NotNull String actionWord, int index);

	/**
	 * Inserts the given {@link KeywordTestStep} at the index value of the {@link TestCase} identified by the given id, with the given action word id.
	 * @param parentTestCaseId The id of the parent TestCase
	 * @param keyword The new step Keyword
	 * @param actionWord The step ActionWord
	 * @param actionWordId The step ActionWord id to bind the keyword test step
	 * @param index Position of the keywordTestStep in the testCase
	 * @return The created KeywordTestStep
	 */
	KeywordTestStep addKeywordTestStep(@Id long parentTestCaseId, @NotNull String keyword, @NotNull String actionWord, @NotNull Long actionWordId, int index);

	/**
	 * Inserts the given {@link KeywordTestStep} inserted at the index value in the {@link TestCase} identified by the given id.
	 * @param parentTestCaseId The id of the parent TestCase
	 * @param newTestStep The new KeywordTestStep
	 * @param index Position of the keywordTestStep in the testCase
	 * @return The created KeywordTestStep
	 */
	KeywordTestStep addKeywordTestStep(@Id long parentTestCaseId, KeywordTestStep newTestStep, int index);

	/**
	 * Inserts the given {@link KeywordTestStep} in last position of the {@link TestCase} identified by the given id.
	 * @param parentTestCaseId The id of the parent TestCase
	 * @param newTestStep The new KeywordTestStep
	 * @return
	 */
	KeywordTestStep addKeywordTestStep(@Id long parentTestCaseId, KeywordTestStep newTestStep);

	/**
	 * Updates the {@link KeywordTestStep} of given id with new values from the given {@link KeywordTestStep}
	 * @param testStepId The id of the KeywordTestStep to update
	 * @param updatedKeywordTestStep The updated KeywordTestStep
	 */
	void updateKeywordTestStep(long testStepId, KeywordTestStep updatedKeywordTestStep);

	/**
	 * Updates the {@link KeywordTestStep} of given id with given {@link Keyword}
	 * @param testStepId The id of the KeywordTestStep to update
	 * @param updatedKeyword The updated Keyword
	 */
	void updateKeywordTestStep(long testStepId, Keyword updatedKeyword);

	/**
	 * Updates the {@link KeywordTestStep} of given id with given datatable
	 * @param testStepId The id of the KeywordTestStep to update
	 * @param updatedDatatable The updated datatable
	 */
	void updateKeywordTestStepDatatable(long testStepId, String updatedDatatable);

	/**
	 * Updates the {@link KeywordTestStep} of given id with given docstring
	 * @param testStepId The id of the KeywordTestStep to update
	 * @param updatedDocstring The updated docstring
	 */
	void updateKeywordTestStepDocstring(long testStepId, String updatedDocstring);

	/**
	 * Updates the {@link KeywordTestStep} of given id with given comment
	 * @param testStepId The id of the KeywordTestStep to update
	 * @param updatedComment The updated comment
	 */
	void updateKeywordTestStepComment(long testStepId, String updatedComment);

	/**
	 * Updates the {@link KeywordTestStep} of given id with given Word
	 * @param testStepId The id of the KeywordTestStep to update
	 * @param updatedWord The updated Word of the ActionWord
	 */
	void updateKeywordTestStep(long testStepId, String updatedWord);

	/**
	 * Updates the {@link KeywordTestStep} of given id with given Word and given action word id
	 * @param testStepId The id of the KeywordTestStep to update
	 * @param updatedWord The updated Word of the ActionWord
	 * @param actionWordId The given action word id to bind the KeywordTestStep
	 */
	void updateKeywordTestStep(long testStepId, @NotNull String updatedWord, long actionWordId);

	/**
	 * Inserts the given step in last position of the test script of the test case identified by the given id.
	 *
	 * @param parentTestCaseId
	 * @param newTestStep
	 * @return
	 */
	ActionTestStep addActionTestStep(@Id long parentTestCaseId, ActionTestStep newTestStep);

	/**
	 * Inserts the given step at specified index in the test script of the test case identified by the given id. If position is
	 * negative, the step will be inserted in last position instead.
	 *
	 * @param parentTestCaseId
	 * @param newTestStep
	 * @param index
	 * @return
	 */
	ActionTestStep addActionTestStep(@Id long parentTestCaseId, ActionTestStep newTestStep, int index);

	/**
	 * Adds an action test step to a test case, and its initial custom field values.
	 * The initial custom field values are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 *
	 * @param customFieldValues
	 */
	ActionTestStep addActionTestStep(@Id long parentTestCaseId, ActionTestStep newTestStep, Map<Long, RawValue> customFieldValues);

	/**
	 * Adds an action test step to a test case, and its initial custom field values, inserted at the index value
	 * The initial custom field values are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 *
	 * @param customFieldValues
	 * @param index
	 */
	ActionTestStep addActionTestStep(@Id long parentTestCaseId, ActionTestStep newTestStep, Map<Long, RawValue> customFieldValues,int index);

	void updateTestStepAction(long testStepId, String newAction);

	void updateTestStepExpectedResult(long testStepId, String newExpectedResult);

	/**
	 * @deprecated does not seem to be used any longer
	 */
	@Deprecated
	void changeTestStepPosition(@Id long testCaseId, long testStepId, int newStepPosition);

	/**
	 * Will move a list of steps to a new position.
	 *
	 * @param testCaseId
	 *            the id of the test case
	 * @param newPosition
	 *            the position we want the first element of movedSteps to be once the operation is complete
	 * @param stepIds
	 *            the list of steps to move, sorted by rank among each others.
	 */
	void changeTestStepsPosition(@Id long testCaseId, int newPosition, List<Long> stepIds);

	void removeStepFromTestCase(@Id long testCaseId, long testStepId);

	void removeStepFromTestCaseByIndex(@Id long testCaseId, int stepIndex);

	List<TestStep> removeListOfSteps(@Id long testCaseId, List<Long> testStepIds);

	/**
	 * will insert a test step into a test case script, after the step identified by idInsertion.
	 * If the copied step id a call step the method returns true, or false when it is a regular step.
	 *
	 * @param testCaseId
	 *            the id of the test case.
	 * @param idInsertion
	 *            the id of the step after which we'll insert the copy of a step
	 * @param copiedTestStepId
	 *            the id of the testStep to copy.
	 *
	 * @return true if the copied step is instance of CallStep, false otherwise
	 *
	 */
	boolean pasteCopiedTestStep(@Id long testCaseId, long idInsertion, long copiedTestStepId);

	/**
	 * will insert a test step into a test case script, at the last position.
	 * If the copied step id a call step the method returns true, or false when it is a regular step.
	 *
	 * @param testCaseId
	 *            the id of the test case.
	 * @param copiedTestStepId
	 *            the id of the testStep to copy.
	 *
	 * @return true if the copied step is instance of CallStep, false otherwise
	 *
	 */
	boolean pasteCopiedTestStepToLastIndex(@Id long testCaseId, long copiedTestStepId);


	/**
	 * Same as {@link #pasteCopiedTestStep(long, long, long)}, accepting a list of step ids
	 *
	 * @param testCaseId
	 * @param idInsertion
	 * @param copiedTestStepIds
	 * @return
	 */
	boolean pasteCopiedTestSteps(@Id long testCaseId, long idInsertion, List<Long> copiedTestStepIds);


	/**
	 * Same as {@link #pasteCopiedTestStepToLastIndex(long, long)}, accepting a list of step ids
	 *
	 * @param testCaseId
	 * @param copiedTestStepIds
	 * @return
	 */
	boolean pasteCopiedTestStepToLastIndex(@Id long testCaseId, List<Long> copiedTestStepIds);


	/**
	 * will change the test case importance too if auto is true.
	 *
	 * @param testCaseId
	 * @param auto
	 */
	void changeImportanceAuto(long testCaseId, boolean auto);



	/**
	 * Will create a new version of a test case and insert it next to it. It's basically a cheap copy where the test
	 * steps, attachments, parameters and custom fields are the only elements duplicated, and some other properties are
	 * overriden by the content of newTestCase. The verified requirements are left out.
	 *
	 * @return the newly created test case version
	 * @param originalTcId
	 * @param newVersionData
	 */
	public TestCase addNewTestCaseVersion(long originalTcId, TestCase newVersionData);

	public void addParametersFromPrerequisite(long testCaseId);

	// *************** test automation section ******************

	Collection<TestAutomationProjectContent> findAssignableAutomationTests(long testCaseId);


	AutomatedTest bindAutomatedTest(Long testCaseId, Long taProjectId, String testName);

	/**
	 * Essentially the same than {@link #bindAutomatedTest(Long, Long, String)}. The single argument (the testPath) is the concatenation
	 * of the TA project <b>label</b> and the test name.
	 *
	 * @param testCaseId
	 * @param testPath
	 * @return
	 */
	AutomatedTest bindAutomatedTest(Long testCaseId, String testPath);

	/**
	 * Will delete the link
	 * @param testCaseId
	 */
	void removeAutomation(long testCaseId);

	void changeNature(long testCaseId, String natureCode);

	void changeType(long testCaseId, String typeCode);


	/* ********************** milestones section ******************* */

	void bindMilestones(long testCaseId, Collection<Long> milestoneIds);

	void unbindMilestones(long testCaseId, Collection<Long> milestoneIds);

	Collection<Milestone> findAssociableMilestones(long testCaseId);

	Collection<Milestone> findAssociableMilestonesForMassModif(List<Long> testCaseIds);


	Collection<Long> findBindedMilestonesIdForMassModif(List<Long> testCaseIds);

	boolean haveSamePerimeter(List<Long> testCaseIds);

	boolean changeAutomatable(TestCaseAutomatable automatable, Long testCaseId);

	Map<String, Object> transmitEligibleNodes(Map<String, List<Long>> selectedNodes);


	// ********************* Automation request *********************** */

	void createRequestForTestCase(long testCaseId, AutomationRequestStatus automationRequestStatus);

	AutomatedTest bindAutomatedTestByAutomationProgrammer(Long testCaseId, String testPath);

	/**
	 * Same than {@link #bindAutomatedTest(Long, Long, String)}, minus the hibernate PreAuthorize annotation check.
	 * @param testCaseId
	 * @param taProjectId
	 * @param testName
	 * @return
	 */
	AutomatedTest bindAutomatedTestAutomatically(Long testCaseId, Long taProjectId, String testName);

	Collection<TestAutomationProjectContent> findAssignableAutomationTestsToAutomationProgramer(long testCaseId);

}
