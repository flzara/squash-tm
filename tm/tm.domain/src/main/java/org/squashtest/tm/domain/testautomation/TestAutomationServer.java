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
package org.squashtest.tm.domain.testautomation;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.servers.ThirdPartyServer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URL;


/**
 * An AutomatedTestServer represents both a repository of automated tests, and an automated test execution platform.
 *
 * @author bsiri
 *
 */


@NamedQueries({
    @NamedQuery(name="TestAutomationServer.findByUrlAndLogin", query="from TestAutomationServer where baseURL = :url and login = :login"),
    @NamedQuery(name="testAutomationServer.findAllHostedProjects", query="select p from TestAutomationProject p join p.server s where s.id = :serverId"),
    @NamedQuery(name="testAutomationServer.hasBoundProjects", query="select count(*) from TestAutomationProject where server.id = :serverId"),
    @NamedQuery(name="testAutomationServer.dereferenceProjects", query="update GenericProject set testAutomationServer = null where testAutomationServer.id = :serverId"),
    @NamedQuery(name="testAutomationServer.deleteServer", query="delete from TestAutomationServer serv where serv.id = :serverId")
})
@Entity
@Auditable
@PrimaryKeyJoinColumn(name = "SERVER_ID")
public class TestAutomationServer extends ThirdPartyServer implements Identified{

	private static final String DEFAULT_KIND = "jenkins";

	/**
	 * The kind of the remote TA server. It'll help selecting the correct connector. Default is {@link #DEFAULT_KIND}
	 */
	@Column
	@Size(min = 0, max = 30)
	private String kind = DEFAULT_KIND;

	@Column(name="MANUAL_SLAVE_SELECTION")
	private boolean manualSlaveSelection = false;

	@Column(name="DESCRIPTION")
	private String description = "";

	public TestAutomationServer(){
		super();
	}

	public String getKind() {
		return kind;
	}

	public TestAutomationServer(String kind){
		this.kind = kind;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("TestAutomationServer{");
		sb.append("id=").append(getId());
		sb.append(", name='").append(getName()).append('\'');
		sb.append(", url='").append(getUrl()).append('\'');
		sb.append(", kind='").append(kind).append('\'');
		sb.append('}');
		return sb.toString();
	}

	public boolean isManualSlaveSelection() {
		return manualSlaveSelection;
	}

	public void setManualSlaveSelection(boolean manualSlaveSelection) {
		this.manualSlaveSelection = manualSlaveSelection;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TestAutomationServer createCopy() {
		TestAutomationServer testAutomationServerCopy = new TestAutomationServer(this.getKind());
		testAutomationServerCopy.setName(this.getName());
		testAutomationServerCopy.setUrl(this.getUrl());
		testAutomationServerCopy.setDescription(this.getDescription());
		testAutomationServerCopy.setManualSlaveSelection(this.isManualSlaveSelection());
		return testAutomationServerCopy;
	}
}
