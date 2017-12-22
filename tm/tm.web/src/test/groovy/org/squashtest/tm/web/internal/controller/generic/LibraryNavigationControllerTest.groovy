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
package org.squashtest.tm.web.internal.controller.generic

import java.util.Optional
import org.squashtest.tm.core.foundation.exception.NullArgumentException
import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.library.*
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCaseLibraryPluginBinding
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.security.annotation.AclConstrainedObject
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.squashtest.tm.service.library.LibraryNavigationService
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder
import org.squashtest.tm.service.user.UserAccountService
import org.squashtest.tm.service.workspace.WorkspaceDisplayService
import spock.lang.Specification

class LibraryNavigationControllerTest extends Specification {
	DummyController controller = new DummyController()
	LibraryNavigationService<DummyLibrary, DummyFolder, DummyNode> service = Mock()
	UserAccountService userAccountService = Mock()
	WorkspaceDisplayService workspaceDisplayService = Mock()
	ActiveMilestoneHolder activeMilestoneHolder = Mock()

	def setup() {
		controller.service = service
		controller.userAccountService = userAccountService
		controller.workspaceDisplayService = workspaceDisplayService
		controller.activeMilestoneHolder = activeMilestoneHolder
		Optional<Long> activeMilestoneId = Optional.of(-9000L)
		controller.activeMilestoneHolder.activeMilestoneId >> activeMilestoneId
	}

	def "should add folder to root of library and return folder node model"() {
		given:
		DummyFolder folder = new DummyFolder()

		when:
		def res = controller.addNewFolderToLibraryRootContent(10, folder)

		then:
		1 * service.addFolderToLibrary(10, folder)
		res != null
	}


	def "should return root nodes of library"() {
		given:
		JsTreeNode rootFolder = Mock()
		workspaceDisplayService.getNodeContent(_, _, _, _) >> [rootFolder]

		when:
		def res = controller.getRootContentTreeModel(10)

		then:
		res.size() == 1
	}


	def "should return content nodes of folder"() {
		given:
		JsTreeNode content = Mock()
		List<JsTreeNode> rootnodes = new ArrayList<>()
		rootnodes.add(rootnodes)
		workspaceDisplayService.getNodeContent(_, _, _, _) >> rootnodes

		when:
		def res = controller.getFolderContentTreeModel(10)

		then:
		res.size() == 1
	}


	def "should add folder to folder content and return folder node model"() {
		given:
		DummyFolder folder = new DummyFolder();

		when:
		JsTreeNode res = controller.addNewFolderToFolderContent(100, folder)

		then:
		1 * service.addFolderToFolder(100, folder)
		res != null
	}


}

class DummyController extends LibraryNavigationController<DummyLibrary, DummyFolder, DummyNode> {
	LibraryNavigationService service
	WorkspaceDisplayService workspaceDisplayService

	LibraryNavigationService getLibraryNavigationService() {
		service
	}

	JsTreeNode createTreeNodeFromLibraryNode(LibraryNode node, List<Long> milestoneIds) {
		new JsTreeNode()
	}


	protected JsTreeNode createJsTreeNode(DummyNode resource) {
		return null;
	}


	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(DummyNode resource) {
		new JsTreeNode()
	}

	@Override
	protected WorkspaceDisplayService workspaceDisplayService() {
		return workspaceDisplayService
	}

}

class DummyFolder extends DummyNode implements Folder<DummyNode> {
	public void removeContent(DummyNode contentToRemove) throws NullArgumentException {}

	@Override
	public List<String> getContentNames() { return null; }

	@Override
	public void addContent(DummyNode contentToAdd) throws DuplicateNameException, NullArgumentException {}

	@Override
	public void addContent(DummyNode contentToAdd, int position) throws DuplicateNameException, NullArgumentException {}

	@Override
	public boolean isContentNameAvailable(String name) {}

	List getContent() {}

	Collection getOrderedContent() {}

	void addContent(LibraryNode node) {}

	void addContent(LibraryNode node, int position) {}

	void accept(NodeContainerVisitor visitor) {}

	void removeContent(LibraryNode node) {}

	@Override
	Copiable createCopy() {}

	@Override
	boolean hasContent() { return true }
}

class DummyNode implements LibraryNode {
	Long getId() {}

	String getName() {}

	String getDescription() {}

	void setDescription(String description) {}

	void setName(String name) {}

	void deleteMe() {}

	Project getProject() {}

	Library getLibrary() {}

	void notifyAssociatedWithProject(Project project) {}

	Copiable createCopy() { return null }

	void accept(NodeVisitor visitor) {}

	AttachmentList getAttachmentList() {}
}

class DummyLibrary implements Library<DummyNode> {
	public void removeContent(DummyNode contentToRemove) throws NullArgumentException {}

	public List<String> getContentNames() { return null; }

	@Override
	public Long getId() {
		return null
	}

	public void addRootContent(DummyNode node) {}

	public void removeRootContent(DummyNode node) {}

	public boolean isContentNameAvailable(String name) {}

	Set getRootContent() {}

	void accept(NodeContainerVisitor visitor) {}

	@Override
	List getContent() {
		return null;
	}

	@Override
	Collection getOrderedContent() {
		return null;
	}

	@Override
	public Project getProject() {
		return null
	}

	@Override
	@AclConstrainedObject
	public Library getLibrary() {
		return this;
	}

	void notifyAssociatedWithProject(GenericProject project) {}

	@Override
	String getClassSimpleName() {
		return "DummyLibrary";
	}

	@Override
	String getClassName() {
		return "org.squashtest.tm.web.internal.controller.generic.DummyLibrary";
	}

	@Override
	boolean hasContent() {
		return true;
	}

	public void addContent(DummyNode contentToAdd) throws DuplicateNameException, NullArgumentException {
	}

	public void addContent(DummyNode contentToAdd, int position) throws DuplicateNameException, NullArgumentException {
	}

	@Override
	public AttachmentList getAttachmentList() {
		return null;
	}

	@Override
	public Set<String> getEnabledPlugins() {
		return []
	}

	@Override
	public void disablePlugin(String pluginId) {

	}

	@Override
	public void enablePlugin(String pluginId) {

	}

	@Override
	public boolean isPluginEnabled(String pluginId) {
		return false;
	}

	public Set<TestCaseLibraryPluginBinding> getAllPluginBindings() {
		return [] as Set
	}

	public TestCaseLibraryPluginBinding getPluginBinding(String pluginId) {
		return null;
	}
}

