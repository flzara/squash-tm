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
package org.squashtest.tm.service.library;

import java.util.List;

import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;

/**
 * Defines common methods for a library navigation service, mainly library access and folder manipulation.
 *
 * TODO Move all methods which modify a folder's content to another service
 *
 * @author Gregory Fouquet
 *
 */
public interface LibraryNavigationService<LIBRARY extends Library<? extends NODE>, FOLDER extends Folder<? extends NODE>, NODE extends LibraryNode> {

	/**
	 * will create a deep copy of the given LibraryNodes, paste them in the destination folder, and return the copies.
	 *
	 *
	 * @param destinationId
	 *            the id of the folder where you need to copy to.
	 * @param sourceNodesIds
	 *            the list of the librarynodes we want copies of.
	 * @return the list of the copies themselves.
	 */
	List<NODE> copyNodesToFolder(long destinationId, Long[] sourceNodesIds);

	/**
	 * same, when the destination is a Library.
	 *
	 *
	 * @param destinationId
	 *            the id of the library where you need to copy to.
	 * @param targetId
	 *            the list of the librarynodes we want copies of.
	 * @return the list of the copies themselves.
	 */
	List<NODE> copyNodesToLibrary(long destinationId, Long[] targetId);

	void moveNodesToFolder(long destinationId, Long[] targetId);

	void moveNodesToLibrary(long destinationId, Long[] targetId);

	void moveNodesToFolder(long destinationId, Long[] targetId, int position);

	void moveNodesToLibrary(long destinationId, Long[] targetId, int position);

	/**
	 *{@link Id} annotation is used by {@link PreventConcurrent}, {@link BatchPreventConcurrent} and {@link PreventConcurrents} in sub classes
	 * @param destinationId
	 * @param newFolder
	 */
	void addFolderToLibrary(@Id long destinationId, FOLDER newFolder);

	/**
	 * {@link Id} annotation is used by {@link PreventConcurrent}, {@link BatchPreventConcurrent} and {@link PreventConcurrents} in sub classes
	 * @param destinationId
	 * @param newFolder
	 */
	void addFolderToFolder(@Id long destinationId, FOLDER newFolder);

	FOLDER findFolder(long folderId);

	List<NODE> findLibraryRootContent(long libraryId);

	/**
	 * Returns the content of the folder designated by its id.
	 * <
	 * @param folderId
	 * @return
	 */
	List<NODE> findFolderContent(long folderId);

	LIBRARY findLibrary(long libraryId);
	LIBRARY findCreatableLibrary(long libraryId);

	/**
	 * that method should investigate the consequences of the deletion request, and return a report about what will
	 * happen.
	 *
	 * @param targetIds
	 * @return
	 */
	List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds);

	/**
	 * that method should delete the nodes. It still takes care of non deletable nodes so the implementation should
	 * filter out the ids who can't be deleted.
	 *
	 *
	 * @param targetIds
	 * @return
	 */
	OperationReport deleteNodes(List<Long> targetIds);

	/**
	 * That method should find the parentFolder of the library Node if it has one
	 *
	 * @param node
	 * @return the parent node or null if do not exists
	 */
	FOLDER findParentIfExists(LibraryNode node);

	/**
	 * That method should find the library of the root node
	 *
	 * @param id
	 * @return the library or null
	 */
	LIBRARY findLibraryOfRootNodeIfExist(NODE node);

}
