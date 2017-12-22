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
package org.squashtest.tm.domain.users;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_GROUP")
public class UsersGroup {
	/**
	 * Qualified name of User group
	 */
	public static final String USER = "squashtest.authz.group.tm.User";
	/**
	 * Qualified name of Admin group
	 */
	public static final String ADMIN = "squashtest.authz.group.core.Admin";


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "core_group_id_seq")
	@SequenceGenerator(name = "core_group_id_seq", sequenceName = "core_group_id_seq", allocationSize = 1)
	private Long id;
	private String qualifiedName;
	private transient String simpleName;

	public UsersGroup() {
		if (qualifiedName != null) {
			this.calculateSimpleName();
		}
	}

	public String getSimpleName() {
		this.calculateSimpleName();
		return simpleName;
	}

	private void calculateSimpleName() {
		this.simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
	}

	public UsersGroup(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public String getQualifiedName() {
		return qualifiedName;

	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public Long getId() {
		return id;
	}

}
