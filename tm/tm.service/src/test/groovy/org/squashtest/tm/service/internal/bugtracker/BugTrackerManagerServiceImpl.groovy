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
package org.squashtest.tm.service.internal.bugtracker

import org.squashtest.tm.domain.bugtracker.BugTrackerBinding
import org.squashtest.tm.domain.bugtracker.Issue
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.BugTrackerBindingDao
import org.squashtest.tm.service.internal.repository.BugTrackerDao
import org.squashtest.tm.service.internal.repository.IssueDao
import org.squashtest.tm.service.project.GenericProjectManagerService

import spock.lang.Specification

class BugTrackerManagerServiceImplTest extends Specification  {
    BugTrackerBindingDao bugTrackerBindingDao = Mock()
    IssueDao  issueDao = Mock()
    BugTrackerDao bugTrackerDao = Mock()
    GenericProjectManagerService genericProjectManagerService = Mock()
    BugTrackerManagerServiceImpl service = new BugTrackerManagerServiceImpl()
	org.squashtest.tm.service.internal.repository.RequirementSyncExtenderDao syncreqDao = Mock()

    def setup(){
        service.bugTrackerBindingDao = bugTrackerBindingDao
        service.bugTrackerDao = bugTrackerDao
        service.issueDao = issueDao
        service.genericProjectManagerService =  genericProjectManagerService
		service.syncreqDao = syncreqDao;
    }

    def "should delete bugtrackers"(){
        given :"list of ids of the bugtracker to delete"
        def bugtrackerIds = (1L..5L).collect{it}
        and : "each bugtracker is bind to 2 projects"
        (1L..5L).each {bugTrackerBindingDao.findByBugtrackerId(it)    >> [it * 10, it *10 + 1].collect{Project p = Mock(); p.getId() >> it;  return new BugTrackerBinding(project:p)}}
        and : "each bugtracker get 3 issues associated "
        (1L..5L).each{issueDao.getAllIssueFromBugTrackerId(it) >> [it * 10, it *10 + 1, it * 10 + 2].collect{ new Issue(id:it)}}


        when :
        service.deleteBugTrackers(bugtrackerIds)

        then :
        5 * bugTrackerDao.delete(_)
        10 * genericProjectManagerService.removeBugTracker(_)
        15 * issueDao.delete(_)
		5 * syncreqDao.deleteAllByServer(_)
    }
}
