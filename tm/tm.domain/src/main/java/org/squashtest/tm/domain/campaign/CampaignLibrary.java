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
package org.squashtest.tm.domain.campaign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.hibernate.annotations.Where;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.project.GenericLibrary;
import org.squashtest.tm.domain.project.GenericProject;

@Entity
public class CampaignLibrary extends GenericLibrary<CampaignLibraryNode> {

	private static final String CLASS_NAME = "org.squashtest.tm.domain.campaign.CampaignLibrary";
	private static final String SIMPLE_CLASS_NAME = "CampaignLibrary";

	@Id
	@Column(name = "CL_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "campaign_library_cl_id_seq")
	@SequenceGenerator(name = "campaign_library_cl_id_seq", sequenceName = "campaign_library_cl_id_seq", allocationSize = 1)
	private Long id;

	@OneToMany // no cascade is desired because we need to handle it manually
	@OrderColumn(name = "CONTENT_ORDER")
	@JoinTable(name = "CAMPAIGN_LIBRARY_CONTENT", joinColumns = @JoinColumn(name = "LIBRARY_ID"), inverseJoinColumns = @JoinColumn(name = "CONTENT_ID"))
	private List<CampaignLibraryNode> rootContent = new ArrayList<>();

	@OneToOne(mappedBy = "campaignLibrary")
	private GenericProject project;

	@OneToMany(cascade = { CascadeType.ALL}, orphanRemoval=true)
	@JoinColumn(name="LIBRARY_ID")
	@Where(clause="LIBRARY_TYPE = 'C'")
	private Set<CampaignLibraryPluginBinding> enabledPlugins = new HashSet<>(5);

	@ElementCollection
	@Enumerated(EnumType.STRING)
	@JoinTable(name = "DISABLED_EXECUTION_STATUS", joinColumns= @JoinColumn(name = "CL_ID"))
	@Column(name = "EXECUTION_STATUS")
	private Set<ExecutionStatus> disabledStatuses = new HashSet<>(ExecutionStatus.DEFAULT_DISABLED_STATUSES);


	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	public List<CampaignLibraryNode> getRootContent() {
		return rootContent;
	}

	@Override
	public List<CampaignLibraryNode> getContent(){
		return getRootContent();
	}

	@Override
	public void removeContent(CampaignLibraryNode node) {
		if (node == null) {
			throw new NullArgumentException("CampaignLibrary : cannot remove null node");
		}
		rootContent.remove(node);
		rootContent = new ArrayList<>(rootContent);
	}

	@Override
	public GenericProject getProject() {
		return project;
	}

	@Override
	public void notifyAssociatedWithProject(GenericProject p) {
		this.project = p;
	}

	public Set<ExecutionStatus> getDisabledStatuses() {
		return disabledStatuses;
	}

	public void setDisabledStatuses(Set<ExecutionStatus> disabledStatuses) {
		this.disabledStatuses = disabledStatuses;
	}

	public void enableStatus(ExecutionStatus executionStatus){
		if(executionStatus.canBeDisabled()){
			this.disabledStatuses.remove(executionStatus);
		}
	}

	public void disableStatus(ExecutionStatus executionStatus){
		if(executionStatus.canBeDisabled()){
			this.disabledStatuses.add(executionStatus);
		}
	}

	public boolean allowsStatus(ExecutionStatus executionStatus){
		return !this.disabledStatuses.contains(executionStatus);
	}

	// ***************************** PluginReferencer section ****************************


	@Override
	public Set<String> getEnabledPlugins() {
		Set<String> pluginIds = new HashSet<>(enabledPlugins.size());
		for (CampaignLibraryPluginBinding binding : enabledPlugins){
			pluginIds.add(binding.getPluginId());
		}
		return pluginIds;
	}


	@Override
	public Set<CampaignLibraryPluginBinding> getAllPluginBindings() {
		return enabledPlugins;
	}

	@Override
	public void enablePlugin(String pluginId) {
		if (! isPluginEnabled(pluginId)){
			CampaignLibraryPluginBinding newBinding = new CampaignLibraryPluginBinding(pluginId);
			enabledPlugins.add(newBinding);
		}
	}

	@Override
	public void disablePlugin(String pluginId) {
		CampaignLibraryPluginBinding binding = getPluginBinding(pluginId);
		if (binding != null){
			enabledPlugins.remove(binding);
		}
	}

	@Override
	public CampaignLibraryPluginBinding getPluginBinding(String pluginId) {
		for (CampaignLibraryPluginBinding binding : enabledPlugins){
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
		return CampaignLibrary.SIMPLE_CLASS_NAME;
	}

	@Override
	public String getClassName() {
		return CampaignLibrary.CLASS_NAME;
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
	public Collection<CampaignLibraryNode> getOrderedContent() {
		return rootContent;
	}


}
