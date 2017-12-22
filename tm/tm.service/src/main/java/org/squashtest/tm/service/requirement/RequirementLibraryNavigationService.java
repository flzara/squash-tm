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
package org.squashtest.tm.service.requirement;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.Ids;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.internal.requirement.coercers.RLNAndParentIdsCoercerForArray;
import org.squashtest.tm.service.internal.requirement.coercers.RequirementLibraryIdsCoercerForArray;
import org.squashtest.tm.service.library.LibraryNavigationService;

@SuppressWarnings("rawtypes")
public interface RequirementLibraryNavigationService extends
LibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode>,
RequirementLibraryFinderService {

	String REQUIREMENT_ID = "requirementId";
	String NODE_IDS = "nodeIds";
	String SOURCE_NODES_IDS = "sourceNodesIds";
	String DESTINATION_ID = "destinationId";
	String TARGET_ID = "targetId";
	String TARGET_IDS = "targetIds";

	/**
	 * Will add a Requirement at the root of the given library and bind it to the given milestones. The custom fields
	 * for this requirement will be created with their default value.
	 * @deprecated Looks like this method is useless
	 */
	@Deprecated
	@PreventConcurrent(entityType=RequirementLibrary.class)
	Requirement addRequirementToRequirementLibrary(@Id long libraryId, @NotNull Requirement requirement, List<Long> milestoneIds);

	/**
	 * Given a DTO that eventually generates a RequirementVersion, will create a Requirement using this version then add it to the given
	 * library with and bind it to the given milestones. The DTO may hold additional information for initialization of the custom fields.
	 * On the other hand this method is not suitable for creation of synchronized Requirements.
	 *
	 */
	@PreventConcurrent(entityType=RequirementLibrary.class)
	Requirement addRequirementToRequirementLibrary(@Id long libraryId, @NotNull NewRequirementVersionDto newRequirement, List<Long> milestoneIds);

	/**
	 * Same than {@link #addRequirementToRequirementLibrary(long, Requirement, List)}, except that the requirement will be added to the
	 * given folder.
	 * @deprecated Looks like this methid is useless
	 */
	@Deprecated
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	Requirement addRequirementToRequirementFolder(@Id long folderId, @NotNull Requirement requirement, List<Long> milestoneIds);

	/**
	 * Same than {@link #addRequirementToRequirementLibrary(long, NewRequirementVersionDto, List), except that the requirement will be added to the
	 * given folder.
	 *
	 * @param folderId
	 * @param requirement
	 * @param milestoneIds
	 * @return
	 */
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	Requirement addRequirementToRequirementFolder(@Id long folderId, @NotNull NewRequirementVersionDto newRequirement, List<Long> milestoneIds);

	/**
	 * Same than {@link #addRequirementToRequirementLibrary(long, Requirement, List)}, except that the requirement will be added to the
	 * given parent requirement.
	 *
	 */
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	Requirement addRequirementToRequirement(@Id long requirementId, @NotNull Requirement newRequirement, List<Long> milestoneIds);

	/**
	 * Same than {@link #addRequirementToRequirementLibrary(long, NewRequirementVersionDto, List), except that the requirement will be added to the
	 * given parent requirement.
	 *
	 * @param folderId
	 * @param requirement
	 * @param milestoneIds
	 * @return
	 */
	@PreventConcurrent(entityType=RequirementLibraryNode.class)
	Requirement addRequirementToRequirement(@Id long requirementId, @NotNull NewRequirementVersionDto newRequirement, List<Long> milestoneIds);

	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= REQUIREMENT_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= SOURCE_NODES_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= SOURCE_NODES_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	List<Requirement> copyNodesToRequirement(@Id(REQUIREMENT_ID)long requirementId, @Ids(SOURCE_NODES_IDS) Long[] sourceNodesIds);

	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= REQUIREMENT_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= NODE_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= NODE_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToRequirement(@Id(REQUIREMENT_ID)long requirementId, @Ids(NODE_IDS) Long[] nodeIds);

	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= REQUIREMENT_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= NODE_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= NODE_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToRequirement(@Id(REQUIREMENT_ID)long requirementId, @Ids(NODE_IDS) Long[] nodeIds, int position);

	Requirement findRequirement(long reqId);

	/**
	 * Will find all requirements found in the given projects and return their information as a list of
	 * {@linkplain ExportRequirementData}
	 *
	 * @param libraryIds
	 *            ids of {@linkplain Project}
	 * @return a list of {@linkplain ExportRequirementData}
	 */
	List<ExportRequirementData> findRequirementsToExportFromLibrary(@NotNull List<Long> libraryIds);

	/**
	 * Will find all requirements of the given ids and contained in folders of the given ids, and return their
	 * information as a list of {@linkplain ExportRequirementData}
	 *
	 * @param nodesIds
	 *            ids of {@linkplain RequirementLibraryNode}
	 * @return a list of {@linkplain ExportRequirementData}
	 */
	List<ExportRequirementData> findRequirementsToExportFromNodes(@NotNull List<Long> nodesIds);

	List<Requirement> findChildrenRequirements(long requirementId);

	List<String> getParentNodesAsStringList(Long elementId);

	/**
	 * Generate a xls file to export requirements
	 * @param libraryIds List of libraryIds (ie project ids) selected for export
	 * @param nodeIds List of nodeIds (ie req id or folder id) selected for export
	 */
	File exportRequirementAsExcel(List<Long> libraryIds, List<Long> nodeIds,
			boolean keepRteFormat, MessageSource messageSource);

	/**
	 * Generate a xls file to export requirements from research screen
	 * @param nodeIds List of nodeIds (ie req id or folder id) selected for export
	 */
	File searchExportRequirementAsExcel(List<Long> nodeIds,
			boolean keepRteFormat, MessageSource messageSource);

	ImportLog simulateImportExcelRequirement(File xls);


	ImportLog importExcelRequirement(File xls);

	/**
	 * Create a hierarchy of requirement library node.
	 * The type of node depends of the first existing node in hierarchy :
	 * <code>
	 * <ul>
	 * <li>If no node exist before, all created node will be {@link RequirementFolder}</li>
	 * <li>If the last existing node on path is a {@link RequirementFolder}, all created node will be {@link RequirementFolder} </li>
	 * <li>If the last existing node on path is a {@link Requirement}, all created node will be {@link Requirement} </li>
	 * </ul>
	 * </code>
	 * @param folderpath the complete path
	 * @return the ID of the created node. Take care that it can be an ID corresponding to a {@link RequirementFolder} or a {@link Requirement}. See above...
	 */
	Long mkdirs(String folderpath);

	/**
	 * Change the current version number.
	 * Used by import to change the last created version number.
	 * This method also modify the {@link Requirement#getCurrentVersion()} if needed.
	 */
	void changeCurrentVersionNumber(Requirement requirement, Integer noVersion);

	/**
	 * Initialize the CUF values for a {@link RequirementVersion}
	 * @param initialCustomFieldValues map the id of the CUF to the value.
	 * Beware, it's not the id of the CUFValue entry in db but the id of the CUF itself
	 */
	void initCUFvalues(RequirementVersion reqVersion, Map<Long, RawValue> initialCustomFieldValues);

	RequirementLibraryNode findRequirementLibraryNodeById(Long id);

	List<String> findNamesInNodeStartingWith(final long folderId, final String nameStart);

	List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart);

	List<Long> findAllRequirementIdsInMilestone(Milestone activeMilestone);
	
	// ##################### PREVENT CONCURENCY OVERRIDES ##########################
	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= SOURCE_NODES_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= SOURCE_NODES_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	List<RequirementLibraryNode> copyNodesToFolder(@Id(DESTINATION_ID) long destinationId, @Ids(SOURCE_NODES_IDS) Long[] sourceNodesIds);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibrary.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	List<RequirementLibraryNode> copyNodesToLibrary(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToFolder(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibraryNode.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToFolder(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId, int position);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibrary.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToLibrary(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType=RequirementLibrary.class, paramName= DESTINATION_ID)},
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_ID, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_ID, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToLibrary(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId, int position);

	@Override
	@PreventConcurrents(
			batchsLocks ={@BatchPreventConcurrent(entityType=RequirementLibraryNode.class, paramName= TARGET_IDS, coercer=RLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType=RequirementLibrary.class, paramName= TARGET_IDS, coercer=RequirementLibraryIdsCoercerForArray.class)}
			)
	OperationReport deleteNodes(@Ids(TARGET_IDS) List<Long> targetIds);

	// #################### /PREVENT CONCURENCY OVERRIDES ##########################
}
