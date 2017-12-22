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
package org.squashtest.tm.web.internal.model.testautomation;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import org.squashtest.tm.web.internal.model.testautomation.TATestNode.Attr;
import org.squashtest.tm.web.internal.model.testautomation.TATestNode.Data;

public class TATestNodeListBuilder {

	public Collection<TATestNode> build(Collection<TestAutomationProjectContent> projectContents) {

		Collection<TATestNode> nodeList = new LinkedList<>();

		for (TestAutomationProjectContent content : projectContents) {

			TATestNode projectNode = createProjectNode(content);

			for (AutomatedTest test : content.getTests()) {
				merge(projectNode, test);
			}

			nodeList.add(projectNode);
		}

		return nodeList;

	}

	private void merge(TATestNode projectNode, AutomatedTest test) {

		String[] pathArray = test.getName().trim().split("\\/");
		List<String> path = Arrays.asList(pathArray);

		if (path.isEmpty()) {
			return;
		}

		TATestNode parent = projectNode;
		TATestNode current;

		ListIterator<String> iterator = path.listIterator();

		while (iterator.hasNext()) {

			String name = iterator.next();
			current = parent.findChild(name);

			if (current == null) {
				if (iterator.hasNext()) {
					current = createFolderNode(name);
				} else {
					current = createTestNode(name);
				}
				parent.getChildren().add(current);
			}

			parent = current;
		}

	}

	private TATestNode createTestNode(String name) {

		TATestNode node = new TATestNode();

		node.setState(State.leaf);

		Attr attr = new Attr();
		Data data = new Data();

		attr.setRel("ta-test");
		attr.setRestype("ta-test");
		attr.setName(name);

		data.setTitle(name);

		node.setAttr(attr);
		node.setData(data);

		return node;
	}

	private TATestNode createFolderNode(String name) {
		return createFolderNode(name, true);
	}

	private TATestNode createFolderNode(String name, boolean hasChildren) {

		TATestNode node = new TATestNode();

		State state = hasChildren ? State.closed : State.leaf;
		node.setState(state);

		Attr attr = new Attr();
		Data data = new Data();

		attr.setRel("folder");
		attr.setRestype("ta-folder");
		attr.setName(name);

		data.setTitle(name);

		node.setAttr(attr);
		node.setData(data);

		return node;

	}

	private TATestNode createProjectNode(TestAutomationProjectContent content) {

		TestAutomationProject project = content.getProject();

		TATestNode node = new TATestNode();

		State state = content.getTests().isEmpty() ? State.leaf : State.closed;
		node.setState(state);

		Attr attr = new Attr();
		Data data = new Data();

		attr.setId(project.getId().toString());
		attr.setRel("drive");
		attr.setName(project.getLabel());
		attr.setRestype("ta-project");

		data.setTitle(project.getLabel());

		node.setAttr(attr);
		node.setData(data);

		return node;
	}

}
