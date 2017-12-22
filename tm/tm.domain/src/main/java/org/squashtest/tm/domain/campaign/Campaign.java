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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.library.HasExecutionStatus;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;
import org.squashtest.tm.domain.search.LevelEnumBridge;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.DuplicateNameException;

@Entity
@PrimaryKeyJoinColumn(name = "CLN_ID")
public class Campaign extends CampaignLibraryNode implements NodeContainer<Iteration>, BoundEntity, MilestoneHolder{


	public static final int MAX_REF_SIZE = 50;

	@Embedded
	@Valid
	private ScheduledTimePeriod scheduledPeriod = new ScheduledTimePeriod();

	@Embedded
	@Valid
	private final ActualTimePeriod actualPeriod = new ActualTimePeriod();

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@OrderColumn(name = "ITERATION_ORDER")
	@JoinTable(name = "CAMPAIGN_ITERATION", joinColumns = @JoinColumn(name = "CAMPAIGN_ID"), inverseJoinColumns = @JoinColumn(name = "ITERATION_ID"))
	private final List<Iteration> iterations = new ArrayList<>();

	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@OrderColumn(name = "TEST_PLAN_ORDER")
	@JoinColumn(name = "CAMPAIGN_ID")
	private final List<CampaignTestPlanItem> testPlan = new ArrayList<>();

	@ManyToMany
	@JoinTable(name = "MILESTONE_CAMPAIGN", joinColumns = @JoinColumn(name = "CAMPAIGN_ID"), inverseJoinColumns = @JoinColumn(name = "MILESTONE_ID"))
	private Set<Milestone> milestones = new HashSet<>();


	@NotNull
	@Size(min = 0, max = MAX_REF_SIZE)
	private String reference = "";

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "CAMPAIGN_STATUS")
	@Field(analyze = Analyze.NO, store = Store.YES)
	@FieldBridge(impl = LevelEnumBridge.class)
	private CampaignStatus status = CampaignStatus.UNDEFINED;

	public Campaign() {
		super();
	}

	@Override
	public void accept(CampaignLibraryNodeVisitor visitor) {
		visitor.visit(this);

	}

	@Override
	public void accept(NodeContainerVisitor visitor) {
		visitor.visit(this);

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

	public Date getActualStartDate() {
		return actualPeriod.getActualStartDate();
	}

	public void setActualStartDate(Date actualStartDate) {
		actualPeriod.setActualStartDate(actualStartDate);
	}

	public Date getActualEndDate() {
		return actualPeriod.getActualEndDate();
	}

	public List<CampaignTestPlanItem> getTestPlan() {
		return testPlan;
	}

	public void setActualEndDate(Date actualEndDate) {
		actualPeriod.setActualEndDate(actualEndDate);
	}

	public boolean isActualStartAuto() {
		return actualPeriod.isActualStartAuto();
	}

	public boolean isActualEndAuto() {
		return actualPeriod.isActualEndAuto();
	}

	public CampaignStatus getStatus() {
		return status;
	}

	public void setStatus(@NotNull CampaignStatus status) {
		this.status = status;
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

	/**
	 * @param testCase
	 * @return the test plan item which references the given test case, if any.
	 */
	public CampaignTestPlanItem findTestPlanItem(TestCase testCase) {
		for (CampaignTestPlanItem campTestPlan : this.getTestPlan()) {
			if (campTestPlan.getReferencedTestCase().equals(testCase)) {
				return campTestPlan;
			}
		}
		return null;
	}

	/**
	 * @param itemTestPlan
	 */
	public void addToTestPlan(@NotNull CampaignTestPlanItem itemTestPlan) {
		this.getTestPlan().add(itemTestPlan);
		itemTestPlan.setCampaign(this);
	}

	public void removeTestPlanItem(@NotNull CampaignTestPlanItem itemTestPlan) {
		getTestPlan().remove(itemTestPlan);
	}

	public void removeTestPlanItem(long itemId) {
		Iterator<CampaignTestPlanItem> it = testPlan.iterator();
		while (it.hasNext()) {
			if (it.next().getId() == itemId) {
				it.remove();
				return;
			}
		}
	}

	public void removeTestPlanItems(List<Long> itemIds) {
		Iterator<CampaignTestPlanItem> it = testPlan.iterator();
		while (it.hasNext()) {
			if (itemIds.contains(it.next().getId())) {
				it.remove();
			}
		}
	}

	public void removeIteration(@NotNull Iteration iteration) {
		getIterations().remove(iteration);
	}

	public void moveIterations(int newIndex, List<Iteration> moved) {
		if (!iterations.isEmpty()) {
			iterations.removeAll(moved);
			iterations.addAll(newIndex, moved);
		}
	}

	public List<Iteration> getIterations() {
		return iterations;
	}

	public void addIteration(@NotNull Iteration iteration) {
		if (!isContentNameAvailable(iteration.getName())) {
			throw new DuplicateNameException(iteration.getName(), iteration.getName());
		}
		getIterations().add(iteration);
		iteration.setCampaign(this);
	}

	public void addIteration(@NotNull Iteration iteration, int position) {
		if (!isContentNameAvailable(iteration.getName())) {
			throw new DuplicateNameException(iteration.getName(), iteration.getName());
		}
		getIterations().add(position, iteration);
		iteration.setCampaign(this);
	}

	private ScheduledTimePeriod getScheduledPeriod() {
		// Hibernate workaround : when STP fields are null, component is set to null
		if (scheduledPeriod == null) {
			scheduledPeriod = new ScheduledTimePeriod();
		}
		return scheduledPeriod;
	}


	@Override
	public Campaign createCopy() {
		Campaign copy = new Campaign();
		copy.setName(this.getName());
		copy.setReference(this.getReference());
		copy.setDescription(this.getDescription());

		// as of 0.22.0 we do not copy actual start and end dates.
		if (this.getScheduledStartDate() != null) {
			copy.setScheduledStartDate((Date) this.getScheduledStartDate().clone());
		}
		if (this.getScheduledEndDate() != null) {
			copy.setScheduledEndDate((Date) this.getScheduledEndDate().clone());
		}
		copy.setActualEndAuto(this.isActualEndAuto());
		copy.setActualStartAuto(this.isActualStartAuto());
		for (Attachment tcAttach : this.getAttachmentList().getAllAttachments()) {
			Attachment atCopy = tcAttach.hardCopy();
			copy.getAttachmentList().addAttachment(atCopy);
		}

		for (CampaignTestPlanItem itemTestPlan : this.getTestPlan()) {
			copy.addToTestPlan(itemTestPlan.createCampaignlessCopy());
		}

		copy.notifyAssociatedWithProject(this.getProject());

		copy.bindSameMilestones(this);

		return copy;
	}

	@Override
	public boolean isContentNameAvailable(String name) {
		for (Iteration content : getIterations()) {
			if (content.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	/* ******** dates autosetting code ***** */

	/**
	 * If the iteration have autodates set, they will be updated accordingly.
	 *
	 * @param newIterationStartDate
	 */
	public void updateActualStart(Date newIterationStartDate) {

		if (isActualStartAuto()) {
			// if we're lucky we can save a heavier computation
			if (getActualStartDate() == null) {
				setActualStartDate(newIterationStartDate);
			} else if (newIterationStartDate != null && getActualStartDate().compareTo(newIterationStartDate) > 0) {
				setActualStartDate(newIterationStartDate);
			}

			// well too bad, we have to recompute that.
			else {
				autoSetActualStartDate();
			}
		}
	}

	public void updateActualEnd(Date newIterationEndDate) {
		if (isActualEndAuto()) {
			// if we're lucky we can save a heavier computation
			if (getActualEndDate() == null) {
				setActualEndDate(newIterationEndDate);
			} else if (newIterationEndDate != null && getActualEndDate().compareTo(newIterationEndDate) < 0) {
				setActualEndDate(newIterationEndDate);
			}

			// well too bad, we have to recompute that.
			else {
				autoSetActualEndDate();
			}
		}

	}

	private void autoSetActualStartDate() {
		Date actualDate = getFirstIterationActualStartDate();

		setActualStartDate(actualDate);
	}

	private void autoSetActualEndDate() {
		Date actualDate = getLastIterationActualEndDate();

		setActualEndDate(actualDate);
	}

	private Date getFirstIterationActualStartDate() {

		Iteration firstIteration = getFirstIteration();
		if (firstIteration != null) {
			return firstIteration.getActualStartDate();
		} else {
			return null;
		}

	}

	private Iteration getFirstIteration() {
		if (getIterations().isEmpty()) {
			return null;
		} else {
			return Collections
				.min(getIterations(), CascadingAutoDateComparatorBuilder.buildIterationActualStartOrder());
		}
	}

	private Date getLastIterationActualEndDate() {
		Iteration lastIteration = getLastIteration();
		if (lastIteration != null) {
			return lastIteration.getActualEndDate();

		} else {
			return null;
		}
	}

	private Iteration getLastIteration() {
		if (getIterations().isEmpty()) {
			return null;
		} else {
			return Collections.max(getIterations(), CascadingAutoDateComparatorBuilder.buildIterationActualEndOrder());
		}
	}

	public boolean testPlanContains(@NotNull TestCase tc) {
		return findTestPlanItem(tc) != null;
	}

	public boolean hasIterations() {
		return !iterations.isEmpty();
	}

	public void moveTestPlanItems(int targetIndex, List<Long> itemIds) {
		if (itemIds.isEmpty()) {
			return;
		}

		List<CampaignTestPlanItem> moved = new ArrayList<>(itemIds.size());

		for (CampaignTestPlanItem item : testPlan) {
			if (itemIds.contains(item.getId())) {
				moved.add(item);
			}
		}

		testPlan.removeAll(moved);
		testPlan.addAll(targetIndex, moved);
	}


	private void bindSameMilestones(Campaign src) {
		for (Milestone m : src.getMilestones()) {
			bindMilestone(m);
		}
	}


	// ***************** (detached) custom field section *************

	@Override
	public Long getBoundEntityId() {
		return getId();
	}

	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.CAMPAIGN;
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public List<Iteration> getContent() {
		List<Iteration> iterationList = new ArrayList<>();
		iterationList.addAll(getIterations());
		return iterationList;
	}

	@Override
	public List<Iteration> getOrderedContent() {
		return getIterations();
	}

	@Override
	public boolean hasContent() {
		return !getContent().isEmpty();
	}

	@Override
	public void addContent(Iteration iteration) throws DuplicateNameException, NullArgumentException {
		addIteration(iteration);

	}

	@Override
	public void addContent(Iteration iteration, int position) throws DuplicateNameException, NullArgumentException {
		addIteration(iteration, position);

	}

	@Override
	public void removeContent(Iteration contentToRemove) throws NullArgumentException {
		removeIteration(contentToRemove);

	}

	@Override
	public List<String> getContentNames() {
		List<String> iterationNames = new ArrayList<>(iterations.size());
		for (Iteration iteration : iterations) {
			iterationNames.add(iteration.getName());
		}
		return iterationNames;
	}

	@Override
	public Set<Milestone> getMilestones() {
		return milestones;
	}


	@Override
	public boolean isMemberOf(Milestone milestone) {
		return milestones.contains(milestone);
	}

	/**
	 * A campaign can belong to one milestone at a time. When a milestone
	 * is bound to a campaign, the previous milestone will be unbound.
	 */
	@Override
	public void bindMilestone(Milestone milestone) {
		milestones.clear();
		milestones.add(milestone);
	}

	@Override
	public void unbindMilestone(Milestone milestone) {
		unbindMilestone(milestone.getId());
	}

	@Override
	public void unbindMilestone(Long milestoneId) {
		Iterator<Milestone> iter = milestones.iterator();

		while (iter.hasNext()) {
			Milestone m = iter.next();
			if (m.getId().equals(milestoneId)) {
				iter.remove();
				break;
			}
		}
	}

	/**
	 * @see org.squashtest.tm.domain.milestone.MilestoneHolder#unbindAllMilestones()
	 */
	@Override
	public void unbindAllMilestones() {
		milestones.clear();

	}

	public boolean isBindableToMilestone() {
		return milestonesAllowBind();
	}

	private boolean milestonesAllowBind() {

		for (Milestone m : milestones) {
			if (!m.getStatus().isAllowObjectModification()) {
				return false;
			}
		}
		return true;

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
