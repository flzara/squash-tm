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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.library.FolderModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;

/**
 * Generic management service for folders. It is responsible for common rename / move / copy / remove operations.
 *
 * @author Gregory Fouquet
 *
 * @param <FOLDER>
 *            Type of folders managed by this object
 * @param <NODE>
 *            Supertype of FOLDER manageable by a Library
 */
@Transactional
public class GenericFolderModificationService<FOLDER extends Folder<NODE>, NODE extends LibraryNode> implements
FolderModificationService<FOLDER> {

	private final PermissionEvaluationService permissionService;
	private final GenericNodeManagementService<FOLDER, NODE, FOLDER> delegate;
	private final FolderDao<FOLDER, NODE> folderDao;
	private final LibraryDao<? extends Library<NODE>, NODE> libraryDao;

	public GenericFolderModificationService(PermissionEvaluationService permissionService, FolderDao<FOLDER, NODE> folderDao, LibraryDao<? extends Library<NODE>, NODE> libraryDao) {
		this.permissionService = permissionService;
		this.folderDao = folderDao;
		this.libraryDao = libraryDao;
        delegate = new GenericNodeManagementService<>(permissionService, folderDao, folderDao, libraryDao);
	}

	@Transactional(readOnly = true)
	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public FOLDER findFolder(long folderId) {
		return delegate.findNode(folderId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void removeFolder(long folderId) {
		// check
		checkPermission(new SecurityCheckableItem(folderId, SecurityCheckableItem.FOLDER, "DELETE"));
		// proceed
		delegate.removeNode(folderId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void renameFolder(long folderId, String newName) {
		// check
		checkPermission(new SecurityCheckableItem(folderId, SecurityCheckableItem.FOLDER, "WRITE"));
		// proceed
		delegate.renameNode(folderId, newName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void updateFolderDescription(long folderId, String newDescription) {
		// check
		checkPermission(new SecurityCheckableItem(folderId, SecurityCheckableItem.FOLDER, "WRITE"));
		// proceed
		delegate.updateNodeDescription(folderId, newDescription);
	}

	/* *************** private section ************************ */

	private class SecurityCheckableItem {
		private static final String FOLDER = "folder";
		private static final String LIBRARY = "library";

		private final long domainObjectId;
		private String domainObjectKind; // which should be one of the two above
		private final String permission;

		public SecurityCheckableItem(long domainObjectId, String domainObjectKind, String permission) {
			super();
			this.domainObjectId = domainObjectId;
			setKind(domainObjectKind);
			this.domainObjectKind = domainObjectKind;
			this.permission = permission;
		}

		private void setKind(String kind) {
			if (!kind.equals(SecurityCheckableItem.FOLDER) || kind.equals(SecurityCheckableItem.LIBRARY)) {
				throw new IllegalArgumentException(
						"(dev note : AbstracLibraryNavigationService : manual security checks aren't correctly configured");
			}
			domainObjectKind = kind;
		}

		public long getId() {
			return domainObjectId;
		}

		public String getKind() {
			return domainObjectKind;
		}

		public String getPermission() {
			return permission;
		}

	}

	private void checkPermission(SecurityCheckableItem... securityCheckableItems) throws AccessDeniedException {

		for (SecurityCheckableItem item : securityCheckableItems) {

			Object domainObject;

			if (item.getKind().equals(SecurityCheckableItem.FOLDER)) {
				domainObject = folderDao.findById(item.getId());
			} else {
				domainObject = libraryDao.findById(item.getId());
			}

			if (!permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", item.getPermission(), domainObject)) {
				throw new AccessDeniedException("Access is denied");
			}
		}
	}

}
