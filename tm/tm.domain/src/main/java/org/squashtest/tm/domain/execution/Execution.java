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
package org.squashtest.tm.domain.execution;

import static org.squashtest.tm.domain.testcase.TestCaseImportance.LOW;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.collections.ListUtils;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Persister;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueList;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.infolist.DenormalizedNature;
import org.squashtest.tm.domain.infolist.DenormalizedType;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.library.HasExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testcase.*;
import org.squashtest.tm.exception.NotAutomatedException;
import org.squashtest.tm.exception.execution.ExecutionHasNoRunnableStepException;
import org.squashtest.tm.exception.execution.ExecutionHasNoStepsException;
import org.squashtest.tm.exception.execution.IllegalExecutionStatusException;
import org.squashtest.tm.infrastructure.hibernate.ReadOnlyCollectionPersister;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Auditable
@Entity
/*
 *  the following annotation is a trick, see same thing in class documentation in RequirementLibraryNode
 */
//@Table(appliesTo="EXECUTION_ISSUES_CLOSURE", sqlDelete=@SQLDelete(sql="delete from EXECUTION_ISSUES_CLOSURE where EXECUTION_ID=null and EXECUTION_ID=?"))
public class Execution implements AttachmentHolder, IssueDetector, Identified, HasExecutionStatus,
DenormalizedFieldHolder, BoundEntity {

	static final Set<ExecutionStatus> LEGAL_EXEC_STATUS;

	public static final String NO_DATASET_USED_LABEL = "";
	public static final String NO_DATASET_APPLICABLE_LABEL = null;
	private static final String PARAM_PREFIX = "\\Q${\\E";
	private static final String PARAM_SUFFIX = "\\Q}\\E";
	private static final String PARAM_PATTERN = PARAM_PREFIX + "([A-Za-z0-9_-]{1,255})" + PARAM_SUFFIX;
	private static final String NO_PARAM = "&lt;no_value&gt;";
	static {
		Set<ExecutionStatus> set = new HashSet<>();
		set.add(ExecutionStatus.SUCCESS);
		set.add(ExecutionStatus.BLOCKED);
		set.add(ExecutionStatus.FAILURE);
		set.add(ExecutionStatus.RUNNING);
		set.add(ExecutionStatus.READY);
		set.add(ExecutionStatus.UNTESTABLE);
		set.add(ExecutionStatus.SETTLED);
		LEGAL_EXEC_STATUS = Collections.unmodifiableSet(set);
	}

	@Id
	@Column(name = "EXECUTION_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "execution_execution_id_seq")
	@SequenceGenerator(name = "execution_execution_id_seq", sequenceName = "execution_execution_id_seq", allocationSize = 1)
	private Long id;

	// Not Null & Column missed comparing to requirementStatus
	@Enumerated(EnumType.STRING)
	private ExecutionStatus executionStatus = ExecutionStatus.READY;

	@Enumerated(EnumType.STRING)
	protected TestCaseExecutionMode executionMode = TestCaseExecutionMode.MANUAL;

	@Lob
	@Type(type="org.hibernate.type.TextType")
	// XXX if you rename it 'comment', let @Column(name="DESCRIPTION") anyway
	// no need to mess with the DB
	private String description;

	@Lob
	@Type(type="org.hibernate.type.TextType")
	private String prerequisite = "";

	@NotNull
	private String reference = "";

	@Lob
	@Type(type="org.hibernate.type.TextType")
	@Column(name = "TC_DESCRIPTION")
	private String tcdescription;

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	private TestCaseImportance importance = LOW;

	@Embedded
	private DenormalizedNature nature;

	@Embedded
	private DenormalizedType type;

	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	@Column(name = "TC_STATUS")
	private TestCaseStatus status = TestCaseStatus.WORK_IN_PROGRESS;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	private String name;

	@Column
	private String datasetLabel;

	// TODO rename as testPlanItem
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "ITEM_TEST_PLAN_EXECUTION", joinColumns = @JoinColumn(name = "EXECUTION_ID", insertable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "ITEM_TEST_PLAN_ID", insertable = false, updatable = false))
	private IterationTestPlanItem testPlan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TCLN_ID", referencedColumnName = "TCLN_ID")
	// @IndexedEmbedded
	private TestCase referencedTestCase;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name = "EXECUTION_STEP_ORDER")
	@JoinTable(name = "EXECUTION_EXECUTION_STEPS", joinColumns = @JoinColumn(name = "EXECUTION_ID"), inverseJoinColumns = @JoinColumn(name = "EXECUTION_STEP_ID"))
	private final List<ExecutionStep> steps = new ArrayList<>();

	@Formula("(select ITEM_TEST_PLAN_EXECUTION.EXECUTION_ORDER from ITEM_TEST_PLAN_EXECUTION where ITEM_TEST_PLAN_EXECUTION.EXECUTION_ID = EXECUTION_ID)")
	private Integer executionOrder;

	@Column(insertable = false)
	private String lastExecutedBy;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastExecutedOn;

	@OneToOne(mappedBy = "execution", cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, optional = true)
	private AutomatedExecutionExtender automatedExecutionExtender;

	/* *********************** attachment attributes ************************ */

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE,CascadeType.DETACH, CascadeType.REMOVE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	/* *********************** / attachement attributes ************************ */

	/* *********************** issues attributes ************************ */

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH,CascadeType.REMOVE })
	@JoinColumn(name = "ISSUE_LIST_ID")
	private IssueList issueList = new IssueList();



	/*
	 * TRANSITIONAL - job half done here. The full job would involve something among the lines of RequirementVersionCoverage
	 *
	 * The following mapping gives all issues reported in the scope of this execution : its own issues, and
	 * the issues reported in its steps.
	 *
	 * The underlying table is actually a view. So this one is read only and might be quite slow to use.
	 */

	@OneToMany(fetch=FetchType.LAZY)
	@JoinTable(name="EXECUTION_ISSUES_CLOSURE",
	joinColumns=@JoinColumn(name="EXECUTION_ID", insertable=false, updatable=false ),
	inverseJoinColumns = @JoinColumn(name="ISSUE_ID"))
	@Persister(impl = ReadOnlyCollectionPersister.class)
	@Immutable
	private List<Issue> issues = new ArrayList<>();


	@Transient
	private Map<String, String> dataset = new HashMap<>();

	/* *********************** /issues attributes ************************ */

	public List<ExecutionStep> getSteps() {
		return steps;
	}

	public Execution() {

	}

	/**
	 * Creates an execution for the test case references by the given tess plan item. Should be used by
	 * {@link IterationTestPlanItem} only.
	 *
	 * @param testPlanItem
	 */
	public Execution(TestCase testCase) {
		this(testCase, null);
	}

	public Execution(TestCase testCase, Dataset dataset) {
		fillParameterMap(dataset);
		setReferencedTestCase(testCase);
		populateSteps(dataset);
		populateAttachments();
		setDatasetLabel(testCase, dataset);
	}

	public void removeStep(long executionStepId) {

		Iterator<ExecutionStep> it = steps.iterator();
		while (it.hasNext()) {
			if (it.next().getId() == executionStepId) {
				it.remove();
				return;
			}
		}

	}


	private void setDatasetLabel(TestCase testCase, Dataset dataset){
		String label;

		// case one : there was no dataset whatsoever
		if (testCase.getDatasets().isEmpty()){
			label = null;
		}
		// case two : there are datasets available but none was choosen for that execution
		else if (dataset == null){
			label = "";
		} else {
			label = dataset.getName();
		}

		datasetLabel =  label;
	}

	private void populateAttachments() {
		for (Attachment tcAttach : referencedTestCase.getAllAttachments()) {
			Attachment clone = tcAttach.hardCopy();
			attachmentList.addAttachment(clone);
		}
	}

	private void populateSteps(Dataset dataset) {
		for (TestStep step : referencedTestCase.getSteps()) {
			List<ExecutionStep> execList = step.createExecutionSteps(dataset);
			for (ExecutionStep executionStep : execList) {
				addStep(executionStep);
			}
		}
	}


	/* ******************** HasExecutionStatus implementation ************** */

	@Override
	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	/* ******************** /HasExecutionStatus implementation ************** */

	public Integer getExecutionOrder() {
		return executionOrder;
	}

	public String getLastExecutedBy() {
		return lastExecutedBy;
	}

	public void setLastExecutedBy(String lastExecutedBy) {
		this.lastExecutedBy = lastExecutedBy;
	}

	public Date getLastExecutedOn() {
		return lastExecutedOn;
	}

	public void setLastExecutedOn(Date lastExecutedOn) {
		this.lastExecutedOn = lastExecutedOn;
	}

	private void setReferencedTestCase(TestCase testCase) {

		referencedTestCase = testCase;

		if (testCase.getReference() != null && !testCase.getReference().isEmpty()) {
			setName(testCase.getReference() + " - " + testCase.getName());
		} else {
			setName(testCase.getName());
		}

		nullSafeSetTestCaseData(testCase);
		setImportance(testCase.getImportance());

		setStatus(testCase.getStatus());

		// the nature and type now
		InfoListItem nature = testCase.getNature();
		this.nature = new DenormalizedNature(nature.getLabel(), nature.getCode(), nature.getIconName());

		InfoListItem type = testCase.getType();
		this.type = new DenormalizedType(type.getLabel(), type.getCode(), type.getIconName());

	}

	private void nullSafeSetTestCaseData(TestCase testCase) {

		// though it's constrained by the app, database allows null test case prerequisite or reference. hence this
		// safety belt.

		String pr = testCase.getPrerequisite();
		setPrerequisite(pr == null ? "" : valueParams(pr));

		pr = testCase.getReference();
		setReference(pr == null ? "" : pr);

		pr = testCase.getDescription();
		setTcdescription(pr == null ? "" : pr);

	}


	public void fillParameterMap(Dataset dataset){
		if(dataset != null){
			for(DatasetParamValue param : dataset.getParameterValues()){
				String key = param.getParameter().getName();
				String value = param.getParamValue();
				this.dataset.put(key, value);
			}
		}
	}

	private String valueParams(String content){

		String result = null;

		if(content != null){

			StringBuilder builder = new StringBuilder(content);

			Pattern pattern = Pattern.compile(PARAM_PATTERN);
			Matcher matcher = pattern.matcher(content);

			// each time a replacement occurs in the stringbuilder deviates
			// a bit further from string scanned by the matcher.
			//
			// Consequently the more the string is modified the more the length might be altered,
			// which leads to inconsistencies in the position of a given substring in the original string
			// and the modified string.
			//
			// Thus, the following variable keeps track
			// of the modifications to adjust the start and end position
			//
			int offset = 0;

			while (matcher.find()){
				String paramName = matcher.group(1);

				String paramValue = dataset.get(paramName);
				if( paramValue == null|| paramValue.isEmpty()) {
					paramValue = NO_PARAM;
				}

				int start = matcher.start();
				int end = matcher.end();

				builder.replace(start + offset, end + offset, paramValue);

				offset += paramValue.length() - (end - start);

			}

			result = builder.toString();
		}

		return result;


	}

	public TestCaseExecutionMode getExecutionMode() {
		return executionMode;
	}

	@Override
	public Long getId() {
		return id;
	}

	public TestCase getReferencedTestCase() {
		return referencedTestCase;
	}

	public String getName() {
		return this.name;
	}

	public final void setName(String name) {
		this.name = name;
	}


	// XXX the interface knows this as the 'comment', it should appear as getComment
	// However don't remove getDescription because plugins may break
	public String getDescription() {
		return this.description;
	}

	// XXX the interface knows this as the 'comment', it should appear as setComment
	// However don't remove setDescription because plugins may break
	public void setDescription(String description) {
		this.description = description;
	}

	public String getPrerequisite() {
		return prerequisite;
	}

	/**
	 * @param prerequisite
	 *            the prerequisite to set
	 */
	public void setPrerequisite(@NotNull String prerequisite) {
		this.prerequisite = prerequisite;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public TestCaseImportance getImportance() {
		return importance;
	}

	public void setImportance(@NotNull TestCaseImportance importance) {
		this.importance = importance;
	}

	public DenormalizedNature getNature() {
		return nature;
	}

	public void setNature(@NotNull DenormalizedNature nature) {
		this.nature = nature;
	}

	public DenormalizedType getType() {
		return type;
	}

	public void setType(@NotNull DenormalizedType type) {
		this.type = type;
	}

	public TestCaseStatus getStatus() {
		return status;
	}

	public void setStatus(@NotNull TestCaseStatus status) {
		this.status = status;
	}

	public String getTcdescription() {
		return tcdescription;
	}

	public void setTcdescription(String tcdescription) {
		this.tcdescription = tcdescription;
	}

	private void addStep(@NotNull ExecutionStep step) {
		steps.add(step);
	}




	public String getDatasetLabel() {
		return datasetLabel;
	}


	/**
	 * <p>
	 * return the first step with a running or a ready state.<br>
	 * Or null if there is none or the execution has no steps
	 * </p>
	 *
	 * @return
	 */
	public ExecutionStep findFirstUnexecutedStep() {
		if (!this.getSteps().isEmpty()) {
			for (ExecutionStep step : this.getSteps()) {
				if (!step.getExecutionStatus().isTerminatedStatus()) {
					return step;
				}
			}
		}
		return null;
	}

	public boolean hasUnexecutedSteps() {
		return findFirstUnexecutedStep() != null;
	}

	/* *************** Attachable implementation ****************** */
	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	public IterationTestPlanItem getTestPlan() {
		return testPlan;
	}

	@AclConstrainedObject
	public CampaignLibrary getCampaignLibrary() {
		return testPlan.getProject().getCampaignLibrary();
	}

	/* ***************** Bugged implementation *********************** */

	@Override
	public Project getProject() {
		return testPlan.getProject();
	}

	@Override
	public IssueList getIssueList() {
		return issueList;
	}

	public List<Issue> getIssues(){
		return 	ListUtils.unmodifiableList(issues);
	}

	@Override
	public Long getIssueListId() {
		return issueList.getId();
	}

	@Override
	public void detachIssue(Long id) {
		issueList.removeIssue(id);
	}

	/* ***************** /Bugged implementation *********************** */

	public void notifyAddedTo(IterationTestPlanItem testPlan) {
		this.testPlan = testPlan;
	}

	/**
	 * @return the first step not in success.
	 * @throws ExecutionHasNoStepsException
	 * @throws ExecutionHasNoRunnableStepException
	 */
	public ExecutionStep findFirstRunnableStep() throws ExecutionHasNoStepsException,
	ExecutionHasNoRunnableStepException {
		// Note : this was transplanted from untested HibernateExecDao method, I'm not sure of biz rules
		if (steps.isEmpty()) {
			throw new ExecutionHasNoStepsException();
		}

		for (ExecutionStep step : steps) {
			if (step.getExecutionStatus().isNoneOf(ExecutionStatus.SUCCESS)) {
				return step;
			}
		}

		throw new ExecutionHasNoRunnableStepException();
	}

	/**
	 * @return the last step of the execution.
	 * @throws ExecutionHasNoStepsException
	 *             if there are no steps
	 */
	public ExecutionStep getLastStep() throws ExecutionHasNoStepsException {
		if (steps.isEmpty()) {
			throw new ExecutionHasNoStepsException();
		}
		return steps.get(steps.size() - 1);
	}

	@Override
	public List<Long> getAllIssueListId() {
		List<Long> list = new LinkedList<>();

		list.add(issueList.getId());

		for (ExecutionStep step : steps) {
			list.addAll(step.getAllIssueListId());
		}

		return list;
	}

	@Override
	public BugTracker getBugTracker() {
		return getProject().findBugTracker();
	}

	/*
	 * ************************** test automation section (delegate to AutomatedExecutionExtender )
	 * *************************
	 */

	public AutomatedExecutionExtender getAutomatedExecutionExtender() {
		return automatedExecutionExtender;
	}

	public void setAutomatedExecutionExtender(AutomatedExecutionExtender extender) {
		this.automatedExecutionExtender = extender;
		executionMode = TestCaseExecutionMode.AUTOMATED;
	}

	public boolean isAutomated() {
		return executionMode == TestCaseExecutionMode.AUTOMATED && automatedExecutionExtender != null;
	}

	private boolean checkValidNewStatus(ExecutionStatus status) {
		if (isAutomated()) {
			return automatedExecutionExtender.getLegalStatusSet().contains(status);
		} else {
			return getLegalStatusSet().contains(status);
		}
	}

	public void setExecutionStatus(ExecutionStatus status) {

		if (!checkValidNewStatus(status)) {
			throw new IllegalExecutionStatusException();
		}

		executionStatus = status;

		// update parentTestPlan status

		IterationTestPlanItem itp = getTestPlan();

		if (itp != null) {
			itp.updateExecutionStatus();
		}
	}

	@Override
	public Set<ExecutionStatus> getLegalStatusSet() {
		if (isAutomated()) {
			return automatedExecutionExtender.getLegalStatusSet();
		} else {
			return LEGAL_EXEC_STATUS;
		}
	}

	public AutomatedTest getAutomatedTest() {
		if (isAutomated()) {
			return automatedExecutionExtender.getAutomatedTest();
		}

		throw new NotAutomatedException();
	}

	public URL getResultURL() {
		if (isAutomated()) {
			return automatedExecutionExtender.getResultURL();
		}
		throw new NotAutomatedException();
	}

	public AutomatedSuite getAutomatedSuite() {
		if (isAutomated()) {
			return automatedExecutionExtender.getAutomatedSuite();
		}
		throw new NotAutomatedException();
	}

	public String getResultSummary() {
		if (isAutomated()) {
			return automatedExecutionExtender.getResultSummary();
		}
		throw new NotAutomatedException();
	}

	@Override
	public Long getDenormalizedFieldHolderId() {
		return getId();
	}

	@Override
	public DenormalizedFieldHolderType getDenormalizedFieldHolderType() {
		return DenormalizedFieldHolderType.EXECUTION;
	}

	/**
	 * returns the index of the step matching the given id or <code>-1</code> if step is not found.
	 *
	 * @return index of step or -1
	 */
	public int getStepIndex(long stepId) {
		for (ExecutionStep step : steps) {
			if (step.getId() == stepId) {
				return steps.indexOf(step);
			}
		}
		return -1;
	}

	// ***************** (detached) custom field section *************

	@Override
	public Long getBoundEntityId() {
		return getId();
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.EXECUTION;
	}

	/**
	 * will compute from scratch a status using a complete report.
	 *
	 * @param report
	 *            : ExecutionStatusReport.
	 * @return : ExecutionStatus.
	 *
	 */
	public static ExecutionStatus computeNewStatus(ExecutionStatusReport report) {

		ExecutionStatus newStatus = ExecutionStatus.READY;

		if (report.has(ExecutionStatus.BLOCKED)) {
			newStatus = ExecutionStatus.BLOCKED;

		} else if (report.has(ExecutionStatus.FAILURE)) {
			newStatus = ExecutionStatus.FAILURE;

		} else if (report.allOf(ExecutionStatus.UNTESTABLE)) {
			newStatus = ExecutionStatus.UNTESTABLE;

		} else if (report.allOf(ExecutionStatus.SETTLED, ExecutionStatus.UNTESTABLE)) {
			newStatus = ExecutionStatus.SETTLED;

		} else if (report.allOf(ExecutionStatus.SUCCESS, ExecutionStatus.UNTESTABLE, ExecutionStatus.SETTLED)) {
			newStatus = ExecutionStatus.SUCCESS;

		} else if (report.anyOf(ExecutionStatus.SUCCESS, ExecutionStatus.SETTLED)) {
			newStatus = ExecutionStatus.RUNNING;

		}

		return newStatus;
	}

	public Iteration getIteration() {
		return testPlan.getIteration();
	}


	public Campaign getCampaign() {
		return getIteration().getCampaign();
	}

}
