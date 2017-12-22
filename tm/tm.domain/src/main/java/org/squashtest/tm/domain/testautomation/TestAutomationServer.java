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

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.audit.Auditable;


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
public class TestAutomationServer implements Identified{

	private static final String DEFAULT_KIND = "jenkins";

	/**
	 * this is the ID (technical information)
	 *
	 */
	@Id
	@Column(name = "SERVER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "test_automation_server_server_id_seq")
	@SequenceGenerator(name = "test_automation_server_server_id_seq", sequenceName = "test_automation_server_server_id_seq", allocationSize = 1)
	private Long id;

	@Column(unique=true)
	private String name;

	/**
	 * This is the url where to reach the server.
	 */
	@Column
	private URL baseURL ;

	/**
 * The login that the TM server should use when dealing with the remote TA server.
	 */
	@Column
	@Size(min = 0, max = 50)
	private String login;

	/**
	 * The password to be used with the login above
	 */
	//TODO : eeer... clear password in the database ?
	@Column
	@Size(max = 255)
	private String password;

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

	public TestAutomationServer(String name){
		super();
		this.name = name;
	}

	public TestAutomationServer(Long id){
		super();
		this.id = id;
	}

	public TestAutomationServer(String name, URL baseURL, String login, String password) {
		this(name);
		this.baseURL = baseURL;
		this.login = login;
		this.password = password;
	}

	public TestAutomationServer(String name, URL baseURL, String login, String password, String kind) {
		this(name, baseURL, login, password);
		this.kind = kind;
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		this.name = name;
	}

	public URL getBaseURL() {
		return baseURL;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getKind() {
		return kind;
	}

	@Override
	public String toString(){
		if (baseURL!=null){
			return baseURL.toExternalForm();
		}
		return super.toString();
	}

	public void setBaseURL(URL baseURL) {
		this.baseURL = baseURL;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setPassword(String password) {
		this.password = password;
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
		TestAutomationServer testAutomationServerCopy = new TestAutomationServer(
				this.getName(), this.getBaseURL(), this.getLogin(), this.getPassword(), this.getKind());
		testAutomationServerCopy.setDescription(this.getDescription());
		testAutomationServerCopy.setManualSlaveSelection(this.isManualSlaveSelection());
		return testAutomationServerCopy;
	}
}
