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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;

@Component
@Scope("prototype")
public class NextLayerFeeder implements NodeVisitor {

	private Collection<NodePairing> nextLayer;
	private TreeNode destination;
	private Collection<? extends TreeNode> outputList;

	/**
	 * This method is used with the {@linkplain PasteStrategy} that goes through a three generation by generation (or
	 * layer by layer).<br>
	 * It will feed the next layer to process (nextLayer) with the children of the processed node (source).<br>
	 * <br>
	 * The next layer maps these next layer nodes with their future destination.<br>
	 * The future destination is the result of a {@linkplain PasteOperation} on the source node. Hence destination and
	 * source are of the same type.<br>
	 * <br>
	 * This method doesn't add output nodes to next layer so that we avoid infinite loops when a node is copied and
	 * pasted in himself.<br>
	 * example : 1 node <code> A (containing C) </code><br>
	 * <code> copy A into A </code> should lead to <code> A (containing A' + C) </code> with
	 * <code> A' containing C'.</code><br>
	 * When we process the first layer, A is copied as A' into A.<br>
	 * Then we fill the next layer to copy that is the <code> content of A = A' + C </code>.<br>
	 * We don't want to copy A' that is already the result of the copy. It would lead to infinite loop : copy A' into
	 * A', A'' into A'' ...<br>
	 * Thus we need to fill the next generation without A'.<br>
	 * <br>
	 *
	 * <u>Why can't we fill the next layer before copying ? </u> We need to fill the next layer node by node because we
	 * have to remember the node destination and to know it it is allowed to go deeper on the node (both depend on the
	 * operation). <br>
	 * <br>
	 *
	 * @param destination
	 *            : the result of a {@linkplain PasteOperation} on the source node
	 * @param source
	 *            : The TreeNode in which a paste operation has been processed.
	 * @param nextLayer
	 *            : a map to fill with an entry of new source-nodes mapped by their destination
	 * @param outputList
	 *            : the output list of the paste strategy.
	 */
	public void feedNextLayer(TreeNode destination, TreeNode source,
			Collection<NodePairing> nextLayer, Collection<? extends TreeNode> outputList) {
		this.nextLayer = nextLayer;
		this.destination = destination;
		this.outputList = outputList;
		source.accept(this);
	}


	@Override
	public void visit(CampaignFolder campaignFolder) {
		saveNextToCopy(campaignFolder, (CampaignFolder) destination);

	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		saveNextToCopy(requirementFolder, (RequirementFolder) destination);

	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		saveNextToCopy(testCaseFolder, (TestCaseFolder) destination);

	}

	@Override
	public void visit(Campaign campaign) {
		saveNextToCopy(campaign, (Campaign) destination);

	}

	@Override
	public void visit(Iteration iteration) {
		saveNextToCopy(iteration, (Iteration) destination);

	}

	@Override
	public void visit(TestSuite testSuite) {
		// nope

	}

	@Override
	public void visit(Requirement requirement) {
		saveNextToCopy(requirement, (Requirement) destination);
	}

	@Override
	public void visit(TestCase testCase) {
		// nope
	}

	@SuppressWarnings("unchecked")
	private void saveNextToCopy(NodeContainer<? extends TreeNode> source, NodeContainer<? extends TreeNode> destination) {
		if (source.hasContent()) {
			List<TreeNode> sourceContent = new ArrayList<>(source.getOrderedContent());
			sourceContent.removeAll(outputList);
			nextLayer.add(new NodePairing((NodeContainer<TreeNode>)destination, sourceContent));
		}
	}

}
