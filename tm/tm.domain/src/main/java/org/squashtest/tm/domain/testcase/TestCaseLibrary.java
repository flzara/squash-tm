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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.Where;
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.project.GenericLibrary;
import org.squashtest.tm.domain.project.GenericProject;

@Entity
public class TestCaseLibrary extends GenericLibrary<TestCaseLibraryNode> {

	private static final String CLASS_NAME = "org.squashtest.tm.domain.testcase.TestCaseLibrary";
	private static final String SIMPLE_CLASS_NAME = "TestCaseLibrary";

	@Id
	@Column(name = "TCL_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "test_case_library_tcl_id_seq")
	@SequenceGenerator(name = "test_case_library_tcl_id_seq", sequenceName = "test_case_library_tcl_id_seq", allocationSize = 1)
	private Long id;

	@OneToMany // no cascade is desired because we need to handle it manually
	@OrderColumn(name = "CONTENT_ORDER")
	@JoinTable(name = "TEST_CASE_LIBRARY_CONTENT", joinColumns = @JoinColumn(name = "LIBRARY_ID"), inverseJoinColumns = @JoinColumn(name = "CONTENT_ID"))
	private List<TestCaseLibraryNode> rootContent = new ArrayList<>();

	@OneToOne(mappedBy = "testCaseLibrary")
	private GenericProject project;

	@OneToMany(cascade = { CascadeType.ALL}, orphanRemoval=true)
	@JoinColumn(name="LIBRARY_ID")
	@Where(clause="LIBRARY_TYPE = 'T'")
	private Set<TestCaseLibraryPluginBinding> enabledPlugins = new HashSet<>(5);

	public List<TestCaseLibraryNode> getRootContent() {
		return rootContent;
	}
	@Override
	public List<TestCaseLibraryNode> getContent(){
		return getRootContent();
	}
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public GenericProject getProject() {
		return project;
	}

	@Override
	public void notifyAssociatedWithProject(GenericProject p) {
		this.project = p;
	}

	@Override
	public void removeContent(TestCaseLibraryNode node) {
		rootContent.remove(node);
		rootContent = new ArrayList<>(rootContent);
	}

	// ***************************** PluginReferencer section ****************************

	@Override
	public Set<String> getEnabledPlugins() {
		Set<String> pluginIds = new HashSet<>(enabledPlugins.size());
		for (TestCaseLibraryPluginBinding binding : enabledPlugins){
			pluginIds.add(binding.getPluginId());
		}
		return pluginIds;
	}


	@Override
	public Set<TestCaseLibraryPluginBinding> getAllPluginBindings() {
		return enabledPlugins;
	}

	@Override
	public void enablePlugin(String pluginId) {
		if (! isPluginEnabled(pluginId)){
			TestCaseLibraryPluginBinding newBinding = new TestCaseLibraryPluginBinding(pluginId);
			enabledPlugins.add(newBinding);
		}
	}

	@Override
	public void disablePlugin(String pluginId) {
		TestCaseLibraryPluginBinding binding = getPluginBinding(pluginId);
		if (binding != null){
			enabledPlugins.remove(binding);
		}
	}

	@Override
	public TestCaseLibraryPluginBinding getPluginBinding(String pluginId) {
		for (TestCaseLibraryPluginBinding binding : enabledPlugins){
			if (binding.getPluginId().equals(pluginId)){
				return binding;
			}
		}
		return null;
	}

	@Override
	public boolean isPluginEnabled(String pluginId) {
		return getPluginBinding(pluginId) != null;
	}

	/* ***************************** SelfClassAware section ******************************* */

	@Override
	public String getClassSimpleName() {
		return TestCaseLibrary.SIMPLE_CLASS_NAME;
	}

	@Override
	public String getClassName() {
		return TestCaseLibrary.CLASS_NAME;
	}

	@Override
	public boolean hasContent() {
		return !rootContent.isEmpty();
	}

	@Override
	public void accept(NodeContainerVisitor visitor) {
		visitor.visit(this);

	}
	@Override
	public Collection<TestCaseLibraryNode> getOrderedContent() {
		return rootContent;
	}

}
