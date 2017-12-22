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
package org.squashtest.tm.service.internal.library;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.internal.repository.EntityDao;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

/**
 * Generic management service for library nodes. It is responsible for common operations such as rename / move / copy
 * and so on.
 *
 * @author Gregory Fouquet
 *
 * @param <FOLDER>
 *            Type of folder which can contain managed type
 * @param <NODE>
 *            Type of common node supertype of FOLDER and MANAGED
 * @param <MANAGED>
 *            Type of nodes manged by this class
 */
@Transactional
public class GenericNodeManagementService<MANAGED extends LibraryNode, NODE extends LibraryNode, FOLDER extends Folder<NODE>>
	implements NodeManagementService<MANAGED, NODE, FOLDER> {

	private final PermissionEvaluationService permissionService;

	private final EntityDao<MANAGED> nodeDao;

	private final FolderDao<FOLDER, NODE> folderDao;
	private final LibraryDao<? extends Library<NODE>, NODE> libraryDao;

	public GenericNodeManagementService(PermissionEvaluationService permissionService, EntityDao<MANAGED> nodeDao, FolderDao<FOLDER, NODE> folderDao, LibraryDao<? extends Library<NODE>, NODE> libraryDao) {
		this.permissionService = permissionService;
		this.nodeDao = nodeDao;
		this.folderDao = folderDao;
		this.libraryDao = libraryDao;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.csp.tm.internal.service.NodeManagementService#findNode(long)
	 */
	@Override
	@Transactional(readOnly = true)
	@PostAuthorize("hasPermission(returnObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public MANAGED findNode(long nodeId) {
		return nodeDao.findById(nodeId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.csp.tm.internal.service.NodeManagementService#removeNode(long)
	 */
	@Override
	public/* final */void removeNode(long nodeId) {
		MANAGED node = checkDeletableNode(nodeId);

		// proceed
		// TODO throw some exception instead
		if (node == null) {
			return;
		}
		nodeDao.remove(node);

	}

	/**
	 * check if the current user context has delete permission on the node.
	 *
	 * @param nodeId
	 * @return
	 */
	private MANAGED checkDeletableNode(long nodeId) {
		MANAGED node = nodeDao.findById(nodeId);
		checkPermission(new SecurityCheckableObject(node, "DELETE"));
		return node;
	}

	/**
	 * check if the current user context has WRITE permission on the node.
	 *
	 * @param nodeId
	 * @return
	 */
	private MANAGED checkWritableNode(long nodeId) {
		MANAGED node = nodeDao.findById(nodeId);
		checkPermission(new SecurityCheckableObject(node, "WRITE"));
		return node;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.squashtest.csp.tm.internal.service.NodeManagementService#renameNode(long, java.lang.String)
	 */
	@Override
	public final void renameNode(long nodeId, String newName) throws DuplicateNameException {
		MANAGED node = checkWritableNode(nodeId);

		// proceed
		renameNode(newName, node);
	}

	private void renameNode(String newName, MANAGED node) {
		String trimedNewName = newName.trim();
		if (notCurrentNameOfNode(trimedNewName, node)) {
			forcedRenameNode(node, trimedNewName);
		}
	}

	@Override
	public final void updateNodeDescription(long nodeId, String newDescription) {
		MANAGED node = checkWritableNode(nodeId);

		// proceed
		node.setDescription(newDescription);
	}

	/**
	 * Renames the node regardless its current name. In other words, renaming a node to its current name should fail.
	 *
	 * @param node
	 * @param trimedNewName the new name previously trimed.
	 */
	@SuppressWarnings("unchecked")
	private void forcedRenameNode(MANAGED node, String trimedNewName) {
		Library<?> library = libraryDao.findByRootContent((NODE) node);

		if (library != null) {
			if (!library.isContentNameAvailable(trimedNewName)) {
				throw new DuplicateNameException(node.getName(), trimedNewName);
			}
		} else {
			FOLDER parentFolder = folderDao.findByContent((NODE) node);

			if (parentFolder != null && !parentFolder.isContentNameAvailable(trimedNewName)) {
				throw new DuplicateNameException(node.getName(), trimedNewName);
			}
		}

		// TODO throw some exception if node is null
		node.setName(trimedNewName);
	}

	private boolean notCurrentNameOfNode(String newName, MANAGED node) {
		return !node.getName().equals(newName);
	}

	private void checkPermission(SecurityCheckableObject... checkableObjects) {
		PermissionsUtils.checkPermission(permissionService, checkableObjects);
	}
}
