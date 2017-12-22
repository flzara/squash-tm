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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.library.LibraryUtils;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  04/05/16
 */
@Component
@Scope("prototype")
public class TestCaseFacility extends EntityFacilitySupport {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacilityImpl.class);

	@Inject
	private InfoListItemFinderService listItemFinderService;

	@Inject
	private TestCaseLibraryNavigationService navigationService;

	@Inject
	private TestCaseModificationService testcaseModificationService;

	private final FacilityImplHelper helper = new FacilityImplHelper(this);

	public LogTrain createTestCase(TestCaseInstruction instr) {
		LogTrain train = validator.createTestCase(instr);

		if (!train.hasCriticalErrors()) {
			instr.getTestCase().setName(instr.getTarget().getName());
			train = createTCRoutine(train, instr);
		}

		return train;
	}

	/**
	 * May be called either by the create test case scenario, or in an update scenario (business says that
	 * updating a test case that dont exist implies to create it first).
	 */
	private LogTrain createTCRoutine(LogTrain train, TestCaseInstruction instruction) {
		TestCase testCase = instruction.getTestCase();
		Map<String, String> cufValues = instruction.getCustomFields();
		TestCaseTarget target = instruction.getTarget();

		try {
			helper.fillNullWithDefaults(testCase);
			helper.truncate(testCase, cufValues);
			fixNatureAndType(target, testCase);

			doCreateTestcase(instruction);
			validator.getModel().setExists(target, testCase.getId());

			LOGGER.debug(EXCEL_ERR_PREFIX + "Created Test Case \t'" + target + "'");

		} catch (Exception ex) {
			train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
				new Object[]{ex.getClass().getName()}));
			validator.getModel().setNotExists(target);
			LOGGER.error(EXCEL_ERR_PREFIX + UNEXPECTED_ERROR_WHILE_IMPORTING + target + " : ", ex);
		}

		return train;
	}

	private void fixNatureAndType(TestCaseTarget target, TestCase testCase) {

		// at this point of the process the target is assumed to be safe for
		// use,
		// no need to defensively check that the project exists and such
		TargetStatus projectStatus = validator.getModel().getProjectStatus(target.getProject());

		InfoListItem nature = testCase.getNature();
		if (nature != null && !listItemFinderService.isNatureConsistent(projectStatus.getId(), nature.getCode())) {
				testCase.setNature(listItemFinderService.findDefaultTestCaseNature(projectStatus.getId()));
		}

		InfoListItem type = testCase.getType();
		if (type != null && !listItemFinderService.isTypeConsistent(projectStatus.getId(), type.getCode())) {
				testCase.setType(listItemFinderService.findDefaultTestCaseType(projectStatus.getId()));
		}

	}

	// because this time we're not toying around man, this is the real thing
	private void doCreateTestcase(TestCaseInstruction instr) {
		TestCase testCase = instr.getTestCase();
		Map<String, String> cufValues = instr.getCustomFields();
		TestCaseTarget target = instr.getTarget();

		Map<Long, RawValue> acceptableCufs = toAcceptableCufs(cufValues);
		List<Long> milestoneIds = boundMilestonesIds(instr);

		// case 1 : this test case lies at the root of the project
		if (target.isRootTestCase()) {
			// libraryId is never null because the checks ensured that the
			// project exists
			Long libraryId = validator.getModel().getProjectStatus(target.getProject()).getTestCaseLibraryId();
			unescapeTCName(testCase);
			Collection<String> siblingNames = navigationService.findNamesInLibraryStartingWith(libraryId,
				testCase.getName());
			renameIfNeeded(testCase, siblingNames);
			navigationService.addTestCaseToLibrary(libraryId, testCase, acceptableCufs, target.getOrder(),
				milestoneIds);
		}
		// case 2 : this test case exists within a folder
		else {
			Long folderId = navigationService.mkdirs(target.getFolder());
			unescapeTCName(testCase);
			Collection<String> siblingNames = navigationService.findNamesInFolderStartingWith(folderId,
				testCase.getName());
			renameIfNeeded(testCase, siblingNames);
			navigationService.addTestCaseToFolder(folderId, testCase, acceptableCufs, target.getOrder(), milestoneIds);
		}

	}


	/**
	 * @param instr            instruction read from import file, pointing to a TRANSIENT test case template
	 * @param persistentSource the PERSISTENT test case
	 */
	private void rebindMilestones(TestCaseInstruction instr, TestCase persistentSource) {
		if (!instr.getMilestones().isEmpty()) {
			List<Milestone> ms = milestoneHelper.findBindable(instr.getMilestones());
			persistentSource.getMilestones().clear();
			persistentSource.bindAllMilsetones(ms);
		}
		//feat 5169 if milestone cell is empty in xls import file, unbind all milestones
		else {
			persistentSource.getMilestones().clear();
		}

	}

	private void renameIfNeeded(TestCase testCase, Collection<String> siblingNames) {
		String newName = LibraryUtils.generateNonClashingName(testCase.getName(), siblingNames, Sizes.NAME_MAX);
		if (!newName.equals(testCase.getName())) {
			testCase.setName(newName);
		}
	}


	private void unescapeTCName(TestCase testCase) {
		testCase.setName(PathUtils.unescapePathPartSlashes(testCase.getName()));
	}

	public LogTrain updateTestCase(TestCaseInstruction instr) {
		TestCaseTarget target = instr.getTarget();
		TestCase testCase = instr.getTestCase();
		Map<String, String> cufValues = instr.getCustomFields();

		TargetStatus status = validator.getModel().getStatus(target);

		LogTrain train = validator.updateTestCase(instr);

		if (!train.hasCriticalErrors()) {

			if (status.status == Existence.NOT_EXISTS) {

				train = createTCRoutine(train, instr);

			} else {
				try {

					helper.truncate(testCase, cufValues);
					fixNatureAndType(target, testCase);

					doUpdateTestcase(instr);

					LOGGER.debug(EXCEL_ERR_PREFIX + "Updated Test Case \t'" + target + "'");

				} catch (Exception ex) {
					train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
						new Object[]{ex.getClass().getName()}));
					LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while updating " + target + " : ", ex);
				}

			}

		}

		return train;
	}

	private void doUpdateTestcase(TestCaseInstruction instr) {
		TestCaseTarget target = instr.getTarget();
		TestCase testCase = instr.getTestCase();
		Map<String, String> cufValues = instr.getCustomFields();

		TestCase orig = validator.getModel().get(target);
		Long origId = orig.getId();


		// update the custom field values

		doUpdateCustomFields(cufValues, orig);

		if (validator.areMilestoneValid(instr)) {
			rebindMilestones(instr, orig);
		}
		

		/*
		 * Issue #6968 : Because renaming / changing the ref of a test case triggers an immediate reindexation 
		 * of the related ITPIs (and the test case itself by the way). In the process the session attached to 
		 * the persistent collection of the milestones is killed, not sure why, thus triggering the 
		 * lazy exception.
		 * 
		 *  The hack to prevent this is to make sure the indexation happens last, which here can be done 
		 *  by updating the core attributes to the last position.
		 */
		// update the test case core attributes last
		
		doUpdateTestCaseCoreAttributes(testCase, orig);

		// move the test case if its index says it has to move
		Integer order = target.getOrder();
		if (order != null && order > -1 && order < navigationService.countSiblingsOfNode(origId)) {
			if (target.isRootTestCase()) {
				Long libraryId = validator.getModel().getProjectStatus(target.getProject()).getTestCaseLibraryId();
				navigationService.moveNodesToLibrary(libraryId, new Long[]{origId}, order);
			} else {
				Long folderId = navigationService.findNodeIdByPath(target.getFolder());
				navigationService.moveNodesToFolder(folderId, new Long[]{origId}, order);
			}
		}

	}

	private void doUpdateTestCaseCoreAttributes(TestCase testCase, TestCase orig) {

		Long origId = orig.getId();
		String newName = testCase.getName();

		if (!StringUtils.isBlank(newName) && !newName.equals(orig.getName())) {
			testcaseModificationService.rename(origId, newName);
		}

		String newRef = testCase.getReference();
		if (!StringUtils.isBlank(newRef) && !newRef.equals(orig.getReference())) {
			testcaseModificationService.changeReference(origId, newRef);
		}

		String newDesc = testCase.getDescription();
		if (!StringUtils.isBlank(newDesc) && !newDesc.equals(orig.getDescription())) {
			testcaseModificationService.changeDescription(origId, newDesc);
		}

		String newPrereq = testCase.getPrerequisite();
		if (!StringUtils.isBlank(newPrereq) && !newPrereq.equals(orig.getPrerequisite())) {
			testcaseModificationService.changePrerequisite(origId, newPrereq);
		}

		TestCaseImportance newImp = testCase.getImportance();
		if (newImp != null && orig.getImportance() != newImp) {
			testcaseModificationService.changeImportance(origId, newImp);
		}

		InfoListItem newNat = testCase.getNature();
		if (newNat != null && !newNat.references(orig.getNature())) {
			testcaseModificationService.changeNature(origId, newNat.getCode());
		}

		InfoListItem newType = testCase.getType();
		if (newType != null && !newType.references(orig.getType())) {
			testcaseModificationService.changeType(origId, newType.getCode());
		}

		TestCaseStatus newStatus = testCase.getStatus();
		if (newStatus != null && orig.getStatus() != newStatus) {
			testcaseModificationService.changeStatus(origId, newStatus);
		}

		Boolean newImportanceAuto = testCase.isImportanceAuto();
		if (orig.isImportanceAuto().equals(newImportanceAuto)) {
			testcaseModificationService.changeImportanceAuto(origId, newImportanceAuto);
		}
	}

	public LogTrain deleteTestCase(TestCaseTarget target) {

		LogTrain train = validator.deleteTestCase(target);

		if (!train.hasCriticalErrors()) {
			try {

				doDeleteTestCase(target);
				validator.getModel().setDeleted(target);

				LOGGER.debug(EXCEL_ERR_PREFIX + "Deleted Test Case \t'" + target + "'");

			} catch (Exception ex) {
				train.addEntry(new LogEntry(target, ImportStatus.FAILURE, Messages.ERROR_UNEXPECTED_ERROR,
					new Object[]{ex.getClass().getName()}));

				LOGGER.error(EXCEL_ERR_PREFIX + "unexpected error while deleting " + target + " : ", ex);
			}
		}

		return train;
	}

	private void doDeleteTestCase(TestCaseTarget target) {
		TestCase tc = validator.getModel().get(target);
		navigationService.deleteNodes(Collections.singletonList(tc.getId()));
	}
}
