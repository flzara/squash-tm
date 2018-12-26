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
package org.squashtest.tm.domain.scm;

import org.squashtest.tm.domain.servers.ThirdPartyServer;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SCM_SERVER")
@PrimaryKeyJoinColumn(name = "SERVER_ID")
public class ScmServer extends ThirdPartyServer {


	@Column(name = "KIND")
	@Size(max = 30)
	private String kind;

	@OneToMany(mappedBy = "scmServer", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<ScmRepository> repositories = new ArrayList<>();



	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}

	public List<ScmRepository> getRepositories() {
		return repositories;
	}
	public void setRepositories(List<ScmRepository> repositories) {
		this.repositories = repositories;
	}
}
