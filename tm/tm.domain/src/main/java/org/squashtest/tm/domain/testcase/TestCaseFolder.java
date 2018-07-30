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
package org.squashtest.tm.domain.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.FolderSupport;
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.project.Project;

@Entity
@PrimaryKeyJoinColumn(name = "TCLN_ID")
public class TestCaseFolder extends TestCaseLibraryNode implements Folder<TestCaseLibraryNode> {
	/**
	 * Delegate implementation of folder responsibilities.
	 */

	private static final String CLASS_NAME = "org.squashtest.tm.domain.testcase.TestCaseFolder";
	private static final String SIMPLE_CLASS_NAME = "TestCaseFolder";

	@Transient
	private final FolderSupport<TestCaseLibraryNode, TestCaseFolder> folderSupport = new FolderSupport<>(this);

        
        /*
        Note about cascading: 
        CascadeType.PERSIST is desirable because it allows us to cascade-create a complete grape of object (useful when importing for instance)
        CascadeType.DELETE is not desirable, because we need to call custom code for proper deletion (see the deletion services)
        */
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@OrderColumn(name = "CONTENT_ORDER")
	@JoinTable(name = "TCLN_RELATIONSHIP", joinColumns = @JoinColumn(name = "ANCESTOR_ID"), inverseJoinColumns = @JoinColumn(name = "DESCENDANT_ID"))
	private List<TestCaseLibraryNode> content = new ArrayList<>();

	@Override
	public void addContent(TestCaseLibraryNode node) {
		folderSupport.addContent(node);
	}

	@Override
	public void addContent(TestCaseLibraryNode node, int position) {
		folderSupport.addContent(node, position);
		// the following enforces that hibernate reinsert the data with their index,
		// and makes sure it works along the triggers.
		content = new ArrayList<>(content);

	}

	@Override
	public List<TestCaseLibraryNode> getContent() {
		return content;
	}

	@Override
	public void accept(TestCaseLibraryNodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(NodeContainerVisitor visitor) {
		visitor.visit(this);

	}

	@Override
	public boolean isContentNameAvailable(String name) {
		return folderSupport.isContentNameAvailable(name);
	}

	@Override
	public void removeContent(TestCaseLibraryNode node) throws NullArgumentException {
		content.remove(node);
		content = new ArrayList<>(content);

	}
	@Override
	public TestCaseFolder createCopy() {
		return folderSupport.createCopy(new TestCaseFolder());
	}

	@Override
	public void notifyAssociatedWithProject(Project project) {
		Project former = getProject();
		super.notifyAssociatedWithProject(project);
		folderSupport.notifyAssociatedProjectWasSet(former, project);

	}

	@Override
	public String getClassSimpleName() {
		return TestCaseFolder.SIMPLE_CLASS_NAME;
	}

	@Override
	public String getClassName() {
		return TestCaseFolder.CLASS_NAME;
	}

	@Override
	public boolean hasContent() {
		return folderSupport.hasContent();
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public List<String> getContentNames() {
		return folderSupport.getContentNames();
	}

	@Override
	public Collection<TestCaseLibraryNode> getOrderedContent() {
		return content;
	}

}
