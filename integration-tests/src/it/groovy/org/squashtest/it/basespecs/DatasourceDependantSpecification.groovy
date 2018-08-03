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
package org.squashtest.it.basespecs

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.EnableLoadTimeWeaving
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.springframework.context.annotation.aspectj.SpringConfiguredConfiguration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.squashtest.it.config.DatasourceSpecConfig
import org.squashtest.it.config.JooqSpecConfig
import org.squashtest.tm.service.RepositoryConfig
import org.squashtest.tm.service.internal.campaign.scripted.ScriptedExecutionConfiguration
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@ContextConfiguration(classes = [DatasourceSpecConfig, RepositoryConfig, JooqSpecConfig, ScriptedExecutionConfiguration])
@TestPropertySource(["classpath:other_properties.properties", "classpath:hibernate.properties"])
@ImportAutoConfiguration(HibernateJpaAutoConfiguration)
class DatasourceDependantSpecification extends SpringConfigurableConfiguration {

	@PersistenceContext
	EntityManager em;



}
