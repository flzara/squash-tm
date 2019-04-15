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
package org.squashtest.tm.service.requirement


import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.projectfilter.ProjectFilter
import org.squashtest.tm.domain.requirement.*
import org.squashtest.tm.domain.resource.Resource
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService
import org.squashtest.tm.service.infolist.InfoListItemFinderService
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.service.internal.repository.RequirementFolderDao
import org.squashtest.tm.service.internal.repository.RequirementLibraryDao
import org.squashtest.tm.service.internal.requirement.RequirementLibraryNavigationServiceImpl
import org.squashtest.tm.service.milestone.MilestoneMembershipManager
import org.squashtest.tm.service.project.ProjectFilterModificationService
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.service.testutils.MockFactory
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import spock.lang.Specification

class RequirementLibraryNavigationServiceImplTest extends Specification {

	RequirementLibraryNavigationServiceImpl service = new RequirementLibraryNavigationServiceImpl()
	RequirementLibraryDao requirementLibraryDao = Mock()
	RequirementFolderDao requirementFolderDao = Mock()
	RequirementDao requirementDao = Mock()
	PermissionEvaluationService permissionService = Mock()
	ProjectFilterModificationService projectFilterModificationService = Mock()
	InfoListItemFinderService infoListItemService = Mock()
	MilestoneMembershipManager milestoneService = Mock()
	PrivateCustomFieldValueService customValueService = Mock()
	CustomFieldBindingFinderService customFieldBindingFinderService  = Mock()
	MockFactory mockFactory = new MockFactory()

	RequirementVersion version;	// used in some hacks

	def setup() {
		NewRequirementVersionDto.metaClass.sameAs = {
			it.name == delegate.name &&
					it.description == delegate.description &&
					it.criticality &&
					it.reference == delegate.reference
		}

		service.requirementLibraryDao = requirementLibraryDao
		service.requirementFolderDao = requirementFolderDao
		service.requirementDao = requirementDao
		service.projectFilterModificationService = projectFilterModificationService
		permissionService.hasRoleOrPermissionOnObject(_, _, _) >> true
		service.infoListItemService = infoListItemService
		service.milestoneService = milestoneService
		service.customFieldBindingFinderService = customFieldBindingFinderService
		service.customValueService = customValueService

		use (ReflectionCategory) {
			AbstractLibraryNavigationService.set(field: "permissionService", of: service, to: permissionService)
			AbstractLibraryNavigationService.set(field: "customFieldValuesService", of: service, to: customValueService)
		}

		customValueService.findAllCustomFieldValues(_) >> []
		customValueService.findAllCustomFieldValues(_, _) >> []

	}

	def "should add folder to library and persist the folder"() {
		given:
		RequirementFolder newFolder = Mock()

		and:
		Project project = Mock()
		newFolder.getProject() >> project
		project.getId() >>  10L

		and:
		CustomField cuf = Mock()
		cuf.getId() >> 4L

		BindableEntity entity1 = Mock()
		BindableEntity entity2 = Mock()

		CustomFieldBinding binding1 = Mock()
		CustomFieldBinding binding2 = Mock()

		binding1.getBoundEntity() >> entity1
		binding1.getCustomField() >> cuf

		binding2.getBoundEntity() >> entity2
		binding2.getCustomField() >> cuf

		List<CustomFieldBinding> bindings = [binding1, binding2]
		customFieldBindingFinderService.findCustomFieldsForProjectAndEntity(10L, BindableEntity.REQUIREMENT_FOLDER) >> bindings

		and:
		RequirementLibrary lib = Mock()
		requirementLibraryDao.findById(10) >> lib
		newFolder.getContent()>> []
		when:
		service.addFolderToLibrary(10, newFolder)

		then:
		1 * lib.addContent(newFolder)
		1 * requirementFolderDao.persist(newFolder)
	}

	def "should return root content of library"() {
		given:
		requirementLibraryDao.findAllRootContentById(10) >> [Mock(RequirementFolder)]

		when:
		def root = service.findLibraryRootContent(10)

		then:
		root.size() == 1
	}

	def "should return content of folder"() {
		given:
		requirementFolderDao.findAllContentById(10) >> [
			Mock(RequirementFolder),
			Mock(Requirement)
		]

		when:
		def root = service.findFolderContent(10);

		then:
		root.size() == 2
	}


	def "should create a Requirement in a RootContent"(){

		given :
		RequirementLibrary lib = Mock(RequirementLibrary)
		def proj =  mockFactory.mockProject()
		requirementLibraryDao.findById(1) >> lib

		and:
		def req = new NewRequirementVersionDto(name:"name", description: "desc", reference: "ref", category : "CAT_BUSINESS")

		lib.isContentNameAvailable(req.name) >> true

		when :
		def res = service.addRequirementToRequirementLibrary(1, req, [])

		then :
		1 * lib.addContent({
			this.version = it.currentVersion
			use (ReflectionCategory) {
				Resource.set(field : 'id', of : version, to : 1l)
			}
			it.notifyAssociatedWithProject(proj);
			req.sameAs it.currentVersion
		})
		1 * requirementDao.persist ({ req.sameAs it.currentVersion })
		req.sameAs res
	}


	def "should create a Requirement in a Folder"() {

		given :
		RequirementFolder folder = Mock(RequirementFolder)
		def proj =  mockFactory.mockProject()
		requirementFolderDao.findById(1) >> folder

		and:
		def req = new NewRequirementVersionDto(name:"name", category : "CAT_BUSINESS")
		folder.isContentNameAvailable(req.name) >> true

		when :
		def res = service.addRequirementToRequirementFolder(1, req, [])

		then :
		1 * folder.addContent({
			this.version = it.currentVersion
			use (ReflectionCategory) {
				Resource.set(field : 'id', of : version, to : 1l)
			}
			it.notifyAssociatedWithProject(proj);
			req.sameAs it.currentVersion
		})
		1 * requirementDao.persist ({ req.sameAs it.currentVersion })
		req.sameAs res
	}

	def "should not raise a duplicate name"(){
		given :
		RequirementLibrary lib = Mock(RequirementLibrary)
		def proj =  mockFactory.mockProject()
		requirementLibraryDao.findById(1) >> lib

		and:
		def req = new NewRequirementVersionDto(name:"name", category : "CAT_BUSINESS")
		lib.isContentNameAvailable(req.name) >> false

		when :
		def added = service.addRequirementToRequirementLibrary(1, req, [])

		then :
		1 * lib.addContent({
			this.version = it.currentVersion
			use (ReflectionCategory) {
				Resource.set(field : 'id', of : version, to : 1l)
			}
			it.notifyAssociatedWithProject(proj);
			req.sameAs it.currentVersion
		})
		1 * requirementDao.persist ({ req.sameAs it.currentVersion })
		req.sameAs added

	}

	def "should find library"() {
		given:
		RequirementLibrary l = Mock()
		requirementLibraryDao.findById(10) >> l

		when:
		def found = service.findLibrary(10)

		then:
		found == l
	}

	def "should find libraries of linkable requirements"() {
		given:
		RequirementLibrary lib = Mock()
		ProjectFilter pf = new ProjectFilter()
		pf.setActivated(false)
		projectFilterModificationService.findProjectFilterByUserLogin() >> pf
		requirementLibraryDao.findAll() >> [lib]

		when:
		def res =
				service.findLinkableRequirementLibraries()

		then:
		res == [lib]
	}

}
