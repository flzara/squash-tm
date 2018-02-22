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
package org.squashtest.tm.service.internal.infolist

import org.squashtest.tm.domain.infolist.InfoList
import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.service.internal.repository.GenericProjectDao
import org.squashtest.tm.service.internal.repository.InfoListDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import spock.lang.Specification;

public class InfoListBindingManagerServiceImplTest extends Specification {

	InfoListBindingManagerServiceImpl manager = new InfoListBindingManagerServiceImpl()

	GenericProjectDao genericProjectDao = Mock()
	ProjectDao projectDao = Mock()
	InfoListDao infoListDao = Mock()

	def setup() {
		manager.genericProjectDao = genericProjectDao
		manager.projectDao = projectDao
		manager.infoListDao = infoListDao
	}

	def "#bindListToProjectReqCategory - Should bind ReqCategory InfoList to a Project"() {

		given:

		Project project = new Project()

		InfoList infoList = Mock()
		InfoListItem defaultItem = Mock()
		infoList.getDefaultItem() >> defaultItem

		and:

		genericProjectDao.findOne(404L) >> project
		infoListDao.findOne(7331L) >> infoList

		when:

		manager.bindListToProjectReqCategory(7331L, 404L)

		then:

		project.getRequirementCategories() == infoList
		1 * infoListDao.setDefaultCategoryForProject(404L, defaultItem)
	}

	def "#bindListToProjectReqCategory - Should bind ReqCategory InfoList to a Template and propagate it to its bound Projects"() {

		given:

		ProjectTemplate template = new ProjectTemplate()

		Project project1 = Mock()
		project1.getId() >> 4L
		Project project2 = Mock()
		project2.getId() >> 5L

		InfoList infoList = Mock()
		InfoListItem defaultItem = Mock()
		infoList.getDefaultItem() >> defaultItem

		and:

		genericProjectDao.findOne(404L) >> template
		infoListDao.findOne(7331L) >> infoList

		and:

		projectDao.findAllBoundToTemplate(404L) >> [project1, project2]

		when:

		manager.bindListToProjectReqCategory(7331L, 404L)

		then:

		template.getRequirementCategories() == infoList
		1 * infoListDao.setDefaultCategoryForProject(404L, defaultItem)
		1 * project1.setRequirementCategories(infoList)
		1 * project2.setRequirementCategories(infoList)
		1 * infoListDao.setDefaultCategoryForProject(4L, defaultItem)
		1 * infoListDao.setDefaultCategoryForProject(5L, defaultItem)
	}


	def "#bindListToProjectTcNature - Should bind TcNatureInfoList to a Project"() {

		given:

		Project project = new Project()

		InfoList infoList = Mock()
		InfoListItem defaultItem = Mock()
		infoList.getDefaultItem() >> defaultItem

		and:

		genericProjectDao.findOne(404L) >> project
		infoListDao.findOne(7331L) >> infoList

		when:

		manager.bindListToProjectTcNature(7331L, 404L)

		then:

		project.getTestCaseNatures() == infoList
		1 * infoListDao.setDefaultNatureForProject(404L, defaultItem)
	}

	def "#bindListToProjectTcNature - Should bind TcNatureInfoList InfoList to a Template and propagate it to its bound Projects"() {

		given:

		ProjectTemplate template = new ProjectTemplate()

		Project project1 = Mock()
		project1.getId() >> 4L
		Project project2 = Mock()
		project2.getId() >> 5L

		InfoList infoList = Mock()
		InfoListItem defaultItem = Mock()
		infoList.getDefaultItem() >> defaultItem

		and:

		genericProjectDao.findOne(404L) >> template
		infoListDao.findOne(7331L) >> infoList

		and:

		projectDao.findAllBoundToTemplate(404L) >> [project1, project2]

		when:

		manager.bindListToProjectTcNature(7331L, 404L)

		then:

		template.getTestCaseNatures() == infoList
		1 * infoListDao.setDefaultNatureForProject(404L, defaultItem)
		1 * project1.setTestCaseNatures(infoList)
		1 * project2.setTestCaseNatures(infoList)
		1 * infoListDao.setDefaultNatureForProject(4L, defaultItem)
		1 * infoListDao.setDefaultNatureForProject(5L, defaultItem)
	}



	def "#bindListToProjectTcType - Should bind TcTypeInfoList to a Project"() {

		given:

		Project project = new Project()

		InfoList infoList = Mock()
		InfoListItem defaultItem = Mock()
		infoList.getDefaultItem() >> defaultItem

		and:

		genericProjectDao.findOne(404L) >> project
		infoListDao.findOne(7331L) >> infoList

		when:

		manager.bindListToProjectTcType(7331L, 404L)

		then:

		project.getTestCaseTypes() == infoList
		1 * infoListDao.setDefaultTypeForProject(404L, defaultItem)
	}

	def "#bindListToProjectTcType - Should bind TcTypeInfoList InfoList to a Template and propagate it to its bound Projects"() {

		given:

		ProjectTemplate template = new ProjectTemplate()

		Project project1 = Mock()
		project1.getId() >> 4L
		Project project2 = Mock()
		project2.getId() >> 5L

		InfoList infoList = Mock()
		InfoListItem defaultItem = Mock()
		infoList.getDefaultItem() >> defaultItem

		and:

		genericProjectDao.findOne(404L) >> template
		infoListDao.findOne(7331L) >> infoList

		and:

		projectDao.findAllBoundToTemplate(404L) >> [project1, project2]

		when:

		manager.bindListToProjectTcType(7331L, 404L)

		then:

		template.getTestCaseTypes() == infoList
		1 * infoListDao.setDefaultTypeForProject(404L, defaultItem)
		1 * project1.setTestCaseTypes(infoList)
		1 * project2.setTestCaseTypes(infoList)
		1 * infoListDao.setDefaultTypeForProject(4L, defaultItem)
		1 * infoListDao.setDefaultTypeForProject(5L, defaultItem)
	}

}
