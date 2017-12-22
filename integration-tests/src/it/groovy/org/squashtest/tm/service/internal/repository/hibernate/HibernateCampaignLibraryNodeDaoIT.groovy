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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Specification
import spock.unitils.UnitilsSupport;


@UnitilsSupport
class HibernateCampaignLibraryNodeDaoIT extends DbunitDaoSpecification {

	/*
	@Inject
	HibernateCampaignLibraryNodeDao dao;
	
	@DataSet("HibernateCampaignDaoIt.small hierarchy.xml")
	def "should return the list of the parents names"(){

		when :
			def res = dao.getParentsName(-30L)
			
		then :
			res == ["elder", "grandpa", "pa", "son"]
		
	}
	
	*/
}
