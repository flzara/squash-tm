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

	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.stereotype.Component;
	import org.squashtest.tm.core.automationworkflow.AutomationWorkflow;
	import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

	import javax.inject.Inject;
	import java.util.Collection;
	import java.util.Collections;
	import java.util.LinkedHashMap;
	import java.util.Locale;
	import java.util.Map;

@Component
public class AutomationWorkflowPluginManagerImpl implements AutomationWorkflowPluginManager {

	private static final String NONE = "NONE";
	private static final String I18N_KEY_NONE = "label.None";
	private static final String NATIVE = "NATIVE";
	private static final String I18N_KEY_NATIVE = "label.Native";

	@Inject
	InternationalizationHelper i18nHelper;

	@Autowired(required = false)
	Collection<AutomationWorkflow> plugins = Collections.EMPTY_LIST;

	@Override
	public Map<String, String> getAutomationWorkflowsMap(Locale locale) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(NONE, i18nHelper.internationalize(I18N_KEY_NONE, locale));
		result.put(NATIVE, i18nHelper.internationalize(I18N_KEY_NATIVE, locale));
		for(AutomationWorkflow workflow : plugins) {
			result.put(workflow.getWorkflowName(), workflow.getWorkflowName());
		}
		return result;
	}
}
