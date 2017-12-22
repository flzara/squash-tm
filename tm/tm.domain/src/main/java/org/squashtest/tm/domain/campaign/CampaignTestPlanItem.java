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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.security.annotation.InheritsAcls;

@Entity
@Table(name = "CAMPAIGN_TEST_PLAN_ITEM")
@InheritsAcls(constrainedClass = Campaign.class, collectionName = "testPlan")
public class CampaignTestPlanItem implements Identified{
	// TODO give meaningful name ! eg assigned user
	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private User user;

	@Id
	@Column(name = "CTPI_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "campaign_test_plan_item_ctpi_id_seq")
	@SequenceGenerator(name = "campaign_test_plan_item_ctpi_id_seq", sequenceName = "campaign_test_plan_item_ctpi_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "TEST_CASE_ID")
	private TestCase referencedTestCase;

	@ManyToOne
	@JoinColumn(name = "DATASET_ID", referencedColumnName = "DATASET_ID")
	private Dataset referencedDataset;

	@ManyToOne
	@JoinColumn(name = "CAMPAIGN_ID", insertable = false, updatable = false)
	private Campaign campaign;

	@Override
	public Long getId() {
		return id;
	}

	public CampaignTestPlanItem() {
		super();
	}

	public CampaignTestPlanItem(TestCase testCase) {
		this.referencedTestCase = testCase;
	}

	public CampaignTestPlanItem(TestCase testCase, Dataset dataset){
		this.referencedTestCase = testCase;
		this.referencedDataset = dataset;
	}

	public TestCase getReferencedTestCase() {
		return this.referencedTestCase;
	}

	public void setReferencedTestCase(TestCase referencedTestCase) {
		this.referencedTestCase = referencedTestCase;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}


	/**
	 * Factory method. Creates a copy of this object according to copy / paste rules. The copy is associated to no
	 * {@link Campaign}, it needs to be added to a campaign afterwards.
	 *
	 * @return the copy, never <code>null</code>
	 */
	public CampaignTestPlanItem createCampaignlessCopy() {
		CampaignTestPlanItem copy = new CampaignTestPlanItem();

		copy.setUser(this.getUser());
		copy.setReferencedTestCase(this.getReferencedTestCase());
		copy.setReferencedDataset(this.getReferencedDataset());

		return copy;
	}

	public Project getProject() {
		return campaign.getProject();
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public Dataset getReferencedDataset() {
		return referencedDataset;
	}

	public void setReferencedDataset(Dataset referencedDataset) {
		this.referencedDataset = referencedDataset;
	}

	/**
	 * Should only be used by the Campaign when this item is added to the test plan.
	 *
	 * @param campaign
	 */
	protected void setCampaign(@NotNull Campaign campaign) {
		this.campaign = campaign;
	}

}
