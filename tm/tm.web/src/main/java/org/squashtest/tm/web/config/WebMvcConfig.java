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
package org.squashtest.tm.web.config;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.squashtest.tm.web.internal.interceptor.ActiveMilestoneInterceptor;
import org.squashtest.tm.web.internal.interceptor.LoggingInterceptor;
import org.squashtest.tm.web.internal.interceptor.SecurityExpressionResolverExposerInterceptor;
import org.squashtest.tm.web.internal.interceptor.openedentity.CampaignViewInterceptor;
import org.squashtest.tm.web.internal.interceptor.openedentity.ExecutionViewInterceptor;
import org.squashtest.tm.web.internal.interceptor.openedentity.IterationViewInterceptor;
import org.squashtest.tm.web.internal.interceptor.openedentity.RequirementViewInterceptor;
import org.squashtest.tm.web.internal.interceptor.openedentity.TestCaseViewInterceptor;
import org.squashtest.tm.web.internal.interceptor.openedentity.TestSuiteViewInterceptor;

/**
 * This class configures Spring MVC.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
	@Value("${info.app.version}")
	private String appVersion;

	@PersistenceUnit
	private EntityManagerFactory emf;

	@Inject
	private ResourceProperties resourceProperties;

	@Inject
	private ResourceResolverProperties resourceResolverProperties;

	@Inject
	private SecurityExpressionResolverExposerInterceptor securityExpressionResolverExposerInterceptor;

	@Inject
	private CampaignViewInterceptor campaignViewInterceptor;
	@Inject
	private ExecutionViewInterceptor executionViewInterceptor;
	@Inject
	private TestCaseViewInterceptor testCaseViewInterceptor;
	@Inject
	private RequirementViewInterceptor requirementViewInterceptor;
	@Inject
	private IterationViewInterceptor iterationViewInterceptor;
	@Inject
	private TestSuiteViewInterceptor testSuiteViewInterceptor;

	@Inject
	private ActiveMilestoneInterceptor milestoneInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// Log4j output enhancement
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		registry.addWebRequestInterceptor(loggingInterceptor);

		// OSIV
		OpenEntityManagerInViewInterceptor osiv = new OpenEntityManagerInViewInterceptor();
		osiv.setEntityManagerFactory(emf);
		registry.addWebRequestInterceptor(osiv);

		registry.addInterceptor(milestoneInterceptor);

		// #sec in thymeleaf
		registry.addInterceptor(securityExpressionResolverExposerInterceptor)
			.excludePathPatterns("/", "/login");

		// Opened test cases handling
		registry.addWebRequestInterceptor(testCaseViewInterceptor)
			.addPathPatterns(
				"/test-cases/*",
				"/test-cases/*/info",
				"/test-cases/*/verified-requirement-versions/manager"
			);

		// Opened requirements handling
		registry.addWebRequestInterceptor(requirementViewInterceptor)
			.addPathPatterns(
				"/requirement-versions/*",
				"/requirement-versions/*/info",
				"/requirement-versions/*/verifying-test-cases/manager",
				"/requirement-versions/*/linked-requirement-versions/manager"
			);

		// Opened campaigns handling
		registry.addWebRequestInterceptor(campaignViewInterceptor)
			.addPathPatterns(
				"/campaigns/*",
				"/campaigns/*/info",
				"/campaigns/*/test-plan/manager"
			);

		// Opened iterations handling
		registry.addWebRequestInterceptor(iterationViewInterceptor)
			.addPathPatterns(
				"/iterations/*",
				"/iterations/*/info",
				"/iterations/*/test-plan/manager"
			);

		// Opened test-suites handling
		registry.addWebRequestInterceptor(testSuiteViewInterceptor)
			.addPathPatterns(
				"/test-suites/*",
				"/test-suites/*/info",
				"/test-suites/*/test-plan/manager"
			);

		// Opened executions handling
		registry.addWebRequestInterceptor(executionViewInterceptor)
			.addPathPatterns(
				"/executions/*",
				"/executions/*/info"
			);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/home-workspace");
		super.addViewControllers(registry);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		VersionResourceResolver versionResolver = new VersionResourceResolver()
			.addContentVersionStrategy(appVersion, "/**/*.png", "/**/*.gif", "/**/*.jpg", "/**/*.css")
			.addFixedVersionStrategy(appVersion, "/**/*.js");
		GzipResourceResolver gzipResolver = new GzipResourceResolver();

		CssLinkResourceTransformer transformer = new CssLinkResourceTransformer();

		registry.addResourceHandler("/images/**")
			.addResourceLocations("/images/", "classpath:/images/")
			.setCachePeriod(resourceProperties.getCachePeriod())
			.resourceChain(resourceResolverProperties.isCache())
			.addResolver(versionResolver)
			.addTransformer(transformer);

		registry.addResourceHandler("/styles/**")
			.addResourceLocations("/styles/", "classpath:/styles/")
			.setCachePeriod(resourceProperties.getCachePeriod())
			.resourceChain(resourceResolverProperties.isCache())
			.addResolver(gzipResolver)
			.addResolver(versionResolver)
			.addTransformer(transformer);

		registry.addResourceHandler("/scripts/**")
			.addResourceLocations("/scripts/", "classpath:/scripts/")
			.setCachePeriod(resourceProperties.getCachePeriod())
			.resourceChain(resourceResolverProperties.isCache())
			.addResolver(gzipResolver)
			.addResolver(versionResolver);
	}

}
