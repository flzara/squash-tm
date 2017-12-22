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
/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.tm.web.internal.model.builder;

import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import java.util.Locale;

@Component
@Scope("prototype")
public class TestSuiteNodeBuilder extends GenericJsTreeNodeBuilder<TestSuite, TestSuiteNodeBuilder> {

	protected InternationalizationHelper internationalizationHelper;

	@Inject
	protected TestSuiteNodeBuilder(PermissionEvaluationService permissionEvaluationService, InternationalizationHelper internationalizationHelper) {
		super(permissionEvaluationService);
		this.internationalizationHelper = internationalizationHelper;
	}

	@Override
	protected JsTreeNode doBuild(JsTreeNode node, TestSuite model) {
		node.addAttr("rel", "test-suite");
		node.addAttr("resId", String.valueOf(model.getId()));
		node.addAttr("resType", "test-suites");
		node.setState(State.leaf);
		node.setTitle(model.getName());
		node.addAttr("name", model.getName());
		node.addAttr("id", model.getClass().getSimpleName() + '-' + model.getId());
		node.addAttr("executionStatus", model.getExecutionStatus().toString());

		//build tooltip
		String status = model.getExecutionStatus().getI18nKey();
		Locale locale = LocaleContextHolder.getLocale();
		String localizedStatus = internationalizationHelper.internationalize(status, locale);
		String[] args = {localizedStatus};
		String tooltip = internationalizationHelper.getMessage("label.tree.testSuite.tooltip", args, status, locale);
		String description;
		try {
			description = HTMLCleanupUtils.htmlToText(model.getFirstPlannedTestCase().getDescription());
			if (description.length() > 30) {
				description = description.substring(0, 30) + "...";
			}
		} catch (Exception e) {
			description = "";
		}
		node.addAttr("title", tooltip + "\n" + description);

		//milestone attributes
		node.addAttr("milestones", model.getMilestones().size());
		node.addAttr("milestone-creatable-deletable", model.doMilestonesAllowCreation().toString());
		node.addAttr("milestone-editable", model.doMilestonesAllowEdition().toString());

		return node;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder #doAddChildren(org.squashtest.tm.service.internal.dto.json.JsTreeNode, java.lang.Object)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, TestSuite model) {
		// NOOP
		// Test suite ain't got no children
	}


}
