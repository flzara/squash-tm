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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.lang.Couple;
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
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.internal.campaign.CampaignNodeDeletionHandler;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;
import org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;
import org.squashtest.tm.service.testautomation.model.SuiteExecutionConfiguration;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;
import org.squashtest.tm.service.testautomation.spi.UnknownConnectorKind;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Transactional
@Service("squashtest.tm.service.AutomatedSuiteManagementService")
public class AutomatedSuiteManagerServiceImpl implements AutomatedSuiteManagerService {

	private static final String DELETE = "DELETE";

	private static final String EXECUTE = "EXECUTE";

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	private static final int DEFAULT_THREAD_TIMEOUT = 30000; // timeout as milliseconds

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
	private IndexationService indexationService;

	@Inject
	private PrivateCustomFieldValueService customFieldValuesService;

	public int getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(int timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}

	/**
	 *
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#findById(java.lang.String)
	 */
	@Override
	public AutomatedSuite findById(String id) {
		return autoSuiteDao.findById(id);
	}

	/**
	 *
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#createFromIterationTestPlan(long)
	 */
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'EXECUTE')" + OR_HAS_ROLE_ADMIN)
	public AutomatedSuite createFromIterationTestPlan(long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		List<IterationTestPlanItem> items = iteration.getTestPlans();
		return createFromItems(items);
	}

	/**
	 *
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#createFromTestSuiteTestPlan(long)
	 */
	@Override
	@PreAuthorize("hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'EXECUTE')" + OR_HAS_ROLE_ADMIN)
	public AutomatedSuite createFromTestSuiteTestPlan(long testSuiteId) {
		TestSuite suite = testSuiteDao.findOne(testSuiteId);
		List<IterationTestPlanItem> items = suite.getTestPlan();
		return createFromItems(items);
	}

	/**
	 *
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#sortByProject(java.lang.String)
	 */
	@Override
	public Collection<TestAutomationProjectContent> sortByProject(String autoSuiteId) {
		// security delegated to sortByProject(AutomatedSuite)
		AutomatedSuite suite = findById(autoSuiteId);
		return sortByProject(suite);
	}

	/**
	 *
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
	 *
	 * @see org.squashtest.tm.service.testautomation.AutomatedSuiteManagerService#delete(java.lang.String)
	 */
	@Override
	// security delegated to delete(AutomatedSuite)
	public void delete(String automatedSuiteId) {
		AutomatedSuite suite = findById(automatedSuiteId);
		delete(suite);
	}

	/**
	 *
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

		PermissionsUtils.checkPermission(permissionService, suite.getExecutionExtenders(), EXECUTE);

		ExtenderSorter sorter = new ExtenderSorter(suite, configuration);

		TestAutomationCallbackService securedCallback = new CallbackServiceSecurityWrapper(callbackService);

		while (sorter.hasNext()) {

			Entry<String, Collection<AutomatedExecutionExtender>> extendersByKind = sorter.getNextEntry();

			TestAutomationConnector connector;

			try {
				connector = connectorRegistry.getConnectorForKind(extendersByKind.getKey());
				Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> tests = collectAutomatedExecs(extendersByKind
					.getValue());
				connector.executeParameterizedTests(tests, suite.getId(), securedCallback);
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
	}


	// ******************* create suite private methods ***************************


	private AutomatedSuite createFromItems(List<IterationTestPlanItem> items) {

		AutomatedSuite newSuite = autoSuiteDao.createNewSuite();

		for (IterationTestPlanItem item : items) {
			if (item.isAutomated()) {
				Execution exec = addAutomatedExecution(item);
				newSuite.addExtender(exec.getAutomatedExecutionExtender());
			}
		}


		return newSuite;

	}

	private Execution addAutomatedExecution(IterationTestPlanItem item) throws TestPlanItemNotExecutableException {

		Execution execution = item.createAutomatedExecution();

		executionDao.save(execution);
		item.addExecution(execution);

		createCustomFieldsForExecutionAndExecutionSteps(execution);
		createDenormalizedFieldsForExecutionAndExecutionSteps(execution);
		indexationService.reindexTestCase(item.getReferencedTestCase().getId());

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


	// ******************* execute suite private methods **************************

	private Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> collectAutomatedExecs(
		Collection<AutomatedExecutionExtender> extenders) {

		Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> tests = new ArrayList<>(
			extenders.size());

		for (AutomatedExecutionExtender extender : extenders) {
			tests.add(createAutomatedExecAndParams(extender));
		}

		return tests;

	}

	private Couple<AutomatedExecutionExtender, Map<String, Object>> createAutomatedExecAndParams(AutomatedExecutionExtender extender) {
		Execution execution = extender.getExecution();

		Collection<CustomFieldValue> tcFields = customFieldValueFinder.findAllCustomFieldValues(execution
			.getReferencedTestCase());
		Collection<CustomFieldValue> iterFields = customFieldValueFinder.findAllCustomFieldValues(execution
			.getIteration());
		Collection<CustomFieldValue> campFields = customFieldValueFinder.findAllCustomFieldValues(execution
			.getCampaign());

		Map<String, Object> params = paramBuilder.get().testCase().addEntity(execution.getReferencedTestCase())
			.addCustomFields(tcFields).iteration().addCustomFields(iterFields).campaign()
			.addCustomFields(campFields).build();

		return new Couple<>(extender, params);
	}

	private void notifyExecutionError(Collection<AutomatedExecutionExtender> failedExecExtenders, String message) {
		for (AutomatedExecutionExtender extender : failedExecExtenders) {
			extender.setExecutionStatus(ExecutionStatus.ERROR);
			extender.setResultSummary(message);
		}
	}


	/**
	 * That wrapper is a TestAutomationCallbackService, that ensures that the security context is properly set for any
	 * thread that requires its services.
	 *
	 * @author bsiri
	 *
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
			extendersByKind = new TreeMap();

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

}
