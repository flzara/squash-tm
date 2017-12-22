/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.requirement;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.InconsistentInfoListItemException;
import org.squashtest.tm.exception.library.NameAlreadyExistsAtDestinationException;
import org.squashtest.tm.exception.requirement.CopyPasteObsoleteException;
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.Ids;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.batchexport.ExportDao;
import org.squashtest.tm.service.internal.batchexport.RequirementExcelExporter;
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel;
import org.squashtest.tm.service.internal.batchexport.SearchRequirementExcelExporter;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementExcelBatchImporter;
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.repository.RequirementLibraryDao;
import org.squashtest.tm.service.internal.requirement.coercers.RLNAndParentIdsCoercerForArray;
import org.squashtest.tm.service.internal.requirement.coercers.RLNAndParentIdsCoercerForList;
import org.squashtest.tm.service.internal.requirement.coercers.RequirementLibraryIdsCoercerForArray;
import org.squashtest.tm.service.internal.requirement.coercers.RequirementLibraryIdsCoercerForList;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

@SuppressWarnings("rawtypes")
@Service("squashtest.tm.service.RequirementLibraryNavigationService")
@Transactional
public class RequirementLibraryNavigationServiceImpl extends
	AbstractLibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode> implements
	RequirementLibraryNavigationService, RequirementLibraryFinderService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementLibraryNavigationServiceImpl.class);
	private static final String REQUIREMENT_ID = "requirementId";
	private static final String SOURCE_NODES_IDS = "sourceNodesIds";
	private static final String DESTINATION_ID = "destinationId";
	private static final String TARGET_ID = "targetId";
	private static final String EXPORT = "EXPORT";
	private static final String NODE_IDS = "nodeIds";

	@Inject
	private RequirementLibraryDao requirementLibraryDao;
	@Inject
	private RequirementFolderDao requirementFolderDao;
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private RequirementNodeDeletionHandler deletionHandler;
	@Inject
	private IndexationService indexationService;
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;
	@Inject
	@Qualifier("squashtest.tm.service.RequirementLibrarySelectionStrategy")
	private LibrarySelectionStrategy<RequirementLibrary, RequirementLibraryNode> libraryStrategy;

	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToRequirementFolderStrategy")
	private Provider<PasteStrategy<RequirementFolder, RequirementLibraryNode>> pasteToRequirementFolderStrategyProvider;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToRequirementLibraryStrategy")
	private Provider<PasteStrategy<RequirementLibrary, RequirementLibraryNode>> pasteToRequirementLibraryStrategyProvider;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToRequirementStrategy")
	private Provider<PasteStrategy<Requirement, Requirement>> pasteToRequirementStrategyProvider;

	@Inject
	private MilestoneMembershipManager milestoneService;

	@Inject
	private InfoListItemFinderService infoListItemService;

	@Inject
	private ExportDao exportDao;

	@Inject
	@Named(value = "requirementExcelExporter")
	private Provider<RequirementExcelExporter> exporterProvider;

	@Inject
	private Provider<SearchRequirementExcelExporter> searchExporterProvider;

	@Inject
	private RequirementExcelBatchImporter batchImporter;

	@Inject
	private ProjectDao projectDao;

	@Override
	protected NodeDeletionHandler<RequirementLibraryNode, RequirementFolder> getDeletionHandler() {
		return deletionHandler;
	}

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') " + OR_HAS_ROLE_ADMIN)
	public Requirement findRequirement(long reqId) {
		return requirementDao.findById(reqId);
	}

	@Override
	protected final RequirementLibraryDao getLibraryDao() {
		return requirementLibraryDao;
	}

	@Override
	protected final RequirementFolderDao getFolderDao() {
		return requirementFolderDao;
	}

	@Override
	protected final LibraryNodeDao<RequirementLibraryNode> getLibraryNodeDao() {
		return requirementLibraryNodeDao;
	}

	@Override
	protected PasteStrategy<RequirementFolder, RequirementLibraryNode> getPasteToFolderStrategy() {
		return pasteToRequirementFolderStrategyProvider.get();
	}

	@Override
	protected PasteStrategy<RequirementLibrary, RequirementLibraryNode> getPasteToLibraryStrategy() {
		return pasteToRequirementLibraryStrategyProvider.get();
	}

	protected PasteStrategy<Requirement, Requirement> getPasteToRequirementStrategy() {
		return pasteToRequirementStrategyProvider.get();
	}

	@Override
	public String getPathAsString(long entityId) {
		// get
		RequirementLibraryNode node = getLibraryNodeDao().findById(entityId);

		// check
		checkPermission(new SecurityCheckableObject(node, "READ"));

		// proceed
		List<String> names = getLibraryNodeDao().getParentsName(entityId);

		return "/" + node.getProject().getName() + "/" + formatPath(names);

	}

	private String formatPath(List<String> names) {
		StringBuilder builder = new StringBuilder();
		for (String name : names) {
			builder.append("/").append(name);
		}
		return builder.toString();
	}


	@Override
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.requirement.RequirementLibrary' , 'CREATE' )"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType=RequirementLibrary.class)
	public void addFolderToLibrary(@Id long destinationId, RequirementFolder newFolder) {

		RequirementLibrary container = getLibraryDao().findById(destinationId);
		container.addContent(newFolder);

		// fix the nature and type for the possible nested test cases inside that folder
		replaceAllInfoListReferences(newFolder);

		// now proceed
		getFolderDao().persist(newFolder);

		// and then create the custom field values, as a better fix for [Issue 2061]
		createAllCustomFieldValues(newFolder);
	}

	@Override
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.requirement.RequirementFolder' , 'CREATE' )"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	public final void addFolderToFolder(@Id long destinationId, RequirementFolder newFolder) {

		RequirementFolder container = getFolderDao().findById(destinationId);
		container.addContent(newFolder);

		// fix the nature and type for the possible nested test cases inside that folder
		replaceAllInfoListReferences(newFolder);

		// now proceed
		getFolderDao().persist(newFolder);

		// and then create the custom field values, as a better fix for [Issue 2061]
		createAllCustomFieldValues(newFolder);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.requirement.RequirementLibrary' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType=RequirementLibrary.class)
	public Requirement addRequirementToRequirementLibrary(@Id long libraryId, @NotNull NewRequirementVersionDto newVersion, List<Long> milestoneIds) {
		RequirementLibrary library = requirementLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(newVersion.getName())) {
			throw new DuplicateNameException(newVersion.getName(), newVersion.getName());
		}

		Requirement newReq = createRequirement(newVersion);

		library.addContent(newReq);

		replaceAllInfoListReferences(newReq);

		requirementDao.persist(newReq);
		createCustomFieldValues(newReq.getCurrentVersion());

		initCustomFieldValues(newReq.getCurrentVersion(), newVersion.getCustomFields());

		milestoneService.bindRequirementVersionToMilestones(newReq.getCurrentVersion().getId(), milestoneIds);

		return newReq;
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.requirement.RequirementLibrary' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType=RequirementLibrary.class)
	public Requirement addRequirementToRequirementLibrary(@Id long libraryId, @NotNull Requirement requirement, List<Long> milestoneIds) {
		RequirementLibrary library = requirementLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(requirement.getName())) {
			throw new DuplicateNameException(requirement.getName(), requirement.getName());
		}

		library.addContent(requirement);
		replaceAllInfoListReferences(requirement);
		requirementDao.persist(requirement);
		createCustomFieldValues(requirement.getCurrentVersion());
		milestoneService.bindRequirementVersionToMilestones(requirement.getCurrentVersion().getId(), milestoneIds);

		return requirement;
	}

	private Requirement createRequirement(NewRequirementVersionDto newVersionData) {
		return new Requirement(newVersionData.toRequirementVersion());
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.requirement.RequirementFolder' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	public Requirement addRequirementToRequirementFolder(@Id long folderId, @NotNull NewRequirementVersionDto firstVersion, List<Long> milestoneIds) {
		RequirementFolder folder = requirementFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(firstVersion.getName())) {
			throw new DuplicateNameException(firstVersion.getName(), firstVersion.getName());
		}

		Requirement newReq = createRequirement(firstVersion);

		folder.addContent(newReq);
		replaceAllInfoListReferences(newReq);
		requirementDao.persist(newReq);
		createCustomFieldValues(newReq.getCurrentVersion());
		initCustomFieldValues(newReq.getCurrentVersion(), firstVersion.getCustomFields());
		milestoneService.bindRequirementVersionToMilestones(newReq.getCurrentVersion().getId(), milestoneIds);

		return newReq;
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.requirement.RequirementFolder' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	public Requirement addRequirementToRequirementFolder(@Id long folderId, @NotNull Requirement requirement, List<Long> milestoneIds) {
		RequirementFolder folder = requirementFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(requirement.getName())) {
			throw new DuplicateNameException(requirement.getName(), requirement.getName());
		}

		folder.addContent(requirement);
		replaceAllInfoListReferences(requirement);
		requirementDao.persist(requirement);
		createCustomFieldValues(requirement.getCurrentVersion());
		milestoneService.bindRequirementVersionToMilestones(requirement.getCurrentVersion().getId(), milestoneIds);

		return requirement;
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	public Requirement addRequirementToRequirement(@Id long requirementId, @NotNull NewRequirementVersionDto newRequirement, List<Long> milestoneIds) {

		Requirement parent = requirementDao.findById(requirementId);
		Requirement child = createRequirement(newRequirement);

		parent.addContent(child);
		replaceAllInfoListReferences(child);
		requirementDao.persist(child);

		createCustomFieldValues(child.getCurrentVersion());
		initCustomFieldValues(child.getCurrentVersion(), newRequirement.getCustomFields());
		indexationService.reindexRequirementVersion(parent.getCurrentVersion().getId());
		indexationService.reindexRequirementVersions(child.getRequirementVersions());
		milestoneService.bindRequirementVersionToMilestones(child.getCurrentVersion().getId(), milestoneIds);

		return child;
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	public Requirement addRequirementToRequirement(@Id long requirementId, @NotNull Requirement newRequirement, List<Long> milestoneIds) {

		Requirement parent = requirementDao.findById(requirementId);

		parent.addContent(newRequirement);
		replaceAllInfoListReferences(newRequirement);
		requirementDao.persist(newRequirement);
		createCustomFieldValues(newRequirement.getCurrentVersion());

		indexationService.reindexRequirementVersions(parent.getRequirementVersions());
		indexationService.reindexRequirementVersions(newRequirement.getRequirementVersions());
		milestoneService.bindRequirementVersionToMilestones(newRequirement.getCurrentVersion().getId(), milestoneIds);

		return newRequirement;
	}

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= REQUIREMENT_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= SOURCE_NODES_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= SOURCE_NODES_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public List<Requirement> copyNodesToRequirement(@Id(REQUIREMENT_ID) long requirementId, @Ids(SOURCE_NODES_IDS) Long[] sourceNodesIds) {
		PasteStrategy<Requirement, Requirement> pasteStrategy = getPasteToRequirementStrategy();
		makeCopierStrategy(pasteStrategy);
		return pasteStrategy.pasteNodes(requirementId, Arrays.asList(sourceNodesIds));
	}

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= REQUIREMENT_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= NODE_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= NODE_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public void moveNodesToRequirement(@Id(REQUIREMENT_ID)long requirementId, @Ids(NODE_IDS) Long[] nodeIds) {
		if (nodeIds.length == 0) {
			return;
		}
		try {
			PasteStrategy<Requirement, Requirement> pasteStrategy = getPasteToRequirementStrategy();
			makeMoverStrategy(pasteStrategy);
			pasteStrategy.pasteNodes(requirementId, Arrays.asList(nodeIds));
		} catch (NullArgumentException | DuplicateNameException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		}
	}

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= REQUIREMENT_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= NODE_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= NODE_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public void moveNodesToRequirement(@Id(REQUIREMENT_ID)long requirementId, @Ids(NODE_IDS) Long[] nodeIds, int position) {
		if (nodeIds.length == 0) {
			return;
		}
		try {
			PasteStrategy<Requirement, Requirement> pasteStrategy = getPasteToRequirementStrategy();
			makeMoverStrategy(pasteStrategy);
			pasteStrategy.pasteNodes(requirementId, Arrays.asList(nodeIds), position);
		} catch (NullArgumentException | DuplicateNameException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		}
	}


	@Override
	public List<ExportRequirementData> findRequirementsToExportFromLibrary(List<Long> libraryIds) {
		PermissionsUtils.checkPermission(permissionService, libraryIds, EXPORT, RequirementLibrary.class.getName());
		return requirementDao.findRequirementToExportFromLibrary(libraryIds);
	}


	@Override
	public List<ExportRequirementData> findRequirementsToExportFromNodes(List<Long> nodesIds) {
		PermissionsUtils.checkPermission(permissionService, nodesIds, EXPORT, RequirementLibraryNode.class.getName());
		return requirementDao.findRequirementToExportFromNodes(nodesIds);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement' , 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	public List<Requirement> findChildrenRequirements(long requirementId) {
		return requirementDao.findChildrenRequirements(requirementId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'LINK') " + OR_HAS_ROLE_ADMIN)
	public List<RequirementLibrary> findLinkableRequirementLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : requirementLibraryDao
			.findAll();
	}

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= SOURCE_NODES_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= SOURCE_NODES_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public List<RequirementLibraryNode> copyNodesToFolder(@Id(DESTINATION_ID) long destinationId, @Ids(SOURCE_NODES_IDS) Long[] sourceNodesIds) {
		try {
			return super.copyNodesToFolder(destinationId, sourceNodesIds);
		} catch (IllegalRequirementModificationException e) {
			LOGGER.warn(e.getMessage());
			throw new CopyPasteObsoleteException(e.getMessage(), e);
		}
	}

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibrary.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public List<RequirementLibraryNode> copyNodesToLibrary(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId) {
		try {
			return super.copyNodesToLibrary(destinationId, targetId);
		} catch (IllegalRequirementModificationException e) {
			LOGGER.warn(e.getMessage());
			throw new CopyPasteObsoleteException(e.getMessage(), e);
		}
	}

	@Override
	public List<String> getParentNodesAsStringList(Long nodeId) {
		RequirementLibraryNode node = requirementLibraryNodeDao.findById(nodeId);
		List<String> parents = new ArrayList<>();

		if (node!=null) {
			Long librabryId = node.getLibrary().getId();
			List<Long> ids = requirementLibraryNodeDao.getParentsIds(nodeId);

			parents.add("#RequirementLibrary-" + librabryId);

			if (ids.size() > 1) {
				for (int i = 0; i < ids.size() - 1; i++) {
					long currentId = ids.get(i);
					RequirementLibraryNode currentNode = requirementLibraryNodeDao.findById(currentId);
					parents.add(currentNode.getClass().getSimpleName() + "-" + String.valueOf(currentId));
				}
			}
		}

		return parents;
	}


	// ******************** more private code *******************

	private void replaceAllInfoListReferences(RequirementFolder folder) {
		new CategoryChainFixer().fix(folder);
	}

	private void replaceAllInfoListReferences(Requirement requirement) {
		new CategoryChainFixer().fix(requirement);
	}

	private void createAllCustomFieldValues(RequirementFolder folder) {
		new CustomFieldValuesFixer().fix(folder);
	}


	private void replaceInfoListReferences(Requirement newReq) {

		Field categoryField = ReflectionUtils.findField(RequirementVersion.class, "category");
		categoryField.setAccessible(true);

		InfoListItem category = newReq.getCategory();

		// if no category set -> set the default one
		if (category == null) {
			ReflectionUtils.setField(categoryField, newReq.getResource(), newReq.getProject().getRequirementCategories().getDefaultItem());
		} else {

			// validate the code
			String categCode = category.getCode();
			if (!infoListItemService.isCategoryConsistent(newReq.getProject().getId(), categCode)) {
				throw new InconsistentInfoListItemException("category", categCode);
			}

			// in case the item used here is merely a reference we need to replace it with
			// a persistent instance
			if (category instanceof ListItemReference) {
				ReflectionUtils.setField(categoryField, newReq.getResource(), infoListItemService.findReference((ListItemReference) category));
			}
		}

	}

	private class CategoryChainFixer implements RequirementLibraryNodeVisitor {

		private void fix(RequirementFolder folder) {
			for (RequirementLibraryNode node : folder.getContent()) {
				node.accept(this);
			}
		}

		private void fix(Requirement req) {
			req.accept(this);
		}

		@Override
		public void visit(Requirement visited) {
			replaceInfoListReferences(visited);
			for (Requirement insider : visited.getContent()) {
				fix(insider);
			}
		}

		@Override
		public void visit(RequirementFolder visited) {
			fix(visited);
		}

	}

	private class CustomFieldValuesFixer implements RequirementLibraryNodeVisitor {

		private void fix(RequirementFolder folder) {
			for (RequirementLibraryNode node : folder.getContent()) {
				node.accept(this);
			}
		}

		private void fix(Requirement req) {
			req.accept(this);
		}

		@Override
		public void visit(Requirement requirement) {
			createCustomFieldValues(requirement.getCurrentVersion());
			for (Requirement req : requirement.getContent()) {
				fix(req);
			}
		}

		@Override
		public void visit(RequirementFolder folder) {
			fix(folder);
		}

	}

	@Override
	public File exportRequirementAsExcel(List<Long> libraryIds,
	                                     List<Long> nodeIds, boolean keepRteFormat,
	                                     MessageSource messageSource) {
		//1. Check permissions for all librairies and all nodes selecteds
		PermissionsUtils.checkPermission(permissionService, libraryIds, EXPORT, RequirementLibrary.class.getName());
		PermissionsUtils.checkPermission(permissionService, nodeIds, EXPORT, RequirementLibraryNode.class.getName());

		//2. Find the list of all req ids that belongs to library and node selection.
		Set<Long> reqIds = new HashSet<>();
		reqIds.addAll(findRequirementIdsFromSelection(libraryIds, nodeIds));

		//3. For each req, find all versions
		List<Long> reqVersionIds = requirementDao.findIdsVersionsForAll(new ArrayList<>(reqIds));

		//4. Get exportModel from database
		RequirementExportModel exportModel = exportDao.findAllRequirementModel(reqVersionIds);

		//5. Instantiate a fresh exporter, append model to excel file and return
		RequirementExcelExporter exporter = exporterProvider.get();
		exporter.appendToWorkbook(exportModel, keepRteFormat);
		return exporter.print();
	}

	@Override
	public File searchExportRequirementAsExcel(List<Long> nodeIds,
	                                           boolean keepRteFormat, MessageSource messageSource) {

		PermissionsUtils.checkPermission(permissionService, nodeIds, EXPORT, RequirementLibraryNode.class.getName());

		Set<Long> reqIds = new HashSet<>();
		reqIds.addAll(requirementDao.findAllRequirementsIdsByNodes(nodeIds));

		List<Long> reqVersionIds = requirementDao.findIdsVersionsForAll(new ArrayList<>(reqIds));

		RequirementExportModel exportModel = exportDao.findAllRequirementModel(reqVersionIds);

		RequirementExcelExporter exporter = searchExporterProvider.get();
		exporter.appendToWorkbook(exportModel, keepRteFormat);
		return exporter.print();
	}

	@Override
	public ImportLog simulateImportExcelRequirement(File xls) {
		return batchImporter.simulateImport(xls);
	}

	@Override
	public ImportLog importExcelRequirement(File xls) {
		return batchImporter.performImport(xls);
	}

	@Override
	public List<Long> findNodeIdsByPath(List<String> paths) {
		return requirementLibraryNodeDao.findNodeIdsByPath(paths);
	}

	@Override
	public Long findNodeIdByPath(String path) {
		return StringUtils.isBlank(path) ? null : requirementLibraryNodeDao.findNodeIdByPath(path);
	}

	@Override
	public Long findNodeIdByRemoteKey(String remoteKey) {
		return requirementDao.findNodeIdByRemoteKey(remoteKey);
	}

	@Override
	public List<Long> findNodeIdsByRemoteKeys(List<String> remoteKeys){
		return requirementDao.findNodeIdsByRemoteKeys(remoteKeys);
	}

	@Override
	public Collection<Long> findRequirementIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds) {

		/*
		 *  first, let's check the permissions on those root nodes
		 *  By transitivity, if the user can read them then it will
		 *  be allowed to read the requirements below
		 */
		Collection<Long> readLibIds = securityFilterIds(libraryIds, RequirementLibrary.class.getName(), "READ");
		Collection<Long> readNodeIds = securityFilterIds(nodeIds, RequirementLibraryNode.class.getName(), "READ");

		// now we can collect the requirements
		Set<Long> reqIds = new HashSet<>();

		if (!readLibIds.isEmpty()) {
			reqIds.addAll(requirementDao.findAllRequirementsIdsByLibrary(readLibIds));
		}
		if (!readNodeIds.isEmpty()) {
			reqIds.addAll(requirementDao.findAllRequirementsIdsByNodes(readNodeIds));
		}

		// return
		return reqIds;

	}

	@Override
	public Long mkdirs(String folderpath) {
		List<String> paths = PathUtils.scanPath(folderpath);
		String[] splits = PathUtils.splitPath(folderpath);
		Project project = projectDao.findByName(PathUtils.unescapePathPartSlashes(splits[0]));

		if (splits.length < 2) {
			throw new IllegalArgumentException("Folder path for mkdir must contains at least a valid /projectName/folder");
		}

		if (project == null) {
			throw new IllegalArgumentException("Folder path for mkdir must concern an existing project");
		}

		List<Long> ids = findNodeIdsByPath(paths);
		RequirementFolder folderTree;

		int position = ids.indexOf(null);

		switch (position) {
			case -1://no null value so all node exists, returning ids of the last folder
				return ids.get(ids.size() - 1);
			case 0://no member of the path exists, we must create the hierachy under the Requirement librairy
				folderTree = makeFolderTree(project, 1, splits);
				addFolderToLibrary(project.getRequirementLibrary().getId(), folderTree);
				break;
			default://Something already exists... requirement or folder ?
				Requirement requirement = findRequirement(ids.get(position - 1));
				if (requirement == null) {
					return createFolderTree(project, position, ids.get(position - 1), splits);
				} else {
					return createRequirementTree(project, position, ids.get(position - 1), splits);
				}
		}

		//now get the last folder on path and return id
		RequirementFolder lastfolder = folderTree;

		while (lastfolder.hasContent()) {
			lastfolder = (RequirementFolder) lastfolder.getContent().get(0);
		}

		return lastfolder.getId();
	}

	private Long createRequirementTree(Project project, int position, Long idBaseRequirement, String[] splits) {
		Requirement requirementTree = makeRequirementTree(project, position + 1, splits);
		List<Long> emptyIds = Collections.emptyList();
		addRequirementToRequirement(idBaseRequirement, requirementTree, emptyIds);

		//now get the last requirement on path and return his id

		Requirement lastRequirement = requirementTree;

		while (lastRequirement.hasContent()) {
			lastRequirement = lastRequirement.getContent().get(0);
		}

		return lastRequirement.getId();
	}

	private Long createFolderTree(Project project, int position, Long idBaseFolder, String[] splits) {
		RequirementFolder folderTree = makeFolderTree(project, position + 1, splits);
		addFolderToFolder(idBaseFolder, folderTree);

		//now get the last folder on path and return his id

		RequirementFolder lastfolder = folderTree;

		while (lastfolder.hasContent()) {
			lastfolder = (RequirementFolder) lastfolder.getContent().get(0);
		}

		return lastfolder.getId();
	}

	private RequirementFolder makeFolderTree(Project project, int startIndex, String[] names) {
		RequirementFolder baseFolder = null;
		RequirementFolder childFolder;
		RequirementFolder parentFolder = null;

		for (int i = startIndex; i < names.length; i++) {
			childFolder = new RequirementFolder();
			childFolder.setName(PathUtils.unescapePathPartSlashes(names[i]));
			childFolder.setDescription("");
			childFolder.notifyAssociatedWithProject(project);
			if (baseFolder == null) {//if we have no folder yet, we are creating the base, witch will be also the first parent
				baseFolder = childFolder;
			} else {
				parentFolder.addContent(childFolder);
			}
			parentFolder = childFolder;
		}

		return baseFolder;
	}

	private Requirement makeRequirementTree(Project project, int startIndex, String[] names) {
		Requirement baseRequirement = null;
		Requirement childRequirement;
		Requirement parentRequirement = null;

		for (int i = startIndex; i < names.length; i++) {
			childRequirement = new Requirement(new RequirementVersion());
			childRequirement.setName(PathUtils.unescapePathPartSlashes(names[i]));
			childRequirement.setDescription("");
			childRequirement.setCategory(infoListItemService.findDefaultRequirementCategory(project.getId()));
			childRequirement.notifyAssociatedWithProject(project);
			if (baseRequirement == null) {//if we have no folder yet, we are creating the base, witch will be also the first parent
				baseRequirement = childRequirement;
			} else {
				parentRequirement.addContent(childRequirement);
			}
			parentRequirement = childRequirement;
		}

		return baseRequirement;
	}

	@Override
	public void changeCurrentVersionNumber(Requirement requirement, Integer noVersion) {
		//if target noVersion = actual noVersion, nothing to change, return
		if (requirement.getCurrentVersion().getVersionNumber() == noVersion) {
			return;
		}
		if (requirement.findRequirementVersion(noVersion) == null) {
			RequirementVersion lastCreatedReqVersion = requirement.getCurrentVersion();
			lastCreatedReqVersion.setVersionNumber(noVersion);
			requirement.setCurrentVersion(requirement.findLastNonObsoleteVersionAfterImport());
		} else {
			throw new IllegalArgumentException("RequirementVersion with version number " + noVersion + " already exist in this Requirement, id : " + requirement.getId());
		}
	}

	@Override
	public void initCUFvalues(RequirementVersion reqVersion,
	                          Map<Long, RawValue> initialCustomFieldValues) {
		initCustomFieldValues(reqVersion, initialCustomFieldValues);
	}

	@Override
	public RequirementLibraryNode findRequirementLibraryNodeById(Long id) {
		return requirementLibraryNodeDao.findById(id);
	}


	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.requirement.RequirementLibraryNode', 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public List<String> findNamesInNodeStartingWith(long folderId, String nameStart) {
		return requirementFolderDao.findNamesInNodeStartingWith(folderId, nameStart);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.requirement.RequirementLibrary', 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart) {
		return requirementFolderDao.findNamesInLibraryStartingWith(libraryId, nameStart);
	}

	// ##################### PREVENT CONCURENCY OVERRIDES ##########################

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public void moveNodesToFolder(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId) {
		super.moveNodesToFolder(destinationId, targetId);
	}

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public void moveNodesToFolder(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId, int position) {
		super.moveNodesToFolder(destinationId, targetId, position);
	}

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibrary.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public void moveNodesToLibrary(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId) {
		super.moveNodesToLibrary(destinationId, targetId);
	}

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibrary.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	public void moveNodesToLibrary(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId, int position) {
		super.moveNodesToLibrary(destinationId, targetId, position);
	}

	@Override
	@PreventConcurrents(
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName="targetIds", coercer=RLNAndParentIdsCoercerForList.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName="targetIds", coercer=RequirementLibraryIdsCoercerForList.class)}
			)
	public OperationReport deleteNodes(@Ids("targetIds") List<Long> targetIds, Long milestoneId) {
		return super.deleteNodes(targetIds, milestoneId);
	}



	// ##################### PREVENT CONCURENCY OVERRIDES ##########################

}
