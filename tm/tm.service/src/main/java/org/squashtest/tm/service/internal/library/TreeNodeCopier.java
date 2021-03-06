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

import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.library.Copiable;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;
import org.squashtest.tm.domain.testcase.ActionStepCollector;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.IsKeywordTestCaseVisitor;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.internal.campaign.IterationTestPlanManager;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.CampaignFolderDao;
import org.squashtest.tm.service.internal.repository.EntityDao;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

@Component
@Scope("prototype")
public class TreeNodeCopier implements NodeVisitor, PasteOperation {

	private static final String UNCHECKED = "unchecked";
	private static final String RAWTYPES = "rawtypes";

	@Inject
	private RequirementDao requirementDao;
	@Inject
	private RequirementFolderDao requirementFolderDao;
	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private TestCaseFolderDao testCaseFolderDao;
	@Inject
	private CampaignDao campaignDao;
	@Inject
	private CampaignFolderDao campaignFolderDao;
	@Inject
	private IterationDao iterationDao;
	@Inject
	private TestSuiteDao testSuiteDao;
	@Inject
	private IterationTestPlanDao iterationTestPlanItemDao;
	@Inject
	private IterationTestPlanManager iterationTestPlanManager;
	@Inject
	private PrivateCustomFieldValueService customFieldValueManagerService;
	@Inject
	private TreeNodeUpdater treeNodeUpdater;
	@Inject
	private PermissionEvaluationService permissionService;
	@Inject
	private RequirementVersionCoverageDao requirementVersionCoverageDao;
	@Inject
	private RequirementVersionLinkDao requirementVersionLinkDao;
	@Inject
	private AttachmentManagerService attachmentManagerService;


	@PersistenceContext
	private EntityManager entityManager;

	private NodeContainer<? extends TreeNode> destination;

	private TreeNode copy;
	private boolean okToGoDeeper = true;
	private boolean projectChanged = true;
	private int batchRequirement = 0;


	@Override
	public TreeNode performOperation(TreeNode source, NodeContainer<TreeNode> destination, Integer position) {
		PermissionsUtils.checkPermission(permissionService, new SecurityCheckableObject(destination, "CREATE"),
			new SecurityCheckableObject(source, "READ"));
		this.okToGoDeeper = true;
		this.destination = destination;
		this.projectChanged = projectchanged(source);
		copy = null;
		source.accept(this);
		if (projectChanged) {
			// see comment on the method flush()
			flush();
			copy.accept(treeNodeUpdater);
		}
		return copy;


	}

	@Override
	public TreeNode performOperationFromReqToTc(TreeNode source, TreeNode transformed, NodeContainer<TreeNode> destination, Integer position) {
		PermissionsUtils.checkPermission(permissionService, new SecurityCheckableObject(destination, "CREATE"),
			new SecurityCheckableObject(source, "READ"));
		this.okToGoDeeper = true;
		this.destination = destination;
		this.projectChanged = projectchanged(source);
		copy = null;
		transformed.accept(this);
		if (projectChanged) {
			// see comment on the method flush()
			flush();
			copy.accept(treeNodeUpdater);
		}
		return copy;

	}

	private boolean projectchanged(TreeNode source) {
		Project projectSource = source.getProject();
		GenericProject projectDestination = destination.getProject();
		return projectSource != null && projectDestination != null && !projectSource.getId().equals(projectDestination.getId());
	}

	@SuppressWarnings({UNCHECKED, RAWTYPES})
	public void visit(Folder source, FolderDao dao) {
		Folder<?> copyFolder = (Folder<?>) source.createCopy();
		persistCopy(copyFolder, dao, Sizes.NAME_MAX);
		copyCustomFields(source,copyFolder);
		copyContentsOnExternalRepository (copyFolder);
	}

	@Override
	public void visit(Campaign source) {
		Campaign copyCampaign = source.createCopy();
		persistCopy(copyCampaign, campaignDao, Sizes.NAME_MAX);
		copyCustomFields(source, copyCampaign);
		copyContentsOnExternalRepository (copyCampaign);
	}

	/**
	 * Will copy paste the iteration with it's test-suites.<br>
	 * Hence it is not ok to go deeper with the paste strategy : we don't want the test-suites to be copied twice.<br>
	 * <br>
	 * <h4>Why don't we use the paste strategy to go through the test-suites ?</h4> Because of functional rules : the requirements for
	 * "copying an test-suite alone" and "copying test-suites of a copied iteration" are different.
	 * <ol>
	 * <li>When copying a test-suite alone all test-plan item of the concerned test-suite will be added to the iteration
	 * even if there were already contained by the iteration.</li>
	 * <li>When copying an interaction, it's copied test-suite should be bound to the already copied
	 * iteration-test-plan-items.</li>
	 * </ol>
	 */
	@Override
	public void visit(Iteration source) {
		Iteration copyIteration = source.createCopy();
		persitIteration(copyIteration);
		copyIterationTestSuites(source, copyIteration);
		copyCustomFields(source, copyIteration);
		copyContentsOnExternalRepository (copyIteration);
		this.okToGoDeeper = false;
	}

	@Override
	public void visit(TestSuite source) {
		TestSuite copyTestSuite = source.createCopy();
		persistCopy(copyTestSuite, testSuiteDao, TestSuite.MAX_NAME_SIZE);
		copyCustomFields(source, copyTestSuite);
		copyTestSuiteTestPlanToDestinationIteration(source, copyTestSuite);
		copyContentsOnExternalRepository (copyTestSuite);
	}

	private void copyTestSuiteTestPlanToDestinationIteration(TestSuite source, TestSuite copy) {
		Iteration iteration = (Iteration) destination;
		List<IterationTestPlanItem> copyOfTestPlan = source.createPastableCopyOfTestPlan();
		for (IterationTestPlanItem itp : copyOfTestPlan) {
			iterationTestPlanItemDao.save(itp);
			iteration.addTestPlan(itp);
		}
		copy.bindTestPlanItems(copyOfTestPlan);
	}

	@Override
	public void visit(Requirement source) {
		//copy simple attributes of requirement
		Requirement copyRequirement = source.createCopy();
		//create copies for requirement versions and remember version's sources
		SortedMap<RequirementVersion, RequirementVersion> previousVersionsCopiesBySources = source
			.addPreviousVersionsCopiesToCopy(copyRequirement);
		persistCopy(copyRequirement, requirementDao, Sizes.NAME_MAX);
		//copy custom fields and requirement-version coverages for Current Version
		copyCustomFields(source.getCurrentVersion(), copyRequirement.getCurrentVersion());
		copyRequirementVersionCoverages(source.getCurrentVersion(), copyRequirement.getCurrentVersion());
		copyRequirementVersionLinks(source.getCurrentVersion(), copyRequirement.getCurrentVersion());
		copyContentsOnExternalRepository (copyRequirement.getCurrentVersion());
		//copy custom fields and requirement-version coverages for older versions
		for (Entry<RequirementVersion, RequirementVersion> previousVersionCopyBySource : previousVersionsCopiesBySources
			.entrySet()) {
			//retrieve entities from entry
			RequirementVersion sourceVersion = previousVersionCopyBySource.getKey();
			RequirementVersion copyVersion = previousVersionCopyBySource.getValue();
			//copy cufs and coverages for entities
			copyRequirementVersionCoverages(sourceVersion, copyVersion);
			copyRequirementVersionLinks(sourceVersion, copyVersion);
			copyCustomFields(sourceVersion, copyVersion);
			copyContentsOnExternalRepository (copyVersion);
		}

		batchRequirement++;
		if (batchRequirement % 10 == 0) {
			flush();
		}

	}


	@Override
	public void visit(TestCase source) {
		TestCase copyTestCase = source.createCopy();
		persistTestCase(copyTestCase);
		copyRequirementVersionCoverage(source, copyTestCase);
		copyContentsOnExternalRepository(copyTestCase);
		IsKeywordTestCaseVisitor visitor = new IsKeywordTestCaseVisitor();
		source.accept(visitor);
		copyCustomFields(source, copyTestCase);
		if(visitor.isKeyword()) {
			//NOOP
		} else {
			copyTestCase.getActionSteps().forEach(this::copyContentsOnExternalRepository);
		}
		batchRequirement++;
		if (batchRequirement % 10 == 0) {
			flush();
		}
	}


	@Override
	public void visit(CampaignFolder campaignFolder) {
		visit(campaignFolder, campaignFolderDao);

	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		visit(requirementFolder, requirementFolderDao);

	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		visit(testCaseFolder, testCaseFolderDao);

	}

	/**************************************************** PRIVATE **********************************************************/

	private void copyIterationTestSuites(Iteration originalIteration, Iteration iterationCopy) {
		Map<TestSuite, List<Integer>> testSuitesPastableCopiesMap = originalIteration.createTestSuitesPastableCopy();
		for (Entry<TestSuite, List<Integer>> testSuitePastableCopyEntry : testSuitesPastableCopiesMap.entrySet()) {
			TestSuite testSuiteCopy = testSuitePastableCopyEntry.getKey();
			iterationTestPlanManager.addTestSuite(iterationCopy, testSuiteCopy);
			bindTestPlanOfCopiedTestSuite(iterationCopy, testSuitePastableCopyEntry, testSuiteCopy);
			copyContentsOnExternalRepository (testSuiteCopy);
		}
	}

	private void bindTestPlanOfCopiedTestSuite(Iteration iterationCopy,
			Entry<TestSuite, List<Integer>> testSuitePastableCopyEntry,
                        TestSuite testSuiteCopy) {
		List<Integer> testSuiteTpiIndexesInIterationList = testSuitePastableCopyEntry.getValue();
		List<IterationTestPlanItem> testPlanItemsToBind = new ArrayList<>();
		List<IterationTestPlanItem> iterationTestPlanCopy = iterationCopy.getTestPlans();
		for (Integer testSuiteTpiIndexInIterationList : testSuiteTpiIndexesInIterationList) {
			IterationTestPlanItem testPlanItemToBind = iterationTestPlanCopy.get(testSuiteTpiIndexInIterationList);
			testPlanItemsToBind.add(testPlanItemToBind);
		}
		testSuiteCopy.bindTestPlanItems(testPlanItemsToBind);
	}

	private void copyCustomFields(Iteration original, Iteration copy) {
		// copy the cufs for both iterations
		customFieldValueManagerService.copyCustomFieldValues(original, copy);

		// now copy the cufs for the test suites
		for (TestSuite originaTestSuite : original.getTestSuites()) {
			TestSuite copyTestSuite = copy.getTestSuiteByName(originaTestSuite.getName());
			// TM-183
			if (projectChanged) {
				updateCustomFieldValues(originaTestSuite, copyTestSuite);
			} else {
				customFieldValueManagerService.copyCustomFieldValuesContent(originaTestSuite, copyTestSuite);
			}
		}
	}

	// Will update values from orginial cfvs to the copy ones if cuf id is the same
	private void updateCustomFieldValues(BoundEntity original, BoundEntity copy) {
		List<CustomFieldValue> originalCfvs = customFieldValueManagerService.findAllCustomFieldValues(original);
		if (originalCfvs.size() != 0) {
			for (CustomFieldValue cfv : customFieldValueManagerService.findAllCustomFieldValues(copy)) {
				for (CustomFieldValue origCfv : customFieldValueManagerService.findAllCustomFieldValues(original)) {
					if (cfv.getCufId().equals(origCfv.getCufId())) {
						cfv.setValue(origCfv.getValue());
						break;
					}
				}
			}
		}
	}

	/**
	 * @see PrivateCustomFieldValueService#copyCustomFieldValues(BoundEntity, BoundEntity)
	 */
	private void copyCustomFields(BoundEntity source, BoundEntity copy) {
		customFieldValueManagerService.copyCustomFieldValues(source, copy);
	}

	private void copyCustomFields(TestCase source, TestCase copy) {
		customFieldValueManagerService.copyCustomFieldValues(source, copy);
		// do the same for the steps if any
		ActionStepCollector collector = new ActionStepCollector();
		List<ActionTestStep> copySteps = collector.collect(copy.getSteps());
		List<ActionTestStep> sourceSteps = collector.collect(source.getSteps());
		int total = copySteps.size();
		Map<Long, BoundEntity> copiedStepsIdsBySource = new HashMap<>(total);
		for (int i = 0; i < total; i++) {
			ActionTestStep copyStep = copySteps.get(i);
			ActionTestStep sourceStep = sourceSteps.get(i);
			copiedStepsIdsBySource.put(sourceStep.getId(), copyStep);
		}
		customFieldValueManagerService.copyCustomFieldValues(copiedStepsIdsBySource, BindableEntity.TEST_STEP);
	}


	@SuppressWarnings(UNCHECKED)
	private <T extends TreeNode> void persistCopy(T copyParam, EntityDao<T> dao, int nameMaxSize) {
		renameIfNeeded((Copiable) copyParam, nameMaxSize);
		dao.persist(copyParam);
		((NodeContainer<T>) destination).addContent(copyParam);
		this.copy = copyParam;
	}

	@SuppressWarnings(UNCHECKED)
	private <T extends TreeNode> void persistCopy(T copyParam, JpaRepository<T, Long> dao, int nameMaxSize) {
		renameIfNeeded((Copiable) copyParam, nameMaxSize);
		dao.save(copyParam);
		((NodeContainer<T>) destination).addContent(copyParam);
		this.copy = copyParam;
	}

	@SuppressWarnings(UNCHECKED)
	private void persistTestCase(TestCase testCase) {
		renameIfNeeded(testCase, Sizes.NAME_MAX);
		testCaseDao.persistTestCaseAndSteps(testCase);
		((NodeContainer<TestCase>) destination).addContent(testCase);
		this.copy = testCase;
	}

	@SuppressWarnings(UNCHECKED)
	private void persitIteration(Iteration copyParam) {
		renameIfNeeded(copyParam, Sizes.NAME_MAX);
		iterationDao.persistIterationAndTestPlan(copyParam);
		((NodeContainer<Iteration>) destination).addContent(copyParam);
		this.copy = copyParam;
	}

	private <T extends Copiable> void renameIfNeeded(T copyParam, int maxNameSize) {
		if (!destination.isContentNameAvailable(copyParam.getName())) {
			String newName = LibraryUtils.generateUniqueCopyName(destination.getContentNames(), copyParam.getName(), maxNameSize);
			copyParam.setName(newName);
		}
	}

	@Override
	public boolean isOkToGoDeeper() {
		return this.okToGoDeeper;
	}

	private void copyRequirementVersionCoverages(RequirementVersion sourceVersion, RequirementVersion copyVersion) {
		List<RequirementVersionCoverage> copies = sourceVersion.createRequirementVersionCoveragesForCopy(copyVersion);
		requirementVersionCoverageDao.persist(copies);
	}

	private void copyRequirementVersionLinks(RequirementVersion sourceVersion, RequirementVersion copyVersion) {
		List<RequirementVersionLink> copies = sourceVersion.createRequirementVersionLinksForCopy(copyVersion);
		requirementVersionLinkDao.addLinks(copies);
	}

	private void copyRequirementVersionCoverage(TestCase source, TestCase copyTestCase) {
		List<RequirementVersionCoverage> copies = source.createRequirementVersionCoveragesForCopy(copyTestCase);
		requirementVersionCoverageDao.persist(copies);
	}

	/*
	 * Where used, that flush matters. Had it been omitted the following scenario would occur :
	 *
	 *  1/ a node is copied along with its custom field values
	 *  2/ has it happens, the copy is created in a different project. Some additional processing is thus carried on by a TreeNodeUpdater
	 *  3/ the custom field values are fixed during this additional step, and the former custom field values are deleted in the process.
	 *  4/ our business code ends, thus triggering the flush of the session.
	 *  5/ Hibernate persists the custom field values created in step 1
	 *  6/ Hibernate deletes those same custom field values in step 3
	 *  7/ The embedded items for custom field values created in step 1 are persisted.
	 *  8/ Since those entities no longer exists a ConstraintViolationException is raised.
	 *
	 * We can prevent this by flushing the session early. This will ensure that the embedded items are persisted and deleted
	 * along with their owners.
	 *
	 */
	private void flush() {
		entityManager.flush();
	}

	private void copyContentsOnExternalRepository (AttachmentHolder attachmentHolder){
		attachmentManagerService.copyContentsOnExternalRepository(attachmentHolder);
	}


}
