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

import org.apache.commons.collections.MultiMap;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;

/**
 * Interface for a builder of {@link JsTreeNode} objects, which provides a fluent API<br />
 *
 * "node" usually refers to the {@link JsTreeNode} being produced<br />
 *
 * "model" refers to the object used to produce a {@link JsTreeNode}
 *
 * @author Gregory Fouquet
 *
 * @param <MODEL>
 * @param <BUILDER>
 */
public interface JsTreeNodeBuilder<MODEL extends Identified, BUILDER extends JsTreeNodeBuilder<MODEL, BUILDER>> {
	/**
	 * Sets the model which should be used to produce a {@link JsTreeNode}. Should not be null.
	 *
	 * @param model
	 * @return
	 */
	BUILDER setModel(MODEL model);

	/**
	 * Creates a {@link JsTreeNode} using the current builder configuration.
	 * May return null if no nodes can be built for the given model, most notably
	 * when the milestone filter is set and that the model doesn't match it.
	 *
	 * @return
	 */
	JsTreeNode build();

	/**
	 * Configures which models should produce expanded (ie having a populated "children" attribute) nodes.
	 *
	 * @param expansionCandidates
	 *            the ids of items to expand mapped by their type.
	 * @return
	 */
	BUILDER expand(MultiMap expansionCandidates);

	/**
	 * Will set the 'index' property of the builder.
	 * @param index
	 * @return this JsTreeNodeBuilder
	 */
	BUILDER setIndex(int index);


	/**
	 *
	 * If desired one can filter out some content that doesn't belong to
	 * the given milestone.
	 *
	 * @param milestone
	 * @return
	 */
	BUILDER filterByMilestone(Milestone milestone);

}
