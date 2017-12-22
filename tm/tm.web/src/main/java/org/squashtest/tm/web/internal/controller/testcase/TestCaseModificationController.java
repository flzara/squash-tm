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
package org.squashtest.tm.web.internal.controller.testcase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.core.foundation.collection.*;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.servers.AuthenticationStatus;
import org.squashtest.tm.domain.testcase.*;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.dto.CustomFieldJsonConverter;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;
import org.squashtest.tm.service.internal.dto.json.JsonInfoList;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.ParameterFinder;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.bugtracker.BugTrackerControllerHelper;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.*;
import org.squashtest.tm.web.internal.controller.testcase.parameters.ParameterNameComparator;
import org.squashtest.tm.web.internal.controller.testcase.parameters.ParametersModelHelper;
import org.squashtest.tm.web.internal.controller.testcase.parameters.TestCaseDatasetsController;
import org.squashtest.tm.web.internal.controller.testcase.steps.TestStepsTableModelBuilder;
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.JsonInfoListBuilder;
import org.squashtest.tm.web.internal.model.combo.OptionTag;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.json.JsonEnumValue;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;
import org.squashtest.tm.web.internal.model.json.JsonTestCase;
import org.squashtest.tm.web.internal.model.json.JsonTestCaseBuilder;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

@Controller
@RequestMapping("/test-cases/{testCaseId}")
public class TestCaseModificationController {
	/**
	 *
	 */
	private static final String TEST_CASE = "testCase";
	private static final String OPTIMIZED = "optimized";

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseModificationController.class);

	/**
	 *
	 */
	private static final String NAME = "name";

	private static final String TEST_CASE_ = "test case "; // NOSONAR generated name

	private final DatatableMapper<String> referencingTestCaseMapper = new NameBasedMapper(6)
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, NAME, Project.class)
		.mapAttribute("tc-reference", "reference", TestCase.class)
		.mapAttribute("tc-name", NAME, TestCase.class)
		.mapAttribute("tc-mode", "executionMode", TestCase.class);


	@Inject
	private TestCaseModificationService testCaseModificationService;

	@Inject
	private ExecutionFinder executionFinder;

	@Inject
	private ParameterFinder parameterFinder;

	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;

	@Inject
	private MessageSource messageSource;

	@Inject
	private InternationalizationHelper internationalizationHelper;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private BugTrackersLocalService bugTrackersLocalService;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;

	@Inject
	private InfoListItemFinderService infoListItemService;

	// ****** custom field services ******************

	@Inject
	private CustomFieldHelperService cufHelperService;

	@Inject
	private CustomFieldJsonConverter converter;

	// ****** /custom field services ******************

	@Inject
	private Provider<TestCaseStatusJeditableComboDataBuilder> statusComboBuilderProvider;

	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatterProvider;

	@Inject
	private Provider<InternationalizableLabelFormatter> labelFormatter;

	@Inject
	private JsonInfoListBuilder infoListBuilder;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;


	@Inject
	private Provider<JsonTestCaseBuilder> builder;

	@Inject
	private PermissionEvaluationService permissionService;

	/**
	 * Returns the fragment html view of test case
	 *
	 * @param testCaseId
	 * @param locale
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showTestCase(@PathVariable long testCaseId, Locale locale) {
		ModelAndView mav = new ModelAndView("fragment/test-cases/test-case");

		TestCase testCase = testCaseModificationService.findById(testCaseId);

		populateModelWithTestCaseEditionData(mav, testCase, locale);

		return mav;
	}

	/**
	 * Returns the full-page html view of test case
	 *
	 * @param testCaseId
	 * @param locale
	 * @return
	 */
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showTestCaseInfo(@PathVariable long testCaseId, Locale locale) {

		LOGGER.trace("TestCaseModificationController : getting infos");

		ModelAndView mav = new ModelAndView("page/test-case-workspace/show-test-case");

		TestCase testCase = testCaseModificationService.findTestCaseWithSteps(testCaseId);

		populateModelWithTestCaseEditionData(mav, testCase, locale);

		return mav;
	}

	@RequestMapping(value = "/edit-from-exec/{execId}", method = RequestMethod.GET, params = OPTIMIZED)
	public ModelAndView editTestCaseFromExecution(@PathVariable long testCaseId, Locale locale,
												  @PathVariable long execId,
												  @RequestParam(OPTIMIZED) boolean optimized) {

		ModelAndView mav = new ModelAndView("page/test-case-workspace/edit-test-case-from-exec");
		TestCase testCase = testCaseModificationService.findTestCaseWithSteps(testCaseId);
		populateModelWithTestCaseEditionData(mav, testCase, locale);

		mav.addObject("execId", execId);
		mav.addObject("isIEO", optimized);
		return mav;
	}

	private void populateModelWithTestCaseEditionData(ModelAndView mav, TestCase testCase, Locale locale) {

		boolean hasCUF = cufHelperService.hasCustomFields(testCase);

		// Convert execution mode with local parameter
		List<OptionTag> executionModes = new ArrayList<>();
		for (TestCaseExecutionMode executionMode : TestCaseExecutionMode.values()) {
			OptionTag ot = new OptionTag();
			ot.setLabel(formatExecutionMode(executionMode, locale));
			ot.setValue(executionMode.toString());
			executionModes.add(ot);
		}


		mav.addObject(TEST_CASE, testCase);
		mav.addObject("executionModes", executionModes);
		mav.addObject("testCaseImportanceComboJson", buildImportanceComboData(locale));
		mav.addObject("testCaseImportanceLabel", formatImportance(testCase.getImportance(), locale));
		mav.addObject("testCaseNatures", buildNatureComboData(testCase.getId()));
		mav.addObject("testCaseTypes", buildTypeComboData(testCase.getId()));
		mav.addObject("testCaseStatusComboJson", buildStatusComboData(locale));
		mav.addObject("testCaseStatusLabel", formatStatus(testCase.getStatus(), locale));
		mav.addObject("attachmentsModel", attachmentHelper.findPagedAttachments(testCase));
		mav.addObject("callingTestCasesModel", getCallingTestCaseTableModel(testCase.getId(), new DefaultPagingAndSorting("TestCase.name"), ""));
		mav.addObject("hasCUF", hasCUF);

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(testCase);
		mav.addObject("milestoneConf", milestoneConf);
	}

	@RequestMapping(value = "/importance-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public String buildImportanceComboData(Locale locale) {
		return importanceComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	@RequestMapping(value = "/nature-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public JsonInfoList buildNatureComboData(@PathVariable("testCaseId") Long testCaseId) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		return infoListBuilder.toJson(testCase.getProject().getTestCaseNatures());
	}

	@RequestMapping(value = "/type-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public JsonInfoList buildTypeComboData(@PathVariable("testCaseId") Long testCaseId) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		return infoListBuilder.toJson(testCase.getProject().getTestCaseTypes());
	}

	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	private String buildStatusComboData(Locale locale) {
		return statusComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return internationalizationHelper.internationalize(mode, locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = {"id=test-case-description", VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeDescription(@RequestParam(VALUE) String testCaseDescription, @PathVariable long testCaseId) {

		testCaseModificationService.changeDescription(testCaseId, testCaseDescription);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated description to " + testCaseDescription);
		}

		return testCaseDescription;
	}


	@ResponseBody
	@RequestMapping(value = "/new-version", method = RequestMethod.GET)
	public JsonTestCase getNewVersionTemplate(@PathVariable("testCaseId") Long testCaseId) {

		TestCase testCase = testCaseModificationService.findById(testCaseId);

		return builder.get()
			.extended()
			.entities(Arrays.asList(testCase))
			.toJson()
			.get(0);

	}


	@ResponseBody
	@RequestMapping(value = "/new-version", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public JsonTestCase createNewVersion(@PathVariable("testCaseId") Long originalId, @RequestBody TestCase newVersionData) {

		TestCase newTestCase = testCaseModificationService.addNewTestCaseVersion(originalId, newVersionData);

		JsonTestCase jsTestCase = new JsonTestCase();
		jsTestCase.setId(newTestCase.getId());
		jsTestCase.setName(newTestCase.getName());

		return jsTestCase;

	}


	@RequestMapping(method = RequestMethod.POST, params = {"id=test-case-reference", VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeReference(@RequestParam(VALUE) String testCaseReference, @PathVariable long testCaseId) {

		testCaseReference = testCaseReference.substring(0, Math.min(testCaseReference.length(), 50));
		testCaseModificationService.changeReference(testCaseId, testCaseReference);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated reference to " + testCaseReference);
		}

		return HtmlUtils.htmlEscape(testCaseReference);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"id=test-case-importance", VALUE})
	public String changeImportance(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseImportance importance,
								   Locale locale) {
		testCaseModificationService.changeImportance(testCaseId, importance);

		return formatImportance(importance, locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = {"id=test-case-nature", VALUE})
	@ResponseBody
	public String changeNature(@PathVariable long testCaseId, @RequestParam(VALUE) String nature, Locale locale) {

		testCaseModificationService.changeNature(testCaseId, nature);
		InfoListItem newNature = infoListItemService.findByCode(nature);
		return formatInfoItem(newNature, locale);

	}

	@RequestMapping(method = RequestMethod.POST, params = {"id=test-case-newname", VALUE})
	@ResponseBody
	public Object changeName(@PathVariable long testCaseId, @RequestParam(VALUE) String newName) {

		testCaseModificationService.rename(testCaseId, newName);
		LOGGER.info("TestCaseModificationController : renaming {} as {}", testCaseId, newName);

		return HtmlUtils.htmlEscape(newName);

	}

	@RequestMapping(method = RequestMethod.POST, params = {"id=test-case-type", VALUE})
	@ResponseBody
	public String changeType(@PathVariable long testCaseId, @RequestParam(VALUE) String type, Locale locale) {

		testCaseModificationService.changeType(testCaseId, type);
		InfoListItem newType = infoListItemService.findByCode(type);
		return formatInfoItem(newType, locale);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"id=test-case-status", VALUE})
	public String changeStatus(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseStatus status, Locale locale) {
		testCaseModificationService.changeStatus(testCaseId, status);

		return formatStatus(status, locale);
	}

	@RequestMapping(value = "/importanceAuto", method = RequestMethod.POST, params = {"importanceAuto"})
	@ResponseBody
	public JsonEnumValue changeImportanceAuto(@PathVariable long testCaseId,
											  @RequestParam(value = "importanceAuto") boolean auto, Locale locale) {
		testCaseModificationService.changeImportanceAuto(testCaseId, auto);
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		return new JsonEnumValue(testCase.getImportance().toString(), formatImportance(testCase.getImportance(), locale));
	}


	@RequestMapping(method = RequestMethod.POST, params = {"id=test-case-prerequisite", VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changePrerequisite(@RequestParam(VALUE) String testCasePrerequisite, @PathVariable long testCaseId) {

		testCaseModificationService.changePrerequisite(testCaseId, testCasePrerequisite);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated prerequisite to " + testCasePrerequisite);
		}

		testCaseModificationService.addParametersFromPrerequisite(testCaseId);

		return testCasePrerequisite;
	}

	@RequestMapping(method = RequestMethod.POST, params = {"newName"})
	@ResponseBody
	public Object changeName(HttpServletResponse response, @PathVariable long testCaseId, @RequestParam String newName) {

		testCaseModificationService.rename(testCaseId, newName);
		LOGGER.info("TestCaseModificationController : renaming {} as {}", testCaseId, newName);

		return new RenameModel(newName);

	}

	@ResponseBody
	@RequestMapping(value = "/importance", method = RequestMethod.GET)
	public String getImportance(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseImportance importance = testCase.getImportance();
		return formatImportance(importance, locale);
	}

	@ResponseBody
	@RequestMapping(value = "/nature", method = RequestMethod.GET)
	public String getNature(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		InfoListItem nature = testCase.getNature();
		return formatInfoItem(nature, locale);
	}

	@ResponseBody
	@RequestMapping(value = "/type", method = RequestMethod.GET)
	public String getType(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		InfoListItem type = testCase.getType();
		return formatInfoItem(type, locale);
	}

	@ResponseBody
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public String getStatus(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseStatus status = testCase.getStatus();
		return formatStatus(status, locale);
	}

	private String formatImportance(TestCaseImportance importance, Locale locale) {
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(importance);
	}

	private String formatInfoItem(InfoListItem nature, Locale locale) {
		String item = internationalizationHelper.getMessage(nature.getLabel(), null, nature.getLabel(), locale);
		return HtmlUtils.htmlEscape(item);
	}

	private String formatStatus(TestCaseStatus status, Locale locale) {
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(status);
	}


	@RequestMapping(value = "/general", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable long testCaseId) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		return new JsonGeneralInfo((AuditableMixin) testCase);
	}


	@RequestMapping(value = "/calling-test-cases/table", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getCallingTestCaseTableModel(@PathVariable long testCaseId, DataTableDrawParameters params,
													   final Locale locale) {

		LOGGER.trace("TestCaseModificationController: getCallingTestCaseTableModel called ");

		PagingAndSorting paging = createPaging(params, referencingTestCaseMapper);

		return getCallingTestCaseTableModel(testCaseId, paging, params.getsEcho());

	}


	private DataTableModel getCallingTestCaseTableModel(long testCaseId, PagingAndSorting paging, String sEcho) {

		PagedCollectionHolder<List<CallTestStep>> holder = testCaseModificationService.findCallingTestSteps(testCaseId,
			paging);

		return new CallingTestCasesTableModelBuilder(internationalizationHelper).buildDataModel(holder, sEcho);
	}

	/* **********************************************************************
	 *
	 * Milestones section
	 *
	 ********************************************************************** */

	@RequestMapping(value = "/milestones", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getBoundMilestones(@PathVariable long testCaseId, DataTableDrawParameters params) {

		Collection<Milestone> allMilestones = testCaseModificationService.findAllMilestones(testCaseId);

		return buildMilestoneTableModel(testCaseId, allMilestones, params.getsEcho());
	}

	@RequestMapping(value = "/milestones/{milestoneIds}", method = RequestMethod.POST)
	@ResponseBody
	public void bindMilestones(@PathVariable long testCaseId, @PathVariable("milestoneIds") List<Long> milestoneIds) {

		testCaseModificationService.bindMilestones(testCaseId, milestoneIds);
	}

	@RequestMapping(value = "/milestones/{milestoneIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindMilestones(@PathVariable long testCaseId, @PathVariable("milestoneIds") List<Long> milestoneIds) {

		testCaseModificationService.unbindMilestones(testCaseId, milestoneIds);
	}

	@RequestMapping(value = "/milestones/associables", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getNotYetBoundMilestones(@PathVariable long testCaseId, DataTableDrawParameters params) {
		Collection<Milestone> notBoundMilestones = testCaseModificationService.findAssociableMilestones(testCaseId);
		return buildMilestoneTableModel(testCaseId, notBoundMilestones, params.getsEcho());
	}


	@RequestMapping(value = "/milestones/panel", method = RequestMethod.GET)
	public String getMilestonesPanel(@PathVariable Long testCaseId, Model model) {

		MilestonePanelConfiguration conf = new MilestonePanelConfiguration();

		TestCase tc = testCaseModificationService.findById(testCaseId);
		// build the needed data
		Collection<Milestone> allMilestones = testCaseModificationService.findAllMilestones(testCaseId);
		List<?> currentModel = buildMilestoneTableModel(testCaseId, allMilestones, "0").getAaData();

		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "test-cases");
		identity.put("resid", testCaseId.toString());

		String rootPath = "/test-cases/" + testCaseId.toString();

		Boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "LINK", tc);

		List<Milestone> mil = tc.getProject().getMilestones();
		CollectionUtils.filter(mil, new Predicate() {
			@Override
			public boolean evaluate(Object milestone) {
				return ((Milestone) milestone).getStatus().isBindableToObject();
			}
		});


		// add them to the model
		Boolean isMilestoneInProject = !mil.isEmpty();
		conf.setNodeType("testcase");
		conf.setRootPath(rootPath);
		conf.setIdentity(identity);
		conf.setCurrentModel(currentModel);
		conf.setEditable(editable);
		conf.setIsMilestoneInProject(isMilestoneInProject);

		model.addAttribute("conf", conf);

		return "milestones/milestones-tab.html";

	}


	private PagingAndSorting createPaging(final DataTableDrawParameters params, final DatatableMapper<?> dtMapper) {
		return new DataTableSorting(params, dtMapper);
	}


	private DataTableModel buildMilestoneTableModel(long testCaseId, Collection<Milestone> milestones, String sEcho) {

		TestCase tc = testCaseModificationService.findById(testCaseId);

		List<MetaMilestone> metaMilestones = new ArrayList<>(milestones.size());

		for (Milestone m : milestones) {
			metaMilestones.add(new MetaMilestone(m, tc.isMemberOf(m)));
		}

		PagedCollectionHolder<List<MetaMilestone>> collectionHolder =
			new SinglePageCollectionHolder<>(metaMilestones);

		Locale locale = LocaleContextHolder.getLocale();
		return new TestCaseBoundMilestoneTableModelHelper(internationalizationHelper, locale).buildDataModel(collectionHolder, sEcho);

	}

	/* ********************************** localization stuffs ****************************** */

	/**
	 * Return view for Printable test case
	 *
	 * @param testCaseId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, params = "format=printable")
	public ModelAndView showPrintableTestCase(@PathVariable long testCaseId, Locale locale) {
		// TODO smells like copy-pasta
		LOGGER.debug("get printable test case");
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		if (testCase == null) {
			throw new UnknownEntityException(testCaseId, TestCase.class);
		}
		ModelAndView mav = new ModelAndView("print-test-case.html");
		mav.addObject(TEST_CASE, testCase);

		// ============================BUGTRACKER
		if (testCase.getProject().isBugtrackerConnected()) {
			Project project = testCase.getProject();
			AuthenticationStatus status = bugTrackersLocalService.checkBugTrackerStatus(project.getId());
			BugTrackerInterfaceDescriptor descriptor = bugTrackersLocalService.getInterfaceDescriptor(project
				.findBugTracker());
			descriptor.setLocale(locale);

			mav.addObject("interfaceDescriptor", descriptor);
			mav.addObject("bugTrackerStatus", status);

			List<DecoratedIssueOwnership> decoratedIssues = Collections.emptyList();
			if (status == AuthenticationStatus.AUTHENTICATED) {
				try {
					List<IssueOwnership<RemoteIssueDecorator>> issuesOwnerShipList = Collections.emptyList();
					issuesOwnerShipList = bugTrackersLocalService.findIssueOwnershipForTestCase(testCaseId);
					decoratedIssues = new ArrayList<>(
						issuesOwnerShipList.size());
					for (IssueOwnership<RemoteIssueDecorator> ownerShip : issuesOwnerShipList) {
						decoratedIssues.add(new DecoratedIssueOwnership(ownerShip, locale));
					}

				}
				// it's okay if the bugtracker fails, it should not forbid the rest to work
				catch (BugTrackerRemoteException | NullArgumentException whatever) { // NOSONAR : this exception is part of the nominal use case
				}
			}
			mav.addObject("issuesOwnerShipList", decoratedIssues);

		}

		mav.addObject(TEST_CASE, testCase);

		// =================CUFS
		List<CustomFieldValue> customFieldValues = cufHelperService.newHelper(testCase).getCustomFieldValues();
		mav.addObject("testCaseCufValues", customFieldValues);

		// ================= EXECUTIONS
		Paging paging = createSinglePagePaging();
		List<Execution> executions = executionFinder.findAllByTestCaseIdOrderByRunDate(testCaseId, paging);
		mav.addObject("execs", executions);

		// =================STEPS

		List<TestStep> steps = testCaseModificationService.findStepsByTestCaseId(testCaseId);

		// the custom fields definitions
		CustomFieldHelper<ActionTestStep> helper = cufHelperService.newStepsHelper(steps, testCase.getProject())
			.setRenderingLocations(RenderingLocation.STEP_TABLE).restrictToCommonFields();

		List<CustomFieldModel> cufDefinitions = convertToJsonCustomField(helper.getCustomFieldConfiguration());
		List<CustomFieldValue> stepCufValues = helper.getCustomFieldValues();

		TestStepsTableModelBuilder builder = new TestStepsTableModelBuilder();
		builder.usingCustomFields(stepCufValues, cufDefinitions.size());
		Collection<Object> stepsData = builder.buildRawModel(steps, 1);
		mav.addObject("stepsData", stepsData);
		mav.addObject("cufDefinitions", cufDefinitions);

		// ================PARAMETERS
		List<Parameter> parameters = parameterFinder.findAllParameters(testCaseId);
		Collections.sort(parameters, new ParameterNameComparator(SortOrder.ASCENDING));

		ParametersModelHelper paramHelper = new ParametersModelHelper(testCaseId, messageSource,
			locale);
		Collection<Object> parameterDatas = paramHelper.buildRawModel(parameters);
		mav.addObject("paramDatas", parameterDatas);

		// ================DATASETS
		Map<String, String> paramHeadersByParamId = TestCaseDatasetsController.findDatasetParamHeadersByParamId(
			testCaseId, locale, parameters, messageSource);
		List<Object[]> datasetsparamValuesById = getParamValuesById(testCase.getDatasets());
		mav.addObject("paramIds", IdentifiedUtil.extractIds(parameters));
		mav.addObject("paramHeadersById", paramHeadersByParamId);
		mav.addObject("datasetsparamValuesById", datasetsparamValuesById);

		// ========================CALLING TC
		List<CallTestStep> callingSteps = testCaseModificationService.findAllCallingTestSteps(testCaseId);
		mav.addObject("callingSteps", callingSteps);

		// ========================VERIFIED REQUIREMENTS
		List<VerifiedRequirement> verifReq = verifiedRequirementsManagerService
			.findAllVerifiedRequirementsByTestCaseId(testCaseId);
		mav.addObject("verifiedRequirements", verifReq);

		// ========================THE LOVELY MILESTONES
		Collection<Milestone> allMilestones = testCaseModificationService.findAllMilestones(testCaseId);
		List<?> milestoneModels = buildMilestoneTableModel(testCaseId, allMilestones, "0").getAaData();
		mav.addObject("milestones", milestoneModels);

		return mav;
	}

	/**
	 * Return a list of dataset's organized infos as an object with : <br>
	 * object[0] = dataset's name object[1] = the dataset's paramValues as a map, mapping the paramValue.parameter.id to
	 * the paramValue.value
	 *
	 * @param datasets
	 * @return a list of Object[] with each object representing a dataset's information
	 */
	private List<Object[]> getParamValuesById(Set<Dataset> datasets) {
		List<Object[]> result = new ArrayList<>(datasets.size());

		for (Dataset dataset : datasets) {
			Set<DatasetParamValue> datasetParamValues = dataset.getParameterValues();
			Map<String, String> datasetParamValuesById = new HashMap<>(datasetParamValues.size());

			for (DatasetParamValue datasetParamValue : datasetParamValues) {
				datasetParamValuesById.put(datasetParamValue.getParameter().getId().toString(),
					datasetParamValue.getParamValue());
			}
			String datasetName = dataset.getName();
			Object[] datasetView = new Object[2];
			datasetView[0] = datasetName;
			datasetView[1] = datasetParamValuesById;
			result.add(datasetView);
		}
		return result;
	}

	/**
	 * Creates a paging which shows all entries on a single page.
	 *
	 * @return
	 */
	private Paging createSinglePagePaging() {
		return new Paging() {

			@Override
			public boolean shouldDisplayAll() {
				return true;
			}

			@Override
			public int getPageSize() {
				return 0;
			}

			@Override
			public int getFirstItemIndex() {
				return 0;
			}
		};
	}

	public class DecoratedIssueOwnership {
		private IssueOwnership<RemoteIssueDecorator> ownership;
		private String ownerDesc;

		public DecoratedIssueOwnership(IssueOwnership<RemoteIssueDecorator> ownership, Locale locale) {
			this.ownership = ownership;
			this.ownerDesc = BugTrackerControllerHelper.findOwnerDescForTestCase(ownership.getOwner(), messageSource,
				locale);
		}

		public String getOwnerDesc() {
			return ownerDesc;
		}

		public IssueOwnership<RemoteIssueDecorator> getOwnership() {
			return ownership;
		}

	}

	private List<CustomFieldModel> convertToJsonCustomField(Collection<CustomField> customFields) {
		List<CustomFieldModel> models = new ArrayList<>(customFields.size());
		for (CustomField field : customFields) {
			models.add(converter.toJson(field));
		}
		return models;
	}

}
