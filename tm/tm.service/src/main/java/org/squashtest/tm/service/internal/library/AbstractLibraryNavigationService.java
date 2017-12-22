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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.library.ExportData;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.library.NameAlreadyExistsAtDestinationException;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

/**
 * Generic implementation of a library navigation service.
 *
 * @param <LIBRARY>
 * @param <FOLDER>
 * @param <NODE>
 * @author Gregory Fouquet
 */

/*
 * Security Implementation note :
 *
 * this is sad but we can't use the annotations here. We would need the actual type of the entities we need to check
 * instead of the generics. So we'll call the PermissionEvaluationService explicitly once we've fetched the entities
 * ourselves.
 *
 *
 * @author bsiri
 */

/*
 * Note : about methods moving entities from source to destinations :
 *
 * Basically such operations need to be performed in a precise order, that is : 1) remove the entity from the source
 * collection and 2) insert it in the new one.
 *
 * However Hibernate performs batch updates in the wrong order, ie it inserts new data before deleting the former ones,
 * thus violating many unique constraints DB side. So we explicitly flush the session between the removal and the
 * insertion.
 *
 *
 * @author bsiri
 */

/*
 * Note regarding type safety when calling checkPermission(SecurityCheckableObject...) : see bug at
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6227971
 *
 * @author bsiri
 */

@Transactional
public abstract class AbstractLibraryNavigationService<LIBRARY extends Library<NODE>, FOLDER extends Folder<NODE>, NODE extends LibraryNode>
	implements LibraryNavigationService<LIBRARY, FOLDER, NODE> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLibraryNavigationService.class);
	private static final String CREATE = "CREATE";
	private static final String READ = "READ";

	@Inject
	protected PermissionEvaluationService permissionService;


	@Inject
	private PrivateCustomFieldValueService customFieldValuesService;
	@Inject
	private Provider<TreeNodeCopier> treeNodeCopierProvider;
	@Inject
	private Provider<FirstLayerTreeNodeMover> firstLayerMoverProvider;
	@Inject
	private Provider<NextLayersTreeNodeMover> nextLayersMoverProvider;

	public AbstractLibraryNavigationService() {
		super();
	}


	protected abstract FolderDao<FOLDER, NODE> getFolderDao();

	protected abstract LibraryDao<LIBRARY, NODE> getLibraryDao();

	protected abstract LibraryNodeDao<NODE> getLibraryNodeDao();

	protected abstract NodeDeletionHandler<NODE, FOLDER> getDeletionHandler();

	protected abstract PasteStrategy<FOLDER, NODE> getPasteToFolderStrategy();

	protected abstract PasteStrategy<LIBRARY, NODE> getPasteToLibraryStrategy();
	
	
	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public final List<NODE> findLibraryRootContent(long libraryId) {
		return getLibraryDao().findAllRootContentById(libraryId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public final List<NODE> findFolderContent(long folderId) {
		return getFolderDao().findAllContentById(folderId);
	}

	@Override
	public final LIBRARY findLibrary(long libraryId) {
		// fetch
		LIBRARY library = getLibraryDao().findById(libraryId);
		// check
		checkPermission(new SecurityCheckableObject(library, READ));
		// proceed
		return library;
	}

	@Override
	public final LIBRARY findCreatableLibrary(long libraryId) {
		// fetch
		LIBRARY library = getLibraryDao().findById(libraryId);
		// check
		checkPermission(new SecurityCheckableObject(library, CREATE));
		// proceed
		return library;
	}

	@Override
	public final FOLDER findFolder(long folderId) {
		// fetch
		FOLDER folder = getFolderDao().findById(folderId);
		// check
		checkPermission(new SecurityCheckableObject(folder, READ));
		// proceed
		return getFolderDao().findById(folderId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addFolderToLibrary(long destinationId, FOLDER newFolder) {
		// fetch
		LIBRARY container = getLibraryDao().findById(destinationId);
		// check
		checkPermission(new SecurityCheckableObject(container, CREATE));

		// proceed
		container.addContent((NODE) newFolder);
		getFolderDao().persist(newFolder);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addFolderToFolder(long destinationId, FOLDER newFolder) {
		// fetch
		FOLDER container = getFolderDao().findById(destinationId);
		// check
		checkPermission(new SecurityCheckableObject(container, CREATE));

		container.addContent((NODE) newFolder);
		getFolderDao().persist(newFolder);

	}

	@Override
	public FOLDER findParentIfExists(LibraryNode node) {
		return getFolderDao().findParentOf(node.getId());
	}

	@Override
	public LIBRARY findLibraryOfRootNodeIfExist(NODE node) {
		return getLibraryDao().findByRootContent(node);
	}

	// ************************* custom field values *************************

	protected void createCustomFieldValues(BoundEntity entity) {
		customFieldValuesService.createAllCustomFieldValues(entity, entity.getProject());
	}

	protected void createCustomFieldValues(Collection<? extends BoundEntity> entities) {
		for (BoundEntity entity : entities) {
			createCustomFieldValues(entity);
		}
	}

	/**
	 * initialCustomFieldValues maps the id of a CustomField to the value of the corresponding CustomFieldValues for
	 * that BoundEntity.
	 * read it again until it makes sense.
	 * it assumes that the CustomFieldValues instances already exists.
	 */
	protected void initCustomFieldValues(BoundEntity entity, Map<Long, RawValue> initialCustomFieldValues) {

		List<CustomFieldValue> persistentValues = customFieldValuesService.findAllCustomFieldValues(entity);

		for (CustomFieldValue value : persistentValues) {
			Long customFieldId = value.getCustomField().getId();

			if (initialCustomFieldValues.containsKey(customFieldId)) {
				RawValue newValue = initialCustomFieldValues.get(customFieldId);
				newValue.setValueFor(value);
			}
		}
	}

	/* ********************** move operations *************************** */
	@Override
	public void moveNodesToFolder(long destinationId, Long[] targetIds) {
		if (targetIds.length == 0) {
			return;
		}
		try {
			PasteStrategy<FOLDER, NODE> pasteStrategy = getPasteToFolderStrategy();
			makeMoverStrategy(pasteStrategy);
			pasteStrategy.pasteNodes(destinationId, Arrays.asList(targetIds));
		} catch (NullArgumentException | DuplicateNameException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		}

	}

	@Override
	public void moveNodesToLibrary(long destinationId, Long[] targetIds) {
		if (targetIds.length == 0) {
			return;
		}
		try {
			PasteStrategy<LIBRARY, NODE> pasteStrategy = getPasteToLibraryStrategy();
			makeMoverStrategy(pasteStrategy);
			pasteStrategy.pasteNodes(destinationId, Arrays.asList(targetIds));
		} catch (NullArgumentException | DuplicateNameException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		}

	}

	@Override
	public void moveNodesToFolder(long destinationId, Long[] targetIds, int position) {
		if (targetIds.length == 0) {
			return;
		}
		try {
			PasteStrategy<FOLDER, NODE> pasteStrategy = getPasteToFolderStrategy();
			makeMoverStrategy(pasteStrategy);
			pasteStrategy.pasteNodes(destinationId, Arrays.asList(targetIds), position);
		} catch (NullArgumentException | DuplicateNameException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		}

	}

	@Override
	public void moveNodesToLibrary(long destinationId, Long[] targetIds, int position) {
		if (targetIds.length == 0) {
			return;
		}
		try {
			PasteStrategy<LIBRARY, NODE> pasteStrategy = getPasteToLibraryStrategy();
			makeMoverStrategy(pasteStrategy);
			pasteStrategy.pasteNodes(destinationId, Arrays.asList(targetIds), position);
		} catch (NullArgumentException | DuplicateNameException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		}

	}
	/* ********************************* copy operations ****************************** */

	@Override
	public List<NODE> copyNodesToFolder(long destinationId, Long[] sourceNodesIds) {
		PasteStrategy<FOLDER, NODE> pasteStrategy = getPasteToFolderStrategy();
		makeCopierStrategy(pasteStrategy);
		return pasteStrategy.pasteNodes(destinationId, Arrays.asList(sourceNodesIds));

	}

	@Override
	public List<NODE> copyNodesToLibrary(long destinationId, Long[] targetIds) {
		PasteStrategy<LIBRARY, NODE> pasteStrategy = getPasteToLibraryStrategy();
		makeCopierStrategy(pasteStrategy);
		return pasteStrategy.pasteNodes(destinationId, Arrays.asList(targetIds));

	}

	/* ***************************** deletion operations *************************** */

	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		return getDeletionHandler().simulateDeletion(targetIds);
	}

	@Override
	public OperationReport deleteNodes(List<Long> targetIds) {

		// check. Note : we wont recursively check for the whole hierarchy as it's supposed to have the same
		// identity holder
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			checkPermission(new SecurityCheckableObject(node, "DELETE"));
		}

		return getDeletionHandler().deleteNodes(targetIds);
	}

	/* ************************* private stuffs ************************* */

	protected void makeCopierStrategy(PasteStrategy<?, ?> pasteStrategy) {
		pasteStrategy.setFirstLayerOperationFactory(treeNodeCopierProvider);
		pasteStrategy.setNextLayersOperationFactory(treeNodeCopierProvider);
	}

	protected void makeMoverStrategy(PasteStrategy<?, ?> pasteStrategy) {
		pasteStrategy.setFirstLayerOperationFactory(firstLayerMoverProvider);
		pasteStrategy.setNextLayersOperationFactory(nextLayersMoverProvider);
	}

	protected void checkPermission(SecurityCheckableObject... checkableObjects) {
		PermissionsUtils.checkPermission(permissionService, checkableObjects);
	}

	protected List<? extends ExportData> setFullFolderPath(List<? extends ExportData> dataset) {
		for (ExportData data : dataset) {
			// get folder id
			Long id = data.getFolderId();
			// set the full path attribute
			StringBuilder path = new StringBuilder();

			// if the requirement is not directly located under
			if (!id.equals(ExportData.NO_FOLDER)) {
				for (String name : getLibraryNodeDao().getParentsName(id)) {
					path.append('/').append(name);
				}
				if (path.length() != 0) {
					path.deleteCharAt(0);
				}
			}
			data.setFolderName(path.toString());
		}
		return dataset;
	}

	protected Set<Long> securityFilterIds(Collection<Long> original, String entityType, String permission) {
		Set<Long> effective = new HashSet<>();
		for (Long id : original) {
			if (permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", permission, id, entityType)) {
				effective.add(id);
			}
		}
		return effective;
	}

}
