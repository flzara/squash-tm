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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneMember;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneFinderService;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;

import java.util.Optional;


/**
 *
 * That service helps other controllers to configure the UI served to the client, by creating
 * configuration beans for that purpose. Those beans are : {@link MilestoneFeatureConfiguration}
 * and {@link MilestonePanelConfiguration}.
 *
 * @author bsiri
 *
 */
@Component
public class MilestoneUIConfigurationService {

	@Inject
	MilestoneFinderService milestoneFinder;

	@Inject
	FeatureManager featureManager;

	@Inject
	TestCaseFinder testCaseFinder;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;


	public MilestoneFeatureConfiguration configure(TestCase testcase) {
		MilestoneFeatureConfiguration conf = createCommonConf(testcase);
		Map<String, String> identity = createIdentity(testcase);
		conf.setIdentity(identity);

		// for the test cases one must account for inherited milestones
		boolean locked = isMilestoneLocked(testcase);
		conf.setMilestoneLocked(locked);
		return conf;
	}

	public MilestoneFeatureConfiguration configure(RequirementVersion version) {
		MilestoneFeatureConfiguration conf = createCommonConf(version);
		Map<String, String> identity = createIdentity(version);
		conf.setIdentity(identity);
		return conf;
	}

	public MilestoneFeatureConfiguration configure(Campaign campaign) {
		MilestoneFeatureConfiguration conf = createCommonConf(campaign);
		Map<String, String> identity = createIdentity(campaign);
		conf.setIdentity(identity);
		return conf;
	}

	public MilestoneFeatureConfiguration configure(Iteration iteration) {
		MilestoneFeatureConfiguration conf = createCommonConf(iteration);
		Map<String, String> identity = createIdentity(iteration);
		conf.setIdentity(identity);
		return conf;
	}

	public MilestoneFeatureConfiguration configure(TestSuite testSuite) {
		MilestoneFeatureConfiguration conf = createCommonConf(testSuite);
		Map<String, String> identity = createIdentity(testSuite);
		conf.setIdentity(identity);
		return conf;
	}


	// ************************** private stuffs *******************************************


	private MilestoneFeatureConfiguration createCommonConf(MilestoneMember member) {

		MilestoneFeatureConfiguration conf = new MilestoneFeatureConfiguration();

		Optional<Milestone> currentMilestone = activeMilestoneHolder.getActiveMilestone();

		JsonMilestone activeMilestone = new JsonMilestone();
		boolean globallyEnabled;
		boolean userEnabled;
		boolean locked;
		int totalMilestones;

		// TODO : test whether the functionality is globally enabled
		globallyEnabled = featureManager.isEnabled(Feature.MILESTONE);
		if (!globallyEnabled) {
			conf.setGloballyEnabled(false);
			return conf;
		}
		// from now on, we are GLOBALLY ENABLED

		// checks whether the entity is locked by milestone status
		locked = isMilestoneLocked(member);

		conf.setMilestoneLocked(locked);

		// does the user actually use the feature
		userEnabled = currentMilestone.isPresent();
		if (!userEnabled) {
			conf.setUserEnabled(false);
		}

		// total count of milestones
		totalMilestones = member.getMilestones().size();
		conf.setTotalMilestones(totalMilestones);

		// if both globally and user enabled, fetch the active milestones etc
		if (userEnabled && currentMilestone.isPresent()) { // NOSONAR redundant check, yet it expresses the business rule

			activeMilestone.setId(currentMilestone.get().getId());
			activeMilestone.setLabel(currentMilestone.get().getLabel());
			activeMilestone.setStatus(currentMilestone.get().getStatus());
			conf.setActiveMilestone(activeMilestone);

		}

		return conf;
	}

	private boolean isMilestoneLocked(TestCase testCase) {
		boolean locked = false;
		Collection<Milestone> milestones = testCaseFinder.findAllMilestones(testCase.getId());


		for (Milestone m : milestones) {
			MilestoneStatus status = m.getStatus();
			if (!status.isAllowObjectModification()) {
				locked = true;
				break;
			}
		}

		return locked;
	}

	private boolean isMilestoneLocked(MilestoneMember member) {
		return !member.doMilestonesAllowEdition();
	}


	private Map<String, String> createIdentity(TestCase testCase) {
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "test-cases");
		identity.put("resid", testCase.getId().toString());
		return identity;
	}

	private Map<String, String> createIdentity(Campaign campaign) {
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "campaigns");
		identity.put("resid", campaign.getId().toString());
		return identity;
	}

	private Map<String, String> createIdentity(RequirementVersion version) {
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "requirements");
		identity.put("resid", version.getRequirement().getId().toString());
		return identity;
	}

	private Map<String, String> createIdentity(Iteration iteration) {
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "iterations");
		identity.put("resid", iteration.getId().toString());
		return identity;
	}

	private Map<String, String> createIdentity(TestSuite testSuite) {
		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "test-suites");
		identity.put("resid", testSuite.getId().toString());
		return identity;
	}
}
