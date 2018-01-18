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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.SameRequirementLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.UnlinkableLinkedRequirementVersionException;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.library.LibraryUtils;
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkTypeDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateRequirementLibraryNodeDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  04/05/16
 */
@Component
@Scope("prototype")
class RequirementFacility extends EntityFacilitySupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityImpl.class);

	private interface ImportPostProcessHandler {
		void doPostProcess(List<Instruction<?>> instructions);
	}

	// TODO : this will not work as intended (see the XXX  "and what if")
	// unless I've missed a catch, better have a unique strategy that can handle both create and update,
	// using instruction.getImportMode() to know which case it is
	private class CreateRequirementVersionPostProcessStrategy implements ImportPostProcessHandler {

		@Override
		public void doPostProcess(List<Instruction<?>> instructions) {
			for (Instruction<?> instruction : instructions) {
				if (instruction instanceof RequirementVersionInstruction) {
					RequirementVersionInstruction rvi = (RequirementVersionInstruction) instruction;
					if (!rvi.isFatalError()) {
						changeRequirementVersionStatus(rvi);
					}
				}
			}
		}
	}

	private class UpdateRequirementVersionPostProcessStrategy implements ImportPostProcessHandler {

		@Override
		public void doPostProcess(List<Instruction<?>> instructions) {
			for (Instruction<?> instruction : instructions) {
				if (instruction instanceof RequirementVersionInstruction) {
					RequirementVersionInstruction rvi = (RequirementVersionInstruction) instruction;
					if (!rvi.isFatalError()) {
						renameRequirementVersion(rvi);
						changeRequirementVersionStatus(rvi);
					}
				}
			}
		}
	}


	@Inject
	private RequirementLibraryFinderService reqFinderService;
	@Inject
	private MilestoneMembershipManager milestoneService;
	@Inject
	private HibernateRequirementLibraryNodeDao rlnDao;
	@Inject
	private InfoListItemFinderService listItemFinderService;
	@Inject
	private RequirementVersionManagerService requirementVersionManagerService;
	@Inject
	private RequirementLibraryNavigationService reqLibNavigationService;
	@Inject
	private LinkedRequirementVersionManagerService reqlinkService;
	@Inject
	private RequirementVersionLinkTypeDao reqlinkTypeDao;

	private final FacilityImplHelper helper = new FacilityImplHelper(this);

	private ImportPostProcessHandler postProcessHandler;

	public LogTrain createRequirementVersion(RequirementVersionInstruction instr) {
		LogTrain train = validator.createRequirementVersion(instr);
		if (!train.hasCriticalErrors()) {
			//CREATE REQUIREMENT VERSION IN DB
			createReqVersionRoutine(train, instr);
			//Assign the create requirement strategy to postProcessHandler


			// XXX and what if the same import file define both a creation and an update ?
			postProcessHandler = new CreateRequirementVersionPostProcessStrategy();
		}
		return train;
	}

	private void changeRequirementVersionStatus(
		RequirementVersionInstruction rvi) {
		RequirementStatus newstatus = rvi.getTarget().getImportedRequirementStatus();
		RequirementStatus oldStatus = rvi.getRequirementVersion().getStatus();

		if (newstatus == null || newstatus == oldStatus) {
			return;
		}

		//The only forbidden transition is from WORK_IN_PROGRESS to APPROVED,
		// so we need to update to UNDER_REVIEW before updating to APPROVED
		if (newstatus == RequirementStatus.APPROVED && oldStatus == RequirementStatus.WORK_IN_PROGRESS) {
			requirementVersionManagerService.changeStatus
				(rvi.getRequirementVersion().getId(), RequirementStatus.UNDER_REVIEW);
		}

		requirementVersionManagerService.changeStatus(rvi.getRequirementVersion().getId(), newstatus);
	}

	private LogTrain createReqVersionRoutine(LogTrain train, RequirementVersionInstruction instruction) {
		RequirementVersion reqVersion = instruction.getRequirementVersion();
		Map<String, String> cufValues = instruction.getCustomFields();
		RequirementVersionTarget target = instruction.getTarget();

		try {
			helper.fillNullWithDefaults(reqVersion);
			helper.truncate(reqVersion, cufValues);
			fixCategory(target, reqVersion);
			RequirementVersion newVersion = doCreateRequirementVersion(instruction);

			//update model
			validator.getModel().addRequirement(target.getRequirement(),
				new TargetStatus(Existence.EXISTS, newVersion.getRequirement().getId()));

			validator.getModel().addRequirementVersion
				(target, new TargetStatus(Existence.EXISTS, newVersion.getId()));

			//update the instruction, needed for postProcess.
			instruction.setRequirementVersion(newVersion);

			LOGGER.debug(EXCEL_ERR_PREFIX + "Created Requirement version \t'" + target + "'");

		} catch (Exception ex) {
			train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
				new Object[]{ex.getClass().getName()}));
			validator.getModel().setNotExists(target);
			LOGGER.error(EXCEL_ERR_PREFIX + UNEXPECTED_ERROR_WHILE_IMPORTING + target + " : ", ex);
		}

		return train;
	}

	/**
	 * 1 . First create the requirement if not exist in database
	 * 1.1 - Requirement is root (ie under a {@link RequirementLibrary})
	 * This one is simple, just create the requirement and set the status in requirement tree
	 * 1.2 - Requirement is under another {@link RequirementLibraryNode}
	 * Must create all the node above the requirement that doesn't exists.
	 * As specified in 5085 all new nodes above the requirement will be treated as folder
	 * 2 . Create the requirement version :
	 *
	 */
	private RequirementVersion doCreateRequirementVersion(
		RequirementVersionInstruction instruction) {
		RequirementVersionTarget target = instruction.getTarget();
		Long reqId = validator.getModel().getRequirementId(target);
		if (reqId == null) {
			return doCreateRequirementAndVersion(instruction);
		} else {
			return doAddingNewVersionToRequirement(instruction, reqId);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private RequirementVersion doCreateRequirementAndVersion(
		final RequirementVersionInstruction instruction) {

		//convenient references as the process is complex...
		final RequirementVersionTarget target = instruction.getTarget();
		String projectName = PathUtils.extractProjectName(target.getPath());
		projectName = PathUtils.unescapePathPartSlashes(projectName);
		RequirementVersion requirementVersion = instruction.getRequirementVersion();
		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(instruction.getCustomFields());

		//creating the dto needed for adding new requirement
		final NewRequirementVersionDto dto = new NewRequirementVersionDto(requirementVersion, acceptableCufs);
		dto.setName(PathUtils.unescapePathPartSlashes(dto.getName()));

		//making arrays to avoid immutable problem in visitor inner class
		Requirement finalRequirement;
		Long finalParentId;


		//now creating requirement with following logic :
		//	If requirement is root requirement
		//		-> addRequirementToRequirementLibrary
		//	Else
		//		If parent doesn't exist in database
		//			-> Create it and needed hierarchy
		//		-> Now create the imported requirement using visitor polymorphism
		//	-> Do postCreation stuff
		if (target.getRequirement().isRootRequirement()) {
			Long requirementLibrairyId = validator.getModel().getProjectStatus(projectName).getRequirementLibraryId();


			Collection<String> siblingNames = reqLibNavigationService.findNamesInLibraryStartingWith(requirementLibrairyId,
				dto.getName());
			renameIfNeeded(dto, siblingNames);

			finalRequirement = reqLibNavigationService.addRequirementToRequirementLibrary(
				requirementLibrairyId, dto, Collections.<Long>emptyList());
			//Issue 6533
			//We need to deactivate the move node because it change the EntityManager.fushMode due to "Dark Magic"...
			//See IndexationServiceImpl.getFullTextSession()
			//With the time remaining for the 1.15 and risk of side effect we decided to neutralize this feature in order to preserve import
			//moveNodesToLibrary(requirementLibrairyId, new Long[]{finalRequirement.getId()}, target.getRequirement().getOrder());
			milestoneService.bindRequirementVersionToMilestones(finalRequirement.getCurrentVersion().getId(), boundMilestonesIds(instruction));
		} else {
			List<String> paths = PathUtils.scanPath(target.getPath());
			String parentPath = paths.get(paths.size() - 2); //we know that path is composite of at least 3 elements

			// note : the following instruction might lead to horribe result if the parent path
			// is ambiguous due to duplicate names (which is possible for now for requirements)
			// due to lazy business analysts
			finalParentId = reqFinderService.findNodeIdByPath(parentPath);

			//if parent doesn't exist, we must create it and all needed hierarchy above
			if (finalParentId == null) {
				finalParentId = reqLibNavigationService.mkdirs(parentPath);
			}

			Collection<String> siblingNames = reqLibNavigationService.findNamesInNodeStartingWith(finalParentId,
				dto.getName());
			renameIfNeeded(dto, siblingNames);

			RequirementLibraryNode parent = reqLibNavigationService.findRequirementLibraryNodeById(finalParentId);
			//now creating the visitor to requirementLibrairyNode
			//this visitor will invoke the good method as parent can be Requirement or RequirementFolder
			AddRequirementVisitor visitor = new AddRequirementVisitor(instruction, finalParentId, dto);
			parent.accept(visitor);
			finalRequirement = visitor.getFinalRequirement();
		}

		return doAfterCreationProcess(finalRequirement, instruction, requirementVersion);
	}

	private void renameIfNeeded(NewRequirementVersionDto version, Collection<String> siblingNames) {
		String newName = LibraryUtils.generateNonClashingName(version.getName(), siblingNames, Sizes.NAME_MAX);
		if (!newName.equals(version.getName())) {
			version.setName(newName);
		}
	}


	private void moveNodesToLibrary(Long requirementLibrairyId, Long[] longs,
		Integer order) {
		if (order != null && order > 0) {
			reqLibNavigationService.moveNodesToLibrary(requirementLibrairyId, longs, order);
		}

	}

	/**
	 * Here we do all the needed modifications to the freshly created requirement.
	 *
	 * @return the current version, needed for global post process
	 */
	private RequirementVersion doAfterCreationProcess(Requirement persistedRequirement, RequirementVersionInstruction instruction, RequirementVersion requirementVersion) {
		RequirementVersionTarget target = instruction.getTarget();
		//bind milestone for import
		bindRequirementVersionToMilestones(persistedRequirement.getCurrentVersion(), boundMilestonesIds(instruction));
		//updating attributes that creation process haven't set (Category... )
		doUpdateRequirementCategory(requirementVersion, persistedRequirement.getCurrentVersion());
		doUpdateRequirementMetadata((AuditableMixin) requirementVersion, (AuditableMixin) persistedRequirement.getCurrentVersion());
		//setting the version number correctly as we can add version number non sequentially with import process
		fixVersionNumber(persistedRequirement, target.getVersion());
		return persistedRequirement.getCurrentVersion();//we have only one version in the new requirement...
	}


	/**
	 * In this method, we assumes that noVersion of the added requirement version is correct
	 * It has been checked and modified if needed by validator
	 * The proccess of creating a new version directly at required position and with correct attributes
	 * is fairly complex, so we follow normal flow in squash TM : create a new requirement version and modify it after
	 *
	 */
	private RequirementVersion doAddingNewVersionToRequirement(
		RequirementVersionInstruction instruction, Long reqId) {

		RequirementVersionTarget target = instruction.getTarget();
		Requirement requirement = reqLibNavigationService.findRequirement(reqId);
		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(instruction.getCustomFields());
		RequirementVersion requirementVersion = instruction.getRequirementVersion();
		requirementVersion.setVersionNumber(instruction.getTarget().getVersion());
		//creating new version with service
		requirementVersionManagerService.createNewVersion(reqId, false, false);
		//and updating persisted reqVersion
		RequirementVersion requirementVersionPersisted = requirement.getCurrentVersion();
		reqLibNavigationService.initCUFvalues(requirementVersionPersisted, acceptableCufs);
		bindRequirementVersionToMilestones(requirementVersionPersisted, boundMilestonesIds(instruction));
		doUpdateRequirementCoreAttributes(requirementVersion, requirementVersionPersisted);
		doUpdateRequirementMetadata((AuditableMixin) requirementVersion, (AuditableMixin) requirementVersionPersisted);
		fixVersionNumber(requirement, target.getVersion());
		return requirement.findRequirementVersion(target.getVersion());
	}

	/**
	 * This method ensure that multiple milestone binding to several {@link RequirementVersion} of
	 * the same {@link Requirement} is forbidden. The method in service can't prevent this for import as we are
	 * in a unique transaction for all import lines. So the n-n relationship between milestones and requirementVersion isn't
	 * fixed until transaction is closed and {@link MilestoneMembershipManager#bindRequirementVersionToMilestones(long, Collection)}
	 * will let horrible things appends if this list isn't up to date
	 */
	private void bindRequirementVersionToMilestones(RequirementVersion requirementVersionPersisted,
		List<Long> boundMilestonesIds) {
		List<RequirementVersion> allVersion = requirementVersionPersisted.getRequirement().getRequirementVersions();
		Set<Milestone> milestoneBinded = new HashSet<>();
		Set<Long> milestoneBindedId = new HashSet<>();
		Set<Long> checkedMilestones = new HashSet<>();

		for (RequirementVersion requirementVersion : allVersion) {
			milestoneBinded.addAll(requirementVersion.getMilestones());
		}

		for (Milestone milestone : milestoneBinded) {
			milestoneBindedId.add(milestone.getId());
		}

		for (Long id : boundMilestonesIds) {
			if (!milestoneBindedId.contains(id)) {
				checkedMilestones.add(id);
			}
		}

		if (!checkedMilestones.isEmpty()) {
			requirementVersionPersisted.getMilestones().clear();
			requirementVersionManagerService.bindMilestones(requirementVersionPersisted.getId(), checkedMilestones);
		}
	}

	private void doUpdateRequirementCoreAttributes(
		RequirementVersion reqVersion, RequirementVersion orig) {

		doUpdateRequirementReference(reqVersion, orig);
		doUpdateRequirementDescription(reqVersion, orig);
		doUpdateRequirementCriticality(reqVersion, orig);
		doUpdateRequirementCategory(reqVersion, orig);
	}

	private void doUpdateRequirementReference(RequirementVersion reqVersion, RequirementVersion orig) {
		String newReference = reqVersion.getReference();
		if (!StringUtils.isBlank(newReference) && !newReference.equals(orig.getReference())) {
			requirementVersionManagerService.changeReference(orig.getId(), newReference);
		}
	}

	private void doUpdateRequirementDescription(RequirementVersion reqVersion, RequirementVersion orig) {
		String newDescription = reqVersion.getDescription();
		if (!StringUtils.isBlank(newDescription) && !newDescription.equals(orig.getDescription())) {
			requirementVersionManagerService.changeDescription(orig.getId(), newDescription);
		}
	}

	private void doUpdateRequirementCriticality(RequirementVersion reqVersion, RequirementVersion orig) {
		RequirementCriticality newCriticality = reqVersion.getCriticality();
		if (newCriticality != null && newCriticality != orig.getCriticality()) {
			requirementVersionManagerService.changeCriticality(orig.getId(), newCriticality);
		}
	}

	private void doUpdateRequirementCategory(
		RequirementVersion reqVersion, RequirementVersion orig) {
		Long idOrig = orig.getId();

		InfoListItem oldCategory = orig.getCategory();
		InfoListItem newCategory = reqVersion.getCategory();

		if (newCategory != null && !oldCategory.references(newCategory)) {
			requirementVersionManagerService.changeCategory(idOrig, newCategory.getCode());
		}
	}

	private void doUpdateRequirementMetadata(AuditableMixin requirementVersion,
		AuditableMixin persistedVersion) {
		persistedVersion.setCreatedBy(requirementVersion.getCreatedBy());
		persistedVersion.setCreatedOn(requirementVersion.getCreatedOn());
	}

	private void fixVersionNumber(Requirement requirement, Integer version) {
		reqLibNavigationService.changeCurrentVersionNumber(requirement, version);
	}

	public LogTrain updateRequirementVersion(RequirementVersionInstruction instr) {
		LogTrain train = validator.updateRequirementVersion(instr);
		if (!train.hasCriticalErrors()) {
			updateRequirementVersionRoutine(train, instr);

			// XXX and what if the same import file define both a creation and an update ?
			postProcessHandler = new UpdateRequirementVersionPostProcessStrategy();
		}
		return train;
	}

	private void updateRequirementVersionRoutine(LogTrain train,
		RequirementVersionInstruction instruction) {

		RequirementVersion reqVersion = instruction.getRequirementVersion();
		Map<String, String> cufValues = instruction.getCustomFields();
		RequirementVersionTarget target = instruction.getTarget();


		try {
			helper.fillNullWithDefaults(reqVersion);
			helper.truncate(reqVersion, cufValues);
			fixCategory(target, reqVersion);
			RequirementVersion newVersion = doUpdateRequirementVersion(instruction, cufValues);

			//update the instruction with persisted one, needed for postProcess.
			instruction.setRequirementVersion(newVersion);

			//update model
			validator.getModel().bindMilestonesToRequirementVersion(target, instruction.getMilestones());

			LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Requirement Version \t'" + target + "'");

		} catch (Exception ex) {
			train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
				new Object[]{ex.getClass().getName()}));
			validator.getModel().setNotExists(target);
			LOGGER.error(EXCEL_ERR_PREFIX + UNEXPECTED_ERROR_WHILE_IMPORTING + target + " : ", ex);
		}
	}


	public LogTrain createRequirementLink(RequirementLinkInstruction instr) {
		LogTrain train = validator.createRequirementLink(instr);
		if (! train.hasCriticalErrors()){
			createOrUpdateLink(instr, train);
		}
		return train;
	}

	public LogTrain updateRequirementLink(RequirementLinkInstruction instr) {
		LogTrain train = validator.updateRequirementLink(instr);
		if (! train.hasCriticalErrors()){
			createOrUpdateLink(instr, train);
		}
		return train;
	}

	public LogTrain deleteRequirementLink(RequirementLinkInstruction instr) {
		LogTrain train = validator.deleteRequirementLink(instr);
		if (! train.hasCriticalErrors()){
			long sourceId = findVersionIdByTarget(instr.getTarget().getSourceVersion());
			long destId = findVersionIdByTarget(instr.getTarget().getDestVersion());
			reqlinkService.removeLinkedRequirementVersionsFromRequirementVersion(sourceId, Arrays.asList(destId));
		}
		return train;
	}

	private void createOrUpdateLink(RequirementLinkInstruction instr, LogTrain train){

		long sourceId = findVersionIdByTarget(instr.getTarget().getSourceVersion());
		long destId = findVersionIdByTarget(instr.getTarget().getDestVersion());

		String destRole = instr.getRelationRole();
		if (StringUtils.isBlank(destRole)){
			destRole = reqlinkTypeDao.getDefaultRequirementVersionLinkType().getRole2Code();
		}

		try{
			reqlinkService.addOrUpdateRequirementLink(sourceId, destId, destRole);
		}
		catch(SameRequirementLinkedRequirementVersionException ex){
			train.addEntry(LogEntry
							.failure()
							.forTarget(instr.getTarget())
							.withMessage(Messages.ERROR_REQ_LINK_SAME_VERSION)
							.build());
			LOGGER.debug(ex.getMessage(), ex);
		}
		catch(UnlinkableLinkedRequirementVersionException ex){
			train.addEntry(LogEntry
					.failure()
					.forTarget(instr.getTarget())
					.withMessage(Messages.ERROR_REQ_LINK_NOT_LINKABLE)
					.build());
			LOGGER.debug(ex.getMessage(), ex);
		}
	}

	private long findVersionIdByTarget(RequirementVersionTarget versTarget){
		Long reqId = validator.getModel().getRequirementId(versTarget);
		return requirementVersionManagerService.findReqVersionIdByRequirementAndVersionNumber(reqId, versTarget.getVersion());
	}

	private void renameRequirementVersion(RequirementVersionInstruction rvi) {
		String unconsistentName = rvi.getTarget().getUnconsistentName();
		if (unconsistentName != null && !StringUtils.isEmpty(unconsistentName)) {
			String newName = PathUtils.unescapePathPartSlashes(unconsistentName);
			RequirementVersionTarget target = rvi.getTarget();
			Requirement req = reqLibNavigationService.findRequirement(target.getRequirement().getId());
			RequirementVersion orig = req.findRequirementVersion(target.getVersion());
			orig.setName(newName);
		}
	}

	private void fixCategory(RequirementVersionTarget target, RequirementVersion requirementVersion) {
		TargetStatus projectStatus = validator.getModel().getProjectStatus(target.getProject());

		InfoListItem category = requirementVersion.getCategory();
		//if category is null or inconsistent for project, setting to default project category
		if (category == null || !listItemFinderService.isCategoryConsistent(projectStatus.getId(), category.getCode())) {
			requirementVersion.setCategory(listItemFinderService.findDefaultRequirementCategory(projectStatus.getId()));
		}
	}


	private RequirementVersion doUpdateRequirementVersion(
		RequirementVersionInstruction instruction, Map<String, String> cufValues) {

		RequirementVersionTarget target = instruction.getTarget();
		RequirementVersion reqVersion = instruction.getRequirementVersion();

		Requirement req = reqLibNavigationService.
			findRequirement(target.getRequirement().getId());

		RequirementVersion orig = req.findRequirementVersion(target.getVersion());

		doUpdateRequirementCoreAttributes(reqVersion, orig);
		doUpdateRequirementCategory(reqVersion, orig);

		//Feat 5169, unbind all milestones if cell is empty in import file.
		//Else, bind milestones if possible
		if (CollectionUtils.isEmpty(instruction.getMilestones())) {
			orig.getMilestones().clear();
		} else {
			updateRequirementVersionToMilestones(target.isRejectedMilestone(), orig, boundMilestonesIds(instruction));
		}
		doUpdateCustomFields(cufValues, orig);
		doUpdateRequirementMetadata((AuditableMixin) reqVersion, (AuditableMixin) orig);
		moveRequirement(target.getRequirement(), req);
		//we return the persisted RequirementVersion for post process
		return orig;
	}

	private void updateRequirementVersionToMilestones(boolean corruptedMilestones, RequirementVersion requirementVersionPersisted,
		List<Long> boundMilestonesIds) {
		if (!corruptedMilestones) {
			bindRequirementVersionToMilestones(requirementVersionPersisted, boundMilestonesIds);
		}
	}

	@SuppressWarnings("rawtypes")
	private void moveRequirement(RequirementTarget target, final Requirement req) {
		final Integer newPosition = target.getOrder();
		if (newPosition == null) {
			return;
		}
		if (newPosition <= 0) {
			return;
		}
		if (target.isRootRequirement()) {
			reqLibNavigationService.moveNodesToLibrary(req.getLibrary().getId(), new Long[]{req.getId()}, newPosition);
		} else {
			List<Long> ids = rlnDao.getParentsIds(req.getId());
			Long firstParentId = ids.get(ids.size() - 2);
			final RequirementLibraryNode parent = reqLibNavigationService.findRequirementLibraryNodeById(firstParentId);

			//creating haddock visitor
			RequirementLibraryNodeVisitor visitor = new RequirementLibraryNodeVisitor() {

				@Override
				public void visit(Requirement requirement) {
					reqLibNavigationService.moveNodesToRequirement(parent.getId(), new Long[]{req.getId()}, newPosition);
				}

				@Override
				public void visit(RequirementFolder folder) {
					reqLibNavigationService.moveNodesToFolder(parent.getId(), new Long[]{req.getId()}, newPosition);
				}
			};
			parent.accept(visitor);
		}
	}

	public LogTrain deleteRequirementVersion(RequirementVersionInstruction instr) {
		throw new NotImplementedException("implement me - must return a Failure : Not implemented in the log train instead of throwing this exception");
	}

	/**
	 * for all other stuffs that need to be done afterward
	 *
	 */
	public void postprocess(List<Instruction<?>> instructions) {
		if (postProcessHandler != null) {
			postProcessHandler.doPostProcess(instructions);
		}
	}


	private class AddRequirementVisitor implements RequirementLibraryNodeVisitor {

		private final RequirementVersionInstruction instruction;
		private final RequirementVersionTarget target;

		private Requirement finalRequirement;

		private final Long finalParentId;
		private final NewRequirementVersionDto dto;

		public AddRequirementVisitor(RequirementVersionInstruction instruction, Long finalParentId, NewRequirementVersionDto dto) {
			this.instruction = instruction;
			this.target = instruction.getTarget();
			this.finalParentId = finalParentId;
			this.dto = dto;
		}

		public Requirement getFinalRequirement() {
			return finalRequirement;
		}

		@Override
		public void visit(Requirement requirement) {
			//Integer finalPosition = target.getRequirement().getOrder();
			finalRequirement = reqLibNavigationService.addRequirementToRequirement(finalParentId, dto, boundMilestonesIds(instruction));
			//Issue 6533
			//We need to deactivate the move node because it change the EntityManager.flushMode due to "Dark Magic"...
			//See IndexationServiceImpl.getFullTextSession()
			//With the time remaining for the 1.15 and risk of side effect we decided to neutralize this feature in order to preserve import
//			if (finalPosition != null && finalPosition > 0) {
//				reqLibNavigationService.moveNodesToRequirement(finalParentId, new Long[]{finalRequirement.getId()}, target.getRequirement().getOrder());
//			}
		}

		@Override
		public void visit(RequirementFolder folder) {
			//Integer finalPosition = target.getRequirement().getOrder();
			finalRequirement = reqLibNavigationService.addRequirementToRequirementFolder(finalParentId, dto, boundMilestonesIds(instruction));
			//Issue 6533
			//We need to deactivate the move node because it change the EntityManager.flushMode due to "Dark Magic"...
			//See IndexationServiceImpl.getFullTextSession()
			//With the time remaining for the 1.15 and risk of side effect we decided to neutralize this feature in order to preserve import
//			if (finalPosition != null && finalPosition > 0) {
//				reqLibNavigationService.moveNodesToFolder(finalParentId, new Long[]{finalRequirement.getId()}, target.getRequirement().getOrder());
//			}
		}
	}
}
