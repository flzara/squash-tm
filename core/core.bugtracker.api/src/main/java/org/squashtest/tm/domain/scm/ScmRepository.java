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

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "SCM_REPOSITORY")
public class ScmRepository {

	@Id
	@Column(name = "SCM_REPOSITORY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "scm_repository_id_seq")
	@SequenceGenerator(name = "scm_repository_id_seq", sequenceName = "scm_repository_id_seq")
	private Long id;

	@Column(name = "NAME")
	@NotBlank
	@Size(max = 255)
	private String name;

	@Column(name = "REPOSITORY_PATH")
	@NotBlank
	private String repositoryPath;

	@Column(name = "FOLDER_PATH")
	private String folderPath;

	@Column(name = "BRANCH")
	@NotBlank
	private String branch;

	@ManyToOne
	@JoinColumn(name = "SCM_SERVER_ID", nullable = false)
	private ScmServer scmServer;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}
	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public ScmServer getScmServer() {
		return scmServer;
	}
	public void setScmServer(ScmServer scmServer) {
		this.scmServer = scmServer;
	}
}
