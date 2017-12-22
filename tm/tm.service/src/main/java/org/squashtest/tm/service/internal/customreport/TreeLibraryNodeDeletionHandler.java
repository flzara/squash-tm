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
package org.squashtest.tm.service.internal.customreport;

import java.util.List;

import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;


/**
 * 
 * <p>This interface describes the contract for handlers responsible for deleting domain objects. Deleting an object is not straight forward
 * since one have to deal with the relationships among them, eg a TestCase and a Requirement being bound together. As a consequence the application must perform
 * several checks beforehand in order to decide the actual course of action.</p>
 * 
 * <p>As of Squash TM version 1.0, the specifications are the following :
 * <ol>
 * 	<li>when requesting a remove operation the user must be informed of its consequences,</li>
 *  <li>when a user marks a node for deletion its children is included implicitly,</li>
 *  <li>depending on which relationships might be broken by removing one or several domain objects the operation might be allowed, allowed with a warning or denied,</li>
 *  <li>the batch removal is not atomic, eg when a subset of the operations are allowed while a second subset is denied, then the first subset will be processed anyway.</li>
 * </ol>
 * </p>
 *
 * <p>The details of what is green, orange or red depends on the implementation and will not be discussed here.</p>
 * 
 * <p>For the rest of this documentation let the nodes which can be deleted without consequences be the 'green nodes', those generating warnings the 'orange nodes' and
 * those that cannot be deleted be the 'red nodes'. Let the node expressly selected by the user be the 'input nodes', the input nodes and their children be the
 * 'candidate nodes', and the nodes that can be removed (ie green and orange) the 'okay nodes'.</p>
 * 
 * @author bsiri
 * 
 */

public interface TreeLibraryNodeDeletionHandler<NODE extends TreeLibraryNode> {
	/**
	 * <p>(see above for definitions) That method should investigate the consequences of the deletion request, and return a report about what will happen. It accepts the list of input node ids and
	 * should return a list of SuppressionPreviewReport. A SuppressionPreviewReport hold informations regarding orange and red nodes. As of version 1.0 and until further notice, the green nodes
	 * generate no information. In other words if all the selected nodes are green node the returned list will be empty. Note that this method should not actually perform the deletion, and
	 * should first calculate then include all the candidate nodes.</p>
	 * 
	 * @param targetIds the ids of the input node.
	 * @param milestoneId if non null, will carry on the simulation on milestone mode.
	 * @return a list of SuppressionPreviewReport.
	 */
	List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds);


	/**
	 * <p>(see above for definitions) That method perform actual node deletion. It accepts the list of input node ids and returns the okay node ids. For various reasons (security, practical use
	 * in stateless apps etc) that method should must again and separate the okay nodes from the red nodes, and then remove the okay node only. Note that if should first calculate then include all
	 * the candidate nodes before filtering them.</p>
	 * 
	 * 
	 * @param targetIds the list of node ids selected by the user.
	 * @param milestoneId if non null, deletion will be performed in milestone mode
	 * @return an OperationReport saying what happened.
	 */
	OperationReport deleteNodes(List<Long> targetIds);

}
