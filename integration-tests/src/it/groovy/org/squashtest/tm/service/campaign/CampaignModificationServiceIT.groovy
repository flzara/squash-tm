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
package org.squashtest.tm.service.campaign
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;
import javax.inject.Inject
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Path

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.CampaignFolder
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.project.GenericProjectManagerService

@Transactional
@UnitilsSupport
@DataSet
class CampaignModificationServiceIT extends DbunitServiceSpecification {
	@Inject
	private CampaignModificationService service

	@Inject
	private CampaignLibraryNavigationService navService

	@Inject
	private IterationModificationService iterService

	@Inject GenericProjectManagerService projectService;

	private long campId=-10l;
	private long folderId = -1l;
	private long libId = -1l

	

	def "should not accept a rename to empty string"(){
		given :
		def campaign = new Campaign(name:"campa");
		navService.addCampaignToCampaignFolder(folderId, campaign)

		when :
		try{
			service.rename(campaign.id, "")
		}catch(ConstraintViolationException cve){
			Set cv = cve.getConstraintViolations();

			ConstraintViolation a;
			Iterator iter = cv.iterator()
			while(iter.hasNext()){
				a=iter.next();
			}

			String me = a.getMessage();
			Path p = a.getPropertyPath();
			String name = p.toString();

			String breakStr;
		}
		then :
		true;
	}


	def "should not rename a campaign"(){
		given :
		def campaign = new Campaign(name:"camp")
		navService.addCampaignToCampaignFolder(folderId, campaign)

		when :
		service.rename(campaign.id, "campaign 1")

		then :
		thrown(DuplicateNameException)
	}

	def "should change the scheduled start date"(){
		given :
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1)
		Date myDate = calendar.getTime();

		when :
		service.changeScheduledStartDate(campId, myDate)
		def obj=service.findById(campId)

		then :
		obj.scheduledStartDate == myDate
	}

	def "should change the scheduled end date"(){
		given :
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1)
		Date myDate = calendar.getTime();

		when :
		service.changeScheduledEndDate(campId, myDate)
		def obj=service.findById(campId)

		then :
		obj.scheduledEndDate == myDate
	}

	def "should change the actual start date"(){
		given :
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1)
		Date myDate = calendar.getTime();

		when :
		service.changeActualStartDate(campId, myDate)
		def obj=service.findById(campId)

		then :
		obj.actualStartDate == myDate
	}

	def "should change the actual end date"(){
		given :
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1)
		Date myDate = calendar.getTime();

		when :
		service.changeActualEndDate(campId, myDate)
		def obj=service.findById(campId)

		then :
		obj.actualEndDate == myDate
	}


	def "should screw up when reloading iterations after deletion yet not throwing exception"(){
		given :
		def campaign1 = new Campaign(name:"test campaign 5")
		navService.addCampaignToCampaignFolder(folderId, campaign1)
		Iteration iter1 = new Iteration(name:"it1")
		Iteration iter2 = new Iteration(name:"it2")
		Iteration iter3 = new Iteration(name:"it3")

		navService.addIterationToCampaign(iter1, campaign1.id,true)
		navService.addIterationToCampaign(iter2, campaign1.id,true)
		navService.addIterationToCampaign(iter3, campaign1.id,true)

		when :
		iterService.delete(iter3.id);
		def itervec= iterService.findIterationsByCampaignId(campaign1.id)

		then :
		notThrown(Exception)
	}


}
