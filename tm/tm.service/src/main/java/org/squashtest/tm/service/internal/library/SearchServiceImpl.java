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
package org.squashtest.tm.service.internal.library;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.project.ProjectResource;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.library.SearchService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;

@Service("squashtest.tm.service.SearchService")
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {


	@Inject
	private CampaignDao campaignDao;

	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	/*
	 * TODO : the user project filter is applied in a straight forward manner in the following methods. The relevant
	 * code should be moved in an aspect, because the need for filtering might appear in other parts of the app in the
	 * future.
	 *
	 * See task (TODO : write the task)
	 *
	 * (non-Javadoc)
	 *
	 * @see
	 * org.squashtest.csp.tm.service.SearchService#findAllBySearchCriteria(org.squashtest.tm.domain.requirement.
	 * RequirementSearchCriteria)
	 */
	private static final String FILTRED_READ_OR_ADMIN = "hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN;

	@Override
	@PostFilter(FILTRED_READ_OR_ADMIN)
	public List<CampaignLibraryNode> findCampaignByName(String aName, boolean groupByProject) {
		List<CampaignLibraryNode> list = campaignDao.findAllByNameContaining(aName, groupByProject);
		return applyProjectFilter(list);
	}



	protected <PR extends ProjectResource<?>> List<PR> applyProjectFilter(List<PR> initialList) {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		if (!pf.getActivated()) {
			return initialList;
		} else {
			return applyFilter(initialList, pf);
		}

	}

	private <PR extends ProjectResource<?>> List<PR> applyFilter(List<PR> unfilteredResources, ProjectFilter filter) {
		List<PR> filtered = new ArrayList<>(unfilteredResources.size());

		for (PR resource : unfilteredResources) {
			if (filter.isProjectSelected(resource.getProject())) {
				filtered.add(resource);
			}
		}
		return filtered;
	}



	// -------------------------------------TODO mutualize duplicated code



	@Override
	public List<String> findBreadCrumbForCampaign(String className, Long id, String rejex) {
		List<String> result = null;
		if (!"Iteration".equals(className) && !"TestSuite".equals(className)) {
			CampaignLibraryNode node;
			if (className.endsWith("Folder")) {
				node = campaignLibraryNavigationService.findFolder(id);
			} else {
				node = campaignDao.findById(id);
			}
			result = findBreadCrumbOfCampaignNode(node, campaignLibraryNavigationService, rejex);
		}

		// TODO complete for iteration or test suite search
		return result;
	}

	private List<String> findBreadCrumbOfCampaignNode(CampaignLibraryNode node,
			CampaignLibraryNavigationService libraryNavigationService, String rejex) {
		List<String> result = new ArrayList<>();
		result.add(node.getClass().getSimpleName() + rejex + node.getId());
		CampaignFolder parent = libraryNavigationService.findParentIfExists(node);
		fillBreadCrumbListUntillLibraryForCampaign(node, libraryNavigationService, rejex, result, parent);

		return result;
	}

	private void fillBreadCrumbListUntillLibraryForCampaign(CampaignLibraryNode node,
			CampaignLibraryNavigationService libraryNavigationService, String rejex, List<String> result,
			CampaignFolder parent) {
		CampaignLibraryNode root = node;
		CampaignFolder ancestor = parent;

		while (ancestor != null) {
			result.add(ancestor.getClass().getSimpleName() + rejex + ancestor.getId());
			root = ancestor;
			ancestor = libraryNavigationService.findParentIfExists(root);
		}
		CampaignLibrary library = libraryNavigationService.findLibraryOfRootNodeIfExist(root);
		result.add(library.getClassSimpleName() + rejex + library.getId());
	}
	// -----------------------------------------------------------------------------end TODO
}
