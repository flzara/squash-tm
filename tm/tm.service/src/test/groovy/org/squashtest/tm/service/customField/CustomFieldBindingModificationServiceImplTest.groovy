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


	def "should copy paste cuf binding from template"() {
		given: "a project"
		Project project = Mock()
		project.getId() >> 3L
		genericProjectDao.findById(3L) >> project
		and: "a template"
		ProjectTemplate template = Mock()
		template.getId() >> 2L
		CustomField cuf = Mock()
		cuf.getId() >> 4L
		customFieldDao.findById(4L) >> cuf
		BindableEntity entity1 = Mock()
		customFieldBindingDao.countAllForProjectAndEntity(3L, entity1) >> 1
		BindableEntity entity2 = Mock()
		customFieldBindingDao.countAllForProjectAndEntity(3L, entity2) >> 2
		CustomFieldBinding binding1 = Mock()
		binding1.getBoundEntity() >> entity1
		binding1.getCustomField() >> cuf
		CustomFieldBinding binding2 = Mock()
		binding2.getBoundEntity() >> entity2
		binding2.getCustomField() >> cuf
		List<CustomFieldBinding> bindings = [binding1, binding2]
		customFieldBindingDao.findAllForGenericProject(2L) >> bindings

		when:
		service.copyCustomFieldsSettingsFromTemplate(project, template)
		then:
		2 * customFieldBindingDao.save(_)
		2 * customValueService.cascadeCustomFieldValuesCreation(_)
	}
}
