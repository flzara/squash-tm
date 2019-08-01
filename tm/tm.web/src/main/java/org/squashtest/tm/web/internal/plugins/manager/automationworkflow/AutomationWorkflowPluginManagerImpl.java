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
package org.squashtest.tm.web.internal.plugins.manager.automationworkflow;

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.scheduling.TaskScheduler;
	import org.springframework.stereotype.Component;
	import org.squashtest.tm.api.wizard.AutomationWorkflow;
	import org.squashtest.tm.domain.project.AutomationWorkflowType;
	import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

	import javax.annotation.PostConstruct;
	import javax.inject.Inject;
	import javax.inject.Named;
	import java.util.ArrayList;
	import java.util.Collection;
	import java.util.Collections;
	import java.util.LinkedHashMap;
	import java.util.List;
	import java.util.Locale;
	import java.util.Map;

@Component
public class AutomationWorkflowPluginManagerImpl implements AutomationWorkflowPluginManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomationWorkflowPluginManagerImpl.class);

	private static final String NONE = "NONE";
	private static final String I18N_KEY_NONE = "label.None";
	private static final String NATIVE = "NATIVE";
	private static final String REMOTE = "REMOTE_WORKFLOW";
	private static final String I18N_KEY_REMOTE = "label.Remote";
	private static final String I18N_KEY_NATIVE = "label.Native";

	public static final int DEFAULT_DELAY = 30;

	@Inject
	InternationalizationHelper i18nHelper;

	@Autowired(required = false)
	Collection<AutomationWorkflow> plugins = Collections.EMPTY_LIST;

	@Inject
	@Named("squashtest.tm.service.ThreadPoolTaskScheduler")
	private TaskScheduler taskScheduler;

	@PostConstruct
	public void scheduleSynchronization() {
		for(AutomationWorkflow workflowPlugin : plugins) {
			LOGGER.info("Registering automation workflow plugin {} as {}.", workflowPlugin, workflowPlugin.getId());
			taskScheduler.scheduleWithFixedDelay(workflowPlugin.getSynchronizationTask(), DEFAULT_DELAY * 1000);
		}
	}

	@Override
	public Map<String, String> getAutomationWorkflowsMapFilteredByIds(Collection<String> activePluginsIds, Locale locale) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(NONE, i18nHelper.internationalize(I18N_KEY_NONE, locale));
		result.put(NATIVE, i18nHelper.internationalize(I18N_KEY_NATIVE, locale));
		for(AutomationWorkflow workflow : plugins) {
			if(activePluginsIds.contains(workflow.getId())) {
				result.put(workflow.getId(), workflow.getWorkflowName());
			}
		}
		return result;
	}

	@Override
	public Map<String, String> getAutomationWorkflowsTypeFilteredByIds(Collection<String> activePluginsIds, Locale locale) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(NONE, i18nHelper.internationalize(I18N_KEY_NONE, locale));
		result.put(NATIVE, i18nHelper.internationalize(I18N_KEY_NATIVE, locale));
		if(plugins.size()!=0) result.put(REMOTE, i18nHelper.internationalize(I18N_KEY_REMOTE, locale));


		return result;
	}

	@Override
	public Collection<String> getAutomationWorkflowsIds() {
		List<String> result = new ArrayList<>();
		result.add(NONE);
		result.add(NATIVE);
		for(AutomationWorkflow workflow : plugins) {
			result.add(workflow.getId());
		}
		return result;
	}

	@Override
	public Collection<AutomationWorkflowType> getAutomationWorkflowsType() {
		List<AutomationWorkflowType> result = new ArrayList<>();
		result.add(AutomationWorkflowType.NONE);
		result.add(AutomationWorkflowType.NATIVE);
		if(plugins.size()!=0) result.add(AutomationWorkflowType.REMOTE_WORKFLOW);

		return result;
	}
}
