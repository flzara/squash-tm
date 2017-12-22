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
package org.squashtest.tm.web.internal.model.builder;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;

@SuppressWarnings("rawtypes")
@Component
@Scope("prototype")
public class RequirementLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<RequirementLibraryNode> {
	/**
	 * This visitor is used to populate custom attributes of the {@link JsTreeNode} currently built
	 *
	 */
	private class CustomAttributesPopulator implements RequirementLibraryNodeVisitor {
		private final JsTreeNode builtNode;

		public CustomAttributesPopulator(JsTreeNode builtNode) {
			super();
			this.builtNode = builtNode;
		}

		/**
		 *
		 * @see org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor#visit(org.squashtest.tm.domain.requirement.RequirementFolder)
		 */
		@Override
		public void visit(RequirementFolder folder) {
			addFolderAttributes("requirement-folders");
			State state = folder.hasContent() ? State.closed : State.leaf;
			builtNode.setState(state);

		}


		/**
		 *
		 * @see org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor#visit(org.squashtest.tm.domain.requirement.Requirement)
		 */
		@Override
		public void visit(Requirement requirement) {

			// supposed not to be null;
			RequirementVersion version = milestoneFilter == null ? requirement.getCurrentVersion() : requirement.findByMilestone(milestoneFilter);

			//version can be null if it not in the current milestone but on of his child is.
			if (version == null){
				version = requirement.getCurrentVersion();
				builtNode.addAttr("milestones-dont-allow-click", "true");
			}


			// the name and title, usually treated as a common attributes, must be overriden in this case
			builtNode.addAttr("name", version.getName());
			builtNode.addAttr("reference", version.getReference());
			builtNode.setTitle(version.getFullName());

			// for-display instructions
			addLeafAttributes("requirement", "requirements");

			State state = requirement.hasContent() ? State.closed : State.leaf;
			builtNode.setState(state);

			// spec 4553
			String iconName = version.getCategory().getIconName();
			if (InfoListItem.NO_ICON.equals(iconName)){
				iconName = "def_cat_noicon";
			}
			builtNode.addAttr("category-icon", iconName);

			//miletsones
			builtNode.addAttr("milestones", totalMilestones(requirement));
			builtNode.addAttr("milestone-creatable-deletable", version.doMilestonesAllowCreation().toString());
			builtNode.addAttr("milestone-editable", version.doMilestonesAllowEdition().toString());

			//synchronized requirements
			if (requirement.isSynchronized()){
				builtNode.addAttr("synchronized", "true");
			}

			if (version.isModifiable()){
				builtNode.addAttr("req-version-modifiable","true");
			}

		}

	}

	private int totalMilestones(Requirement requirement){
		int count=0;
		for (RequirementVersion v : requirement.getRequirementVersions()){
			count += v.getMilestones().size();
		}
		return count;
	}

	@Inject
	public RequirementLibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	protected void addCustomAttributes(RequirementLibraryNode libraryNode, JsTreeNode treeNode) {
		libraryNode.accept(new CustomAttributesPopulator(treeNode));

	}


	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(JsTreeNode,
	 *      org.squashtest.tm.domain.Identified)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doAddChildren(JsTreeNode builtNode, RequirementLibraryNode model) {
		NodeContainer<RequirementLibraryNode<?>> container = (NodeContainer<RequirementLibraryNode<?>>) model;

		if (container.hasContent()) {

			RequirementLibraryTreeNodeBuilder childrenBuilder = new RequirementLibraryTreeNodeBuilder(permissionEvaluationService);
			childrenBuilder.filterByMilestone(milestoneFilter);

			Collection<RequirementLibraryNode<?>> content = container.getOrderedContent();

			List<JsTreeNode> children = new JsTreeNodeListBuilder<RequirementLibraryNode<?>>(childrenBuilder)
					.expand(getExpansionCandidates())
					.setModel(content)
					.build();

			builtNode.setChildren(children);

			// because of the milestoneFilter it may happen that the children collection ends up empty.
			// in that case we must set the state of the node accordingly
			State state =  children.isEmpty() ? State.leaf : State.open;
			builtNode.setState(state);
		}
	}



	@Override
	protected boolean passesMilestoneFilter() {
		if (milestoneFilter != null){
			return new MilestoneFilter(milestoneFilter).isValid(node);
		}
		else{
			return true;
		}
	}



	private static final class MilestoneFilter implements RequirementLibraryNodeVisitor{

		private Milestone milestone;
		private boolean isValid;


		private MilestoneFilter(Milestone milestone){
			this.milestone = milestone;
		}

		public boolean isValid(RequirementLibraryNode node){
			isValid = false;
			node.accept(this);
			return isValid;
		}

		@Override
		public void visit(RequirementFolder folder) {
			isValid = true;
		}

		@Override
		public void visit(Requirement requirement) {
			isValid = requirement.meOrMyChildHaveAVersionBoundToMilestone(milestone);
		}

	}


}
