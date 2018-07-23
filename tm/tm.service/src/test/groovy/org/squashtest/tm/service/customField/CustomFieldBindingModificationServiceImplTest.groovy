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
package org.squashtest.tm.service.customField


import org.springframework.context.ApplicationEventPublisher
import org.springframework.util.ReflectionUtils
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.project.ProjectTemplate
import org.squashtest.tm.exception.project.LockedParameterException
import org.squashtest.tm.service.internal.customfield.CustomFieldBindingModificationServiceImpl
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.dto.BindableEntityModel
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel
import org.squashtest.tm.service.internal.dto.CustomFieldModel
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao
import org.squashtest.tm.service.internal.repository.CustomFieldDao
import org.squashtest.tm.service.internal.repository.GenericProjectDao
import org.squashtest.tm.service.internal.repository.ProjectDao
import spock.lang.IgnoreIf
import spock.lang.Specification

class CustomFieldBindingModificationServiceImplTest extends Specification {

	CustomFieldBindingModificationServiceImpl service = new CustomFieldBindingModificationServiceImpl()

	CustomFieldDao customFieldDao = Mock()
	CustomFieldBindingDao customFieldBindingDao = Mock()
	GenericProjectDao genericProjectDao = Mock()
	ProjectDao projectDao = Mock()

	PrivateCustomFieldValueService customValueService = Mock()
	ApplicationEventPublisher eventPublisher = Mock()

	def setup() {
		service.customFieldDao = customFieldDao
		service.customFieldBindingDao = customFieldBindingDao
		service.genericProjectDao = genericProjectDao
		service.projectDao = projectDao

		service.customValueService = customValueService

		service.eventPublisher = eventPublisher
	}


	def "#copyCustomFieldsSettingsFromTemplate - Should copy paste all cuf binding from template"() {

		given: "The Project"

		Project project = Mock()
		project.getId() >> 3L

		and:

		genericProjectDao.getOne(3L) >> project

		and: "The Template"

		ProjectTemplate template = Mock()
		template.getId() >> 2L

		and: "Some CustomFieldsBindings"

		CustomField cuf = Mock()
		cuf.getId() >> 4L

		customFieldDao.getOne(4L) >> cuf

		BindableEntity entity1 = Mock()
		BindableEntity entity2 = Mock()

		CustomFieldBinding binding1 = Mock()
		CustomFieldBinding binding2 = Mock()

		binding1.getBoundEntity() >> entity1
		binding1.getCustomField() >> cuf

		binding2.getBoundEntity() >> entity2
		binding2.getCustomField() >> cuf

		List<CustomFieldBinding> bindings = [binding1, binding2]

		and:

		customFieldBindingDao.findAllForGenericProject(2L) >> bindings

		genericProjectDao.isProjectTemplate(3L) >> false

		customFieldBindingDao.cufBindingAlreadyExists(3L, _, 4L) >> false

		customFieldBindingDao.countAllForProjectAndEntity(3L, entity1) >> 1
		customFieldBindingDao.countAllForProjectAndEntity(3L, entity2) >> 2

		when:

		service.copyCustomFieldsSettingsFromTemplate(project, template)

		then:

		2 * customFieldBindingDao.save(_)
		2 * customValueService.cascadeCustomFieldValuesCreation(_)

	}

	def "#copyCustomFieldsSettingsFromTemplate - Should only copy one cuf binding from template"() {

		given: "The Template"

		ProjectTemplate template = Mock()
		template.getId() >> 1L

		and: "The Project"

		Project project = Mock()
		project.getId() >> 2L

		and: "Some CustomFieldBindings"

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

		and:

		customFieldBindingDao.findAllForGenericProject(1L) >> bindings

		customFieldBindingDao.cufBindingAlreadyExists(2L, (BindableEntity) _, 4L) >>> [true, false]

		genericProjectDao.getOne(2L) >> project
		customFieldDao.getOne(4L) >> cuf
		customFieldBindingDao.countAllForProjectAndEntity(2L, entity2) >> 1L

		genericProjectDao.isProjectTemplate(2L) >> false

		when:

		service.copyCustomFieldsSettingsFromTemplate(project, template)

		then:

		1 * customFieldBindingDao.save(_)
		1 * customValueService.cascadeCustomFieldValuesCreation(_)

	}

	def "#createNewBindings - Should create some CustomFieldBindings"() {

		given: "2 CustomField Models"


		BindableEntityModel entity01 = Mock()
		BindableEntity entity1 = Mock()
		entity01.toDomain() >> entity1

		CustomFieldModel cuf01 = Mock()
		cuf01.getId() >> 1L

		CustomFieldBindingModel cufModel1 = Mock()
		cufModel1.getProjectId() >> 404L
		cufModel1.getCustomField() >> cuf01
		cufModel1.getBoundEntity() >> entity01


		BindableEntityModel entity02 = Mock()
		BindableEntity entity2 = Mock()
		entity02.toDomain() >> entity2

		CustomFieldModel cuf02 = Mock()
		cuf02.getId() >> 2L

		CustomFieldBindingModel cufModel2 = Mock()
		cufModel2.getProjectId() >> 404L
		cufModel2.getCustomField() >> cuf02
		cufModel2.getBoundEntity() >> entity02

		CustomFieldBindingModel[] bindingModels = [cufModel1, cufModel2]

		and: "2 real CustomFields and 1 Project"

		GenericProject project = Mock()
		project.getId() >> 404L
		CustomField cuf1 = Mock()
		CustomField cuf2 = Mock()

		and:

		genericProjectDao.isBoundToATemplate(404L) >> false
		genericProjectDao.getOne(404) >> project
		customFieldDao.getOne(1L) >> cuf1
		customFieldDao.getOne(2L) >> cuf2
		customFieldBindingDao.countAllForProjectAndEntity(404L, _) >> 2L

		and:

		genericProjectDao.isProjectTemplate(_) >> false

		when:

		service.createNewBindings(bindingModels)

		then:

		2 * customFieldBindingDao.save(_)
		2 * eventPublisher.publishEvent(_)
	}

	def "#createNewBindings - Should not create some CustomFieldBindings because the Project is bound to a Template"() {

		given: "2 CustomField Models"


		BindableEntityModel entity01 = Mock()
		BindableEntity entity1 = Mock()
		entity01.toDomain() >> entity1

		CustomFieldModel cuf01 = Mock()
		cuf01.getId() >> 1L

		CustomFieldBindingModel cufModel1 = Mock()
		cufModel1.getProjectId() >> 404L
		cufModel1.getCustomField() >> cuf01
		cufModel1.getBoundEntity() >> entity01


		BindableEntityModel entity02 = Mock()
		BindableEntity entity2 = Mock()
		entity02.toDomain() >> entity2

		CustomFieldModel cuf02 = Mock()
		cuf02.getId() >> 2L

		CustomFieldBindingModel cufModel2 = Mock()
		cufModel2.getProjectId() >> 404L
		cufModel2.getCustomField() >> cuf02
		cufModel2.getBoundEntity() >> entity02

		CustomFieldBindingModel[] bindingModels = [cufModel1, cufModel2]

		and:

		genericProjectDao.isBoundToATemplate(404L) >> true

		when:

		service.createNewBindings(bindingModels)

		then:

		thrown LockedParameterException
	}

	def "#createNewBindings - Should create CustomFieldBindings on Template and propagate it to the bound Project"() {

		given: "2 CustomField Models"

		BindableEntityModel entity01 = Mock()
		BindableEntity entity1 = Mock()
		entity01.toDomain() >> entity1

		CustomFieldModel cuf01 = Mock()
		cuf01.getId() >> 1L

		CustomFieldBindingModel cufModel1 = Mock()
		cufModel1.getProjectId() >> 404L
		cufModel1.getCustomField() >> cuf01
		cufModel1.getBoundEntity() >> entity01


		BindableEntityModel entity02 = Mock()
		BindableEntity entity2 = Mock()
		entity02.toDomain() >> entity2

		CustomFieldModel cuf02 = Mock()
		cuf02.getId() >> 2L

		CustomFieldBindingModel cufModel2 = Mock()
		cufModel2.getProjectId() >> 404L
		cufModel2.getCustomField() >> cuf02
		cufModel2.getBoundEntity() >> entity02

		CustomFieldBindingModel[] bindingModels = [cufModel1, cufModel2]

		and: "2 real CustomFields and 1 Project"

		ProjectTemplate template = Mock()
		template.getId() >> 404L
		Project project = Mock()
		project.getId() >> 42L
		CustomField cuf1 = Mock()
		CustomField cuf2 = Mock()

		/* --== Template part ==-- */
		and:

		genericProjectDao.isBoundToATemplate(404L) >> false
		genericProjectDao.getOne(404L) >> template
		customFieldDao.getOne(1L) >> cuf1
		customFieldDao.getOne(2L) >> cuf2
		customFieldBindingDao.countAllForProjectAndEntity(404L, _) >> 1

		and:

		genericProjectDao.isProjectTemplate(404L) >> true
		/* --== Project part ==--*/
		and:

		projectDao.findAllIdsBoundToTemplate(404L) >> [42L]
		customFieldBindingDao.cufBindingAlreadyExists(42, _, _) >>> [true, false]

		genericProjectDao.getOne(42L) >> project
		customFieldDao.getOne(2l) >> cuf2
		customFieldBindingDao.countAllForProjectAndEntity(42L, _) >> 1

		genericProjectDao.isProjectTemplate(42L) >> false

		when:

		service.createNewBindings(bindingModels)

		then:

		2 * customFieldBindingDao.save(_)
		2 * eventPublisher.publishEvent(_)

		1 * customFieldBindingDao.save(_)
		1 * eventPublisher.publishEvent(_)
		1 * customValueService.cascadeCustomFieldValuesCreation(_)
	}

	def "#removeCustomFieldBindings - Should delete some CustomFieldBindings"() {

		given:

		List<Long> bindingIds = [7L, 19L, 32L, 42L]

		and:

		genericProjectDao.oneIsBoundToABoundProject(_) >> false
		customFieldBindingDao.findEquivalentBindingsForBoundProjects(_) >> [9L, 52L, 90L, 44L]

		when:

		service.removeCustomFieldBindings(bindingIds)

		then:

		1 * customValueService.cascadeCustomFieldValuesDeletion(_)
		1 * customFieldBindingDao.removeCustomFieldBindings(_)
		1 * eventPublisher.publishEvent(_)
	}

	def "#removeCustomFieldBindings - Should not delete some CustomFieldBindings because one Project implied is bound to a Template"() {

		given:

		List<Long> bindingIds = [7L, 19L, 32L, 42L]

		and:

		genericProjectDao.oneIsBoundToABoundProject(_) >> true

		when:

		service.removeCustomFieldBindings(bindingIds)

		then:

		thrown LockedParameterException
	}
}
