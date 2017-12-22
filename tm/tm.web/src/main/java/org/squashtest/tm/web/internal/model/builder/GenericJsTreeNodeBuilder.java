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

import static org.squashtest.tm.api.security.acls.Permission.CREATE;
import static org.squashtest.tm.api.security.acls.Permission.DELETE;
import static org.squashtest.tm.api.security.acls.Permission.EXECUTE;
import static org.squashtest.tm.api.security.acls.Permission.EXPORT;
import static org.squashtest.tm.api.security.acls.Permission.WRITE;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.MultiMap;
import org.squashtest.tm.api.security.acls.Permission;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;

/**
 * Generic superclass for builders of {@link JsTreeNode}
 *
 * @author Gregory Fouquet
 *
 */
public abstract class GenericJsTreeNodeBuilder<MODEL extends Identified, BUILDER extends JsTreeNodeBuilder<MODEL, BUILDER>>
implements JsTreeNodeBuilder<MODEL, BUILDER> {
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final Permission[] NODE_PERMISSIONS = { WRITE, CREATE, DELETE, EXECUTE, EXPORT };
	private static final String[] PERM_NAMES = {WRITE.name(), CREATE.name(), DELETE.name(), EXECUTE.name(), EXPORT.name()};

	protected final PermissionEvaluationService permissionEvaluationService;

	protected Milestone milestoneFilter;

	protected int index;
	/**
	 * What is currently used to produce a {@link JsTreeNode}
	 */
	private MODEL model;
	/**
	 * Ids of items mapped by their tupes which should be expanded.
	 */
	private MultiMap expansionCandidates;

	protected GenericJsTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super();
		this.permissionEvaluationService = permissionEvaluationService;
		assert PERM_NAMES.length == NODE_PERMISSIONS.length;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.JsTreeNodeBuilder#setModel(MODEL)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final BUILDER setModel(MODEL model) {
		this.model = model;
		return (BUILDER) this;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.JsTreeNodeBuilder#build()
	 */
	@Override
	public final JsTreeNode build() {
		JsTreeNode node = new JsTreeNode();

		Map<String, Boolean> permByName = getPermissionEvaluationService().hasRoleOrPermissionsOnObject(ROLE_ADMIN, PERM_NAMES, model);

		for (Permission perm : NODE_PERMISSIONS) {
			node.addAttr(perm.getQuality(), permByName.get(perm.name()).toString());
		}

		node = doBuild(node, model);

		if (node != null && shouldExpandModel()) {
			doAddChildren(node, model);
		}

		return node;
	}

	/**
	 * Tells if the {@link #model} matches any {@link #expansionCandidates}
	 *
	 * @return true if the model should be expanded.
	 */
	protected boolean shouldExpandModel() {
		if (expansionCandidates == null || expansionCandidates.isEmpty()) {
			return false;
		}

		Collection<?> candidateIds = (Collection<?>) expansionCandidates.get(modelShortClassName());

		return candidateIds != null && candidateIds.contains(model.getId());
	}

	@SuppressWarnings("rawtypes")
	private String modelShortClassName() {
		String className;

		if (model instanceof NodeContainer) {
			NodeClassNameReader classNameReader = new NodeClassNameReader();
			((NodeContainer) model).accept(classNameReader);
			className = classNameReader.getSimpleName();

		} else if (model instanceof TreeNode) {
			NodeClassNameReader classNameReader = new NodeClassNameReader();
			((TreeNode) model).accept(classNameReader);
			className = classNameReader.getSimpleName();

		} else {
			className = model.getClass().getSimpleName();
		}
		return className;
	}

	/**
	 * Implementors should "build" (ie populate) the given {@link JsTreeNode} from the model object. This method is
	 * called when the node is expanded only.
	 *
	 * @param node
	 *            the node to populate
	 * @param model
	 *            the model used to build the node
	 * @return TODO
	 */
	protected abstract JsTreeNode doBuild(JsTreeNode node, MODEL model);

	/**
	 * Implementors should add to the given {@link JsTreeNode} the models children if any. It should also set the node's
	 * open/close state accordingly.
	 *
	 * @param node
	 *            the node to populate
	 * @param model
	 *            the model used to build the node
	 */
	protected abstract void doAddChildren(JsTreeNode node, MODEL model);

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.JsTreeNodeBuilder#expand(org.apache.commons.collections.MultiMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public BUILDER expand(MultiMap expansionCandidates) {
		this.expansionCandidates = expansionCandidates;

		return (BUILDER) this;
	}

	/**
	 * @return the expansionCandidates which can be null.
	 */
	protected MultiMap getExpansionCandidates() {
		return expansionCandidates;
	}

	/**
	 * @return the permissionEvaluationService
	 */
	public PermissionEvaluationService getPermissionEvaluationService() {
		return permissionEvaluationService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BUILDER setIndex(int index) {
		this.index = index;
		return (BUILDER) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public BUILDER filterByMilestone(Milestone milestone) {
		this.milestoneFilter = milestone;
		return (BUILDER) this;
	}
}
