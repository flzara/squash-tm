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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "SCM_REPOSITORY")
public class ScmRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmRepository.class);

	private static final Map<Long, Object> repositoriesLocks = new ConcurrentHashMap<>();

	@Id
	@Column(name = "SCM_REPOSITORY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "scm_repository_scm_repository_id_seq")
	@SequenceGenerator(name = "scm_repository_scm_repository_id_seq", sequenceName = "scm_repository_scm_repository_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "NAME")
	@NotBlank
	@Size(max = 255)
	private String name;

	/**
	 * The path of the base folder
	 *
	 */
	@Column(name = "REPOSITORY_PATH")
	@NotBlank
	private String repositoryPath;

	/**
	 * The path of the working folder
	 *
	 */
	@Column(name = "WORKING_FOLDER_PATH")
	private String workingFolderPath;

	@Column(name = "WORKING_BRANCH")
	@NotBlank
	private String workingBranch;

	@ManyToOne
	@JoinColumn(name = "SERVER_ID", nullable = false)
	private ScmServer scmServer;

	private Object acquireLock() {
		return repositoriesLocks.computeIfAbsent(id, id -> new Object());
	}

	public <T> T doWithLock(IOSupplier<T> operation) throws IOException {
		T result;

		LOGGER.trace("attempting to acquire lock on repository '{}'", name);

		Object lock = acquireLock();
		synchronized (lock) {
			LOGGER.trace("lock acquired on repository '{}'", name);
			result = operation.get();
		}
		LOGGER.trace("lock released on repository '{}'", name);
		return result;
	}

	/**
	 * Returns the repository base directory as a File
	 *
	 * @return
	 */
	public File getBaseRepositoryFolder(){
		return new File(getRepositoryPath());
	}

	/**
	 * Returns the repository working folder as a File
	 *
	 * @return
	 */
	public File getWorkingFolder(){
		if (StringUtils.isBlank(workingFolderPath)){
			return getBaseRepositoryFolder();
		}
		else{
			return FileUtils.getFile(repositoryPath, workingFolderPath);
		}
	}

	public Collection<File> listWorkingFolderContent() throws IOException{
		File workingFolder = getWorkingFolder();
		return doWithLock(() -> FileUtils.listFiles(workingFolder, null, true));
	}

	@FunctionalInterface
	public interface IOSupplier<T> {
		T get() throws IOException;
	}

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

	public String getWorkingBranch() {
		return workingBranch;
	}
	public void setWorkingBranch(String workingBranch) {
		this.workingBranch = workingBranch;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}
	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	public String getWorkingFolderPath() {
		return workingFolderPath;
	}
	public void setWorkingFolderPath(String workingFolderPath) {
		this.workingFolderPath = workingFolderPath;
	}

	public ScmServer getScmServer() {
		return scmServer;
	}
	public void setScmServer(ScmServer scmServer) {
		this.scmServer = scmServer;
	}
}
