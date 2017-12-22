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
package org.squashtest.tm.domain.campaign;


import java.util.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.library.*;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneMember;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.LevelEnumBridge;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Auditable
@Entity
public class Iteration implements AttachmentHolder, NodeContainer<TestSuite>, TreeNode, Copiable, Identified,
	BoundEntity, MilestoneMember {
	private static final String ITERATION_ID = "ITERATION_ID";
	public static final int MAX_REF_SIZE = 50;

	@Id
	@Column(name = ITERATION_ID)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "iteration_iteration_id_seq")
	@SequenceGenerator(name = "iteration_iteration_id_seq", sequenceName = "iteration_iteration_id_seq", allocationSize = 1)
	private Long id;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	@Field(analyze = Analyze.NO, store = Store.YES)
	private String name;

	@NotNull
	@Size(max = MAX_REF_SIZE)
	private String reference = "";

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "ITERATION_STATUS")
	@Field(analyze = Analyze.NO, store = Store.YES)
	@FieldBridge(impl = LevelEnumBridge.class)
	private IterationStatus status = IterationStatus.PLANNED;

	@Embedded @Valid
	private ScheduledTimePeriod scheduledPeriod = new ScheduledTimePeriod();

	@Embedded
	@Valid
	private final ActualTimePeriod actualPeriod = new ActualTimePeriod();

	/*
	 * read http://docs.redhat.com/docs/en-US/JBoss_Enterprise_Web_Platform/5/html
	 * /Hibernate_Annotations_Reference_Guide /entity-mapping-association-collection-onetomany.html
	 *
	 * "To map a bidirectional one to many, with the one-to-many side as the owning side, you have to remove the
	 * mappedBy element and set the many to one @JoinColumn as insertable and updatable to false. This solution is
	 * obviously not optimized and will produce some additional UPDATE statements."
	 *
	 * The reason for this is because Hibernate doesn't support the correct mapping (using mappingBy and @OrderColumns).
	 * The solution used here is only a workaround.
	 *
	 * See bug HHH-5390 for a concise discussion about this.
	 */

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "CAMPAIGN_ITERATION", joinColumns = @JoinColumn(name = ITERATION_ID, updatable = false, insertable = false), inverseJoinColumns = @JoinColumn(name = "CAMPAIGN_ID", updatable = false, insertable = false))
	private Campaign campaign;

	/*
	 * FIXME TEST_PLAN might be a little more appropriate. Don't forget to fix the hql/criteria queries as well
	 */
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@OrderColumn(name = "ITEM_TEST_PLAN_ORDER")
	@JoinTable(name = "ITEM_TEST_PLAN_LIST", joinColumns = @JoinColumn(name = ITERATION_ID), inverseJoinColumns = @JoinColumn(name = "ITEM_TEST_PLAN_ID"))
	private final List<IterationTestPlanItem> testPlans = new ArrayList<>();

	/* *********************** attachment attributes ************************ */

	@OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	/* *********************** Test suites ********************************** */

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "ITERATION_TEST_SUITE", joinColumns = @JoinColumn(name = ITERATION_ID), inverseJoinColumns = @JoinColumn(name = "TEST_SUITE_ID"))
	private List<TestSuite> testSuites = new ArrayList<>();

	/**
	 * flattened list of the executions
	 */
	public List<Execution> getExecutions() {
		List<Execution> listExec = new ArrayList<>();
		for (IterationTestPlanItem testplan : testPlans) {
			listExec.addAll(testplan.getExecutions());
		}

		return listExec;
	}

	@Override
	public void setName(String name) {
		this.name = name.trim();
	}

	@Override
	@NotBlank
	public String getName() {
		return this.name;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	/**
	 * @return {reference} - {name} if reference is not empty, or {name} if it is
	 */
	public String getFullName() {
		if (StringUtils.isBlank(reference)) {
			return getName();
		} else {
			return getReference() + " - " + getName();
		}
	}

	public IterationStatus getStatus() {
		return status;
	}

	public void setStatus(@NotNull IterationStatus status) {
		this.status = status;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setScheduledStartDate(Date startDate) {
		getScheduledPeriod().setScheduledStartDate(startDate);
	}

	public Date getScheduledStartDate() {
		return getScheduledPeriod().getScheduledStartDate();
	}

	public void setScheduledEndDate(Date endDate) {
		getScheduledPeriod().setScheduledEndDate(endDate);
	}

	public Date getScheduledEndDate() {
		return getScheduledPeriod().getScheduledEndDate();
	}

	public void setActualStartDate(Date startDate) {
		actualPeriod.setActualStartDate(startDate);
		if (getCampaign() != null) {
			getCampaign().updateActualStart(startDate);
		}
	}

	public Date getActualStartDate() {
		return actualPeriod.getActualStartDate();
	}

	public void setActualEndDate(Date endDate) {
		actualPeriod.setActualEndDate(endDate);
		if (getCampaign() != null) {
			getCampaign().updateActualEnd(endDate);
		}
	}

	public Date getActualEndDate() {
		return actualPeriod.getActualEndDate();
	}

	public boolean isActualStartAuto() {
		return actualPeriod.isActualStartAuto();
	}

	public boolean isActualEndAuto() {
		return actualPeriod.isActualEndAuto();
	}

	public void setActualStartAuto(boolean actualStartAuto) {
		actualPeriod.setActualStartAuto(actualStartAuto);

		if (actualPeriod.isActualStartAuto()) {
			autoSetActualStartDate();
		}
	}

	public void setActualEndAuto(boolean actualEndAuto) {
		actualPeriod.setActualEndAuto(actualEndAuto);

		if (actualPeriod.isActualEndAuto()) {
			autoSetActualEndDate();
		}

	}

	@Override
	public Long getId() {
		return id;
	}

	private ScheduledTimePeriod getScheduledPeriod() {
		// Hibernate workaround : when STP fields are null, component is set to
		// null
		if (scheduledPeriod == null) {
			scheduledPeriod = new ScheduledTimePeriod();
		}
		return scheduledPeriod;
	}

	/**
	 * <p>
	 * copy of iteration <u>doesn't contain test-suites</u> !!<br>
	 * </p>
	 *
	 * @return
	 */
	@Override
	public Iteration createCopy() {
		Iteration clone = new Iteration();
		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		clone.setReference(this.getReference());
		copyPlanning(clone);
		for (IterationTestPlanItem itemTestPlan : testPlans) {
			clone.addTestPlan(itemTestPlan.createCopy());

		}
		for (Attachment attach : this.getAttachmentList().getAllAttachments()) {
			Attachment copyAttach = attach.hardCopy();
			clone.getAttachmentList().addAttachment(copyAttach);
		}

		return clone;
	}

	/**
	 * copy planning info: <br>
	 * if actual end/start is auto => don't copy the actual date.
	 *
	 * @param clone
	 */
	private void copyPlanning(Iteration clone) {
		clone.setActualEndAuto(this.isActualEndAuto());
		clone.setActualStartAuto(this.isActualStartAuto());

		if (this.getScheduledStartDate() != null) {
			clone.setScheduledStartDate((Date) this.getScheduledStartDate().clone());
		}
		if (this.getScheduledEndDate() != null) {
			clone.setScheduledEndDate((Date) this.getScheduledEndDate().clone());
		}

	}

	/*
	 * **************************************** TEST PLAN ****************************************************
	 */

	public List<IterationTestPlanItem> getTestPlans() {
		return testPlans;
	}

	// TODO rename plannedTestCase_s_
	// TODO return a Collection instead of a list
	public List<TestCase> getPlannedTestCase() {
		// FIXME (GRF) I think it's broken because it may return several times the same test case. Cannot fix without
		// checking side effects on campagne epargne wizard beforehand. Note : w wrote a test which i deactivated
		List<TestCase> list = new ArrayList<>(testPlans.size());

		for (IterationTestPlanItem iterTestPlan : testPlans) {
			TestCase testCase = iterTestPlan.getReferencedTestCase();
			if (testCase != null) {
				list.add(testCase);
			}
		}
		return list;
	}

	public void removeTestSuite(@NotNull TestSuite testSuite) {
		testSuites.remove(testSuite);
	}

	public void removeItemFromTestPlan(@NotNull IterationTestPlanItem testPlanItem) {
		testPlans.remove(testPlanItem);

		for (TestSuite testSuite : this.testSuites) {
			testSuite.getTestPlan().remove(testPlanItem);
		}
	}

	public void addTestPlan(@NotNull IterationTestPlanItem testPlan) {
		// TODO undocumented behaviour which silently breaks what the method is
		// supposed to do. gotta come up with something better
		if (testPlan.getReferencedTestCase() == null) {
			return;
		}
		testPlans.add(testPlan);
		testPlan.setIteration(this);
	}

	/***
	 * Method which returns the position of a test case in the current iteration
	 *
	 * @param testCaseId
	 *            the id of the test case we're looking for
	 * @return the position of the test case (int)
	 * @throws UnknownEntityException
	 *             if not found.
	 */
	public int findTestCaseIndexInTestPlan(long testCaseId) {
		ListIterator<IterationTestPlanItem> iterator = testPlans.listIterator();
		while (iterator.hasNext()) {
			IterationTestPlanItem itemTestPlan = iterator.next();

			if (!itemTestPlan.isTestCaseDeleted()
				&& itemTestPlan.getReferencedTestCase().getId().equals(testCaseId)) {
				return iterator.previousIndex();
			}
		}

		throw new UnknownEntityException(testCaseId, TestCase.class);

	}

	/***
	 * Method which returns the position of an item test plan in the current iteration
	 *
	 * @param testPlanId
	 *            the id of the test plan we're looking for
	 * @return the position of the test plan (int)
	 * @throws UnknownEntityException
	 *             if not found.
	 */
	public int findItemIndexInTestPlan(long testPlanId) {

		ListIterator<IterationTestPlanItem> iterator = testPlans.listIterator();
		while (iterator.hasNext()) {
			IterationTestPlanItem itemTestPlan = iterator.next();

			if (itemTestPlan.getId().equals(testPlanId)) {

				return iterator.previousIndex();
			}
		}

		throw new UnknownEntityException(testPlanId, IterationTestPlanItem.class);

	}

	/***
	 * Method which sets a test case at a new position
	 *
	 * @param currentPosition
	 *            the current position
	 * @param newPosition
	 *            the new position
	 */
	@Deprecated
	public void moveTestPlan(int currentPosition, int newPosition) {
		if (currentPosition == newPosition) {
			return;
		}

		IterationTestPlanItem testCaseToMove = testPlans.get(currentPosition);
		testPlans.remove(currentPosition);
		testPlans.add(newPosition, testCaseToMove);
	}

	public void moveTestPlans(int newIndex, List<IterationTestPlanItem> movedItems) {
		if (!testPlans.isEmpty()) {
			testPlans.removeAll(movedItems);
			testPlans.addAll(newIndex, movedItems);
		}
	}

	/* returns the index of that item if found, -1 if not found */
	public int getIndexOf(IterationTestPlanItem item) {

		int i = 0;

		for (IterationTestPlanItem testPlan : testPlans) {
			if (item.equals(testPlan)) {
				return i;
			}
			i++;
		}

		return -1;
	}

	/*
	 * ********************************* TEST SUITE *********************************************
	 */

	public List<TestSuite> getTestSuites() {
		return testSuites;
	}

	public TestSuite getTestSuiteByName(String tsName) {
		for (TestSuite ts : testSuites) {
			if (ts.getName().equals(tsName)) {
				return ts;
			}
		}

		throw new NoSuchElementException("Iteration " + id + " : cannot find test suite named '" + tsName + "'");
	}

	public void addTestSuite(TestSuite suite) {
		if (!checkSuiteNameAvailable(suite.getName())) {
			throw new DuplicateNameException("cannot add suite to iteration " + getName() + " : suite named "
				+ suite.getName() + " already exists");
		}
		testSuites.add(suite);
		suite.setIteration(this);
	}

	public void addTestSuite(TestSuite suite, int position) {
		if (!checkSuiteNameAvailable(suite.getName())) {
			throw new DuplicateNameException("cannot add suite to iteration " + getName() + " : suite named "
				+ suite.getName() + " already exists");
		}
		testSuites.add(position, suite);
		suite.setIteration(this);
	}

	public boolean checkSuiteNameAvailable(String name) {
		for (TestSuite suite : testSuites) {
			if (suite.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	public boolean hasTestSuites() {
		return !testSuites.isEmpty();
	}

	/*
	 * ********************************************** Attachable implementation
	 * ******************************************
	 */

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	@Override
	public Project getProject() {
		if (campaign != null) {
			return campaign.getProject();
		} else {
			return null;
		}
	}

	@AclConstrainedObject
	public CampaignLibrary getCampaignLibrary() {
		return getProject().getCampaignLibrary();
	}

	/*
	 * *********************************************** dates autosetting code
	 * ********************************************
	 */

	/**
	 * If the iteration have autodates set, they will be updated accordingly.
	 *
	 * @param newItemTestPlanDate
	 */
	public void updateAutoDates(Date newItemTestPlanDate) {

		if (isActualStartAuto()) {
			updateAutoDatesAcutalStart(newItemTestPlanDate);
		}
		// check also if the end end can be updated
		if (isActualEndAuto()) {
			updateAutoDatesActualEnd(newItemTestPlanDate);
		}

	}

	private void updateAutoDatesActualEnd(Date newItemTestPlanDate) {
		if (actualEndDateUpdateAuthorization()) {
			// if we're lucky we can save a heavier computation
			if (getActualEndDate() == null) {
				setActualEndDate(newItemTestPlanDate);
			} else if (newItemTestPlanDate != null && getActualEndDate().compareTo(newItemTestPlanDate) < 0) {
				setActualEndDate(newItemTestPlanDate);
			}

			// well too bad, we have to recompute that.
			else {
				autoSetActualEndDateNoCheck();
			}
		} else {
			setActualEndDate(null);
		}
	}

	private void updateAutoDatesAcutalStart(Date newItemTestPlanDate) {
		// if we're lucky we can save a heavier computation
		if (getActualStartDate() == null) {
			setActualStartDate(newItemTestPlanDate);
		} else if (newItemTestPlanDate != null && getActualStartDate().compareTo(newItemTestPlanDate) > 0) {
			setActualStartDate(newItemTestPlanDate);
		}

		// well too bad, we have to recompute that.
		else {
			autoSetActualStartDate();
		}
	}

	private void autoSetActualStartDate() {
		Date actualDate = getFirstExecutedTestPlanDate();

		setActualStartDate(actualDate);
	}

	/***
	 * Same method as autoSetActualEndDate but without actualEndDateUpdateAuthorization call To avoid checking
	 * authorization twice
	 */
	private void autoSetActualEndDateNoCheck() {
		Date actualDate = getLastExecutedTestPlanDate();
		setActualEndDate(actualDate);
	}

	private void autoSetActualEndDate() {
		// Check if end date can be set
		Date actualDate = null;
		if (actualEndDateUpdateAuthorization()) {
			actualDate = getLastExecutedTestPlanDate();
		}
		setActualEndDate(actualDate);
	}

	/***
	 * This methods browses testPlans and checks if at least one testPlanItem has RUNNING or READY for execution status.
	 * If this is the case, the actualEndDate should not be set
	 *
	 * @return false if the date should not be set
	 */
	private boolean actualEndDateUpdateAuthorization() {
		boolean toReturn = true;
		for (IterationTestPlanItem testPlanItem : testPlans) {
			if (!testPlanItem.getExecutionStatus().isTerminatedStatus()) {
				toReturn = false;
			}
		}
		return toReturn;
	}

	private Date getFirstExecutedTestPlanDate() {
		if (getTestPlans().isEmpty()) {
			return null;
		} else {
			IterationTestPlanItem firstTestPlan = Collections.min(getTestPlans(),
				CascadingAutoDateComparatorBuilder.buildTestPlanFirstDateSorter());
			return firstTestPlan.getLastExecutedOn();
		}
	}

	private Date getLastExecutedTestPlanDate() {
		if (getTestPlans().isEmpty()) {
			return null;
		} else {
			IterationTestPlanItem lastTestPlan = Collections.max(getTestPlans(),
				CascadingAutoDateComparatorBuilder.buildTestPlanLastDateSorter());
			return lastTestPlan.getLastExecutedOn();
		}
	}

	/**
	 * this method is used in case of copy paste of an iteration with test suites.<br>
	 *
	 * @return A map of test suite and indexes<br>
	 * One entry-set contains
	 * <ul>
	 * <li>a copied test suite (without it's test plan)</li>
	 * <li>and the indexes of the copied test plan that are to be linked with it
	 * <em>(taking into account test_plan_items that are test_case deleted)</em></li>
	 */
	public Map<TestSuite, List<Integer>> createTestSuitesPastableCopy() {
		Map<TestSuite, List<Integer>> resultMap = new HashMap<>();
		List<IterationTestPlanItem> testPlanWithoutDeletedTestCases = getTestPlanWithoutDeletedTestCases();

		for (TestSuite testSuite : getTestSuites()) {
			List<IterationTestPlanItem> testSuiteTestPlan = testSuite.getTestPlan();
			TestSuite testSuiteCopy = testSuite.createCopy();
			List<Integer> testPlanIndex = new ArrayList<>();

			for (IterationTestPlanItem iterationTestPlanItem : testSuiteTestPlan) {
				int testPlanItemIndex = testPlanWithoutDeletedTestCases.indexOf(iterationTestPlanItem);

				if (testPlanItemIndex > -1) {
					testPlanIndex.add(testPlanItemIndex);
				} // otherwise, test case was deleted
			}

			resultMap.put(testSuiteCopy, testPlanIndex);
		}

		return resultMap;
	}

	private List<IterationTestPlanItem> getTestPlanWithoutDeletedTestCases() {

		List<IterationTestPlanItem> testPlanResult = new LinkedList<>();

		for (IterationTestPlanItem itpi : getTestPlans()) {
			if (!itpi.isTestCaseDeleted()) {
				testPlanResult.add(itpi);
			}
		}
		return testPlanResult;
	}

	/**
	 * will update acual end and start dates if are auto and if they were driven by the execution last-executed on
	 */
	public void updateAutoDatesAfterExecutionDetach(IterationTestPlanItem iterationTestPlanItem) {

		updateAutoEndDateAfterExecutionDetach(iterationTestPlanItem);
		updateStartAutoDateAfterExecutionDetach();

	}

	private void updateStartAutoDateAfterExecutionDetach() {
		if (this.isActualStartAuto()) {
			autoSetActualStartDate();
		}

	}

	private void updateAutoEndDateAfterExecutionDetach(IterationTestPlanItem iterationTestPlanItem) {
		if (this.isActualEndAuto()) {
			if (!iterationTestPlanItem.getExecutionStatus().isTerminatedStatus()) {
				this.setActualEndDate(null);
			} else {
				autoSetActualEndDate();
			}
		}

	}

	// ***************** (detached) custom field section *************

	@Override
	public Long getBoundEntityId() {
		return getId();
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.ITERATION;
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(NodeContainerVisitor visitor) {
		visitor.visit(this);

	}

	@Override
	public void addContent(@NotNull TestSuite testSuite) throws DuplicateNameException, NullArgumentException {
		this.addTestSuite(testSuite);
	}

	@Override
	public void addContent(@NotNull TestSuite testSuite, int position) throws DuplicateNameException,
		NullArgumentException {
		this.addTestSuite(testSuite, position);

	}

	@Override
	public boolean isContentNameAvailable(String name) {
		return checkSuiteNameAvailable(name);
	}

	/**
	 * The content of an iteration means its test suites.
	 *
	 * @see org.squashtest.tm.domain.library.NodeContainer#getContent()
	 */
	@Override
	public List<TestSuite> getContent() {
		return getTestSuites();
	}

	@Override
	public Collection<TestSuite> getOrderedContent() {
		return getTestSuites();
	}

	/**
	 * @return true if there are test suites
	 * @see org.squashtest.tm.domain.library.NodeContainer#hasContent()
	 */
	@Override
	public boolean hasContent() {
		return !getContent().isEmpty();
	}

	@Override
	public void removeContent(TestSuite contentToRemove) throws NullArgumentException {
		removeTestSuite(contentToRemove);

	}

	@Override
	public List<String> getContentNames() {
		List<String> testSuitesNames = new ArrayList<>(testSuites.size());
		for (TestSuite suite : testSuites) {
			testSuitesNames.add(suite.getName());
		}
		return testSuitesNames;
	}

	@Override
	public Set<Milestone> getMilestones() {
		return getCampaign().getMilestones();
	}

	@Override
	public boolean isMemberOf(Milestone milestone) {
		return getCampaign().isMemberOf(milestone);
	}

	@Override
	public Boolean doMilestonesAllowCreation() {
		Boolean allowed = Boolean.TRUE;
		for (Milestone m : getMilestones()) {
			if (!m.getStatus().isAllowObjectCreateAndDelete()) {
				allowed = Boolean.FALSE;
				break;
			}
		}
		return allowed;
	}

	@Override
	public Boolean doMilestonesAllowEdition() {
		Boolean allowed = Boolean.TRUE;
		for (Milestone m : getMilestones()) {
			if (!m.getStatus().isAllowObjectModification()) {
				allowed = Boolean.FALSE;
				break;
			}
		}
		return allowed;
	}

	;
}
