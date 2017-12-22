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
package org.squashtest.tm.service;

import org.aspectj.lang.Aspects;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.squashtest.tm.domain.campaign.*;
import org.squashtest.tm.domain.event.RequirementCreationEventPublisherAspect;
import org.squashtest.tm.domain.event.RequirementModificationEventPublisherAspect;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.event.RequirementAuditor;
import org.squashtest.tm.service.internal.library.*;
import org.squashtest.tm.service.internal.repository.*;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCampaignLibraryNodeDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateRequirementLibraryNodeDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;

/**
 * Spring configuration for tm.service subsystem
 *
 * <b>Important note about transaction management :</b>
 * The app uses default jdk proxy mode for transaction management. But we also weave this jar to enable @Configurable.
 * By doing this, some classes marked @Transactional get woven with the aspectj transaction aspect.
 *
 *
 * AnnotationTransactionAspect has to be initialized at some point TRANSACTION_ASPECT_BEAN_NAME because
 *
 * @author Gregory Fouquet
 * Rem : @Configurable is used in tm.domain by hibernate search bridges
 */
@Configuration
@EnableSpringConfigured
@DependsOn(TransactionManagementConfigUtils.TRANSACTION_ASPECT_BEAN_NAME)
public class TmServiceConfig {
	@Inject
	private PasswordEncoder passwordEncoder;
	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	@Inject
	private RequirementLibraryDao requirementLibraryDao;
	@Inject
	private CampaignLibraryDao campaignLibraryDao;

	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private CampaignDao campaignDao;
	@Inject
	private IterationDao iterationDao;
	@Inject
	private TestSuiteDao testSuiteDao;

	@Inject
	private TestCaseFolderDao testCaseFolderDao;
	@Inject
	private RequirementFolderDao requirementFolderDao;
	@Inject
	private CampaignFolderDao campaignFolderDao;

	@Inject
	private TestCaseLibraryNodeDao testCaseLibraryNodeDao;
	@Inject
	private HibernateRequirementLibraryNodeDao requirementLibraryNodeDao;
	@Inject
	private HibernateCampaignLibraryNodeDao campaignLibraryNodeDao;

	@Inject
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> testCaseLibrarySelector;
	@Inject
	private LibrarySelectionStrategy<RequirementLibrary, RequirementLibraryNode> requirementLibrarySelector;
	@Inject
	private LibrarySelectionStrategy<CampaignLibrary, CampaignLibraryNode> campaignLibrarySelector;

	@Inject
	private ProjectFilterModificationService projectFilterManager;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private RequirementAuditor statusBasedRequirementAuditor;

	public TmServiceConfig() {
		super();
	}

	@Bean
	public static ConfigFileApplicationListener configFileApplicationListener() {
		final String[] propertiesFiles = {
			"squashtest.core.datasource.jdbc.config",
			"squashtest.tm.hibernate.config",
			"squashtest.tm.cfg"
		};

		ConfigFileApplicationListener listener = new ConfigFileApplicationListener();

		StringBuilder searchNames = new StringBuilder();
		for (String file : propertiesFiles) {
			if (searchNames.length() != 0) {
				searchNames.append(", ");
			}
			searchNames.append(file);
		}
		listener.setSearchNames(searchNames.toString());

		return listener;
	}


	@Bean
	public RequirementCreationEventPublisherAspect requirementCreationEventPublisherAspect() {
		RequirementCreationEventPublisherAspect aspect = Aspects.aspectOf(RequirementCreationEventPublisherAspect.class);
		aspect.setAuditor(statusBasedRequirementAuditor);
		return aspect;
	}

	@Bean
	public RequirementModificationEventPublisherAspect requirementModificationEventPublisherAspect() {
		RequirementModificationEventPublisherAspect aspect = Aspects.aspectOf(RequirementModificationEventPublisherAspect.class);
		aspect.setAuditor(statusBasedRequirementAuditor);
		return aspect;
	}

	@Bean(name = "squashtest.tm.service.TestCasesWorkspaceService")
	public GenericWorkspaceService<TestCaseLibrary, TestCaseLibraryNode> testCaseWorkspaceManager() {
		return new GenericWorkspaceService<>(projectFilterManager, testCaseLibraryDao, testCaseLibrarySelector);
	}

	@Bean(name = "squashtest.tm.service.RequirementsWorkspaceService")
	public GenericWorkspaceService<RequirementLibrary, RequirementLibraryNode> requirementWorkspaceManager() {
		return new GenericWorkspaceService<>(projectFilterManager, requirementLibraryDao, requirementLibrarySelector);
	}

	@Bean(name = "squashtest.tm.service.CampaignsWorkspaceService")
	public GenericWorkspaceService<CampaignLibrary, CampaignLibraryNode> campaignWorkspaceManager() {
		return new GenericWorkspaceService<>(projectFilterManager, campaignLibraryDao, campaignLibrarySelector);
	}

	@Bean(name = "squashtest.tm.service.TestCaseFolderModificationService")
	public GenericFolderModificationService<TestCaseFolder, TestCaseLibraryNode> testCaseFolderManager() {
		return new GenericFolderModificationService<>(permissionEvaluationService, testCaseFolderDao, testCaseLibraryDao);
	}

	@Bean(name = "squashtest.tm.service.RequirementFolderModificationService")
	public GenericFolderModificationService<RequirementFolder, RequirementLibraryNode> requirementFolderManager() {
		return new GenericFolderModificationService<>(permissionEvaluationService, requirementFolderDao, requirementLibraryDao);
	}

	@Bean(name = "squashtest.tm.service.CampaignFolderModificationService")
	public GenericFolderModificationService<CampaignFolder, CampaignLibraryNode> campaignFolderManager() {
		return new GenericFolderModificationService<>(permissionEvaluationService, campaignFolderDao, campaignLibraryDao);
	}

	@Bean(name = "squashtest.tm.service.internal.TestCaseManagementService")
	public GenericNodeManagementService<TestCase, TestCaseLibraryNode, TestCaseFolder> testCaseManager() {
		return new GenericNodeManagementService<>(permissionEvaluationService, testCaseDao, testCaseFolderDao, testCaseLibraryDao);
	}

	@Bean(name = "squashtest.tm.service.internal.RequirementManagementService")
	public GenericNodeManagementService<Requirement, RequirementLibraryNode, RequirementFolder> requirementManager() {
		return new GenericNodeManagementService<>(permissionEvaluationService, requirementDao, requirementFolderDao, requirementLibraryDao);
	}

	@Bean(name = "squashtest.tm.service.internal.CampaignManagementService")
	public GenericNodeManagementService<Campaign, CampaignLibraryNode, CampaignFolder> campaignManager() {
		return new GenericNodeManagementService<>(permissionEvaluationService, campaignDao, campaignFolderDao, campaignLibraryDao);
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToTestCaseFolderStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<TestCaseFolder, TestCaseLibraryNode> pasteToTestCaseFolderStrategy() {
		PasteStrategy<TestCaseFolder, TestCaseLibraryNode> paster = new PasteStrategy<>();
		paster.setContainerDao(testCaseFolderDao);
		paster.setNodeType(TestCaseLibraryNode.class);
		return paster;
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToTestCaseLibraryStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<TestCaseLibrary, TestCaseLibraryNode> pasteToTestCaseLibraryStrategy() {
		PasteStrategy<TestCaseLibrary, TestCaseLibraryNode> paster = new PasteStrategy<>();
		paster.setContainerDao(testCaseLibraryDao);
		paster.setNodeType(TestCaseLibraryNode.class);
		return paster;
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToRequirementFolderStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<RequirementFolder, RequirementLibraryNode> pasteToRequirementFolderStrategy() {
		PasteStrategy<RequirementFolder, RequirementLibraryNode> paster = new PasteStrategy<>();
		paster.setContainerDao(requirementFolderDao);
		paster.setNodeType(RequirementLibraryNode.class);
		return paster;
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToRequirementLibraryStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<RequirementLibrary, RequirementLibraryNode> pasteToRequirementLibraryStrategy() {
		PasteStrategy<RequirementLibrary, RequirementLibraryNode> paster = new PasteStrategy<>();
		paster.setContainerDao(requirementLibraryDao);
		paster.setNodeType(RequirementLibraryNode.class);
		return paster;
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToRequirementStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<Requirement, Requirement> pasteToRequirementStrategy() {
		PasteStrategy<Requirement, Requirement> paster = new PasteStrategy<>();
		paster.setContainerDao(requirementDao);
		paster.setNodeType(Requirement.class);
		return paster;
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToCampaignFolderStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<CampaignFolder, CampaignLibraryNode> pasteToCampaignFolderStrategy() {
		PasteStrategy<CampaignFolder, CampaignLibraryNode> paster = new PasteStrategy<>();
		paster.setContainerDao(campaignFolderDao);
		paster.setNodeType(CampaignLibraryNode.class);
		return paster;
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToCampaignLibraryStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<CampaignLibrary, CampaignLibraryNode> pasteToCampaignLibraryStrategy() {
		PasteStrategy<CampaignLibrary, CampaignLibraryNode> paster = new PasteStrategy<>();
		paster.setContainerDao(campaignLibraryDao);
		paster.setNodeType(CampaignLibraryNode.class);
		return paster;
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToCampaignStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<Campaign, Iteration> pasteToCampaignStrategy() {
		PasteStrategy<Campaign, Iteration> paster = new PasteStrategy<>();
		paster.setContainerDao(campaignDao);
		paster.setNodeType(Iteration.class);
		return paster;
	}

	@Bean(name = "squashtest.tm.service.internal.PasteToIterationStrategy")
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PasteStrategy<Iteration, TestSuite> pasteToIterationStrategy() {
		PasteStrategy<Iteration, TestSuite> paster = new PasteStrategy<>();
		paster.setContainerDao(iterationDao);
		paster.setNodeType(TestSuite.class);
		return paster;
	}


}
