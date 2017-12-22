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
package org.squashtest.tm.service.customreport

import org.squashtest.tm.domain.Workspace
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode
import org.squashtest.tm.domain.users.PartyPreference
import org.squashtest.tm.domain.users.preferences.WorkspaceDashboardContentValues
import org.squashtest.tm.service.internal.customreport.CustomReportDashboardServiceImpl
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.user.PartyPreferenceService
import spock.lang.Specification

/**
 * Created by jthebault on 31/03/2016.
 */
class CustomReportDashboardServiceImplTest extends Specification {

	CustomReportDashboardService service =  new CustomReportDashboardServiceImpl();

	CustomReportLibraryNodeService crlnService = Mock();

	PartyPreferenceService partyPreferenceService= Mock();

	PermissionEvaluationService permissionService = Mock();

	CustomReportLibraryNodeDao customReportLibraryNodeDao =Mock();

	def setup(){
		service.crlnService = crlnService;
		service.partyPreferenceService = partyPreferenceService;
		service.permissionService = permissionService;
		service.customReportLibraryNodeDao = customReportLibraryNodeDao;

	}

	def "should not show dashboard"(){
		given:
		def preference = new PartyPreference()
		preference.setPreferenceValue(WorkspaceDashboardContentValues.DEFAULT.getPreferenceValue())

		partyPreferenceService.findPreferenceForCurrentUser(_) >> preference;

		when:
		boolean result = service.shouldShowFavoriteDashboardInWorkspace(Workspace.HOME);

		then:
		result == false;
	}

	def "should not show dashboard because no pref"(){
		given:
		partyPreferenceService.findPreferenceForCurrentUser(_) >> null;

		when:
		boolean result = service.shouldShowFavoriteDashboardInWorkspace(Workspace.HOME);

		then:
		result == false;
	}

	def "should show dashboard"(){
		given:
		def preference = new PartyPreference()
		preference.setPreferenceValue(WorkspaceDashboardContentValues.DASHBOARD.getPreferenceValue())

		partyPreferenceService.findPreferenceForCurrentUser(_) >> preference;

		when:
		boolean result = service.shouldShowFavoriteDashboardInWorkspace(Workspace.HOME);

		then:
		result == true;
	}

	def "can't show dashboard because no pref"(){
		given:
		partyPreferenceService.findPreferenceForCurrentUser(_) >> null;

		when:
		boolean result = service.canShowDashboardInWorkspace(Workspace.HOME);

		then:
		result == false;
	}

	def "can't show dashboard because dashboard node id doesn't exist"(){
		given:
		def preference = new PartyPreference()
		preference.setPreferenceValue('12')
		partyPreferenceService.findPreferenceForCurrentUser(_) >> preference;
		customReportLibraryNodeDao.findOne(_)>>null

		when:
		boolean result = service.canShowDashboardInWorkspace(Workspace.HOME);

		then:
		result == false;
	}

	def "can't show dashboard because rights have been revoked"(){
		given:
		def preference = new PartyPreference()
		preference.setPreferenceValue('12')
		partyPreferenceService.findPreferenceForCurrentUser(_) >> preference
		def node = new CustomReportLibraryNode()
		customReportLibraryNodeDao.findOne(_) >> node
		permissionService.hasRoleOrPermissionOnObject(_,_,_,) >> false

		when:
		boolean result = service.canShowDashboardInWorkspace(Workspace.HOME)

		then:
		result == false
	}

	def "can show dashboard"(){
		given:
		def preference = new PartyPreference()
		preference.setPreferenceValue('12')
		partyPreferenceService.findPreferenceForCurrentUser(_) >> preference
		customReportLibraryNodeDao.findOne(_)>> new CustomReportLibraryNode()
		permissionService.hasRoleOrPermissionOnObject(_,_,_,) >> true

		when:
		boolean result = service.canShowDashboardInWorkspace(Workspace.HOME)

		then:
		result == true
	}
}
