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
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.project.ProjectTemplate
import org.squashtest.tm.service.internal.customfield.CustomFieldBindingModificationServiceImpl
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao
import org.squashtest.tm.service.internal.repository.CustomFieldDao
import org.squashtest.tm.service.internal.repository.GenericProjectDao
import spock.lang.IgnoreIf
import spock.lang.Specification

class CustomFieldBindingModificationServiceImplTest extends Specification {

	CustomFieldBindingModificationServiceImpl service = new CustomFieldBindingModificationServiceImpl()

	CustomFieldDao customFieldDao = Mock()
	CustomFieldBindingDao customFieldBindingDao = Mock()
	GenericProjectDao genericProjectDao = Mock()

	PrivateCustomFieldValueService customValueService = Mock()
	ApplicationEventPublisher eventPublisher = Mock()

	def setup() {
		service.customFieldDao = customFieldDao
		service.customFieldBindingDao = customFieldBindingDao
		service.genericProjectDao = genericProjectDao

		service.customValueService = customValueService

		service.eventPublisher = eventPublisher
	}


	def "#copyCustomFieldsSettingsFromTemplate - Should copy paste all cuf binding from template"() {

		given: "The Project"

		Project project = Mock()
		project.getId() >> 3L

		and:

		genericProjectDao.findOne(3L) >> project

		and: "The Template"

		ProjectTemplate template = Mock()
		template.getId() >> 2L

		and: "Some CustomFieldsBindings"

		CustomField cuf = Mock()
		cuf.getId() >> 4L

		customFieldDao.findById(4L) >> cuf

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

		genericProjectDao.findOne(2L) >> project
		customFieldDao.findById(4L) >> cuf
		customFieldBindingDao.countAllForProjectAndEntity(2L, entity2) >> 1L

		genericProjectDao.isProjectTemplate(2L) >> false

		when:

		service.copyCustomFieldsSettingsFromTemplate(project, template)

		then:

		1 * customFieldBindingDao.save(_)
		1 * customValueService.cascadeCustomFieldValuesCreation(_)

	}
	
}
