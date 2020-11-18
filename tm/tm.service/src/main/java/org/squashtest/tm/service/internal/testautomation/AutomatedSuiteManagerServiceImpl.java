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
package org.squashtest.tm.service.internal.testautomation;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.api.security.acls.Roles;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.internal.campaign.CampaignNodeDeletionHandler;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService;
import org.squashtest.tm.service.internal.repository.AutomatedExecutionExtenderDao;
import org.squashtest.tm.service.internal.repository.AutomatedSuiteDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;
import org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;
import org.squashtest.tm.service.testautomation.model.AutomatedSuiteCreationSpecification;
import org.squashtest.tm.service.testautomation.model.AutomatedSuitePreview;
import org.squashtest.tm.service.testautomation.model.SuiteExecutionConfiguration;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;
import org.squashtest.tm.service.testautomation.spi.UnknownConnectorKind;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.squashtest.tm.service.security.Authorizations.EXECUTE_ITERATION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.EXECUTE_TS_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Transactional
@Service("squashtest.tm.service.AutomatedSuiteManagementService")
public class AutomatedSuiteManagerServiceImpl implements AutomatedSuiteManagerService {

	private static final String DELETE = "DELETE";

	private static final String EXECUTE = "EXECUTE";

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	private static final int DEFAULT_THREAD_TIMEOUT = 30000; // timeout as milliseconds

	public static final long DEFAULT_SUITE_SAVING_DURATION_IN_DAYS = 30; // days

	private int timeoutMillis = DEFAULT_THREAD_TIMEOUT;


	@Inject
	private AutomatedSuiteDao autoSuiteDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private IterationTestPlanDao testPlanDao;

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private AutomatedExecutionExtenderDao automatedExecutionExtenderDao;

	@Inject
	private CustomFieldValueFinderService customFieldValueFinder;

	@Inject
	private PrivateDenormalizedFieldValueService denormalizedFieldValueService;

	@Inject
	private TestAutomationCallbackService callbackService;

	@Inject
	private Provider<TaParametersBuilder> paramBuilder;

	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private CampaignNodeDeletionHandler deletionHandler;

	@Inject
	private PrivateCustomFieldValueService customFieldValuesService;

	@Inject
	private ProjectDao projectDao;

	@PersistenceContext
	private EntityManager entityManager;

	public int getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(int timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#findById(java.lang.String)
	 */
	@Override
	public AutomatedSuite findById(String id) {
		return autoSuiteDao.findById(id);
	}

	@Override
	public List<Long> findTpiIdsWithAutomaticExecutionMode(EntityReference entityReference) {
		if (EntityType.ITERATION.equals(entityReference.getType())) {
			Iteration iteration = iterationDao.findById(entityReference.getId());
			return doFindTpiIdsWithAutomaticExecutionMode(iteration.getTestPlans());
		} else if (EntityType.TEST_SUITE.equals(entityReference.getType())) {
			TestSuite suite = testSuiteDao.getOne(entityReference.getId());
			return doFindTpiIdsWithAutomaticExecutionMode(suite.getTestPlan());
		} else {
			throw new IllegalArgumentException("An iteration or a test suite was expected");
		}
	}

	@Override
	// security delegated to permission utils
	public AutomatedSuitePreview preview(AutomatedSuiteCreationSpecification specification) {

		// first, validate
		specification.validate();

		// check permission
		checkPermission(specification);

		//fetch
		List<Couple<TestAutomationProject, Long>> projects = autoSuiteDao.findAllCalledByTestPlan(specification.getContext(), specification.getTestPlanSubsetIds());

		// create the response
		AutomatedSuitePreview preview = new AutomatedSuitePreview();
		preview.setSpecification(specification);

		boolean hasSlaves = projects.stream().anyMatch(couple -> couple.getA1().getServer().isManualSlaveSelection());

		Collection<AutomatedSuitePreview.TestAutomationProjectPreview> projectPreview =
			projects.stream().map(
				couple -> {
					TestAutomationProject taProject = couple.getA1();
					Long testCount = couple.getA2();
					return new AutomatedSuitePreview.TestAutomationProjectPreview(
						taProject.getId(),
						taProject.getLabel(),
						taProject.getServer().getName(),
						taProject.getSlaves(),
						testCount
					);
				}).collect(Collectors.toList());


		preview.setManualSlaveSelection(hasSlaves);
		preview.setProjects(projectPreview);

		return preview;

	}

	@Override
	// security delegated to permission utils
	public List<String> findTestListPreview(AutomatedSuiteCreationSpecification specification, long automatedProjectId) {
		specification.validate();
		checkPermission(specification);
		return autoSuiteDao.findTestPathForAutomatedSuiteAndProject(specification.getContext(), specification.getTestPlanSubsetIds(), automatedProjectId);
	}

	@Override
	// security delegated to permission utils
	public AutomatedSuite createFromSpecification(AutomatedSuiteCreationSpecification specification) {
		specification.validate();
		checkPermission(specification);

		// TODO : something better
		// for now we plug on the existing methods
		AutomatedSuite suite = null;

		Long contextId = specification.getContext().getId();
		List<Long> subset = specification.getTestPlanSubsetIds();
		boolean hasTestPlanSubset = (subset != null && !subset.isEmpty());

		if (specification.getContext().getType() == EntityType.ITERATION) {
			if (hasTestPlanSubset) {
				suite = createFromItemsAndIteration(subset, contextId);
			} else {
				suite = createFromIterationTestPlan(contextId);
			}
		} else {
			if (hasTestPlanSubset) {
				suite = createFromItemsAndTestSuite(subset, contextId);
			} else {
				suite = createFromTestSuiteTestPlan(contextId);
			}
		}

		return suite;

	}

	@Override
	public AutomatedSuite createAndExecute(AutomatedSuiteCreationSpecification specification) {
		LOGGER.info("START CREATING EXECUTIONS " + new Date());
		AutomatedSuite suite = createFromSpecification(specification);
		LOGGER.info("END CREATING EXECUTIONS " + new Date());
		LOGGER.info("START SENDING EXECUTIONS " + new Date());
		start(suite, specification.getExecutionConfigurations());
		LOGGER.info("END SENDING EXECUTIONS " + new Date());
		return suite;
	}

	// assumes that the specification was validated first
	private void checkPermission(AutomatedSuiteCreationSpecification specification) {
		List<Long> singleId = new ArrayList<>();
		singleId.add(specification.getContext().getId());

		Class<?> clazz = (specification.getContext().getType() == EntityType.ITERATION) ? Iteration.class : TestSuite.class;
		PermissionsUtils.checkPermission(permissionService, singleId, EXECUTE, clazz.getName());
	}

	private void checkPermissionOnIteration(Long iterationId){
		if(!permissionService.hasRole(Roles.ROLE_TA_API_CLIENT) && !permissionService.hasRoleOrPermissionOnObject(Roles.ROLE_ADMIN, EXECUTE, iterationId, Iteration.class.getName())) {
			throw new AccessDeniedException("Access is denied");
		}
	}

	private void checkPermissionOnTestSuite(Long testSuiteId){
		if(!permissionService.hasRole(Roles.ROLE_TA_API_CLIENT) && !permissionService.hasRoleOrPermissionOnObject(Roles.ROLE_ADMIN, EXECUTE, testSuiteId, TestSuite.class.getName())) {
			throw new AccessDeniedException("Access is denied");
		}
	}

	private void checkPermissionOnExecuteAutomatedSuite(AutomatedSuite automatedSuite){
		if(!permissionService.hasRole(Roles.ROLE_TA_API_CLIENT)) {
			throw new AccessDeniedException("Access is denied");
		}
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#createFromIterationTestPlanItems(long, List<IterationTestPlanItem>)
	 */
	@Override
	public AutomatedSuite createFromIterationTestPlanItems(long iterationId, List<IterationTestPlanItem> items) {
		for (IterationTestPlanItem item : items) {
			if(!item.getIteration().getId().equals(iterationId)) {
				throw new IllegalArgumentException("All items must belong to the same selected iteration");
			}
		}
		checkPermissionOnIteration(iterationId);
		return createFromItems(items);
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#createFromIterationTestPlan(long)
	 */
	@Override
	@PreAuthorize(EXECUTE_ITERATION_OR_ROLE_ADMIN)
	public AutomatedSuite createFromIterationTestPlan(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		List<IterationTestPlanItem> items = iteration.getTestPlans();
		return createFromItems(items);
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#createFromTestSuiteTestPlanItems(long, List<IterationTestPlanItem>)
	 */
	@Override
	public AutomatedSuite createFromTestSuiteTestPlanItems(long testSuiteId, List<IterationTestPlanItem> items) {
		for (IterationTestPlanItem item : items) {
			boolean isInTargetTestSuite = item.getTestSuites().stream().map(TestSuite::getId).anyMatch(suiteID -> suiteID.equals(testSuiteId));
			if(!isInTargetTestSuite) {
				throw new IllegalArgumentException("All items must belong to the same selected test suite");
			}
		}
		checkPermissionOnTestSuite(testSuiteId);
		return createFromItems(items);
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#createFromTestSuiteTestPlan(long)
	 */
	@Override
	@PreAuthorize(EXECUTE_TS_OR_ROLE_ADMIN)
	public AutomatedSuite createFromTestSuiteTestPlan(long testSuiteId) {
		TestSuite suite = testSuiteDao.getOne(testSuiteId);
		List<IterationTestPlanItem> items = suite.getTestPlan();
		return createFromItems(items);
	}


	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#sortByProject(java.lang.String)
	 */
	@Override
	public Collection<TestAutomationProjectContent> sortByProject(String autoSuiteId) {
		// security delegated to sortByProject(AutomatedSuite)
		AutomatedSuite suite = findById(autoSuiteId);
		return sortByProject(suite);
	}


	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#sortByProject(org.squashtest.tm.domain.testautomation.AutomatedSuite)
	 */

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Collection<TestAutomationProjectContent> sortByProject(AutomatedSuite suite) {
		// security handled by in the code

		List<AutomatedExecutionExtender> extenders = suite.getExecutionExtenders();

		PermissionsUtils.checkPermission(permissionService, extenders, EXECUTE);

		// first sort them using a map
		MultiMap testsByProjects = new MultiValueMap();

		for (AutomatedExecutionExtender extender : extenders) {
			if (extender.isProjectDisassociated()) {
				continue;
			}
			TestAutomationProject project = extender.getAutomatedProject();
			AutomatedTest test = extender.getAutomatedTest();

			testsByProjects.put(project, test);
		}


		// now make a friendly bean of it
		Collection<TestAutomationProjectContent> projectContents = new LinkedList<>();

		Set<Entry> entries = testsByProjects.entrySet();
		for (Entry e : entries) {
			TestAutomationProject project = (TestAutomationProject) e.getKey();
			Collection<AutomatedTest> tests = (Collection) e.getValue();
			TestAutomationConnector connector = connectorRegistry.getConnectorForKind(project.getServer().getKind());
			boolean orderGuaranteed = connector.testListIsOrderGuaranteed(tests);
			projectContents.add(new TestAutomationProjectContent(project, tests, orderGuaranteed));
		}

		return projectContents;

	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#delete(java.lang.String)
	 */
	@Override
	// security delegated to delete(AutomatedSuite)
	public void delete(String automatedSuiteId) {
		AutomatedSuite suite = findById(automatedSuiteId);
		delete(suite);
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#delete(org.squashtest.tm.domain.testautomation.AutomatedSuite)
	 */
	@SuppressWarnings("unchecked")
	@Override
	// security handled in the code
	public void delete(AutomatedSuite suite) {

		PermissionsUtils.checkPermission(permissionService, suite.getExecutionExtenders(), DELETE);

		List<AutomatedExecutionExtender> toremove = new ArrayList<>(suite.getExecutionExtenders());
		suite.getExecutionExtenders().clear();

		List<Execution> execs =
			new ArrayList<>(CollectionUtils.collect(toremove, new ExecutionCollector()));

		deletionHandler.deleteExecutions(execs);

		autoSuiteDao.delete(suite);
	}

	@PreAuthorize(HAS_ROLE_ADMIN)
	private void deleteAutomatedSuites(List<String> automatedSuiteIds) {
		List<Long> executionIds = executionDao.findAllIdsByAutomatedSuiteIds(automatedSuiteIds);
		if (executionIds.isEmpty()) {
			return;
		}
		List<Execution> executions = executionDao.findAllWithTestPlanItemByIds(executionIds);
		deletionHandler.bulkDeleteExecutions(executions);
		autoSuiteDao.deleteAllByIds(automatedSuiteIds);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void cleanOldSuites() {
		LocalDateTime limitDateTime = LocalDateTime.now().minusDays(DEFAULT_SUITE_SAVING_DURATION_IN_DAYS);

		List<String> oldAutomatedSuiteIds = autoSuiteDao.getOldAutomatedSuiteIds(limitDateTime);
		if (oldAutomatedSuiteIds.isEmpty()) {
			return;
		}
		List<List<String>> automatedSuiteIdPartitions = Lists.partition(oldAutomatedSuiteIds, 10);
		automatedSuiteIdPartitions.forEach(automatedSuiteIdPartition -> {
			deleteAutomatedSuites(automatedSuiteIdPartition);
			entityManager.flush();
			entityManager.clear();
		});
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Execution> findExecutionsByAutomatedTestSuiteId(String automatedTestSuiteId) {

		List<Execution> executions = new ArrayList<>();
		AutomatedSuite suite = autoSuiteDao.findById(automatedTestSuiteId);
		for (AutomatedExecutionExtender e : suite.getExecutionExtenders()) {
			executions.add(e.getExecution());
		}
		return executions;
	}


	@Override
	// security delegated to start(AutomatedSuite, Collection)
	public void start(String autoSuiteId) {
		AutomatedSuite suite = autoSuiteDao.findById(autoSuiteId);
		start(suite, new ArrayList<SuiteExecutionConfiguration>());
	}

	@Override
	// security delegated to start(AutomatedSuite, Collection)
	public void start(AutomatedSuite suite) {
		start(suite, new ArrayList<SuiteExecutionConfiguration>());
	}

	@Override
	// security delegated to start(AutomatedSuite, Collection)
	public void start(String suiteId, Collection<SuiteExecutionConfiguration> configuration) {
		AutomatedSuite suite = autoSuiteDao.findById(suiteId);
		start(suite, configuration);
	}


	@Override
	// security handled in the code
	public void start(AutomatedSuite suite, Collection<SuiteExecutionConfiguration> configuration) {
		List<AutomatedExecutionExtender> executionExtenders = getOptimizedExecutionsExtenders(suite);
		LOGGER.debug("- START CHECKING EXECUTIONS PERMISSIONS " + new Date());
		PermissionsUtils.checkPermission(permissionService, executionExtenders, EXECUTE);
		LOGGER.debug("- END CHECKING EXECUTIONS PERMISSIONS " + new Date());
		LOGGER.debug("- START SORTING EXECUTIONS " + new Date());
		ExtenderSorter sorter = new ExtenderSorter(suite, configuration);
		LOGGER.debug("- END SORTING EXECUTIONS " + new Date());
		LOGGER.debug("- START COLLECTING AND SENDING ALL AUTOMATED EXECUTIONS " + new Date());
		TestAutomationCallbackService securedCallback = new CallbackServiceSecurityWrapper(callbackService);

		while (sorter.hasNext()) {

			Entry<String, Collection<AutomatedExecutionExtender>> extendersByKind = sorter.getNextEntry();

			TestAutomationConnector connector;

			try {
				connector = connectorRegistry.getConnectorForKind(extendersByKind.getKey());
				LOGGER.debug("-- START COLLECTING AUTOMATED EXECUTIONS FOR " + extendersByKind.getKey() + " " + new Date());
				Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> tests = collectAutomatedExecs(extendersByKind
					.getValue(), true);
				LOGGER.debug("-- END COLLECTING AUTOMATED EXECUTIONS FOR " + extendersByKind.getKey() + " " + new Date());
				LOGGER.debug("-- START SENDING AUTOMATED EXECUTIONS FOR " + extendersByKind.getKey() + " " + new Date());
				connector.executeParameterizedTests(tests, suite.getId(), securedCallback);
				LOGGER.debug("-- END SENDING AUTOMATED EXECUTIONS FOR " + extendersByKind.getKey() + " " + new Date());
			} catch (UnknownConnectorKind ex) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Test Automation : unknown connector :", ex);
				}
				notifyExecutionError(extendersByKind.getValue(), ex.getMessage());
			} catch (TestAutomationException ex) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Test Automation : an error occured :", ex);
				}
				notifyExecutionError(extendersByKind.getValue(), ex.getMessage());
			}

		}
		LOGGER.debug("- END COLLECTING AND SENDING ALL EXECUTIONS " + new Date());
	}
	/*
	 * [SQUASH-142]
	 * This method is only used by an automation server hence the permission check
	 */
	@Override
	public Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> prepareExecutionOrder(AutomatedSuite suite, boolean withAllCustomFields) {
		if(!permissionService.hasRole(Roles.ROLE_TA_API_CLIENT)) {
			throw new AccessDeniedException("Access is denied");
		}
		getOptimizedExecutionsExtenders(suite);
		return collectAutomatedExecs(suite.getExecutionExtenders(), withAllCustomFields);
	}


	// ******************* create suite private methods ***************************


	private AutomatedSuite createFromItems(List<IterationTestPlanItem> items) {
		List<Long> itemIds = items.stream().map(IterationTestPlanItem::getId).collect(Collectors.toList());
		Long projectId = items.get(0).getProject().getId();
		String newSuiteId = createSuiteAndClearSession();
		createExecutionsAndClearSession(itemIds, newSuiteId, projectId);
		// session is cleared we must fetch again the automated suite
		return entityManager.find(AutomatedSuite.class, newSuiteId);
	}

	private void createExecutionsAndClearSession(List<Long> itemIds, String newSuiteId, Long projectId) {
		List<List<Long>> partitionedIds = Lists.partition(itemIds, 10);
		for (List<Long> ids : partitionedIds) {
			createOneBatchOfExecution(ids, newSuiteId, projectId);
			entityManager.flush();
			entityManager.clear();
		}
	}

	private String createSuiteAndClearSession() {
		AutomatedSuite newSuite = autoSuiteDao.createNewSuite();
		entityManager.flush();
		String newSuiteId = newSuite.getId();
		entityManager.clear();
		return newSuiteId;
	}

	private void createOneBatchOfExecution(List<Long> ids, String newSuiteId, Long projectId) {
		// prefetch the project to avoid auto proxy queries
		projectDao.fetchForAutomatedExecutionCreation(projectId);
		AutomatedSuite automatedSuite = entityManager.find(AutomatedSuite.class, newSuiteId);
		List<IterationTestPlanItem> items = testPlanDao.fetchForAutomatedExecutionCreation(ids);
		for (IterationTestPlanItem item : items) {
			if (item.isAutomated()) {
				Execution execution = item.createAutomatedExecution();
				executionDao.save(execution);
				item.addExecution(execution);
				createCustomFieldsForExecutionAndExecutionSteps(execution);
				createDenormalizedFieldsForExecutionAndExecutionSteps(execution);
				automatedSuite.addExtender(execution.getAutomatedExecutionExtender());
			}
		}
	}

	private Execution addAutomatedExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

                Execution execution = item.createAutomatedExecution();

		executionDao.save(execution);
                item.addExecution(execution);

		createCustomFieldsForExecutionAndExecutionSteps(execution);
		createDenormalizedFieldsForExecutionAndExecutionSteps(execution);

		return execution;
	}

	private void createCustomFieldsForExecutionAndExecutionSteps(Execution execution) {
		customFieldValuesService.createAllCustomFieldValues(execution, execution.getProject());
		customFieldValuesService.createAllCustomFieldValues(execution.getSteps(), execution.getProject());
	}

	private void createDenormalizedFieldsForExecutionAndExecutionSteps(Execution execution) {
		LOGGER.debug("Create denormalized fields for Execution {}", execution.getId());

		TestCase sourceTC = execution.getReferencedTestCase();
		denormalizedFieldValueService.createAllDenormalizedFieldValues(sourceTC, execution);
		denormalizedFieldValueService.createAllDenormalizedFieldValuesForSteps(execution);

	}

	private List<Long> doFindTpiIdsWithAutomaticExecutionMode(List<IterationTestPlanItem> itpis) {
		return itpis.stream()
			.filter(IterationTestPlanItem::isAutomated)
			.map(IterationTestPlanItem::getId)
			.collect(Collectors.toList());
	}

	private List<AutomatedExecutionExtender> getOptimizedExecutionsExtenders(AutomatedSuite suite){
		LOGGER.debug("- START FETCHING OPTIMIZED " + new Date());
		List<AutomatedExecutionExtender> executionExtenders = autoSuiteDao.findAndFetchForAutomatedExecutionCreation(suite.getId());
		LOGGER.debug("- FETCHED " + executionExtenders.size());
		LOGGER.debug("- END FETCHING OPTIMIZED " + new Date());
		return executionExtenders;
	}


	// ******************* execute suite private methods **************************

	private Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> collectAutomatedExecs(
		Collection<AutomatedExecutionExtender> extenders, boolean withAllCustomFields) {

		Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> tests = new ArrayList<>(
			extenders.size());

		CustomFieldValuesForExec customFieldValuesForExec = fetchCustomFieldValues(extenders, withAllCustomFields);
		for (AutomatedExecutionExtender extender : extenders) {
			tests.add(createAutomatedExecAndParams(extender, customFieldValuesForExec));
		}
		return tests;

	}

	private CustomFieldValuesForExec fetchCustomFieldValues(Collection<AutomatedExecutionExtender> extenders, boolean withAllCustomFields) {
		Map<Long, List<CustomFieldValue>> testCaseCfv = fetchTestCaseCfv(extenders);
		Map<Long, List<CustomFieldValue>> iterationCfv = withAllCustomFields ? fetchIterationCfv(extenders) : Collections.emptyMap();
		Map<Long, List<CustomFieldValue>> campaignCfv = withAllCustomFields ? fetchCampaignCfv(extenders) : Collections.emptyMap();
		Map<Long, List<CustomFieldValue>> testSuiteCfv = withAllCustomFields ? fetchTestSuiteCfv(extenders) : Collections.emptyMap();
		return new CustomFieldValuesForExec(testCaseCfv, iterationCfv, campaignCfv, testSuiteCfv);
	}

	private Map<Long, List<CustomFieldValue>> fetchTestCaseCfv(Collection<AutomatedExecutionExtender> extenders) {
		List<TestCase> testCases = extenders.stream()
			.map(extender -> extender
				.getExecution()
				.getReferencedTestCase())
			.collect(Collectors.toList());
		return customFieldValueFinder.findAllCustomFieldValues(testCases).stream().collect(Collectors.groupingBy(CustomFieldValue::getBoundEntityId));
	}

	private Map<Long, List<CustomFieldValue>> fetchIterationCfv(Collection<AutomatedExecutionExtender> extenders) {
		List<Iteration> iterations = extenders.stream()
			.map(extender -> extender
				.getExecution()
				.getTestPlan()
				.getIteration())
			.collect(Collectors.toList());
		return customFieldValueFinder.findAllCustomFieldValues(iterations).stream().collect(Collectors.groupingBy(CustomFieldValue::getBoundEntityId));
	}

	private Map<Long, List<CustomFieldValue>> fetchCampaignCfv(Collection<AutomatedExecutionExtender> extenders) {
		List<Campaign> iterations = extenders.stream()
			.map(extender -> extender
				.getExecution()
				.getTestPlan()
				.getIteration()
				.getCampaign())
			.collect(Collectors.toList());
		return customFieldValueFinder.findAllCustomFieldValues(iterations).stream().collect(Collectors.groupingBy(CustomFieldValue::getBoundEntityId));
	}

	private Map<Long, List<CustomFieldValue>> fetchTestSuiteCfv(Collection<AutomatedExecutionExtender> extenders) {
		List<TestSuite> testSuites = extenders.stream()
			.map(extender -> extender
				.getExecution()
				.getTestPlan()
				.getTestSuites())
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		return customFieldValueFinder.findAllCustomFieldValues(testSuites).stream().collect(Collectors.groupingBy(CustomFieldValue::getBoundEntityId));
	}

	private Couple<AutomatedExecutionExtender, Map<String, Object>> createAutomatedExecAndParams(AutomatedExecutionExtender extender, CustomFieldValuesForExec customFieldValuesForExec) {
		Execution execution = extender.getExecution();

		Collection<CustomFieldValue> tcFields = customFieldValuesForExec.getValueForTestcase(execution.getReferencedTestCase().getId());
		Collection<CustomFieldValue> iterFields = customFieldValuesForExec.getValueForIteration(execution.getIteration().getId());
		Collection<CustomFieldValue> campFields = customFieldValuesForExec.getValueForCampaign(execution.getCampaign().getId());
		Collection<CustomFieldValue> testSuiteFields = execution
			.getTestPlan()
			.getTestSuites()
			.stream()
			.map(TestSuite::getId)
			.map(customFieldValuesForExec::getValueForTestSuite)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		Map<String, Object> params = paramBuilder.get().testCase().addEntity(
			execution.getReferencedTestCase()).addCustomFields(tcFields).
			iteration().addCustomFields(iterFields).
			campaign().addCustomFields(campFields).
			testSuite().addCustomFields(testSuiteFields).
			dataset().addEntity(execution.getTestPlan().getReferencedDataset())
			.build();

		return new Couple<>(extender, params);
	}

	private void notifyExecutionError(Collection<AutomatedExecutionExtender> failedExecExtenders, String message) {
		for (AutomatedExecutionExtender extender : failedExecExtenders) {
			extender.setExecutionStatus(ExecutionStatus.ERROR);
			extender.setResultSummary(HtmlUtils.htmlEscape(message));
		}
	}


	/**
	 * That wrapper is a TestAutomationCallbackService, that ensures that the security context is properly set for any
	 * thread that requires its services.
	 *
	 * @author bsiri
	 */
	private static class CallbackServiceSecurityWrapper implements TestAutomationCallbackService {

		private SecurityContext secContext;

		private TestAutomationCallbackService wrapped;

		/*
		 * the SecurityContext here is the one from the original thread. The others methods will use that instance of
		 * SecurityContext for all their operations from now on (see the code, it's straightforward).
		 */
		CallbackServiceSecurityWrapper(TestAutomationCallbackService service) {
			secContext = SecurityContextHolder.getContext();
			wrapped = service;
		}

		@Override
		public void updateResultURL(AutomatedExecutionSetIdentifier execIdentifier, URL resultURL) {
			SecurityContextHolder.setContext(secContext);
			wrapped.updateResultURL(execIdentifier, resultURL);
		}

		@Override
		public void updateExecutionStatus(AutomatedExecutionSetIdentifier execIdentifier, ExecutionStatus newStatus) {
			SecurityContextHolder.setContext(secContext);
			wrapped.updateExecutionStatus(execIdentifier, newStatus);

		}

		@Override
		public void updateResultSummary(AutomatedExecutionSetIdentifier execIdentifier, String newSummary) {
			SecurityContextHolder.setContext(secContext);
			wrapped.updateResultSummary(execIdentifier, newSummary);
		}

	}


	private static class ExtenderSorter {

		private Map<Long, SuiteExecutionConfiguration> configurationByProject;

		private Map<String, Collection<AutomatedExecutionExtender>> extendersByKind;

		private Iterator<Entry<String, Collection<AutomatedExecutionExtender>>> iterator = null;

		public ExtenderSorter(AutomatedSuite suite, Collection<SuiteExecutionConfiguration> configuration) {

			configurationByProject = new HashMap<>(configuration.size());

			for (SuiteExecutionConfiguration conf : configuration) {
				configurationByProject.put(conf.getProjectId(), conf);
			}

			// rem : previous impl relied on a HashMap, which broke the tests on java 8. as I have no damn clue about
			// the desired order, let's retort to keys natural order using a TreeMap
			extendersByKind = new TreeMap<>();

			for (AutomatedExecutionExtender extender : suite.getExecutionExtenders()) {

				String serverKind = extender.getAutomatedTest().getProject().getServer().getKind();

				register(extender, serverKind);

			}

			iterator = extendersByKind.entrySet().iterator();

		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public Map.Entry<String, Collection<AutomatedExecutionExtender>> getNextEntry() {

			return iterator.next();

		}

		private void register(AutomatedExecutionExtender extender, String serverKind) {

			if (!extendersByKind.containsKey(serverKind)) {
				extendersByKind.put(serverKind, new LinkedList<AutomatedExecutionExtender>());
			}

			SuiteExecutionConfiguration conf = configurationByProject.get(extender.getAutomatedProject().getId());
			if (conf != null) {
				extender.setNodeName(conf.getNode());
			}

			extendersByKind.get(serverKind).add(extender);

		}

	}

	private static final class ExecutionCollector implements Transformer {
		@Override
		public Object transform(Object input) {
			return ((AutomatedExecutionExtender) input).getExecution();
		}
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#createFromItemsAndIteration(java.util.List, long)
	 */
	@Override
	public AutomatedSuite createFromItemsAndIteration(List<Long> testPlanIds, long iterationId) {
		PermissionsUtils.checkPermission(permissionService, testPlanIds, EXECUTE, IterationTestPlanItem.class.getName());

		List<IterationTestPlanItem> items = testPlanDao.findAllByIdsOrderedByIterationTestPlan(testPlanIds);

		return createFromItems(items);
	}

	/**
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#createFromItemsAndTestSuite(java.util.List, long)
	 */
	@Override
	public AutomatedSuite createFromItemsAndTestSuite(List<Long> testPlanIds, long testSuiteId) {
		PermissionsUtils.checkPermission(permissionService, testPlanIds, EXECUTE, IterationTestPlanItem.class.getName());

		List<IterationTestPlanItem> items = testPlanDao.findAllByIdsOrderedBySuiteTestPlan(testPlanIds, testSuiteId);

		return createFromItems(items);
	}

	@Override
	public PagedCollectionHolder<List<AutomatedSuite>> getAutomatedSuitesByIterationID(Long iterationId, PagingAndMultiSorting paging, ColumnFiltering filter) {
		List<AutomatedSuite> suites = autoSuiteDao.findAutomatedSuitesByIterationID(iterationId, paging, filter);
		long suiteSize = autoSuiteDao.countSuitesByIterationId(iterationId, filter);
		return new PagingBackedPagedCollectionHolder<>(paging, suiteSize, suites);
	}

	@Override
	public PagedCollectionHolder<List<AutomatedSuite>> getAutomatedSuitesByTestSuiteID(Long suiteId, PagingAndMultiSorting paging, ColumnFiltering filter) {
		List<AutomatedSuite> suites = autoSuiteDao.findAutomatedSuitesByTestSuiteID(suiteId, paging, filter);
		long suiteSize = autoSuiteDao.countSuitesByTestSuiteId(suiteId, filter);
		return new PagingBackedPagedCollectionHolder<>(paging, suiteSize, suites);
	}

	public static class CustomFieldValuesForExec {
		Map<Long, List<CustomFieldValue>> testCaseCfv;
		Map<Long, List<CustomFieldValue>> iterationCfv;
		Map<Long, List<CustomFieldValue>> campaignCfv;
		Map<Long, List<CustomFieldValue>> suiteCfv;

		public CustomFieldValuesForExec(Map<Long, List<CustomFieldValue>> testCaseCfv, Map<Long, List<CustomFieldValue>> iterationCfv, Map<Long, List<CustomFieldValue>> campaignCfv, Map<Long, List<CustomFieldValue>> suiteCfv) {
			this.testCaseCfv = testCaseCfv;
			this.iterationCfv = iterationCfv;
			this.campaignCfv = campaignCfv;
			this.suiteCfv = suiteCfv;
		}

		public List<CustomFieldValue> getValueForTestcase(Long testCaseId) {
			return this.testCaseCfv.getOrDefault(testCaseId, emptyList());
		}

		public List<CustomFieldValue> getValueForIteration(Long iterationId) {
			return this.iterationCfv.getOrDefault(iterationId, emptyList());
		}

		public List<CustomFieldValue> getValueForCampaign(Long campaignId) {
			return this.campaignCfv.getOrDefault(campaignId, emptyList());
		}

		public List<CustomFieldValue> getValueForTestSuite(Long suiteId) {
			return this.suiteCfv.getOrDefault(suiteId, emptyList());
		}

	}
}
