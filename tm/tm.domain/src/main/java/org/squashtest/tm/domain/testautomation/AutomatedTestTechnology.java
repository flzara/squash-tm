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
package org.squashtest.tm.domain.testautomation;

import org.squashtest.tm.domain.Identified;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class AutomatedTestTechnology implements Identified {
	@Id
	@Column(name="AT_TECHNOLOGY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "automated_test_technology_at_technology_id_seq")
	@SequenceGenerator(name = "automated_test_technology_at_technology_id_seq", sequenceName = "automated_test_technology_at_technology_id_seq", allocationSize = 1)
	private Long id;

	@NotNull
	@Size(max = 50)
	private String name;

	@NotNull
	@Column(name = "ACTION_PROVIDER_KEY")
	@Size(max = 50)
	private String actionProviderKey;

	@Override
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getActionProviderKey() {
		return actionProviderKey;
	}
}
