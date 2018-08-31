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
package org.squashtest.tm.service.internal.customreport;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportReportBinding;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.domain.users.PartyPreference;
import org.squashtest.tm.domain.users.preferences.CorePartyPreference;
import org.squashtest.tm.domain.users.preferences.WorkspaceDashboardContentValues;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.repository.CustomReportChartBindingDao;
import org.squashtest.tm.service.internal.repository.CustomReportDashboardDao;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;
import org.squashtest.tm.service.internal.repository.CustomReportReportBindingDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.PartyPreferenceService;

@Transactional
@Service("org.squashtest.tm.service.customreport.CustomReportDashboardService")
public class CustomReportDashboardServiceImpl implements
		CustomReportDashboardService {

	@Inject
	private CustomReportDashboardDao customReportDashboardDao;

	@Inject
	private CustomReportChartBindingDao chartBindingDao;

	@Inject
	private CustomReportReportBindingDao reportBindingDao;

	@Inject
	private CustomReportLibraryNodeService crlnService;

	@PersistenceContext
	private EntityManager em;

	@Inject
	protected PermissionEvaluationService permissionService;

	@Inject
	private PartyPreferenceService partyPreferenceService;

	@Inject
	private CustomReportLibraryNodeDao customReportLibraryNodeDao;

	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	@Override
	public CustomReportDashboard findById(Long id) {
		return customReportDashboardDao.getOne(id);
	}

	@Override
	@PreAuthorize("hasPermission(#newBinding.dashboard.id, 'org.squashtest.tm.domain.customreport.CustomReportDashboard' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public void bindReport(CustomReportReportBinding newBinding) {
		reportBindingDao.save(newBinding);
		em.flush();
	}

	@Override
	@PreAuthorize("hasPermission(#newBinding.dashboard.id, 'org.squashtest.tm.domain.customreport.CustomReportDashboard' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void bindChart(CustomReportChartBinding newBinding) {
		chartBindingDao.save(newBinding);
		em.flush();
	}

	@Override
	public void updateGridPosition(List<CustomReportChartBinding> chartBindings, List<CustomReportReportBinding> reportBindings) {
		for (CustomReportChartBinding chartBinding : chartBindings) {
			updateChartBinding(chartBinding);
		}

		for (CustomReportReportBinding reportBinding : reportBindings) {
			updateReportBinding(reportBinding);
		}

	}

	private void updateChartBinding(CustomReportChartBinding transientBinding) {
		CustomReportChartBinding persistedBinding = chartBindingDao.getOne(transientBinding.getId());
		if(permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN,"WRITE",persistedBinding.getDashboard())
				&& persistedBinding.hasMoved(transientBinding)){
			persistedBinding.move(transientBinding);
		}
	}

	private void updateReportBinding(CustomReportReportBinding transientBinding) {
		CustomReportReportBinding persistedBinding = reportBindingDao.getOne(transientBinding.getId());
		if(permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN,"WRITE",persistedBinding.getDashboard())
			&& persistedBinding.hasMoved(transientBinding)){
			persistedBinding.move(transientBinding);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#id, 'org.squashtest.tm.domain.customreport.CustomReportChartBinding' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void unbindChart(Long id) {
		Optional<CustomReportChartBinding> optBinding = chartBindingDao.findById(id);
		if (optBinding.isPresent()) {
			chartBindingDao.delete(optBinding.get());
		}
	}

	@Override
	@PreAuthorize("hasPermission(#id, 'org.squashtest.tm.domain.customreport.CustomReportReportBinding' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public void unbindReport(Long id) {
		Optional<CustomReportReportBinding> optBinding = reportBindingDao.findById(id);
		if (optBinding.isPresent()) {
			reportBindingDao.delete(optBinding.get());
		}
	}

	@Override
	@PreAuthorize("hasPermission(#bindingId, 'org.squashtest.tm.domain.customreport.CustomReportChartBinding' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public CustomReportChartBinding changeBindedChart(long bindingId,
			long chartNodeId) {
		CustomReportChartBinding chartBinding = chartBindingDao.getOne(bindingId);
		ChartDefinition chartDefinition = crlnService.findChartDefinitionByNodeId(chartNodeId);
		chartBinding.setChart(chartDefinition);
		return chartBinding;
	}

	@Override
	@PreAuthorize("hasPermission(#bindingId, 'org.squashtest.tm.domain.customreport.CustomReportReportBinding' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public CustomReportReportBinding changeBindedReport(long bindingId,
													  long reportNodeId) {
		CustomReportReportBinding reportBinding = reportBindingDao.getOne(bindingId);
		ReportDefinition reportDefinition = crlnService.findReportDefinitionByNodeId(reportNodeId);
		reportBinding.setReport(reportDefinition);
		return reportBinding;
	}

	@Override
	public void chooseFavoriteDashboardForCurrentUser(Workspace workspace, long nodeId) {
		CustomReportLibraryNode node = crlnService.findCustomReportLibraryNodeById(nodeId);
		String preferenceKey = getChooseFavoriteDashboardPreferenceKey(workspace);
		if (node != null){
			partyPreferenceService.addOrUpdatePreferenceForCurrentUser(
				preferenceKey,String.valueOf(node.getId()));
		}
	}

	private String getChooseFavoriteDashboardPreferenceKey(Workspace workspace) {
		switch (workspace){
			case HOME:
				return CorePartyPreference.FAVORITE_DASHBOARD_HOME.getPreferenceKey();
			case REQUIREMENT:
				return CorePartyPreference.FAVORITE_DASHBOARD_REQUIREMENT.getPreferenceKey();
			case TEST_CASE:
				return CorePartyPreference.FAVORITE_DASHBOARD_TEST_CASE.getPreferenceKey();
			case CAMPAIGN:
				return CorePartyPreference.FAVORITE_DASHBOARD_CAMPAIGN.getPreferenceKey();
			default:
				throw new IllegalArgumentException("workspace should be home, requirement,test-case or campaign");
		}
	}

	@Override
	public boolean shouldShowFavoriteDashboardInWorkspace(Workspace workspace) {
		String key = getWorkspaceContentPreferenceKey(workspace);
		PartyPreference pref = partyPreferenceService.findPreferenceForCurrentUser(key);
		if (pref == null){
			return false;
		}
		String content = pref.getPreferenceValue();
		if (StringUtils.isEmpty(content) || content.equals(WorkspaceDashboardContentValues.DEFAULT.getPreferenceValue())){
			return false;
		}
		return content.equals(WorkspaceDashboardContentValues.DASHBOARD.getPreferenceValue());
	}

	private String getWorkspaceContentPreferenceKey(Workspace workspace) {
		switch (workspace){
			case HOME :
				return CorePartyPreference.HOME_WORKSPACE_CONTENT.getPreferenceKey();
			case REQUIREMENT :
				return CorePartyPreference.REQUIREMENT_WORKSPACE_CONTENT.getPreferenceKey();
			case TEST_CASE:
				return CorePartyPreference.TEST_CASE_WORKSPACE_CONTENT.getPreferenceKey();
			case CAMPAIGN:
				return CorePartyPreference.CAMPAIGN_WORKSPACE_CONTENT.getPreferenceKey();
			default:
				throw new IllegalArgumentException("workspace should be home, requirement,test-case or campaign");
		}
	}

	@Override
	public boolean canShowDashboardInWorkspace(Workspace workspace) {
		String key = getChooseFavoriteDashboardPreferenceKey(workspace);
		PartyPreference pref = partyPreferenceService.findPreferenceForCurrentUser(key);
		if (pref == null) {
			return false;
		}
		String candidateDashboardId = pref.getPreferenceValue();
		if (StringUtils.isEmpty(candidateDashboardId)) {
			return false;
		}
		Long dashboardId = Long.parseLong(candidateDashboardId);
		Optional<CustomReportLibraryNode> node = customReportLibraryNodeDao.findById(dashboardId);
		return node.isPresent() && permissionService.hasRoleOrPermissionOnObject(ROLE_ADMIN,"READ",node.get());
	}
}
